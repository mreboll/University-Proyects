package simulator.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Event;
import simulator.model.Road;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;
import simulator.model.Vehicle;

public class RoadsTableModel extends AbstractTableModel implements TrafficSimObserver{
	
	private Controller _ctrl;
	private List<Road> roadsList;
	private String[] cols = { "Id", "Length", "Weather", "Max. Speed", "Speed Limit", "Total CO2", "CO2 Limit" };
	
	public RoadsTableModel(Controller ctrl) {
		_ctrl = ctrl;
		roadsList = new ArrayList<Road>();
		
		_ctrl.addObserver(this);
	}

	@Override
	public String getColumnName(int col) {	
		return cols[col];
	}

	@Override
	public int getRowCount() {
		if(roadsList.equals(null))
			return 0;
		return roadsList.size();
	}

	@Override
	public int getColumnCount() {
		return cols.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object obj = null;
		switch (columnIndex) {
			case 0:
				obj = roadsList.get(rowIndex).getId();
				break;
			case 1:
				obj = roadsList.get(rowIndex).getLength();
				break;
			case 2:
				obj = roadsList.get(rowIndex).getWeather();
				break;
			case 3:
				obj = roadsList.get(rowIndex).getMaxSpeed();
				break;
			case 4:
				obj = roadsList.get(rowIndex).getSpeedLimit();
				break;
			case 5:
				obj = roadsList.get(rowIndex).getTotalCO2();
				break;
			case 6:
				obj = roadsList.get(rowIndex).getContLimit();
				break;
			default:
				break;
		}
		return obj;
	}

	@Override
	public void onAdvance(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getRoads());
	}

	@Override
	public void onEventAdded(RoadMap map, Collection<Event> events, Event e, int time) {
		updateList(map.getRoads());
	}

	@Override
	public void onReset(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getRoads());
	}

	@Override
	public void onRegister(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getRoads());
	}
	
	private void updateList(List<Road> roads) {
		roadsList = roads;
		fireTableDataChanged();
	}

}
