package simulator.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import simulator.control.Controller;
import simulator.model.Event;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;

public class StatusBar extends JPanel implements TrafficSimObserver {
	
	
	private Controller _ctrl;
	private JLabel time;
	private JLabel Event = new JLabel();
	private JLabel actualTime = new JLabel();
	
	public StatusBar(Controller ctrl) {
		_ctrl = ctrl;
		initGUI();
		
		ctrl.addObserver(this);
	}
	
	private void initGUI() {
		this.setBorder(BorderFactory.createBevelBorder(1));
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		time = new JLabel("Time: ", JLabel.LEFT);
		this.add(time);
		actualTime = new JLabel("");
		this.add(actualTime);
		this.add(new JSeparator(SwingConstants.VERTICAL));
		Event = new JLabel("");
		this.add(Event);
	}

	@Override
	public void onAdvance(RoadMap map, Collection<Event> events, int time) {
		Event.setText("");
		actualTime.setText("" + time);
	}

	@Override
	public void onEventAdded(RoadMap map, Collection<Event> events, Event e, int time) {
		Event.setText(e.toString());
		actualTime.setText("" + time);
	}

	@Override
	public void onReset(RoadMap map, Collection<Event> events, int time) {
		Event.setText("");
		actualTime.setText("" + time);
	}

	@Override
	public void onRegister(RoadMap map, Collection<Event> events, int time) {
		Event.setText("Welcome!");
		actualTime.setText("" + time);
	}

}
