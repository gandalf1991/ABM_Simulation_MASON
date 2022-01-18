/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import Main.Sim_Controller;
import sim.engine.SimState;
import sim.engine.Steppable;
import Main.Update;

public class StepInitiator implements Steppable {
    private static final long serialVersionUID = 1L;

    public StepInitiator() {}

    public void step(SimState state) {
        // check updates
        while (!Sim_Controller.getUpdates().isEmpty()) {
            Update update = Sim_Controller.getUpdates().pop();
            if(update.is_new()) {
                update.setIs_new(!Sim_Controller.getSimulation().updateSimulationState(update.getUpdate()));
                if(update.is_new()) Sim_Controller.getUpdates().push(update);
            }
        }
    }
}