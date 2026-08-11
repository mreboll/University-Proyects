package simulator.model;

import java.util.List;

import simulator.misc.Pair;

public class SetContClassEvent extends Event {
	
	private List<Pair<String,Integer>> cs;
	
	public SetContClassEvent(int time, List<Pair<String,Integer>> cs)  {
		  super(time);
		  this.cs = cs;
		}
	
	@Override
	void execute(RoadMap map) {
		if(cs == null)
			throw new IllegalArgumentException("La lista que establece la contaminación no puede tomar valores nulos"); 
		Vehicle v;
		for(Pair<String, Integer> p: cs) {
			v = map.getVehicle(p.getFirst());
			if(v == null)
				throw new IllegalArgumentException("El vehículo no existe en el mapa de carreteras.");
			v.setContClass(p.getSecond());
		}	
	}
	
	/*@Override
	public String toString() {
		return "Set Cont Class '"+this.cs.getSecond()+"'";
	}*/
}
