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
import com.jogamp.opengl.math.Quaternion;
import ec.util.MersenneTwisterFast;
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
import java.util.ArrayList;
import java.util.HashMap;

import static com.google.common.io.ByteStreams.newDataOutput;
import static java.lang.System.currentTimeMillis;

public class StepPublisher implements Steppable {
    private static final long serialVersionUID = 1L;
    private MersenneTwisterFast random = new MersenneTwisterFast();
    private static double send_probability = 0.5d;
    int step_id_bytes = 8;
    public static long start_millis;

    public StepPublisher() {}

    public void step(SimState state) {

        JSONArray agent_prototypes;
        JSONArray generic_prototypes;
        JSONArray obstacle_prototypes;
        final long step_id = state.schedule.getSteps();
        final boolean completeStep = step_id == 1 || Sim_Controller.completeStep;                                           // mi assicuro di essere l'unico a leggere true in modo
        Sim_Controller.completeStep = false;                                                                                // da evitare interfogliamenti


        // check if step is complete(mandatory) and eventually avoid skipping it
        if(!completeStep) {
            //  calcolare la send prob                      60 steps/s  /  90 steps/s  => 0.666
            if(Sim_Controller.simStepRate > 0) {
                send_probability = Math.min((Sim_Controller.clientStepRate / Sim_Controller.simStepRate), 1d);
                System.out.println("Produced: " + Sim_Controller.simStepRate + "steps/s | Expected: " + Sim_Controller.clientStepRate + "steps/s");
                System.out.println("Send probability: " + send_probability);
                //  non inviamo lo step con prob -send prob
                if(!random.nextBoolean(send_probability)) return;
            }
        }


        // Update/gather structures and variables to use in new Thread
        boolean is_discrete = GUIState_wrapper.getPrototype().get("type").equals("DISCRETE");

        // Get prototypes
        agent_prototypes = (JSONArray) GUIState_wrapper.getPrototype().get("agent_prototypes");
        generic_prototypes = (JSONArray) GUIState_wrapper.getPrototype().get("generic_prototypes");
        obstacle_prototypes = (JSONArray) GUIState_wrapper.getPrototype().get("obstacle_prototypes");

        // Update wrappers in order to send only useful ones
        Sim_Controller.getSimulation().updateSimulationWrapper(state);

        // Clone data to work concurrently
        HashMap<Pair<Integer, String>, SimObject_wrapper> AGENTS_clone = (HashMap<Pair<Integer, String>, SimObject_wrapper>) GUIState_wrapper.getAGENTS().clone();
        HashMap<Pair<Integer, String>, SimObject_wrapper> GENERICS_clone = (HashMap<Pair<Integer, String>, SimObject_wrapper>) GUIState_wrapper.getGENERICS().clone();
        HashMap<Pair<Integer, String>, SimObject_wrapper> OBSTACLES_clone = (HashMap<Pair<Integer, String>, SimObject_wrapper>) GUIState_wrapper.getOBSTACLES().clone();

        // Create actual step and send it
        Sim_Controller.executor.execute(() -> {

            ByteBuffer rawBB;
            ByteBuffer quantitiesBB;
            ByteBuffer agentsBB;
            ByteBuffer genericsBB;
            ByteBuffer obstaclesBB;
            byte[] message;

            //System.out.print("STEP: |||" + step_id + "||");

            ByteArrayDataOutput[] agentsBBs = constructByteBuffer(AGENTS_clone, completeStep, agent_prototypes);
            ByteArrayDataOutput[] genericsBBs = constructByteBuffer(GENERICS_clone, completeStep, generic_prototypes);
            ByteArrayDataOutput[] obstaclesBBs = constructByteBuffer(OBSTACLES_clone, completeStep, obstacle_prototypes);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(agentsBBs[0].toByteArray());
                outputStream.write(genericsBBs[0].toByteArray());
                outputStream.write(obstaclesBBs[0].toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }

            quantitiesBB = ByteBuffer.wrap(outputStream.toByteArray());
            agentsBB = ByteBuffer.wrap(agentsBBs[1].toByteArray());
            genericsBB = ByteBuffer.wrap(genericsBBs[1].toByteArray());
            obstaclesBB = ByteBuffer.wrap(obstaclesBBs[1].toByteArray());

            rawBB = ByteBuffer.allocate(1 + step_id_bytes + quantitiesBB.capacity() + agentsBB.capacity() + genericsBB.capacity() + obstaclesBB.capacity());
            rawBB.order(ByteOrder.BIG_ENDIAN);
            rawBB.put(completeStep ? (byte) 1 : (byte) 0);                                // Complete flag
            rawBB.putLong(step_id);                                                       // Step ID
            rawBB.put(quantitiesBB);                                                      // Quantities
            rawBB.put(agentsBB);                                                          // Agents
            rawBB.put(genericsBB);                                                        // Generics
            rawBB.put(obstaclesBB);                                                       // Obstacles

            int length = rawBB.position();
            rawBB.position(0);
            message = new byte[length];
            rawBB.get(message, 0, length);

            if(completeStep) {
                Comms_Controller.publishStepOnTopic(message, 0, true);
            }
            else if(Sim_Controller.state != Sim_Controller.SimStateEnum.PLAY) Comms_Controller.publishStepOnTopic(message, 0, true);
            else Comms_Controller.publishStep(step_id, message);
            //System.out.println("STEP: " + step_id + "| Before: " + message.length + "| After: " + after);

            rawBB = null;
            quantitiesBB = null;
            agentsBB = null;
            genericsBB = null;
            obstaclesBB = null;

            //System.out.print("|\n");
        });

        //long time_after = currentTimeMillis();
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
    public ByteArrayDataOutput[] constructByteBuffer(HashMap<Pair<Integer, String>, SimObject_wrapper> simObjectCollection, boolean completeStep, JSONArray prototypes) {
        JSONArray params;

        ByteArrayDataOutput quantitiesBB = newDataOutput();
        ByteArrayDataOutput simObjectsBB = newDataOutput();

        for (Object p : prototypes) {
            int quantity = 0;
            Object[] simObjects = simObjectCollection.entrySet().stream().filter(entry -> entry.getKey().getValue().equals(((JSONObject)p).get("class"))).map(wrapperEntry -> wrapperEntry.getValue()).toArray();
            for (Object s_o : simObjects) {
                params = (JSONArray)((JSONObject)p).get("params");

                if(completeStep){
                    ++quantity;
                    simObjectsBB.writeInt(((SimObject_wrapper)s_o).getID());
                    WriteParams(s_o, params, simObjectsBB, true);
                }
                else {

                    if (((SimObject_wrapper)s_o).getParams().containsKey((String)((JSONObject)p).get("state_if_absent")) && (boolean)((SimObject_wrapper) s_o).getParams().get((String)((JSONObject)p).get("state_if_absent")) && !((SimObject_wrapper)s_o).Is_new()){ continue; }
                    if (((JSONObject)p).get("is_in_step").equals(false) && !((SimObject_wrapper)s_o).Is_new()) {continue;}

                    ++quantity;
                    simObjectsBB.writeInt(((SimObject_wrapper)s_o).getID());
                    if(((SimObject_wrapper)s_o).Is_new()){
                        simObjectsBB.writeBoolean(true);
                        WriteParams(s_o, params, simObjectsBB, true);
                        ((SimObject_wrapper)s_o).IncrementSTLN();
                        if(((SimObject_wrapper)s_o).STLN() >= 15){
                            ((SimObject_wrapper)s_o).setIs_new(false);
                            ((SimObject_wrapper)s_o).setSTLN(0);
                        }
                    }
                    else {
                        simObjectsBB.writeBoolean(false);
                        WriteParams(s_o, params, simObjectsBB, false);
                    }
                }
            }
            //System.out.print(quantity + "||");
            quantitiesBB.writeInt(quantity);
            //System.out.print("|");
        }
        return new ByteArrayDataOutput[]{quantitiesBB, simObjectsBB};
    }
    public void WriteParams(Object s_o, JSONArray params, ByteArrayDataOutput simObjectsBB, Boolean complete){
        for (Object param_prototype : params) {
            Object param_entry = ((SimObject_wrapper)s_o).getParams().get(((JSONObject)param_prototype).get("name"));
            if(!complete && ((JSONObject)param_prototype).get("is_in_step").equals(false)) continue;
            switch (((String)((JSONObject)param_prototype).get("type"))) {
                case "System.Single":
                    //System.out.print((float)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                    simObjectsBB.writeFloat((float)param_entry);
                    break;
                case "System.Int32":
                    //System.out.print((int)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                    simObjectsBB.writeInt((int)param_entry);
                    break;
                case "System.Boolean":
                    //System.out.print(((Boolean)((Map.Entry<String, Object>) param_entry).getValue()) ? (short) 1 : (short) 0 + "|");
                    simObjectsBB.writeBoolean(((Boolean)param_entry));
                    break;
                case "System.String":
                    //System.out.print(((String)((Map.Entry<String, Object>) param_entry).getValue()).getBytes(StandardCharsets.UTF_8) + "|");
                    simObjectsBB.write(((String)param_entry).getBytes(StandardCharsets.UTF_8));
                    break;
                case "System.Position":
                    if(GUIState_wrapper.getDIMENSIONS().size() == 2){
                        //System.out.print(((Float2D)pos).x + "|" + ((Float2D)pos).y + "|");
                        simObjectsBB.writeFloat(((Float2D)param_entry).x);
                        simObjectsBB.writeFloat(((Float2D)param_entry).y);
                    }
                    else {
                        //System.out.print(((Float3D)pos).x + "|" + ((Float3D)pos).y + "|" + ((Float3D)pos).z + "|");
                        simObjectsBB.writeFloat(((Float3D)param_entry).x);
                        simObjectsBB.writeFloat(((Float3D)param_entry).y);
                        simObjectsBB.writeFloat(((Float3D)param_entry).z);
                    }
                    break;
                case "System.Rotation":
                    simObjectsBB.writeFloat(((Quaternion)param_entry).getX());
                    simObjectsBB.writeFloat(((Quaternion)param_entry).getY());
                    simObjectsBB.writeFloat(((Quaternion)param_entry).getZ());
                    simObjectsBB.writeFloat(((Quaternion)param_entry).getW());
                    break;
                case "System.Cells":
                    if(GUIState_wrapper.getDIMENSIONS().size() == 2){
                        for (Int2D cell : (ArrayList<Int2D>) param_entry) {                                         // send bottom-left cell first
                            //System.out.print(((Int2D)pos).x + "|" + ((Int2D)pos).y + "|");
                            simObjectsBB.writeInt(cell.x);
                            simObjectsBB.writeInt(cell.y);
                        }
                    }
                    else {
                        for (Int3D cell : (ArrayList<Int3D>) param_entry) {
                            //System.out.print(((Int3D)pos).x + "|" + ((Int3D)pos).y + "|" + ((Int3D)pos).z + "|");
                            simObjectsBB.writeInt(cell.x);
                            simObjectsBB.writeInt(cell.y);
                            simObjectsBB.writeInt(cell.z);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
