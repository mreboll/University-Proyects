package simulator.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import simulator.control.Controller;
import simulator.model.Event;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;

public class EventsTableModel extends AbstractTableModel implements TrafficSimObserver {
	
	private Controller _ctrl;
	private Collection<Event> eventsList;
	private String[] cols = { "Time", "Desc." };

	public EventsTableModel(Controller ctrl) {
		_ctrl = ctrl;
		eventsList = new ArrayList<Event>();
		_ctrl.addObserver(this);
	}
	
	@Override
	public String getColumnName(int col) {	
		return cols[col];
	}

	@Override
	public int getRowCount() {
		if(eventsList.equals(null))
			return 0;
		return eventsList.size();
	}

	@Override
	public int getColumnCount() {
		return cols.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
			List<Event> e = new ArrayList<>(eventsList);
			Object obj = null;
			switch (columnIndex) {
				case 0:
					obj = e.get(rowIndex).getTime();
					break;
				case 1:
					obj = e.get(rowIndex).toString();
					break;
				default:
					break;
			}	
			return obj;
	}

	@Override
	public void onAdvance(RoadMap map, Collection<Event> events, int time) {
		updateList(events);
	}

	@Override
	public void onEventAdded(RoadMap map, Collection<Event> events, Event e, int time) {
		updateList(events);
	}

	@Override
	public void onReset(RoadMap map, Collection<Event> events, int time) {
		updateList(events);
	}

	@Override
	public void onRegister(RoadMap map, Collection<Event> events, int time) {
		updateList(events);
	}
	
	private void updateList(Collection<Event> events) {
		eventsList = events;
		fireTableDataChanged();
	}

}
