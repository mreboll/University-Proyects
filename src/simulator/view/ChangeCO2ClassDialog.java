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

import simulator.model.RoadMap;
import simulator.model.Vehicle;

public class ChangeCO2ClassDialog extends JDialog{
	
	private JPanel panelprinc;
	private JLabel textprinc;
	private JPanel panelbotones;
	private JLabel textv;
	private DefaultComboBoxModel<Vehicle> vehiclesModel; //ver q es
	private JComboBox<Vehicle> vehicles; //ver q es
	private JLabel textCO2;
	private DefaultComboBoxModel<Integer> CO2Model; //ver q es
	private JComboBox<Integer> CO2; //ver q es
	private JSpinner ticks;
	private JLabel textt;
	private JPanel paneloptions;
	private JButton botonc;
	private int x; //cambiar el nombre
	private JButton botonok;
	private final int MAX_CO2 = 10;

	public ChangeCO2ClassDialog(Frame f) {
		super(f, true);
		
		this.panelprinc = new JPanel();
		this.textprinc = new JLabel("<html>Schedule an event to change the CO2 class of a vehicle after a given number of<br>simulation ticks form now.</html>"); //cambiar este texto
		this.textv = new JLabel("Vehicle: ", JLabel.CENTER);
		this.panelbotones = new JPanel();
		this.vehiclesModel = new DefaultComboBoxModel<Vehicle>();
		this.vehicles = new JComboBox<Vehicle>(vehiclesModel);
		this.textCO2 = new JLabel("CO2 Class: ", JLabel.CENTER);
		this.CO2Model = new DefaultComboBoxModel<Integer>();
		this.CO2 = new JComboBox<Integer>(CO2Model);
		this.ticks = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		this.textt = new JLabel("Ticks: ", JLabel.CENTER);
		this.paneloptions = new JPanel();
		this.botonc = new JButton("Cancel");
		this.x = 0;
		this.botonok = new JButton("OK");

		InitGUI();
	}
	
	private void InitGUI() {
		setTitle("Change CO2 Class");
		
		panelprinc.setLayout(new BoxLayout(panelprinc, BoxLayout.Y_AXIS));
		setContentPane(panelprinc);	
		textprinc.setAlignmentX(CENTER_ALIGNMENT);
		panelprinc.add(textprinc);
		panelprinc.add(Box.createRigidArea(new Dimension(0, 20)));		
		panelbotones.setAlignmentX(CENTER_ALIGNMENT);
		
		panelbotones.add(textv);
		vehicles.setVisible(true);
		panelbotones.add(vehicles);
		panelbotones.add(textCO2);
		CO2.setVisible(true);
		panelbotones.add(CO2);
		
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
				ChangeCO2ClassDialog.this.setVisible(false);
			}
		});
		paneloptions.add(botonc);
		botonok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((vehiclesModel.getSelectedItem() != null) && (CO2Model.getSelectedItem() != null)) {
					x = 1;
					ChangeCO2ClassDialog.this.setVisible(false);
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
		for (Vehicle v : map.getVehicles())
			vehiclesModel.addElement(v);
		for (int i = 0; i <= MAX_CO2; i++)
			CO2Model.addElement(i);
		setVisible(true);
		setLocation(getParent().getLocation().x + 10, getParent().getLocation().y + 10); //no se si es pq el max de co2 es 10 (no creo)
		return x;
	}
	
	public Integer getCO2() {
		return (Integer) CO2Model.getSelectedItem();
	}
	
	public Vehicle getVehicle() {
		return (Vehicle) vehiclesModel.getSelectedItem();
	}
	
	public Integer getTicks() {
		return (Integer) ticks.getValue();
	}
}
