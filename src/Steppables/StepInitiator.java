/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import Main.Sim_Controller;
import Wrappers.GUIState_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONObject;
import sim.engine.SimState;
import sim.engine.Steppable;

public class StepInitiator implements Steppable {
    private static final long serialVersionUID = 1L;

    public StepInitiator() {}

    public void step(SimState state) {

        // check updates
        if (Sim_Controller.getUpdate().is_new()) {
            Sim_Controller.getUpdate().setIs_new(Sim_Controller.getSimulation().updateSimulationState(Sim_Controller.getUpdate().getUpdate()));
        }
    }
}