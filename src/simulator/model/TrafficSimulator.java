package simulator.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.json.JSONObject;

public class TrafficSimulator implements Observable<TrafficSimObserver>{
	
	private RoadMap map;
	private int time;
	private Queue<Event> cola;
	private List<TrafficSimObserver> obs;
	
	public TrafficSimulator() {	
		this.map = new RoadMap();
		this.time = 0;
		this.cola = new PriorityQueue<Event>();
		this.obs = new ArrayList<TrafficSimObserver>();
	}
	
	public void addEvent(Event e) {
		cola.add(e);
		for(TrafficSimObserver o: obs)
		o.onEventAdded(this.map, this.cola, e, this.time);
	}
	
	public void advance() {
		time++;
		while(cola.size()>0 && cola.peek().getTime() == time) {
			cola.peek().execute(map);
			cola.remove();
		}
		List<Junction> j = map.getJunctions();
		List<Road> r = map.getRoads();
		for(Junction a: j)
			a.advance(time);
		for(Road b: r)
			b.advance(time);
		for(TrafficSimObserver o: obs)
		o.onAdvance(this.map, this.cola, this.time);
	}
	
	public void reset() {
		this.time = 0;
		map.reset();
		cola.clear();
		for(TrafficSimObserver o: obs)
		o.onReset(this.map, this.cola, this.time);
	}
	
	public JSONObject report() {
		JSONObject json = new JSONObject();
		json.put("time", this.time);
		json.put("state", map.report());
		return json; 
	}

	@Override
	public void addObserver(TrafficSimObserver o) {
		if(!obs.contains(o))
			obs.add(o);
		o.onRegister(map, cola, time);
	}

	@Override
	public void removeObserver(TrafficSimObserver o) {
		if(obs.contains(o))
			obs.remove(o);
	}

}
