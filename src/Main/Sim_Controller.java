/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Main;

import Events.EventArgs.*;
import Steppables.StepInitiator;
import Steppables.StepPublisher;
import Wrappers.GUIState_wrapper;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sim.display.RateAdjuster;
import sim.display.SimpleController;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Sim_Controller {

	// SIMULATION
	private static JSONObject sim_list = new JSONObject();
	private JSONObject current_sim = new JSONObject();
	private static Stack<Update> updates = new Stack<Update>(){};
	private static GUIState_wrapper simulation;
	private static SimState simstate;
	private static byte[] step;
	private static Class<?> clazz;

	// THREADS
	public static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

	// GETTERS/SETTERS
	public static GUIState_wrapper getSimulation() {
		return simulation;
	}
	public static void setSimulation(GUIState_wrapper simulation) {
		Sim_Controller.simulation = simulation;
	}
	public static Stack<Update> getUpdates() {
		return updates;
	}
	public static void setUpdates(Stack<Update> updates) {
		Sim_Controller.updates = updates;
	}
	public static double getClientStepRate() {
		return clientStepRate;
	}
	public static void setClientStepRate(double clientStepRate) {
		Sim_Controller.clientStepRate = clientStepRate;
	}
	public static byte[] getStep() {
		return step;
	}
	public static void setStep(byte[] step) {
		Sim_Controller.step = step;
	}
	public static Class<?> getClazz() {
		return clazz;
	}
	public static void setClazz(Class<?> clazz) {
		Sim_Controller.clazz = clazz;
	}

	// STATE
	public static SimStateEnum state = SimStateEnum.NOT_READY;
	public enum SimStateEnum {

		NOT_READY(-1),						// sim not init
		READY(0),								// sim init
		PLAY(1),
		PAUSE(2),
		BUSY(3),
		ERROR(999);

		private final int code;
		private SimStateEnum(int code){
			this.code = code;
		}
		public int getCode(){
			return code;
		}
	}

	// VARIABLES
	public static int simTopics = 60;
	public static double simStepRate = 0;
	public static double clientStepRate = 60;
	private static long elapsedTime = 0;
	private static long lastRateTime = 0;
	private static long lastPrintTime = 0;
	private static int currentSteps = 0;
	private static long RATE_UPDATE_INTERVAL = 0L;
	private static long PRINT_INTERVAL = 20000L;
	public static boolean completeStep = false;

	// CONTROLLERS
	private static final Comms_Controller comms_controller = new Comms_Controller();

	// EVENT HANDLES
	private static SimStateEnum onCheckStatusRequestEventHandle(Object source, CheckStatusEventArgs e){
		return CheckStatus();
	}
	private static boolean onDisconnectEventHandle(Object source, DisconnectEventArgs e){
		switch ((Comms_Controller.DisconnectAction) e.getPayload().get("action")){
			case PAUSE:
				if(!state.equals(SimStateEnum.PAUSE)){
					GUIState_wrapper.c.pressPause();
					state = SimStateEnum.PAUSE;
					rateAdjuster_stoppable.stop();
					rateAdjuster = new RateAdjuster(clientStepRate);
					rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
				}
				return true;
			case STOP:
				GUIState_wrapper.c.pressStop();
				stopSimulation();
				rateAdjuster_stoppable.stop();
				rateAdjuster = new RateAdjuster(clientStepRate);
				rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
				return true;
		}
		return false;
	}
	private static JSONObject onSimListRequestEventHandle(Object source, SimListRequestEventArgs e) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		sim_list = (JSONObject) parser.parse(new FileReader("Simulation list example.json"));
		return sim_list;
	}
	private static boolean onSimInitializeEventHandle(Object source, SimInitializeEventArgs e) throws InstantiationException, IllegalAccessException {
		return SimInitialize(e.getPayload());
	}
	private static boolean onSimUpdateEventHandle(Object source, SimUpdateEventArgs e){
		return SimUpdate(e.getPayload());
	}
	private static boolean onSimCommandEventHandle(Object source, SimCommandEventArgs e){
		return SimCommand(e.getPayload());
	}

	// STEPPABLE/STOPPABLE TO WRAP STEP
	private static StepInitiator stepInitiator;
	private static RateAdjuster rateAdjuster;
	private static StepPublisher stepPublisher;
	private static Steppable stepRatePrinter;
	private static Stoppable stepInitiator_stoppable;
	private static Stoppable rateAdjuster_stoppable;
	private static Stoppable stepRatePrinter_stoppable;
	private static Stoppable stepPublisher_stoppable;


	// SUPPORT METHODS
	private static SimStateEnum CheckStatus() {
		return state;
	}
	private static boolean SimInitialize(@NotNull JSONObject payload) throws InstantiationException, IllegalAccessException {
		state = SimStateEnum.BUSY;
		String class_name = (String)payload.get("name");
		try {
			clazz = Class.forName("Sim." + class_name.toLowerCase() + "." + class_name + "ForUnity");
		} catch (ClassNotFoundException e) { e.printStackTrace(); return false;	}
		simulation = (GUIState_wrapper)clazz.newInstance();
		simstate = simulation.state;

		// INIT PROTO & SIM
		simulation.initPrototype(payload);
		if(simulation.initSimulationState()) {
			state = SimStateEnum.READY;
		}
		else {
			state = SimStateEnum.ERROR;
			// SEND SERVER ERROR
		}

		// schedule steppables
		stepInitiator_stoppable = simulation.scheduleRepeatingImmediatelyBefore(stepInitiator);
		stepRatePrinter_stoppable = simulation.scheduleRepeatingImmediatelyAfter(stepRatePrinter);
		stepPublisher_stoppable = simulation.scheduleRepeatingImmediatelyAfter(stepPublisher);
		rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);

		return true;
	}
	private static boolean SimUpdate(@NotNull JSONObject payload) {
		SimStateEnum old_state = state;
		state = SimStateEnum.BUSY;
		updates.push(new Update(true, payload));
		state = old_state;
		return true;
	}
	private static boolean SimCommand(@NotNull JSONObject payload) {

		int command = ((Long)payload.get("command")).intValue();

		switch (command) {
			case 0:
				return simulation.step();
			case 1:
				GUIState_wrapper.c.pressPlay();
				state = SimStateEnum.PLAY;
				return GUIState_wrapper.c.getPlayState() == SimpleController.PS_PLAYING;
			case 2:
				GUIState_wrapper.c.pressPause();
				state = SimStateEnum.PAUSE;
				return GUIState_wrapper.c.getPlayState() == SimpleController.PS_PAUSED;
			case 3:
				GUIState_wrapper.c.pressStop();
				stopSimulation();
				return GUIState_wrapper.c.getPlayState() == SimpleController.PS_STOPPED;
			case 4:
				int speed = ((Long)payload.get("value")).intValue();
				int prev_state = GUIState_wrapper.c.getPlayState();
				switch (speed) {
					case 4:		// MAX SPEED
						rateAdjuster_stoppable.stop();
						return true;
					case 3:		// 2X SPEED
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPause();
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(2 * clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPlay();
						return true;
					case 2:		// 1X SPEED
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPause();
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPlay();
						return true;
					case 1:		// 0.5X SPEED
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPause();
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(0.5f * clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPlay();
						return true;
					case 0:		// 0.25X SPEED
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPause();
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(0.25f * clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						if(prev_state == SimpleController.PS_PLAYING) GUIState_wrapper.c.pressPlay();
						return true;
				}
				break;
			// case 5:
			//	PERFORMANCE = Boolean.parseBoolean(msg[1]);
			//	simTopics = PERFORMANCE ? 60 : 1;
		}
		return false;
	}
	public static void getSimStepRate() {
		++currentSteps;
		long l = System.currentTimeMillis();
		if (l - lastRateTime >= RATE_UPDATE_INTERVAL) {
			simStepRate = currentSteps*(1000/(Double.max(l - lastRateTime, Integer.MIN_VALUE)));		// estimate currentSteps/s as steps produced in x millis * 1 sec/x millis
			currentSteps = 0;
			lastRateTime = l;
			if(l - lastPrintTime >= PRINT_INTERVAL){
				lastPrintTime = l;
				System.out.println("Step: " + simstate.schedule.getSteps() + " - " + simStepRate + " steps/s.");
				System.out.println("| AGENTS: " + GUIState_wrapper.getAGENTS().size() + "| GENERICS: " + GUIState_wrapper.getGENERICS().size());
			}
		}
	}
	public static boolean resetSimulation(){
		Comms_Controller.publishStepOnTopic(new byte[0], 0, true);
		executor.shutdownNow();
		if (executor.isShutdown()) executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
		simulation.state.schedule.reset();
		simulation.resetSimulation();
		state = SimStateEnum.READY;
		return true;
	}
	public static boolean stopSimulation(){
		Comms_Controller.publishStepOnTopic(new byte[0], 0, true);
		executor.shutdownNow();
		if (executor.isShutdown()) executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
		state = SimStateEnum.NOT_READY;
		return true;
	}

	public static void main(String[] args) {

		// register to events
		comms_controller.checkStatusEventArgsEventHandler.subscribe(Sim_Controller::onCheckStatusRequestEventHandle);
		comms_controller.disconnectEventArgsEventHandler.subscribe(Sim_Controller::onDisconnectEventHandle);
		comms_controller.simListRequestEventArgsEventHandler.subscribe(Sim_Controller::onSimListRequestEventHandle);
		comms_controller.simInitializeEventHandler.subscribe(Sim_Controller::onSimInitializeEventHandle);
		comms_controller.simUpdateEventHandler.subscribe(Sim_Controller::onSimUpdateEventHandle);
		comms_controller.simCommandEventHandler.subscribe(Sim_Controller::onSimCommandEventHandle);

		// init steppable
		stepInitiator = new StepInitiator();
		stepPublisher = new StepPublisher();
		rateAdjuster = new RateAdjuster(Sim_Controller.getClientStepRate());
		stepRatePrinter = new Steppable() {
			@Override
			public void step(SimState simState) {
				getSimStepRate();
			}
		};

	}
}
