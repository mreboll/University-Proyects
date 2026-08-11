package simulator.factories;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Pair;
import simulator.model.Event;
import simulator.model.SetWeatherEvent;
import simulator.model.Weather;

public class SetWeatherEventBuilder extends Builder<Event>{

	public SetWeatherEventBuilder() {
		super("set_weather", "Set weather");
	}

	@Override
	protected Event create_instance(JSONObject data) {
		int time = data.getInt("time"); 
		List<Pair<String,Weather>> pair = new ArrayList<Pair<String,Weather>>();
		JSONArray pares = data.getJSONArray("info");
		for(int i=0; i<pares.length(); i++) {
			pair.add(new Pair<String, Weather>(pares.getJSONObject(i).getString("road"), Weather.valueOf(pares.getJSONObject(i).getString("weather"))));
		}
		return new SetWeatherEvent(time, pair);
	}

}
