package simulator.model;

import java.util.List;

public class RoundRobinStrategy implements LightSwitchingStrategy{

	private int timeSlot;
	
	public RoundRobinStrategy(int tick){
		timeSlot = tick;
	}
	
	@Override
	public int chooseNextGreen(List<Road> roads, List<List<Vehicle>> qs, int currGreen, int lastSwitchingTime, int currTime) {
		if(roads.isEmpty())
			return -1;
		if(currGreen == -1)
			return 0;
		if(currTime-lastSwitchingTime < timeSlot)
			return currGreen; 
		if(roads.size()-1 == currGreen)
			return 0;
		return currGreen+1; 
	}

}
