package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class Junction extends SimulatedObject{

	private List<Road> road;
	private Map<Junction,Road> mapCruceCarr;
	private List<List<Vehicle>> colaVehicle;
	private Map<Road,List<Vehicle>> carreteraCola;
	private int semaforoAct;
	private int cambioSemaforo;
	private LightSwitchingStrategy lsStrategy;
	private DequeuingStrategy dqStrategy;
	private int xCoor;
	private int yCoor; 
	
	public Junction(String id, LightSwitchingStrategy lsStrategy, DequeuingStrategy dqStrategy, int xCoor, int yCoor) {
		  super(id);
		  
		  if(lsStrategy.equals(null) || dqStrategy.equals(null)) 
			  throw new IllegalArgumentException("ni LightSwitchingStrategy, ni DequeuingStrategy pueden tomar valores nulos ");
		  if(xCoor<0 || yCoor<0)
			  throw new IllegalArgumentException("Las coordenadas no pueden ser negativas");
		  
		  this.lsStrategy = lsStrategy;
		  this.dqStrategy =dqStrategy;
		  this.xCoor = xCoor;
		  this.yCoor = yCoor;  
		  this.cambioSemaforo = 0;
		  this.semaforoAct = -1;
		  road = new ArrayList<Road>();
		  mapCruceCarr = new HashMap<Junction, Road>(); 
		  colaVehicle = new ArrayList<List<Vehicle>>(); 
		  carreteraCola = new HashMap<Road, List<Vehicle>>(); 
	      
		}

	public void addIncommingRoad(Road r) {	
		if (!r.getDest().equals(this))
			throw new IllegalArgumentException("No es una carretera entrante, el cruce destino no es igual al actual"); 
		else
			road.add(r);
			List<Vehicle> nuevaCola = new LinkedList<>();
			colaVehicle.add(nuevaCola);  
			carreteraCola.put(r, nuevaCola);
	}
	
	public void addOutGoingRoad(Road r) {
		if(!r.getSrc().equals(this)) 
			throw new IllegalArgumentException("La carretera no sale del cruce actual"); 
		if(mapCruceCarr.containsKey(r.getDest()))
			throw new IllegalArgumentException("Ya existe una carretrea con ese cruce destino"); 
		mapCruceCarr.put(r.getDest(), r); 
	}
	
	public void enter(Vehicle v) {	
		carreteraCola.get(v.getRoad()).add(v); 
	}
	
	public Road roadTo(Junction j) {
		return mapCruceCarr.get(j); 
	}
	
	@Override
	void advance(int time) {
		if(!colaVehicle.isEmpty() && semaforoAct!=-1) {
			List<Vehicle> cola = colaVehicle.get(semaforoAct);
			List<Vehicle> avanza = dqStrategy.dequeue(cola);
			for(Vehicle v: avanza) {
				v.moveToNextRoad();
				cola.remove(v);
			}
		}
		int cambia = lsStrategy.chooseNextGreen(road, colaVehicle, semaforoAct, cambioSemaforo, time);
		if(cambia != semaforoAct) {
			semaforoAct = cambia;
			cambioSemaforo = time; 
		}	
	}

	@Override
	public JSONObject report() {
		JSONObject json = new JSONObject();
		JSONArray jq = new JSONArray();
		
		json.put("id", this._id); 
		
		if(semaforoAct==-1) 
			json.put("green", "none");
		else if(road.size()>0)
			json.put("green", road.get(semaforoAct).getId());
		for(Road ro: road) { 
			JSONObject r = new JSONObject();
			r.put("road", ro.getId());
			
			JSONArray v = new JSONArray();
			for(Vehicle ve: carreteraCola.get(ro)) { 
				v.put(ve.getId()); 
			}
			r.put("vehicles", v);
			jq.put(r); 
		}
		json.put("queues", jq); 
		return json; 
	}
	
	public int getX() {
		return this.xCoor;
	}
	
	public int getY() {
		return this.yCoor;
	}
	
	public int getGreenLightIndex() {
		return this.semaforoAct;
	}
	
	public List<Road> getInRoads(){
		return new ArrayList<Road>(this.road);
	}

}
