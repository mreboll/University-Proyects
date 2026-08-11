package simulator.control;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.factories.Factory;
import simulator.model.Event;
import simulator.model.TrafficSimObserver;
import simulator.model.TrafficSimulator;

public class Controller {
	
	private TrafficSimulator trafficSimulator;
	private Factory<Event> eventsFactory;
	
	
	public Controller(TrafficSimulator sim, Factory<Event> eventsFactory) {
		if(sim == null || eventsFactory == null)
			  throw new IllegalArgumentException("Los valores de los parámetros no pueden ser null"); 
		  this.trafficSimulator = sim;
		  this.eventsFactory = eventsFactory;
	}
	
	
	
	public void loadEvents(InputStream in) {
		JSONObject jo = new JSONObject(new JSONTokener(in));
		JSONArray ja = jo.getJSONArray("events");  
		
		if(!jo.has("events"))
			throw new IllegalArgumentException("La entrada JSON no coincide con la necesaria");
		for(int i=0; i<ja.length(); i++) 
			trafficSimulator.addEvent(eventsFactory.create_instance(ja.getJSONObject(i))); 	
	}
	
	
	public void run(int n, OutputStream out) throws InterruptedException {
		if(out != null) {
			PrintStream print = new PrintStream(out);
			print.println("{");
			print.println("\"states\": [");
		
			for (int i=0; i<n; i++) {
				trafficSimulator.advance();
				print.print(trafficSimulator.report().toString()); 
				if(i<n)
					print.println(",");
			}
			print.println("]");
			print.println("}");
		}
		else {
			for(int i=0; i<n;i++)
				trafficSimulator.advance();
		}
	}
	
	public void reset(){
		trafficSimulator.reset();
	}
	
	public void addObserver(TrafficSimObserver o) {
		trafficSimulator.addObserver(o);
	}
	
	public void removeObserver(TrafficSimObserver o){
		trafficSimulator.removeObserver(o);
	}
	
	public void addEvent(Event e) {
		trafficSimulator.addEvent(e);
	}

}