package simulator.model;

public class CityRoad extends Road{

	public CityRoad(String id, Junction srcJunc, Junction destJunc, int maxSpeed, int contLimit, int length, Weather weather) {
		super(id, srcJunc, destJunc, maxSpeed, contLimit, length, weather);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reduceTotalContamination() {
		int x;
		int tc = super.getTotalCO2();
		
		if(super.getWeather().equals(Weather.WINDY) || super.getWeather().equals(Weather.STORM)) 
			x = 10;
		else
			x= 2;
		int tn = tc - x;  
		if(tn<0)
			tn=0;
		super.setTotalCO2(tn); 
	}

	@Override
	public void updateSpeedLimit() {
		super.setSpeedLimit(super.getMaxSpeed());
	}

	@Override
	public int calculateVehicleSpeed(Vehicle v) {
		int s = super.getSpeedLimit();
		int f = v.getContClass(); 
		int speed = ((11-f)*s)/11;
		return speed;
	}

}
