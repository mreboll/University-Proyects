package simulator.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Event;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;
import simulator.model.Vehicle;
import simulator.model.VehicleStatus;

public class VehiclesTableModel extends AbstractTableModel implements TrafficSimObserver{
	
	private Controller _ctrl;
	private List<Vehicle> vehiclesList;
	private String[] cols = { "Id", "Location", "Itinerary", "CO2 class", "Max. Speed", "Speed", "Total CO2", "Distance" };
	
	public VehiclesTableModel(Controller ctrl) {
		_ctrl = ctrl;
		vehiclesList = new ArrayList<Vehicle>();
		_ctrl.addObserver(this);
	}

	@Override
	public String getColumnName(int col) {	
		return cols[col];
	}

	@Override
	public int getRowCount() {
		if(vehiclesList.equals(null))
			return 0;
		return vehiclesList.size();
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
				obj = vehiclesList.get(rowIndex).getId();
				break;
			case 1:
				VehicleStatus status = vehiclesList.get(rowIndex).getStatus();
				StringBuilder text = new StringBuilder();
				switch (status) {
					case ARRIVED:    
						text.append("Arrived");
						break;
					case PENDING:
						text.append("Pending");
						break;
					case TRAVELING:  
						text.append(vehiclesList.get(rowIndex).getRoad() + ": " + vehiclesList.get(rowIndex).getLocation());
						break;	
					case WAITING:    
						text.append("Waiting:"+ vehiclesList.get(rowIndex).getDest());
						break;	
				}
				obj = text.toString();
				break;
			case 2:
				obj = vehiclesList.get(rowIndex).getItinerary();
				break;
			case 3:
				obj = vehiclesList.get(rowIndex).getContClass();
				break;
			case 4:
				obj = vehiclesList.get(rowIndex).getMaxSpeed();
				break;
			case 5:
				obj = vehiclesList.get(rowIndex).getSpeed();
				break;
			case 6:
				obj = vehiclesList.get(rowIndex).getTotalCO2();
				break;
			case 7:
				obj = vehiclesList.get(rowIndex).getDistance();
				break;
			default:
				break;
		}	
		return obj;
	}

	@Override
	public void onAdvance(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getVehicles());
	}

	@Override
	public void onEventAdded(RoadMap map, Collection<Event> events, Event e, int time) {
		updateList(map.getVehicles());
	}

	@Override
	public void onReset(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getVehicles());
	}

	@Override
	public void onRegister(RoadMap map, Collection<Event> events, int time) {
		updateList(map.getVehicles());
	}
	
	private void updateList(List<Vehicle> vehicles) {
		vehiclesList = vehicles;
		fireTableDataChanged();
	}

}
