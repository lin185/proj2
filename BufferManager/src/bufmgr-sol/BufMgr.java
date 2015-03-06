package bufmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;
import chainexception.ChainException;
import diskmgr.DiskMgr;

public class BufMgr extends ChainException implements GlobalConst {

	private byte bufPool[][];
	private int numbufs;
	protected int prefetchSize;
	protected BufDescriptors bufDescr[];

	private Hash hash = new Hash();
	private DiskMgr diskMgr = Minibase.DiskManager;
	private Replacer replacer;

	/**
	 * Create the BufMgr object. Allocate pages (frames) for the buffer pool in
	 * main memory and make the buffer manage aware that the replacement policy
	 * is specified by replacerArg (i.e. LH, Clock, LRU, MRU etc.).
	 * 
	 * @param numbufs
	 *            number of buffers in the buffer pool
	 * @param prefetchSize
	 *            number of pages to be prefetched
	 * @param replacementPolicy
	 *            Name of the replacement policy
	 */
	public BufMgr(int numbufs, int prefetchSize, String replacementPolicy) {
		this.bufPool = new byte[numbufs][PAGE_SIZE];
		this.numbufs = numbufs;
		this.prefetchSize = prefetchSize;
		this.bufDescr = new BufDescriptors[numbufs];
		for (int i = 0; i < numbufs; i++)
			bufDescr[i] = new BufDescriptors();

		// modify else part if cannot change replacementPolicy, default policy is Clock
		if(replacementPolicy.equals("LRU")) { 
			replacer = new LRU(this);
			System.out.println("Policy: LRU");
		} else if(replacementPolicy.equals("LRULA")) { 
			replacer = new LRULA(this);
			System.out.println("Policy: LRULA");
		} else { 
			replacer = new Clock(this);
			System.out.println("Policy: Clock");
		}
	}

	/**
	 * Pin a page. First check if this page is already in the buffer pool. If it
	 * is, increment the pin_count and return a pointer to this page. If the
	 * pin_count was 0 before the call, the page was a replacement candidate,
	 * but is no longer a candidate. If the page is not in the pool, choose a
	 * frame (from the set of replacement candidates) to hold this page, read
	 * the page (using the appropriate method from {\em diskmgr} package) and
	 * pin it. Also, must write out the old page in chosen frame if it is dirty
	 * before reading new page.__ (You can assume that emptyPage==false for this
	 * assignment.)
	 * 
	 * @param pageno
	 *            page number in the Minibase.
	 * @param page
	 *            the pointer point to the page.
	 * @param emptyPage
	 *            true (empty page); false (non-empty page)
	 * @throws InvalidPageNumberException
	 * @throws PagePinnedException
	 * @throws BufferPoolExceededException
	 * @throws ReplacerException
	 * @throws BufMgrException
	 */
	public void pinPage(PageId pageno, Page page, boolean emptyPage)
			throws InvalidPageNumberException, BufferPoolExceededException,
			PagePinnedException, ReplacerException, BufMgrException {

		int index = hash.get(pageno);
		if (index != INVALID_PAGEID) {
			page.setpage(bufPool[index]);
			replacer.pin(index);
			return;
		}

		index = replacer.next();
		if (index == INVALID_PAGEID)
			throw new ReplacerException(null,
					"BufMgr: pinPage() error, replacer next()");

		PageId oldid = new PageId(bufDescr[index].pageNum.pid);
		Page oldpage = new Page(bufPool[index]);

		hash.remove(bufDescr[index].pageNum);
		bufDescr[index].pageNum.copyPageId(pageno);

		if (!oldid.equals(INVALID_PAGEID) && bufDescr[index].dirtybit) {
			try {
				diskMgr.write_page(oldid, oldpage);
			} catch (Exception e) {
				throw new BufMgrException(e, "BufMgr: pinPage() error, page not write");
			}
		}

		bufDescr[index].dirtybit = false;
		hash.put(bufDescr[index].pageNum, index);

		if (!emptyPage) {
			try {
				diskMgr.read_page(pageno, oldpage);
			} catch (Exception e) {
				hash.remove(bufDescr[index].pageNum);
				pageno = new PageId(INVALID_PAGEID);
				throw new BufMgrException(e, "BufMgr: pinPage() error, page not read");
			}
		}
		page.setpage(bufPool[index]);
	}

	/**
	 * Unpin a page specified by a pageId. This method should be called with
	 * dirty==true if the client has modified the page. If so, this call should
	 * set the dirty bit for this frame. Further, if pin_count>0, this method
	 * should decrement it. If pin_count=0 before this call, throw an exception
	 * to report error. (For testing purposes, we ask you to throw an exception
	 * named PageUnpinnedException in case of error.)
	 * 
	 * @param pageno
	 *            page number in the Minibase.
	 * @param dirty
	 *            the dirty bit of the frame
	 * @throws HashEntryNotFoundException
	 * @throws InvalidPageNumberException
	 * @throws ReplacerException
	 * @throws PageNotPinnedException
	 */
	public void unpinPage(PageId pageno, boolean dirty)
			throws HashEntryNotFoundException, InvalidPageNumberException,
			ReplacerException, PageNotPinnedException {

		int index = hash.get(pageno);
		if (index == INVALID_PAGEID)
			throw new HashEntryNotFoundException(null,
					"BufMgr: unpinPage() error, hash not found");

		if (bufDescr[index].pageNum.equals(INVALID_PAGEID))
			throw new InvalidPageNumberException(null,
					"BufMgr: unpinPage() error, invalid pageid");

		if (bufDescr[index].pinCount == 0)
			throw new PageNotPinnedException(null,
					"BufMgr: unpinPage() error, page not pinned");

		if (replacer.unpin(index) != true)
			throw new ReplacerException(null,
					"BufMgr: unpinPage() error, replacer unpin()");

		// if (dirty)
		bufDescr[index].dirtybit = dirty;
	}

	/**
	 * Allocate new pages. Call DB object to allocate a run of new pages and
	 * find a frame in the buffer pool for the first page and pin it. (This call
	 * allows a client of the Buffer Manager to allocate pages on disk.) If
	 * buffer is full, i.e., you can't find a frame for the first page, ask DB
	 * to deallocate all these pages, and return null.
	 * 
	 * @param firstpage
	 *            the address of the first page.
	 * @param howmany
	 *            total number of allocated new pages.
	 * 
	 * @return the first page id of the new pages.__ null, if error.
	 * @throws BufMgrException
	 */
	public PageId newPage(Page firstpage, int howmany) throws BufMgrException {

		PageId pageid = new PageId();
		try {
			diskMgr.allocate_page(pageid, howmany);
		} catch (Exception e) {
			throw new BufMgrException(e, "BufMgr: newPage() error, DiskMgr allocate_page()");
		}
		try {
			pinPage(pageid, firstpage, true);
			return pageid;
		} catch (Exception e) {
			try {
				diskMgr.deallocate_page(pageid, howmany);
			} catch (Exception ex) {
				throw new BufMgrException(ex, "BufMgr: newPage() error, DiskMgr deallocate_page()");
			}
			return null;
		}
	}

	/**
	 * This method should be called to delete a page that is on disk. This
	 * routine must call the method in diskmgr package to deallocate the page.
	 * 
	 * @param globalPageId
	 *            the page number in the data base.
	 * @throws HashOperationException
	 * @throws InvalidBufferException
	 * @throws ReplacerException
	 * @throws BufMgrException
	 * @throws PagePinnedException
	 */
	public void freePage(PageId globalPageId) throws HashOperationException,
			ReplacerException, BufMgrException, PagePinnedException {

		int index = hash.get(globalPageId);
		if (index == INVALID_PAGEID) {
			try {
				diskMgr.deallocate_page(globalPageId);
			} catch (Exception e) {
				throw new BufMgrException(e, "BufMgr: freePage() error, DiskMgr deallocate_page()");
			}
			return;
		}

		if (bufDescr[index].pinCount > 0)
			throw new PagePinnedException(null,
					"BufMgr: freePage() error, page pinned");
		replacer.free(index);
		hash.remove(bufDescr[index].pageNum);

		bufDescr[index].pageNum = new PageId(INVALID_PAGEID);
		bufDescr[index].dirtybit = false;

		try {
			diskMgr.deallocate_page(globalPageId);
		} catch (Exception e) {
			throw new BufMgrException(e, "BufMgr: freePage() error, DiskMgr deallocate_page()");
		}
	}

	/**
	 * Used to flush a particular page of the buffer pool to disk. This method
	 * calls the write_page method of the diskmgr package.
	 * 
	 * @param pageid
	 *            the page number in the database.
	 * @throws HashOperationException
	 * @throws BufMgrException
	 * @throws PagePinnedException
	 * @throws PageNotFoundException
	 */
	public void flushPage(PageId pageid, Page page)
			throws HashOperationException, PageNotFoundException,
			PagePinnedException, BufMgrException {

		int unpinned = 0;
		for (int i = 0; i < numbufs; i++) {
			if (bufDescr[i].pageNum.equals(pageid)) {
				if (bufDescr[i].pinCount != 0)
					unpinned++;
				if (bufDescr[i].dirtybit) {
					if (bufDescr[i].pageNum.pid == INVALID_PAGEID)
						throw new PageNotFoundException(null,
								"BufMgr: flushPage() error, page not found");
					PageId pid = new PageId(bufDescr[i].pageNum.pid);
					try {
						diskMgr.write_page(pid, new Page(bufPool[i]));
					} catch (Exception e) {
						throw new BufMgrException(e, "BufMgr: flushPage() error, page not write");
					}

					hash.remove(pid);
					bufDescr[i].pageNum = new PageId(INVALID_PAGEID);
					bufDescr[i].dirtybit = false;
				}
				if (unpinned != 0)
					throw new PagePinnedException(null,
							"BufMgr: flushPage() error, page pinned");
			}
		}
	}

	/**
	 * Used to flush all dirty pages in the buffer poll to disk
	 * 
	 * @throws HashOperationException
	 * @throws BufMgrException
	 * @throws PagePinnedException
	 * @throws PageNotFoundException
	 * 
	 */
	public void flushAllPages() throws HashOperationException,
			PageNotFoundException, PagePinnedException, BufMgrException {

		int unpinned = 0;
		for (int i = 0; i < numbufs; i++) {
			if (bufDescr[i].pinCount != 0)
				unpinned++;
			if (bufDescr[i].dirtybit) {
				if (bufDescr[i].pageNum.pid == INVALID_PAGEID)
					throw new PageNotFoundException(null,
							"BufMgr: myFlushPage() error, page not found");
				PageId pid = new PageId(bufDescr[i].pageNum.pid);
				try {
					diskMgr.write_page(pid, new Page(bufPool[i]));
				} catch (Exception e) {
					throw new BufMgrException(e, "BufMgr: flushAllPage() error, page not write");
				}

				hash.remove(pid);

				bufDescr[i].pageNum = new PageId(INVALID_PAGEID);
				bufDescr[i].dirtybit = false;
			}
		}

		if (unpinned != 0)
			throw new PagePinnedException(null,
					"BufMgr: myFlushPage() error, page pinned");
	}

	/**
	 * Gets the total number of buffer frames.
	 */
	public int getNumBuffers() {
		return numbufs;
	}

	/**
	 * Gets the total number of unpinned buffer frames.
	 */
	public int getNumUnpinned() {
		int count = 0;
		for (int i = 0; i < numbufs; i++) {
			if (bufDescr[i].pinCount == 0)
				count++;
		}
		return count;
	}

	class BufDescriptors {
		PageId pageNum;
		int pinCount;
		boolean dirtybit;

		BufDescriptors() {
			pageNum = new PageId(INVALID_PAGEID);
			pinCount = 0;
			dirtybit = false;
		}

		public void pin() {
			pinCount++;
		}

		public void unpin() {
			if (pinCount <= 0)
				pinCount = 0;
			else
				pinCount--;
		}
	}
};
