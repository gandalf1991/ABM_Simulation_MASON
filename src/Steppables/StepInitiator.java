/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import Wrappers.GUIState_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONObject;
import sim.engine.SimState;
import sim.engine.Steppable;

public class StepInitiator implements Steppable {
    private static final long serialVersionUID = 1L;
    private GUIState_wrapper simulation;
    private Pair<Boolean, JSONObject> update;

    public StepInitiator(GUIState_wrapper simulation, Pair<Boolean, JSONObject> update) {
        this.simulation = simulation;
        this.update = update;
    }

    public void step(SimState state) {

        // check updates
        if (update.getKey()) {
            simulation.updateSimulationState(update.getValue());
        }
    }
}