package simulator.factories;

import org.json.JSONObject;

import simulator.model.DequeuingStrategy;
import simulator.model.Event;
import simulator.model.LightSwitchingStrategy;
import simulator.model.NewJunctionEvent;

public class NewJunctionEventBuilder extends Builder<Event>{
	private Factory<LightSwitchingStrategy> lssFactory;
	private Factory<DequeuingStrategy> dqsFactory;
	

	public NewJunctionEventBuilder(Factory<LightSwitchingStrategy> lssFactory, Factory<DequeuingStrategy> dqsFactory) {
		super("new_junction", "New junction");
		this.dqsFactory = dqsFactory;
		this.lssFactory = lssFactory;
	}

	@Override
	protected Event create_instance(JSONObject data) {
		int time = data.getInt("time"); 
		String id = data.getString("id");
		int x = data.getJSONArray("coor").getInt(0); 
		int y = data.getJSONArray("coor").getInt(1); 
		JSONObject lightStrategy = data.getJSONObject("ls_strategy"); 
		JSONObject dequeueStrategy = data.getJSONObject("dq_strategy"); 
		LightSwitchingStrategy lss = lssFactory.create_instance(lightStrategy); 
		DequeuingStrategy ds = dqsFactory.create_instance(dequeueStrategy); 
		
		return new NewJunctionEvent(time, id, lss, ds, x, y); 
	}

}
