package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class RoadMap {

	private List<Junction> junctionList;
	private List<Road> roadList; 
	private List<Vehicle> vehicleList;
	private Map<String,Junction> junctionMap;
	private Map<String,Road> roadMap;
	private Map<String,Vehicle> vehicleMap;
	
	
	
	public RoadMap() {
		super();
		this.junctionList = new ArrayList<Junction>();  
		this.roadList = new ArrayList<Road>();
		this.vehicleList = new ArrayList<Vehicle>();
		this.junctionMap = new HashMap<String, Junction>();
		this.roadMap = new HashMap<String, Road>();
		this.vehicleMap = new HashMap<String, Vehicle>();
		
		
	}

	public void addJunction(Junction j) {
		if(junctionMap.containsKey(j.getId()))
			throw new IllegalArgumentException("Ya existe un cruce con el mismo identificador");	
		junctionList.add(j); 
		junctionMap.put(j.getId(), j);  
	}
	
	public void addRoad(Road r) {
		if(roadMap.containsKey(r.getId())) 
			throw new IllegalArgumentException("Ya existe una carretera con el mismo identificador");
		if (!junctionMap.containsKey(r.getSrc().getId()) || !junctionMap.containsKey(r.getDest().getId())) { // ns si se mira en el mapa de carreteras o en el de cruces junctionMap.constainsKey
	        throw new IllegalArgumentException("Los cruces que conecta la carretera no existen en el mapa de carreteras");
	    }
		roadList.add(r); 
		roadMap.put(r.getId(), r);	
	}
	
	public void addVehicle(Vehicle v) {
		if(vehicleMap.containsKey(v.getId()))
			throw new IllegalArgumentException("Ya existe un vehiculo con el mismo identificador");
		List<Junction> cruces = v.getItinerary();
		for (int i = 0; i < cruces.size()-1; i++){
			if (cruces.get(i).roadTo(cruces.get(i+1)) == null)
				throw new IllegalArgumentException("El itineario es nulo.");
		}
		vehicleList.add(v); 
		vehicleMap.put(v.getId(), v);
	}
	
	public Junction getJunction(String id) { 
		for(Junction a: junctionList) {
			if(a.getId().equals(id))
				return a; 
		}
		return null;
	}
	
	public Road getRoad(String id) {
		for(Road a: roadList) {
			if(a.getId().equals(id))
				return a; 
		}
		return null;
	}
	
	public Vehicle getVehicle(String id) {
		for(Vehicle a: vehicleList) {
			if(a.getId().equals(id))
				return a; 
		}
		return null;
	}
	
	public List<Junction> getJunctions(){
		return Collections.unmodifiableList(junctionList);  
	}
	
	public List<Road> getRoads(){
		return Collections.unmodifiableList(roadList); 
	}
	
	public List<Vehicle> getVehicles(){
		return Collections.unmodifiableList(vehicleList);  
	}
	
	public void reset() {
		junctionList.clear();
		roadList.clear();
		vehicleList.clear();
		junctionMap.clear();
		roadMap.clear();
		vehicleMap.clear(); 
	}
	
	public JSONObject report()  {
		JSONObject json = new JSONObject();
		JSONArray jj = new JSONArray();
		JSONArray jr = new JSONArray(); 
		JSONArray jv = new JSONArray();  
		
		for(Junction j: junctionList) 
			jj.put(j.report()); 
		
		for(Road r: roadList) 
			jr.put(r.report()); 
		
		for(Vehicle v: vehicleList) 
			jv.put(v.report()); 
		
		json.put("junctions", jj);
		json.put("roads", jr);
		json.put("vehicles", jv);
		return json;
    }
}
