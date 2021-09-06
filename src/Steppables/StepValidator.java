/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import sim.engine.SimState;
import sim.engine.Steppable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StepValidator implements Steppable {
    private static final long serialVersionUID = 1L;
    private ByteBuffer messageBB;
    private byte[] step;

    public StepValidator(byte[] step) {
        this.step = step;
    }

    public void step(SimState state) {

        //messageBB.order(ByteOrder.LITTLE_ENDIAN);
        //messageBB.putLong(state.schedule.getSteps());

        //                      TODO
        // for (int i = 0; i<state.numFlockers; i++) {
        //     Object f = state.flockers.allObjects.objs[i];
        //     messageBB.putInt(state.flockers.getObjectIndex(f));
        //     messageBB.putFloat((float) state.flockers.getObjectLocation(f).getX());
        //     messageBB.putFloat((float) state.flockers.getObjectLocation(f).getY());
        //     messageBB.putFloat((float) (state.lenght - state.flockers.getObjectLocation(f).getZ()));
        //  }
        //                      TODO

        //step = messageBB.array().clone();
        //messageBB.clear();
    }
}