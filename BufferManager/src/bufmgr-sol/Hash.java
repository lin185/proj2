package bufmgr;

import global.GlobalConst;
import global.PageId;

import java.util.LinkedList;

public class Hash implements GlobalConst{
    int a = 61;
    int b = 811;
    int htsize = 997;
    Bucket dir[];
    
    class Pair {
        int frame_num;
        PageId page_num;

        Pair(int frame_num, PageId page_num) {
            this.frame_num = frame_num;
            this.page_num = page_num;
        }
    }

    class Bucket {
        LinkedList<Pair> pairs;
        
        Bucket() {
            pairs = new LinkedList<Pair>();
        }
    }

    public boolean put(PageId page_num, int frame_num) {
        int key = (page_num.pid * a + b) % 997;
        Pair newPair = new Pair(frame_num, page_num);
        if(dir[key] == null) {
            dir[key] = new Bucket();
            dir[key].pairs.add(newPair);
            // System.out.println("1");
            return true;
        }
        for (Pair temp : dir[key].pairs) {
            if(temp.page_num == page_num) return false;
        }
        dir[key].pairs.add(newPair);
        return true;
    }

    public int get(PageId page_num) {
        int key = (page_num.pid * a + b) % 997;
        if(dir[key] == null){return -1;}
        for(Pair temp : dir[key].pairs) {
            if(temp.page_num.equals(page_num)) return temp.frame_num;
        }
        return INVALID_PAGEID;
    }

    public boolean remove (PageId page_num){
    	if(page_num == null) return false;
        int key = (page_num.pid * a + b) % 997;
        if(dir[key] == null){return false;}
        for(Pair temp : dir[key].pairs) {
        	if(temp.page_num.equals(page_num)) {
                dir[key].pairs.remove(temp);
                return true;
            }
        }
        return false;
    }

    public void clear() {
        dir = new Bucket[htsize];
    }

    public Hash() {
        dir = new Bucket[htsize];
    }
}