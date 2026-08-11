package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import simulator.control.Controller;
import simulator.factories.*;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.Factory;
import simulator.model.DequeuingStrategy;
import simulator.model.Event;
import simulator.model.LightSwitchingStrategy;
import simulator.model.TrafficSimulator;
import simulator.view.MainWindow;

public class Main {

	private static String _inFile = null;
	private static String _outFile = null;
	private static Factory<Event> _eventsFactory = null;
	private static Integer _ticks = 10;  
	private static String _mode = "gui";

	private static void parseArgs(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = buildOptions();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parseHelpOption(line, cmdLineOptions);
			parseTicksOption(line);
			parseInModeOption(line);
			if(_mode.equals("gui")) {
				parseInFileOptionGUI(line);			}
			else {
			parseInFileOption(line);
			parseOutFileOption(line);
			}
			

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options buildOptions() {
		Options cmdLineOptions = new Options();

		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("Events input file").build());
		cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file, where reports are written.").build());
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message").build());
		cmdLineOptions.addOption(Option.builder("t").longOpt("ticks").hasArg().desc("Simulation ticks (default: 10)").build()); 
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg().desc("Simulation mode (default: gui)").build()); 
		return cmdLineOptions;
	}

	private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parseInFileOption(CommandLine line) throws ParseException {
		_inFile = line.getOptionValue("i");
		if (_inFile == null) {
			throw new ParseException("An events file is missing");
		}
	}
	
	private static void parseInFileOptionGUI(CommandLine line) throws ParseException {
		_inFile = line.getOptionValue("i");
	}

	private static void parseOutFileOption(CommandLine line) throws ParseException {
		_outFile = line.getOptionValue("o");
		if (_outFile == null) {
			throw new ParseException("An events file is missing");
		}
	}
	
	private static void parseTicksOption(CommandLine line) throws ParseException {
		if(line.hasOption("t"))
			_ticks = Integer.parseInt(line.getOptionValue("t"));
		if (_ticks < 0) { 
			throw new ParseException("No puede ser 0");
		}
	}
	
	private static void parseInModeOption(CommandLine line) throws ParseException {
		String aux = line.getOptionValue("m");
		if (!aux.equals("gui") && !aux.equals("console")) {
			throw new ParseException("Modo no valido");
		}
		_mode = aux;
	}


	private static void initFactories() {
		List<Builder<LightSwitchingStrategy>> switchStrat = new ArrayList<>();
		switchStrat.add( new RoundRobinStrategyBuilder() );
		switchStrat.add( new MostCrowdedStrategyBuilder() );
		Factory<LightSwitchingStrategy> lssFactory = new BuilderBasedFactory<>(switchStrat);

		List<Builder<DequeuingStrategy>> dequeueStrat = new ArrayList<>();
		dequeueStrat.add( new MoveFirstStrategyBuilder() );
		dequeueStrat.add( new MoveAllStrategyBuilder() );
		Factory<DequeuingStrategy> dqsFactory = new BuilderBasedFactory<>(dequeueStrat);

		// initialize the events factory
		List<Builder<Event>> events = new ArrayList<>();
		events.add( new NewJunctionEventBuilder(lssFactory,dqsFactory) );
		events.add( new NewCityRoadEventBuilder() );
		events.add( new NewInterCityRoadEventBuilder() );
		events.add( new NewVehicleEventBuilder() );
		events.add( new SetWeatherEventBuilder() );
		events.add( new SetContClassEventBuilder() ); 
		// ...
		_eventsFactory = new BuilderBasedFactory<>(events);

		
	}

	private static void startBatchMode() throws IOException, InterruptedException {
		OutputStream out;
		InputStream in = new FileInputStream(new File(_inFile));
		TrafficSimulator sim = new TrafficSimulator();
		Controller controller = new Controller(sim, _eventsFactory);
		
		if (_outFile == null)
		{
			out = System.out;
		}
		else
		{
			out = new FileOutputStream(new File(_outFile));
		}
		
		controller.loadEvents(in);
		controller.run(_ticks, out);
		in.close();
		out.flush();
		out.close();
	}

	private static void start(String[] args) throws IOException, InterruptedException {
		initFactories();
		parseArgs(args);
		if(_mode.equals("gui"))
			startGUIMode();
		else
			startBatchMode();
	}
	
	private static void startGUIMode() throws IOException{
		TrafficSimulator sim = new TrafficSimulator();
		Controller ctrl = new Controller(sim, _eventsFactory);
		if(_inFile != null) {
			InputStream in = new FileInputStream(new File(_inFile));
			ctrl.loadEvents(in);
			in.close();
		}	
		SwingUtilities.invokeLater(new Runnable() {	
			@Override
			public void run() {
				new MainWindow(ctrl);
			}
		});
	}	

	// example command lines:
	//
	// -i resources/examples/ex1.json
	// -i resources/examples/ex1.json -t 300
	// -i resources/examples/ex1.json -o resources/tmp/ex1.out.json
	// --help

	public static void main(String[] args) {
		try {
			start(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
