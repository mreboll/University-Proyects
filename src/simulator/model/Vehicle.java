package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;



public class Vehicle extends SimulatedObject{

	private List<Junction> itinerary;
	private int maxSpeed;
	private int actualSpeed;
	private VehicleStatus status;
	private Road road;
	private int localization;
	private int contClass;
	private int contTotal;
	private int distance;
	
	
	public Vehicle(String id, int maxSpeed, int contClass, List<Junction> itinerary) {
		  super(id);
		  
		  if(maxSpeed<=0)
			  throw new IllegalArgumentException("maxSpeed debe ser mayor que 0"); 
		  if (contClass < 0 || contClass > 10) 
	            throw new IllegalArgumentException("contClass debe estar entre 0 y 10");
		  if (itinerary == null || itinerary.size() < 2) 
	            throw new IllegalArgumentException("itinerary debe contener al menos 2 elementos"); 
		  this.maxSpeed = maxSpeed;
		  this.actualSpeed = 0; 
		  this.contClass = contClass;
		  this.itinerary = Collections.unmodifiableList(new ArrayList<>(itinerary));
		  this.status = VehicleStatus.PENDING;
		  this.road = null;
		  this.distance = 0;
		  this.contTotal = 0;
	}
	
	public void setSpeed(int s) { 
		if (this.status != VehicleStatus.TRAVELING)
			return;
		if(s<=maxSpeed && s>=0)
			actualSpeed = s;
		else if (s>=maxSpeed && s>=0)
			actualSpeed = maxSpeed;
		else
			throw new IllegalArgumentException("la velocidad debe ser un valor positivo");
	} 
	
	
	public void setContClass(int c) {
		if(c>=0 && c<=10)
			contClass = c;
		else
			throw new IllegalArgumentException("la contaminación debe ser un valor entre entre 0 y 10");  
	} 
	
	
	
	public void advance(int currTime) {
		if(status.equals(VehicleStatus.TRAVELING)) {
			int prevLoc = localization;
			localization+=actualSpeed;
			if(localization>=road.getLength()) {
				localization = road.getLength();
				status = VehicleStatus.WAITING; 
				road.getDest().enter(this);
				actualSpeed = 0;
			}
			distance += localization - prevLoc;
			int c = (localization-prevLoc)*contClass;
			contTotal+=c;
			road.addContamination(c);
		}
		else 
			actualSpeed = 0;
	}
	
	public void moveToNextRoad() {
		Road r;
		localization = 0;
		if(status.equals(VehicleStatus.PENDING)) {
			r = itinerary.get(0).roadTo(itinerary.get(1));
			status = VehicleStatus.TRAVELING;
			if(road!=null) 
				this.road.enter(this);
			else {
				this.road = r;
				this.road.enter(this);
			}
		}
		else if (status.equals(VehicleStatus.WAITING)){
			int i=0;
			boolean encontrado = false;
			while(i<itinerary.size()-1 && !encontrado) {
				if(this.road.getDest().equals(itinerary.get(i))) {
					r = itinerary.get(i).roadTo(itinerary.get(i+1));
					encontrado = true;
					status = VehicleStatus.TRAVELING;
					if (road != null) {
					    road.exit(this);
					}
					this.road = r;
					this.road.enter(this);
				}
				else
					i++;	
			}
			if(!encontrado) {
				status = VehicleStatus.ARRIVED;
				this.road.exit(this);
			}
		}
		else
			 throw new IllegalArgumentException("El estado de los vehı́culos no es Pending o Waiting.");
	}
	
	public JSONObject report() {
	JSONObject json = new JSONObject();
	   json.put("id", this.getId());
	   json.put("speed", this.actualSpeed);
	   json.put("distance", this.distance);
	   json.put("co2", this.contTotal);
	   json.put("class", this.contClass);
	   json.put("status", this.status);
	  
	   if (this.status == VehicleStatus.TRAVELING || this.status == VehicleStatus.WAITING) {
		   json.put("road", this.road);
		   json.put("location", this.localization);
	   } 
	   return new JSONObject(json.toString() + "\n");
	}
	
	public int getLocation() {
		return localization;
	}
	
	public int getSpeed() {
		return actualSpeed; 
	}
   
	public int getMaxSpeed() {
		return maxSpeed; 
	}
   
    public VehicleStatus getStatus() {
    	return status;
   
    }
    public int getContClass(){
    	return contClass; 
    
    }
    public int getTotalCO2() {
    	return contTotal; 
    }
   
    public List<Junction> getItinerary(){
    	return itinerary; 
    } 
    
    public Road getRoad() {
    	return road; 
    }
    
    public int getDistance() {
    	return distance;
    }
    
    public Junction getDest() {
    	return road.getDest();
    }
	    
}
