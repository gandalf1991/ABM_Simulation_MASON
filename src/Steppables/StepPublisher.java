/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import Main.Comms_Controller;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StepPublisher implements Steppable {
    private static final long serialVersionUID = 1L;
    private ByteBuffer messageBB;
    private byte[] step;

    public StepPublisher(byte[] step) {
        this.step = step;
    }

    public void step(SimState state) {

        messageBB = ByteBuffer.allocate(8);
        messageBB.order(ByteOrder.LITTLE_ENDIAN);
        messageBB.putLong(state.schedule.getSteps());

        // TODO

        step = messageBB.array();
        messageBB.clear();

        Comms_Controller.publishStep(state.schedule.getSteps(), step);
    }
}
