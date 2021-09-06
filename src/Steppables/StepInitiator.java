/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import javafx.util.Pair;
import org.json.simple.JSONObject;
import sim.engine.SimState;
import sim.engine.Steppable;
import Main.SimStateWithController;

public class StepInitiator implements Steppable {
    private static final long serialVersionUID = 1L;
    private SimStateWithController simulation;
    private Pair<Boolean, JSONObject> update;

    public StepInitiator(SimStateWithController simulation, Pair<Boolean, JSONObject> update) {
        this.simulation = simulation;
        this.update = update;
    }

    public void step(SimState state) {

        // check updates
        //if (update.getKey()) {

            // update sim

                // update sim params

                // update agents

                // update generics

                // update obstacles

        //}
    }
}