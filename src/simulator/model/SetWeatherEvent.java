package simulator.model;

import java.util.List;

import simulator.misc.Pair;

public class SetWeatherEvent extends Event {
	
	private List<Pair<String,Weather>> ws;

	public SetWeatherEvent(int time, List<Pair<String,Weather>> ws) {
		  super(time);
		  this.ws = ws;
		} 

	@Override
	void execute(RoadMap map) {
		if(ws == null)
			throw new IllegalArgumentException("La lista que establece el tiempo no puede tomar valores nulos"); 
		Road r;
		for(Pair<String, Weather> p: ws) {
			r = map.getRoad(p.getFirst());
			if(r == null)
				throw new IllegalArgumentException("La carretera no existe en el mapa de carreteras.");
			r.setWeather(p.getSecond());
		}
		
	}
	
	/*@Override
	public String toString() {
		return "Set Weather '"+this.ws.getSecond()+"'";
	}*/
}
