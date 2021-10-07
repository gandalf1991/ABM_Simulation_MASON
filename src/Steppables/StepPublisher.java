/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import Main.Comms_Controller;
import Main.Sim_Controller;
import Utils.Float2D;
import Utils.Float3D;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.primitives.Ints;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import sim.util.Int3D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import static com.google.common.io.ByteStreams.newDataOutput;
import static java.lang.System.currentTimeMillis;

public class StepPublisher implements Steppable {
    private static final long serialVersionUID = 1L;
    int step_id_bytes = 8;

    public StepPublisher() {}

    public void step(SimState state) {

        long time_before = currentTimeMillis();

        // Update/gather structures and variables to use in new Thread
        boolean is_discrete = GUIState_wrapper.getPrototype().get("type").equals("DISCRETE");
        JSONArray agent_prototypes = getDynamicAgentPrototypes();
        JSONArray generic_prototypes = getDynamicGenericPrototypes();
        Sim_Controller.getSimulation().updateSimulationWrapper(state);
        long step_id = state.schedule.getSteps();
        HashMap<Pair<Integer, String>, SimObject_wrapper> AGENTS_clone = (HashMap<Pair<Integer, String>, SimObject_wrapper>) GUIState_wrapper.getAGENTS().clone();
        HashMap<Pair<Integer, String>, SimObject_wrapper> GENERICS_clone = (HashMap<Pair<Integer, String>, SimObject_wrapper>) GUIState_wrapper.getGENERICS().clone();

        Sim_Controller.executor.execute(() -> {

            ByteBuffer rawBB;
            ByteBuffer quantitiesBB;
            ByteBuffer agentsBB;
            ByteBuffer genericsBB;
            byte[] message;

            //System.out.print("STEP: |||" + step_id + "||");

            ByteArrayDataOutput[] agentsBBs = constructByteBuffer(AGENTS_clone, is_discrete, agent_prototypes);
            ByteArrayDataOutput[] genericsBBs = constructByteBuffer(GENERICS_clone, is_discrete, generic_prototypes);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(agentsBBs[0].toByteArray());
                outputStream.write(genericsBBs[0].toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }

            quantitiesBB = ByteBuffer.wrap(outputStream.toByteArray());
            agentsBB = ByteBuffer.wrap(agentsBBs[1].toByteArray());
            genericsBB = ByteBuffer.wrap(genericsBBs[1].toByteArray());

            rawBB = ByteBuffer.allocate(step_id_bytes + quantitiesBB.capacity() + agentsBB.capacity() + genericsBB.capacity());
            rawBB.order(ByteOrder.BIG_ENDIAN);
            rawBB.putLong(step_id);                                 // Step ID
            rawBB.put(quantitiesBB);                                // Quantities
            rawBB.put(agentsBB);                                    // Agents
            rawBB.put(genericsBB);                                  // Generics

            int length = rawBB.position();
            rawBB.position(0);
            message = new byte[length];
            rawBB.get(message, 0, length);

            Comms_Controller.publishStep(step_id, message);

            //System.out.println("STEP: " + step_id + "| Before: " + message.length + "| After: " + after);
            //System.out.println("STEP_ID: " + step_id_bytes +"| QUANTITIES: " + quantitiesBB.capacity() + "| AGENTS: " + agentsBB.capacity() + "| GENRICS: " + genericsBB.capacity());

            rawBB = null;
            quantitiesBB = null;
            agentsBB = null;
            genericsBB = null;

            //System.out.print("|\n");
        });


        long time_after = currentTimeMillis();
        //System.out.println(getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + "| " + (time_after-time_before) + " millis.");

    }

    public JSONArray getDynamicAgentPrototypes() {
        JSONArray dynamic_protos = new JSONArray();
        for (Object g_p : (JSONArray) GUIState_wrapper.getPrototype().get("agent_prototypes")) {
            if (GUIState_wrapper.dynamicClasses.contains(((JSONObject)g_p).get("class"))){
                dynamic_protos.add(g_p);
            }
        }
        return dynamic_protos;
    }
    public JSONArray getDynamicGenericPrototypes() {
        JSONArray dynamic_protos = new JSONArray();
        for (Object g_p : (JSONArray) GUIState_wrapper.getPrototype().get("generic_prototypes")) {
            if (GUIState_wrapper.dynamicClasses.contains(((JSONObject)g_p).get("class"))){
                dynamic_protos.add(g_p);
            }
        }
        return dynamic_protos;
    }
    public ByteArrayDataOutput[] constructByteBuffer(HashMap<Pair<Integer, String>, SimObject_wrapper> simObjectCollection, boolean is_discrete, JSONArray prototypes) {
        JSONArray params;

        ByteArrayDataOutput quantitiesBB = newDataOutput();
        ByteArrayDataOutput simObjectsBB = newDataOutput();

        for (Object p : prototypes) {
            Object[] simObjects = simObjectCollection.entrySet().stream().filter(entry -> entry.getKey().getValue().equals(((JSONObject)p).get("class"))).map(wrapperEntry -> wrapperEntry.getValue()).toArray();
            //System.out.print(simObjects.length + "||");
            quantitiesBB.writeInt(simObjects.length);                                                                                                                                                   // Agents quantity per class
            for (Object s_o : simObjects) {
                params = (JSONArray)((JSONObject)p).get("params");
                //System.out.print(((SimObject_wrapper)s_o).getID() + "|");
                simObjectsBB.writeInt(((SimObject_wrapper)s_o).getID());                                                                                                                                // ID
                if (((SimObject_wrapper)s_o).getParams().containsKey("position")) {
                    params.forEach(o -> {
                        if (((JSONObject)o).get("name").equals("position") && (((JSONObject)o).get("is_in_step").equals(true) || ((SimObject_wrapper)s_o).Is_new())){
                            Object pos = ((SimObject_wrapper)s_o).getParams().get("position");
                            if(is_discrete) {
                                if(GUIState_wrapper.getDIMENSIONS().size() == 2){
                                    //System.out.print(((Int2D)pos).x + "|" + ((Int2D)pos).y + "|");
                                    simObjectsBB.writeInt(((Int2D)pos).x);
                                    simObjectsBB.writeInt(((Int2D)pos).y);
                                }
                                else {
                                    //System.out.print(((Int3D)pos).x + "|" + ((Int3D)pos).y + "|" + ((Int3D)pos).z + "|");
                                    simObjectsBB.writeInt(((Int3D)pos).x);
                                    simObjectsBB.writeInt(((Int3D)pos).y);
                                    simObjectsBB.writeInt(((Int3D)pos).z);
                                }
                            }
                            else {
                                if(GUIState_wrapper.getDIMENSIONS().size() == 2){
                                    //System.out.print(((Float2D)pos).x + "|" + ((Float2D)pos).y + "|");
                                    simObjectsBB.writeFloat(((Float2D)pos).x);
                                    simObjectsBB.writeFloat(((Float2D)pos).y);
                                }
                                else {
                                    //System.out.print(((Float3D)pos).x + "|" + ((Float3D)pos).y + "|" + ((Float3D)pos).z + "|");
                                    simObjectsBB.writeFloat(((Float3D)pos).x);
                                    simObjectsBB.writeFloat(((Float3D)pos).y);
                                    simObjectsBB.writeFloat(((Float3D)pos).z);
                                }
                            }
                        }

                    });
                }
                for (Object param_entry : ((SimObject_wrapper)s_o).getParams().entrySet()) {
                    params.forEach(o -> {
                            if(((JSONObject)o).get("name").equals(((Map.Entry<String, Object>)param_entry).getKey()) && !((JSONObject)o).get("name").equals("position")  && (((JSONObject)o).get("is_in_step").equals(true) || ((SimObject_wrapper)s_o).Is_new())) {
                                JSONObject param = (JSONObject)o;
                                switch (((String)param.get("type"))) {
                                    case "System.Single":
                                        //System.out.print((float)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                                        simObjectsBB.writeFloat((float)((Map.Entry<String, Object>) param_entry).getValue());
                                        break;
                                    case "System.Int32":
                                        //System.out.print((int)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                                        simObjectsBB.writeInt((int)((Map.Entry<String, Object>) param_entry).getValue());
                                        break;
                                    case "System.Boolean":
                                        //System.out.print(((Boolean)((Map.Entry<String, Object>) param_entry).getValue()) ? (short) 1 : (short) 0 + "|");
                                        simObjectsBB.writeBoolean(((Boolean)((Map.Entry<String, Object>) param_entry).getValue()));
                                        break;
                                    case "System.String":
                                        //System.out.print(((String)((Map.Entry<String, Object>) param_entry).getValue()).getBytes(StandardCharsets.UTF_8) + "|");
                                        simObjectsBB.write(((String)((Map.Entry<String, Object>) param_entry).getValue()).getBytes(StandardCharsets.UTF_8));
                                        break;
                                    default:
                                        break;
                                }
                            }
                    });
                }
                ((SimObject_wrapper)s_o).setIs_new(false);
                //System.out.print("|");
            }
        }
    return new ByteArrayDataOutput[]{quantitiesBB, simObjectsBB};
    }
}
