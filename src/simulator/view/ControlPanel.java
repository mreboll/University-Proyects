package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import simulator.control.Controller;
import simulator.misc.Pair;
import simulator.model.Event;
import simulator.model.RoadMap;
import simulator.model.SetContClassEvent;
import simulator.model.SetWeatherEvent;
import simulator.model.TrafficSimObserver;
import simulator.model.Weather;

public class ControlPanel extends JPanel implements TrafficSimObserver{
	
	private Controller _ctr;
	private JToolBar barra;
	private JFileChooser file;
	private JButton fileButton;
	private JButton CO2Button;
	private JButton weatherButton;
	private JButton runButton;
	private JButton stopButton;
	private JSpinner ticksSpinner;
	private JLabel tickstext;
	private JButton exitButton;
	private ChangeCO2ClassDialog cambioCO2;
	private RoadMap map;
	private int time;
	private boolean pausa;
	private ChangeWeatherDialog cambioWeather;
	
	public ControlPanel(Controller ctr) {
		this._ctr = ctr;
		this.barra = new JToolBar();
		this.file = new JFileChooser();
		this.fileButton = new JButton();
		this.CO2Button = new JButton();
		this.weatherButton = new JButton();
		this.runButton = new JButton();
		this.stopButton = new JButton();
		this.ticksSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 10000, 1));
		this.tickstext = new JLabel("Ticks: ", JLabel.CENTER);
		this.exitButton = new JButton();
		this.cambioCO2 = new ChangeCO2ClassDialog((Frame) SwingUtilities.getWindowAncestor(this));
		this.pausa = true;
		this.cambioWeather = new ChangeWeatherDialog((Frame) SwingUtilities.getWindowAncestor(this));

		this.setLayout(new BorderLayout());
		
		iniciaGUI();
		_ctr.addObserver(this);
	}
	
	
	private void iniciaGUI() {
		barra.setFloatable(false);
		this.add(barra, BorderLayout.PAGE_START);
		barra.addSeparator();
		
		FileButton();
		barra.add(fileButton);
		barra.addSeparator();
		
		CO2Button();
		barra.add(CO2Button);
		WeatherButton();
		barra.add(weatherButton);
		barra.addSeparator();
		
		RunButton();
		barra.add(runButton);
		StopButton();
		barra.add(stopButton);
		ticksSpinner();
		barra.add(ticksSpinner);
		barra.add(tickstext);
		barra.add(Box.createHorizontalGlue());
		barra.addSeparator();
		
		ExitButton();
		barra.add(exitButton);	
	}
	
	private void FileButton() {
		file.setCurrentDirectory(new File("./resources/examples/"));
		file.setDialogTitle("Open");
		file.setFileFilter(new FileNameExtensionFilter("JavaScript Object Notation (JSON)", "json"));
		file.setMultiSelectionEnabled(false);
		
		fileButton.setToolTipText("Open file");
		fileButton.setIcon(new ImageIcon("./resources/icons/open.png"));
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				loadFile();			
			}
		});
	}
	
	private void loadFile() {
		updateUI();
		
		int x = file.showOpenDialog(this.getParent());
		if (x == JFileChooser.APPROVE_OPTION){
			InputStream input;
			try {
				input = new FileInputStream(file.getSelectedFile());
				_ctr.reset();
				_ctr.loadEvents(input);
			} catch (FileNotFoundException exc) {
				JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Error cargando el archivo");
			}					
		}
		else
		{
			JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "ERROR");
		}
		
	}
	
	private void CO2Button() {
		CO2Button.setIcon(new ImageIcon("./resources/icons/co2class.png"));
		CO2Button.setToolTipText("Changfe CO2 class of a vehicle");
		CO2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actualizaCO2();
			}
		});
	}
	
	private void actualizaCO2() {
		int x = 0;
		x = cambioCO2.open(map);
		if (x != 0) {
			List<Pair<String, Integer>> par = new ArrayList<>();
			par.add(new Pair<String, Integer>(cambioCO2.getVehicle().getId(), cambioCO2.getCO2()));
			try {
				_ctr.addEvent(new SetContClassEvent(time+cambioCO2.getTicks(), par));
			} catch (Exception e) {
				JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Error cambiando el CO2 (" + e + ")");
			}
		}
	}
	
	private void WeatherButton() {
		weatherButton.setIcon(new ImageIcon("./resources/icons/weather.png"));
		weatherButton.setToolTipText("Change Weather of a Road");
		weatherButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actualizaWeather();				
			}
		});
	}
	
	private void actualizaWeather() {
		int x = 0;	
		x = cambioWeather.open(map);
		if (x != 0) {
			List<Pair<String, Weather>> par = new ArrayList<>();
			par.add(new Pair<String, Weather>(cambioWeather.getRoad().getId(), cambioWeather.getWeather()));
			try {
				_ctr.addEvent(new SetWeatherEvent(time+cambioWeather.getTicks(), par));
			} catch (Exception e) {
				JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Error cambiando el clima (" + e + ")");
			}
		}
	}
	
	private void RunButton() {
		runButton.setIcon(new ImageIcon("./resources/icons/run.png"));
		runButton.setToolTipText("Run the simulator");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pausa = false;
				actualizaPanel(false);
				run_sim((Integer) ticksSpinner.getValue());			
			}
		});
	}

private void run_sim(int x) {	
	if (x > 0 && !pausa) {
		try {
			_ctr.run(1, null);
		} catch (Exception e ) {
			JOptionPane.showMessageDialog(null, "ERROR");
			pausa = true;
			actualizaPanel(true);
			return;
		}		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {		
				run_sim(x - 1);
			}
		});
	}
	else {
		actualizaPanel(true);
		pausa = true;
	}
}

	private void StopButton() {
		stopButton.setIcon(new ImageIcon("./resources/icons/stop.png"));
		stopButton.setToolTipText("Stop te simulation");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pausa = true;		
			}
		});
	}

	private void ticksSpinner() {
		ticksSpinner.setToolTipText("Simulator ticks to run: 1-10000");
		ticksSpinner.setMaximumSize(new Dimension(200, 30));
		ticksSpinner.setMinimumSize(new Dimension(80, 30));
		ticksSpinner.setPreferredSize(new Dimension(80, 30));
	}

	private void ExitButton() {
		exitButton.setIcon(new ImageIcon("./resources/icons/exit.png"));
		exitButton.setToolTipText("Cerrar simulacion");
		exitButton.setAlignmentX(RIGHT_ALIGNMENT);
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
	}
	
	private void exit() {
		int exit = JOptionPane.showConfirmDialog((Frame) SwingUtilities.getWindowAncestor(this), "Are you sure you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
		if (exit == 0)
		{
			System.exit(0);
		}
	}
	
	private void actualizaPanel(boolean activar) {
		fileButton.setEnabled(activar);
		CO2Button.setEnabled(activar);
		weatherButton.setEnabled(activar);
		runButton.setEnabled(activar);
		ticksSpinner.setEnabled(activar);
		exitButton.setEnabled(activar);
	}

	@Override
	public void onAdvance(RoadMap map, Collection<Event> xevents, int time) {
		this.map = map;
		this.time = time;
	}

	@Override
	public void onEventAdded(RoadMap map, Collection<Event> events, Event e, int time) {
		this.map = map;
		this.time = time;
	}

	@Override
	public void onReset(RoadMap map, Collection<Event> events, int time) {
		this.map = map;
		this.time = time;
	}

	@Override
	public void onRegister(RoadMap map, Collection<Event> events, int time) {
		this.map = map;
		this.time = time;
	}

}
