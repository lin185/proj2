package bufmgr;

import global.PageId;

//The Low Inter-refernce Recency Set(LIRS) Replacement policy
public class LIRS {
	
	public LIRS(){}
	
	//THe Reuse Distance of a page
	private int calcRD(PageId p){
		return 0;
	}

	//The Recency of the last use of a page
	private int calcR(PageId p){
		return 0;
	}

	//Determine which page should be replaced
	public int getVictimPage(Descriptor[] d, int access_count) {
		//free buffer frame
		for(int index = 0; index < d.length; index++){
			if(d[index].t1 == -1 && d[index].t2 == -1) {
				return index;
			}
		}
		
		
		//No space in the buffer pool
		//Find a victim page to replace
		int max_weight = -1;
		int max_weight_index = -1;
		int RD;
		int R;
		for(int index = 0; index < d.length; index++){
			if(d[index].pin_count == 0) {
				if(d[index].t1 == -1 && d[index].t2 != -1) 
					RD = Integer.MAX_VALUE;
				else
					RD = d[index].t2 - d[index].t1;
					
				R = access_count - d[index].t2;
				
				if(RD > R) {
					if(RD > max_weight) {
						max_weight = RD;  
						max_weight_index = index;
					}
				} else {
					if(R > max_weight) {
						max_weight = R;  
						max_weight_index = index;
					}
				}
				
			}
		}
		return max_weight_index;
		//loop through the descriptor to find which has the max RD-R weight
		//if p.Rvalue > p.RDvalue
		//weight = p.Rvalue
		//else 
		//weight = p.RDvalue
		//compare it with current highest weight
		//if higher than victimPage = p
	//update all other pages p.Rvalue
	//set p.Rvalue = 0
	//update p's RD value
	//if(p.rdvalue == 0)
	// set p.rdvalue == infinity
	// store the accessID into another variable for later use
	// else 
	//    p.rdvalue = currentAccessId - previousAccessId
	//    prviousAccessID = currentAcessId;
		//return victimPageID
	}

	public static void setRD(PageId p){
	
	}

	public static void setR(PageId p){
	
	}
	
}

