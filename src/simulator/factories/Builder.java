package simulator.factories;

import org.json.JSONObject;

public abstract class Builder<T> {
	private String _type_tag;
	private String _desc;

	public Builder(String typeTag, String desc) {
		if (typeTag == null || desc == null || typeTag.isBlank() || desc.isBlank())
			throw new IllegalArgumentException("Invalid type/desc");

		_type_tag = typeTag;
		_desc = desc;
	}

	public String get_type_tag() {
		return _type_tag;
	}

	public JSONObject get_info() {
		JSONObject info = new JSONObject();
		info.put("type", _type_tag);
		info.put("desc", _desc);

		JSONObject data = new JSONObject();
		fill_in_fata(data);
		info.put("data", data);
		return info;
	}

	protected void fill_in_fata(JSONObject o) {
	}

	@Override
	public String toString() {
		return _desc;
	}

	protected abstract T create_instance(JSONObject data);
}
