package bufmgr;

import global.*;
public class Tuple {
	private PageId pageId;
	private Integer frameId;
	public Tuple(PageId pageId, Integer frameId){
		this.pageId = pageId;
		this.frameId = frameId;
	}
	public PageId getPageId(){
		return this.pageId;
	}
	public Integer getFrameId(){
		return this.frameId;
	}
}
