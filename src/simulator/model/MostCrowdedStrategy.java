package simulator.model;

import java.util.List;

public class MostCrowdedStrategy implements LightSwitchingStrategy{

	private int timeSlot; 
	
	public MostCrowdedStrategy (int tick){
		timeSlot = tick;
	}
	
	
	@Override
	public int chooseNextGreen(List<Road> roads, List<List<Vehicle>> qs, int currGreen, int lastSwitchingTime, int currTime) {
		if(roads.size()==0)
			return -1;
		if(currGreen == -1) {
			int max = 0, actual = 0;
			for(int i=0; i<qs.size();i++) {
				if(qs.get(i).size()>max) {
					max = qs.get(i).size();
					actual = i;
				}
			}
			return actual;
		}
		if(currTime-lastSwitchingTime < timeSlot)
			return currGreen; 
		int max = 0, actual = 0;
		for(int i= currGreen+1; i<qs.size();i++) {
			if(qs.get(i).size()>max) {
				max = qs.get(i).size();
				actual = i;
			}
		}
		for(int i= 0; i<currGreen+1;i++) {
			if(qs.get(i).size()>max) {
				max = qs.get(i).size();
				actual = i;
			}
		}
		return actual;
		
	}

}
