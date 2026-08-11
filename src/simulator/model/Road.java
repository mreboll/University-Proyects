package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;



public abstract class Road extends SimulatedObject{
	
	private Junction srcJunc;
	private Junction destJunc;
	private int length;
	private int maxSpeed;
	private int speedLimit;
	private int contLimit; 
	private int contTotal;
	private List<Vehicle> vehicle;
	private Weather weather;
	

	public Road(String id, Junction srcJunc, Junction destJunc, int maxSpeed, int contLimit, int length, Weather weather) {
		  super(id);
		  if(maxSpeed<=0)
			  throw new IllegalArgumentException("maxSpeed debe ser positivo"); 
		  if(contLimit<0)
			  throw new IllegalArgumentException("contLimit debe ser no negativo"); 
		  if(length<=0)
			  throw new IllegalArgumentException("length debe ser positivo"); 
		  if (srcJunc.equals(null) ||  destJunc.equals(null) || weather.equals(null))
			  throw new IllegalArgumentException("ni el cruce inicial, ni el cruce destino, ni weather pueden tomar valores nulos ");
		  this.maxSpeed = maxSpeed;
		  this.contLimit = contLimit;
		  this.length = length;
		  this.srcJunc= srcJunc;
		  this.destJunc = destJunc;
		  this.weather = weather;
		 this.speedLimit = maxSpeed;
		  this. vehicle = new ArrayList<Vehicle>();
		  this.srcJunc.addOutGoingRoad(this);
		  this.destJunc.addIncommingRoad(this);
	}
	
	public void enter(Vehicle v) {   
		if (v.getLocation() != 0 && v.getSpeed()!=0)
			throw new IllegalArgumentException("La localizacion y la velocidad deben comenzar con valor 0"); 
		vehicle.add(v); 
	}
	
	public void exit(Vehicle v) { 
		vehicle.remove(v);  
	}
	
	public void setWeather(Weather w) {
		if(w.equals(null)) 
			throw new NullPointerException ("El weather no puede tomar un valor nulo"); 
		weather=w;
	} 
	
	public void addContamination(int c) {
		if(c<0)
			throw new IllegalArgumentException("La contaminación no puede tomar valores negativos");
		contTotal+=c;
	}
	
	public abstract void reduceTotalContamination(); 
	public abstract void updateSpeedLimit();
	public abstract int calculateVehicleSpeed(Vehicle v);
	
	public void advance(int currTime) {
		reduceTotalContamination();
		updateSpeedLimit();
		for(Vehicle v: vehicle) {
			v.setSpeed(calculateVehicleSpeed(v));
			v.advance(currTime); 
		}
		Vehicle aux;
		for(int i = 1; i < vehicle.size(); i++) {
			int j=i-1, k=i;
			while(j >= 0 && vehicle.get(k).getLocation() > vehicle.get(j).getLocation()) {
				aux = vehicle.get(j);
				vehicle.set(j, vehicle.get(k));
				vehicle.set(k, aux);
				j--;
				k--;		
			}
		}		
	}
	
	
	public JSONObject report() {
		JSONObject json = new JSONObject();
		json.put("id", this.getId());
		json.put("speedlimit", this.speedLimit);
		json.put("weather", this.weather);
		json.put("co2", this.contTotal);
		JSONArray jv = new JSONArray();
		json.put("vehicles", jv);
		for (Vehicle v : vehicle)
		{
			jv.put(v.getId());
		}
		json.put("vehicles", jv);
		return new JSONObject(json.toString() + "\n");
	}
	
	public int getLength() {
		return length; 
	}
		
	public Junction getDest() {
		return destJunc;
	}
	
	public Junction getSrc() {
		return srcJunc;
	}
	
	public Weather getWeather() {
		return weather;
	}
	
	public int getContLimit() {
		return contLimit;
	}
	
	public int getMaxSpeed() {
		return maxSpeed;
	}
	
	public int getTotalCO2() {
		return contTotal;
	}
	
	public void setTotalCO2(int tc) {
		contTotal = tc;
	}
		
	public int getSpeedLimit() {
		return speedLimit;
	}
	
	public void setSpeedLimit(int c) {
		speedLimit = c;
	}
	
	
	public List<Vehicle> getVehicles() {
		return Collections.unmodifiableList(vehicle);  
	}

}
