package simulator.factories;

import org.json.JSONObject;

import simulator.model.LightSwitchingStrategy;
import simulator.model.RoundRobinStrategy;

public class RoundRobinStrategyBuilder extends Builder<LightSwitchingStrategy> {

	public RoundRobinStrategyBuilder() {
		super("round_robin_lss", "Round robin light switching strategy");
	}

	@Override
	protected LightSwitchingStrategy create_instance(JSONObject data) {
		int time = 1;
		if(data.has("timeslot"))
			time = data.getInt("timeslot");
		return new RoundRobinStrategy(time); 
	}

}
