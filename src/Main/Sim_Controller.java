/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Main;

import Events.EventArgs.*;
import Steppables.StepInitiator;
import Steppables.StepPublisher;
import Wrappers.GUIState_wrapper;
import javafx.util.Pair;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Sim_Controller {

	// SIMULATION
	private static JSONObject sim_list = new JSONObject();
	private JSONObject current_sim = new JSONObject();
	private static Update update = new Update(false, new JSONObject());
	private static GUIState_wrapper simulation;
	private static SimState simstate;
	private static byte[] step;
	private static Class<?> clazz;

	// THREADS
	public static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5000);

	// GETTERS/SETTERS
	public static GUIState_wrapper getSimulation() {
		return simulation;
	}
	public static void setSimulation(GUIState_wrapper simulation) {
		Sim_Controller.simulation = simulation;
	}
	public static Update getUpdate() {
		return update;
	}
	public static void setUpdate(Update update) {
		Sim_Controller.update = update;
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
		READY(0),							// sim init
		PLAY(1),
		PAUSE(2);

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
	private static double simStepRate;
	private static double clientStepRate = 60;
	private static int currentSteps;
	private static long lastRateTime;
	private static long RATE_UPDATE_INTERVAL = 5000L;

	// CONTROLLERS
	private static final Comms_Controller comms_controller = new Comms_Controller();

	// EVENT HANDLES
	private static SimStateEnum onCheckStatusRequestEventHandle(Object source, CheckStatusEventArgs e){
		return CheckStatus();
	}
	private static JSONObject onSimListRequestEventHandle(Object source, SimListRequestEventArgs e) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		sim_list = (JSONObject) parser.parse(new FileReader("Simulation list example.json"));
		return sim_list;
	}
	private static boolean onSimInitializeEventHandle(Object source, SimInitializeEventArgs e) throws InstantiationException, IllegalAccessException {
		return SimIntialize(e.getPayload());
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
	private static boolean SimIntialize(@NotNull JSONObject payload) throws InstantiationException, IllegalAccessException {

		String class_name = (String)payload.get("name");
		try {
			clazz = Class.forName("Sim." + class_name.toLowerCase() + "." + class_name + "ForUnity");
		} catch (ClassNotFoundException e) { e.printStackTrace(); return false;	}
		simulation = (GUIState_wrapper)clazz.newInstance();
		simstate = simulation.state;

		// INIT PROTO & SIM
		simulation.initPrototype(payload);
		simulation.initSimulationState();

		// schedule steppables
		stepInitiator_stoppable = simulation.scheduleRepeatingImmediatelyBefore(stepInitiator);
		stepPublisher_stoppable = simulation.scheduleRepeatingImmediatelyAfter(stepPublisher);
		stepRatePrinter_stoppable = simulation.scheduleRepeatingImmediatelyAfter(stepRatePrinter);
		rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);

		return true;
	}
	private static boolean SimUpdate(@NotNull JSONObject payload) {
		update = new Update(true, payload);
		return true;
	}
	private static boolean SimCommand(@NotNull JSONObject payload) {

		int command = ((Long)payload.get("command")).intValue();

		switch (command) {
			case 0:
				return simulation.step();
			case 1:
				GUIState_wrapper.c.pressPlay();
				return GUIState_wrapper.c.getPlayState() == SimpleController.PS_PLAYING;
			case 2:
				GUIState_wrapper.c.pressPause();
				return GUIState_wrapper.c.getPlayState() == SimpleController.PS_PAUSED;
			case 3:
				GUIState_wrapper.c.pressStop();
				resetSimulation();
				return GUIState_wrapper.c.getPlayState() == SimpleController.PS_STOPPED;
			case 4:
				int speed = (int)payload.get("value");
				switch (speed) {
					case 4:		// MAX SPEED
						rateAdjuster_stoppable.stop();
						return rateAdjuster==null;
					case 3:		// 2X SPEED
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(2 * clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						return rateAdjuster!=null;
					case 2:		// 1X SPEED
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						return rateAdjuster!=null;
					case 1:		// 0.5X SPEED
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(0.5f * clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						return rateAdjuster!=null;
					case 0:		// 0.25X SPEED
						rateAdjuster_stoppable.stop();
						rateAdjuster = new RateAdjuster(0.25f * clientStepRate);
						rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyAfter(rateAdjuster);
						return rateAdjuster!=null;
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
			simStepRate = (double)currentSteps / ((double)(l - lastRateTime) / 1000.0D);
			currentSteps = 0;
			lastRateTime = l;
			System.out.println("Step: " + simstate.schedule.getSteps() + " - " + simStepRate + " steps/s.");
		}
	}
	private static boolean resetSimulation(){
		simulation.state.schedule.reset();
		simulation.resetSimulation();
		return true;
	}

	public static void main(String[] args) {

		// register to events
		comms_controller.checkStatusEventArgsEventHandler.subscribe(Sim_Controller::onCheckStatusRequestEventHandle);
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
