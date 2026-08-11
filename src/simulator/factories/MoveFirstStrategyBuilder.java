package simulator.factories;

import org.json.JSONObject;

import simulator.model.DequeuingStrategy;
import simulator.model.MoveFirstStrategy;

public class MoveFirstStrategyBuilder extends Builder<DequeuingStrategy>{

	public MoveFirstStrategyBuilder() {
		super("move_first_dqs", "Move first dequeuing strategy");
	}

	@Override
	protected DequeuingStrategy create_instance(JSONObject data) {
		return new MoveFirstStrategy();
	}

}
