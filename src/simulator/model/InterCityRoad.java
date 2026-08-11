	package simulator.model;

public class InterCityRoad extends Road{

	InterCityRoad(String id, Junction srcJunc, Junction destJunc, int maxSpeed, int contLimit, int length, Weather weather) {    
		super(id, srcJunc, destJunc, maxSpeed, contLimit, length, weather);
		// TODO Auto-generated constructor stub
	}

	public void reduceTotalContamination() {
		int x; 
		int tc = super.getTotalCO2(); 
		
		if(super.getWeather().equals(Weather.SUNNY)) 
			x = 2;
		else if(super.getWeather().equals(Weather.CLOUDY)) 
			x = 3;
		else if(super.getWeather().equals(Weather.RAINY)) 
			x = 10;
		else if(super.getWeather().equals(Weather.WINDY)) 
			x = 15;
		else 
			x = 20;
		
		int tn = ((100-x)*tc)/100;  
		super.setTotalCO2(tn);   
	}
	
	public void updateSpeedLimit() {	
		if(super.getTotalCO2() > super.getContLimit()) 
			super.setSpeedLimit(super.getMaxSpeed()/2);
		else
			super.setSpeedLimit(super.getMaxSpeed());
	}
	
	public int calculateVehicleSpeed(Vehicle v) {
		int speed = super.getSpeedLimit();   
		if(super.getWeather().equals(Weather.STORM)) 
			speed = ((getSpeedLimit()*8)/10); 
		return speed; 
	}
	
}
