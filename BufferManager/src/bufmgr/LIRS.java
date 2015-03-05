package bufmgr;

import global.PageId;


public class LIRS {
	
	public LIRS(){}
	
	private int calcRD(PageId p){
		return 0;
	}
	private int calcR(PageId p){
		return 0;
	}
	//Determin which page should be replaced
	public int getVictimPage(Descriptor[] d){
		int index = 0;
		for(; index < d.length; index++){
			if(d[index].pin_count == 0)
				return index;
		}
		return -1;
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

