package simulator.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.Event;
import simulator.model.Junction;
import simulator.model.Road;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;
import simulator.model.Vehicle;
import simulator.model.Weather;

public class MapByRoadComponent extends JComponent implements TrafficSimObserver {
	
	private static final Color _BG_COLOR = Color.WHITE;
	private static final Color _JUNCTION_COLOR = Color.BLUE;
	private static final Color _JUNCTION_LABEL_COLOR = new Color(200, 100, 0);
	private static final Color _GREEN_LIGHT_COLOR = Color.GREEN;
	private static final Color _RED_LIGHT_COLOR = Color.RED;
	private static final int radio = 10;
	
	private RoadMap _map;
	private Image _car;
	
	public MapByRoadComponent(Controller ctrl) {
		
		initGUI();
		ctrl.addObserver(this);	
	}
	
	private void initGUI() {
		_car = loadImage("car_front.png");
	}
	
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// clear with a background color
		g.setColor(_BG_COLOR);
		g.clearRect(0, 0, getWidth(), getHeight());

		if (_map == null || _map.getJunctions().size() == 0) {
			g.setColor(Color.red);
			g.drawString("No map yet!", getWidth() / 2 - 50, getHeight() / 2);
		} else {
			updatePrefferedSize();
			drawMap(g);
		}
	}
	
	private void drawMap(Graphics g) {
		int i=0, inicio = 50, fin=getWidth()-100; 
		
		for(Road road: _map.getRoads()) {
			int coordY = (i+1)*50; 
			g.setColor(Color.BLACK);
			g.drawString(road.getId(), inicio-30, coordY + radio / 2); 
			g.drawLine(inicio, coordY, fin, coordY); 
			g.setColor(_JUNCTION_COLOR);
			g.fillOval(inicio-radio/2, coordY-radio/2, radio, radio); 
	
			
			if(road.getDest().getGreenLightIndex() != -1 && road.equals(road.getDest().getInRoads().get(road.getDest().getGreenLightIndex()))) 
				g.setColor(_GREEN_LIGHT_COLOR);
			else
				g.setColor(_RED_LIGHT_COLOR);
			g.fillOval(inicio-radio/2, coordY-radio/2, radio, radio); 
			g.setColor(_JUNCTION_LABEL_COLOR);
			g.drawString(road.getSrc().toString(), inicio, coordY-radio); 
			g.drawString(road.getDest().toString(), fin, coordY-radio); 
		
			
			Image tiempo = null;
			if(road.getWeather() == Weather.SUNNY)  
				tiempo = loadImage("sun.png"); 
			else if (road.getWeather() == Weather.STORM)
				tiempo = loadImage("storm.png");
			else if (road.getWeather() == Weather.RAINY)
				tiempo = loadImage("rain.png");
			else if (road.getWeather() == Weather.CLOUDY)
				tiempo = loadImage("cloud.png");
			else if (road.getWeather() == Weather.WINDY)
				tiempo = loadImage("wind.png");
			if(tiempo != null)
				g.drawImage(tiempo, fin+15, coordY-radio*2, 32, 32, this); 
			
			
			Image conta = null;
			int c = (int) Math.floor(Math.min((double) road.getTotalCO2() / (1+(double) road.getContLimit()), 1) / 0.19); 
			if(c > 6)
				c=6;
			if(c==0)
				conta = loadImage("cont_0.png");
			else if(c==1)
				conta = loadImage("cont_1.png");
			else if(c==2)
				conta = loadImage("cont_2.png");
			else if(c==3)
				conta = loadImage("cont_3.png");
			else if(c==4)
				conta = loadImage("cont_4.png");
			else if(c==5)
				conta = loadImage("cont_5.png");
			else if(c==6)
				conta = loadImage("cont_6.png");
			if(conta != null)
				g.drawImage(conta, fin+55, coordY-radio*2, 32, 32, this); 
			
			
			for(Vehicle vehicle: road.getVehicles()) {
				int v = inicio + (int) ((fin - inicio)*((double) vehicle.getLocation()/(double) road.getLength()));
				g.setColor(_GREEN_LIGHT_COLOR); 
				g.drawString(vehicle.getId(), v, coordY-radio-5); 
				g.drawImage(_car, v, coordY-radio-3, 16, 16, this); 
			}			
			i++;
		}
	}
	
	private void updatePrefferedSize() {
		int maxW = 200;
		int maxH = 200;
		for (Junction j : _map.getJunctions()) {
			maxW = Math.max(maxW, j.getX());
			maxH = Math.max(maxH, j.getY());
		}
		maxW += 20;
		maxH += 20;
		if (maxW > getWidth() || maxH > getHeight()) {
			setPreferredSize(new Dimension(maxW, maxH));
			setSize(new Dimension(maxW, maxH));
		}
	}
	
	private Image loadImage(String img) {
		Image i = null;
		try {
			return ImageIO.read(new File("resources/icons/" + img));
		} catch (IOException e) {
		}
		return i;
	}

	public void update(RoadMap map) {
		SwingUtilities.invokeLater(() -> {
			_map = map;
			repaint();
		});
	}

	@Override
	public void onAdvance(RoadMap map, Collection<Event> events, int time) {
		update(map);
	}

	@Override
	public void onEventAdded(RoadMap map, Collection<Event> events, Event e, int time) {
		update(map);
	}

	@Override
	public void onReset(RoadMap map, Collection<Event> events, int time) {
		update(map);
	}

	@Override
	public void onRegister(RoadMap map, Collection<Event> events, int time) {
		update(map);
	}

}
