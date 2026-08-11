package simulator.factories;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import simulator.model.Event;
import simulator.model.NewVehicleEvent;

public class NewVehicleEventBuilder extends Builder<Event>{

	public NewVehicleEventBuilder() {
		super("new_vehicle", "New vehicle");
	}

	@Override
	protected Event create_instance(JSONObject data) {
		int time = data.getInt("time"); 
		String id = data.getString("id");
		int maxspeed = data.getInt("maxspeed"); 
		int co2 = data.getInt("class");
		List <String> itinerary = new ArrayList<String>();  
		for(int i=0; i<data.getJSONArray("itinerary").length(); i++) {
			itinerary.add(data.getJSONArray("itinerary").getString(i));
		}
		return new NewVehicleEvent(time, id, maxspeed, co2, itinerary);
	}
}
