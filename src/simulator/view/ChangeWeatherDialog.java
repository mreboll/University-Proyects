package simulator.view;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import simulator.model.Road;
import simulator.model.RoadMap;
import simulator.model.Vehicle;
import simulator.model.Weather;

public class ChangeWeatherDialog extends JDialog{
	
	private JPanel panelprinc;
	private JLabel textprinc;
	private JPanel panelbotones;
	private JLabel textr;
	private DefaultComboBoxModel<Road> roadsModel; //ver q es
	private JComboBox<Road> roads; //ver q es
	private JLabel textw;
	private DefaultComboBoxModel<Weather> weatherModel; //ver q es
	private JComboBox<Weather> weather; //ver q es
	private JSpinner ticks;
	private JLabel textt;
	private JPanel paneloptions;
	private JButton botonc;
	private int x; //cambiar el nombre
	private JButton botonok;
	
public ChangeWeatherDialog(Frame f) {	
		super(f, true);
		
		this.panelprinc = new JPanel();
		this.textprinc = new JLabel("<html>Schedule an event to change the weather class of a road after a given number of<br>simulation ticks form now.</html>"); //cambiar este texto
		this.textr = new JLabel("Road: ", JLabel.CENTER);
		this.panelbotones = new JPanel();
		this.roadsModel = new DefaultComboBoxModel<Road>();
		this.roads = new JComboBox<Road>(roadsModel);
		this.textw = new JLabel("Weather: ", JLabel.CENTER);
		this.weatherModel = new DefaultComboBoxModel<Weather>();
		this.weather = new JComboBox<Weather>(weatherModel);
		this.ticks = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1)); //puede q falle
		this.textt = new JLabel("Ticks: ", JLabel.CENTER);
		this.paneloptions = new JPanel();
		this.botonc = new JButton("Cancel");
		this.x = 0;
		this.botonok = new JButton("OK");
		
		InitGUI();
	}

private void InitGUI() {
	setTitle("Change Road Weather");
	
	panelprinc.setLayout(new BoxLayout(panelprinc, BoxLayout.Y_AXIS));
	setContentPane(panelprinc);	
	textprinc.setAlignmentX(CENTER_ALIGNMENT);
	panelprinc.add(textprinc);
	panelprinc.add(Box.createRigidArea(new Dimension(0, 20)));		
	panelbotones.setAlignmentX(CENTER_ALIGNMENT);
	
	panelbotones.add(textr);
	roads.setVisible(true);
	panelbotones.add(roads);
	panelbotones.add(textw);
	weather.setVisible(true);
	panelbotones.add(weather);
	
	ticks.setMaximumSize(new Dimension(200, 30));
	ticks.setMinimumSize(new Dimension(80, 30));
	ticks.setPreferredSize(new Dimension(80, 30));
	
	panelbotones.add(textt);
	panelbotones.add(ticks);
	paneloptions.setAlignmentX(CENTER_ALIGNMENT);
	panelprinc.add(paneloptions);
	panelprinc.add(panelbotones);
	
	botonc.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			x = 0;
			ChangeWeatherDialog.this.setVisible(false);
		}
	});
	paneloptions.add(botonc);
	botonok.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if ((roadsModel.getSelectedItem() != null) && (roadsModel.getSelectedItem() != null)) {
				x = 1;
				ChangeWeatherDialog.this.setVisible(false);
			}
		}
	});
	paneloptions.add(botonok);

	setPreferredSize(new Dimension(500, 200));
	pack();
	setVisible(false);
	setResizable(false);
}

public int open(RoadMap map) {
	for (Road r : map.getRoads())
		roadsModel.addElement(r);
	for (Weather w : Weather.values())
		weatherModel.addElement(w);
	setVisible(true);
	setLocation(getParent().getLocation().x + 10, getParent().getLocation().y + 10);
	return x;
}

public Weather getWeather() {
	return (Weather) weatherModel.getSelectedItem();
}

public Road getRoad() {
	return (Road) roadsModel.getSelectedItem();
}

public Integer getTicks() {
	return (Integer) ticks.getValue();
}

}

