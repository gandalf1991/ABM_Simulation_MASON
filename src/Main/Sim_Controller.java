/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Main;

import Events.EventArgs.*;
import Steppables.StepInitiator;
import Steppables.StepValidator;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.display.RateAdjuster;
import sim.display.SimpleController;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import java.nio.ByteBuffer;

public class Sim_Controller {

	// CONTROLLERS
	private static final Comms_Controller comms_controller = new Comms_Controller();

	// SIMULATION
	private static JSONObject sim_list = new JSONObject();
	private static SimStateWithController simulation;
	private static SimState simstate;
	private static Pair<Boolean, JSONObject> update;
	private static byte[] step;

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

	// MESSAGE BUFFERS
	private static ByteBuffer messageBB;
	private static ByteBuffer responseBB;

	// VARIABLES
	private static int simTopics = 1;
	private static double simStepRate;
	private static double clientStepRate = 60;
	private static int currentSteps;
	private static long lastRateTime;
	private static long RATE_UPDATE_INTERVAL = 5000L;

	// EVENT HANDLES
	private static SimStateEnum onCheckStatusRequestEventHandle(Object source, CheckStatusEventArgs e){
		return CheckStatus();
	}
	private static JSONObject onSimListRequestEventHandle(Object source, SimListRequestEventArgs e){
		sim_list.put("sim_list", new JSONArray());
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
	private static StepValidator stepValidator;
	private static RateAdjuster rateAdjuster;
	private static Steppable stepRatePrinter;
	private static Stoppable stepInitiator_stoppable;
	private static Stoppable stepValidator_stoppable;
	private static Stoppable rateAdjuster_stoppable;
	private static Stoppable stepRatePrinter_stoppable;


	// SUPPORT METHODS
	private static SimStateEnum CheckStatus() {
		return state;
	}
	private static boolean SimIntialize(@NotNull JSONObject payload) throws InstantiationException, IllegalAccessException {

		String class_name = (String)payload.get("name");
		Class<?> clazz;
		try {
			clazz = Class.forName("sim.app." + class_name.toLowerCase() + "." + class_name + "WithUI");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		// TODO instantiate right simulation with controller
		simulation = new SimStateWithController(clazz);
		simstate = simulation.state;
		simulation.createController();
		// TODO init sim

		System.out.println("Sim type: " + class_name);

		// schedule steppables
		stepInitiator_stoppable = simulation.scheduleRepeatingImmediatelyAfter(stepInitiator);
		rateAdjuster_stoppable = simulation.scheduleRepeatingImmediatelyBefore(rateAdjuster);
		stepRatePrinter_stoppable = simulation.scheduleRepeatingImmediatelyBefore(stepRatePrinter);
		stepValidator_stoppable = simulation.scheduleRepeatingImmediatelyBefore(stepValidator);

		return true;
	}
	private static boolean SimUpdate(@NotNull JSONObject payload) {

		// TODO

		return true;
	}
	private static boolean SimCommand(@NotNull JSONObject payload) {

		int command = ((Long)payload.get("command")).intValue();

		switch (command) {
			case 1:
				System.out.println("command: " +command);
				if (simulation.c.getPlayState() == SimpleController.PS_STOPPED) { simulation.c.pressPlay(); } else if (simulation.c.getPlayState() == SimpleController.PS_PAUSED) {  simulation.c.pressPause(); }
				return simulation.c.getPlayState() == SimpleController.PS_PLAYING;
			case 2:
				if(simulation.c.getPlayState() != SimpleController.PS_PAUSED) { simulation.c.pressPause(); }
				return simulation.c.getPlayState() == SimpleController.PS_PAUSED;
			case 3:
				simulation.c.pressStop();
				stepInitiator_stoppable.stop();
				stepValidator_stoppable.stop();
				rateAdjuster_stoppable.stop();
				stepRatePrinter_stoppable.stop();
				return simulation.c.getPlayState() == SimpleController.PS_STOPPED;
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


	public static void main(String[] args) {

		// register to events
		comms_controller.checkStatusEventArgsEventHandler.subscribe(Sim_Controller::onCheckStatusRequestEventHandle);
		comms_controller.simListRequestEventArgsEventHandler.subscribe(Sim_Controller::onSimListRequestEventHandle);
		comms_controller.simInitializeEventHandler.subscribe(Sim_Controller::onSimInitializeEventHandle);
		comms_controller.simUpdateEventHandler.subscribe(Sim_Controller::onSimUpdateEventHandle);
		comms_controller.simCommandEventHandler.subscribe(Sim_Controller::onSimCommandEventHandle);

		// init steppable
		stepInitiator = new StepInitiator(simulation, update);
		stepValidator = new StepValidator(step);
		rateAdjuster = new RateAdjuster(clientStepRate);
		stepRatePrinter = new Steppable() {
			@Override
			public void step(SimState simState) {
				getSimStepRate();
			}
		};
	}



}
