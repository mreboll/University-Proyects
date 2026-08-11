package simulator.factories;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Pair;
import simulator.model.Event;
import simulator.model.SetContClassEvent;

public class SetContClassEventBuilder extends Builder<Event>{

	public SetContClassEventBuilder() {
		super("set_cont_class", "Set cont class event");

	}

	@Override
	protected Event create_instance(JSONObject data) {
		int time = data.getInt("time"); 
		List<Pair<String,Integer>> pair = new ArrayList<Pair<String,Integer>>();
		JSONArray pares = data.getJSONArray("info");
		for(int i=0; i<pares.length(); i++) {
			pair.add(new Pair<String, Integer>(pares.getJSONObject(i).getString("vehicle"), pares.getJSONObject(i).getInt("class")));
		}
		return new SetContClassEvent(time, pair);
	}
	
}
