package simulator.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Event;
import simulator.model.Junction;
import simulator.model.Road;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;
import simulator.model.Vehicle;

public class JunctionsTableModel extends AbstractTableModel implements TrafficSimObserver{
	
	private Controller _ctrl;
	private List<Junction> junctionsList;
	private String[] cols = { "Id", "Green", "Queues", };

	
	public JunctionsTableModel(Controller ctrl) {
		_ctrl = ctrl;
		junctionsList = new ArrayList<Junction>();
		
		_ctrl.addObserver(this);
	}

	@Override
	public String getColumnName(int col) {	
		return cols[col];
	}

	@Override
	public int getRowCount() {
		if(junctionsList.equals(null))
			return 0;
		return junctionsList.size();
	}

	@Override
	public int getColumnCount() {
		return cols.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object obj = null;
		String queue = "";
		switch (columnIndex) {
			case 0:
				obj = junctionsList.get(rowIndex).getId();
				break;
			case 1:
				int indice = junctionsList.get(rowIndex).getGreenLightIndex();
				
				if (indice == -1)
					obj = "NONE";
				else
					obj = junctionsList.get(rowIndex).getInRoads().get(indice);
				break;
			case 2:
				for (Road road : junctionsList.get(rowIndex).getInRoads())
					queue += road.getId() + ":" + road.getVehicles().toString()+ " ";
				obj = queue;
				break;
			default:
				break;
		}
		
		return obj;
	}

	@Override
	public void onAdvance(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getJunctions());
	}

	@Override
	public void onEventAdded(RoadMap map, Collection<Event> events, Event e, int time) {
		updateList(map.getJunctions());
	}

	@Override
	public void onReset(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getJunctions());
	}

	@Override
	public void onRegister(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getJunctions());
	}
	
	private void updateList(List<Junction> junctions) {
		junctionsList = junctions;
		fireTableDataChanged();
	}

}
