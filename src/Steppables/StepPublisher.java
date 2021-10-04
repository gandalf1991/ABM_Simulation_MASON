/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Steppables;

import Main.Comms_Controller;
import Main.Sim_Controller;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import sim.util.Int3D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import static java.lang.System.currentTimeMillis;

public class StepPublisher implements Steppable {
    private static final long serialVersionUID = 1L;

    public StepPublisher() {}

    public void step(SimState state) {

        long time_before = currentTimeMillis();

        // Update/gather structures and variables to use in new Thread
        Sim_Controller.getSimulation().updateSimulationWrapper(state);
        long step_id = state.schedule.getSteps();
        HashMap<Pair<Integer, String>, SimObject_wrapper> AGENTS_clone = (HashMap<Pair<Integer, String>, SimObject_wrapper>) GUIState_wrapper.getAGENTS().clone();
        HashMap<Pair<Integer, String>, SimObject_wrapper> GENERICS_clone = (HashMap<Pair<Integer, String>, SimObject_wrapper>) GUIState_wrapper.getGENERICS().clone();
        Stream<SimObject_wrapper> dynamicGENERICS = GENERICS_clone.values().stream().filter(simObject_wrapper -> GUIState_wrapper.dynamicClasses.contains(simObject_wrapper.getClass_name()));


        Sim_Controller.executor.execute(() -> {

            ByteBuffer rawBB;
            ByteBuffer quantitiesBB;
            ByteBuffer agentsBB;
            ByteBuffer genericsBB;
            byte[] message;

            int step_id_size = 8;
            int quantities_size = 4 * (GUIState_wrapper.dynamicClasses.size());
            int agents_id_pos_size = (4 + (4 * GUIState_wrapper.getDIMENSIONS().size())) * AGENTS_clone.size();
            int generics_id_pos_size = (4 + (4 * GUIState_wrapper.getDIMENSIONS().size())) * ((Long)dynamicGENERICS.count()).intValue();
            int all_agents_params_size = 0;
            int d_generics_params_size = 0;

            JSONArray a_protos = (JSONArray) GUIState_wrapper.getPrototype().get("agent_prototypes");
            JSONArray d_g_protos = new JSONArray();
            for (Object g_p : (JSONArray) GUIState_wrapper.getPrototype().get("generic_prototypes")) {
                if (GUIState_wrapper.dynamicClasses.contains(((JSONObject)g_p).get("class"))){
                    d_g_protos.add(g_p);
                }
            }

            for (Object a_proto : a_protos) {
                int agent_params_size = 0;
                for (Object p : ((JSONArray)((JSONObject)a_proto).get("params"))){
                    if(!((JSONObject)p).get("name").equals("position") && ((JSONObject)p).get("is_in_step").equals(true)) {
                        switch ((String)((JSONObject)p).get("type")) {
                            case "System.Single":
                                agent_params_size += 4;
                                break;
                            case "System.Int32":
                                agent_params_size += 4;
                                break;
                            case "System.Boolean":
                                agent_params_size += 2;
                                break;
                            case "System.String":
                                agent_params_size += 40;
                                break;
                            default:
                                break;
                        }
                    }
                }
                agent_params_size *= AGENTS_clone.values().stream().filter(o -> o.getClass_name().equals(((JSONObject)a_proto).get("class"))).count();
                all_agents_params_size += agent_params_size;
            }
            for (Object d_g_proto : d_g_protos) {
                int generic_params_size = 0;
                for (Object p : ((JSONArray)((JSONObject)d_g_proto).get("params"))){
                    if(!((JSONObject)p).get("name").equals("position") && ((JSONObject)p).get("is_in_step").equals(true)) {
                        switch ((String)((JSONObject)p).get("type")) {
                            case "System.Single":
                                generic_params_size += 4;
                                break;
                            case "System.Int32":
                                generic_params_size += 4;
                                break;
                            case "System.Boolean":
                                generic_params_size += 2;
                                break;
                            case "System.String":
                                generic_params_size += 40;
                                break;
                            default:
                                break;
                        }
                    }
                }
                generic_params_size *= GENERICS_clone.values().stream().filter(o -> o.getClass_name().equals(((JSONObject)d_g_proto).get("class"))).count();
                d_generics_params_size += generic_params_size;
            }

            rawBB = ByteBuffer.allocate(step_id_size + quantities_size + agents_id_pos_size + generics_id_pos_size + all_agents_params_size + d_generics_params_size);
            quantitiesBB = ByteBuffer.allocate(quantities_size);
            agentsBB = ByteBuffer.allocate(agents_id_pos_size + all_agents_params_size);
            genericsBB = ByteBuffer.allocate(generics_id_pos_size + d_generics_params_size);
            boolean is_discrete = GUIState_wrapper.getPrototype().get("type").equals("DISCRETE");

            rawBB.order(ByteOrder.LITTLE_ENDIAN);
            quantitiesBB.order(ByteOrder.LITTLE_ENDIAN);
            agentsBB.order(ByteOrder.LITTLE_ENDIAN);
            genericsBB.order(ByteOrder.LITTLE_ENDIAN);

            rawBB.putLong(step_id);                                 // Step ID

            //System.out.print("STEP: |||" + state.schedule.getSteps() + "||| ");

            // AGENTS
            JSONArray prototypes = (JSONArray) GUIState_wrapper.getPrototype().get("agent_prototypes");
            JSONObject correct_prototype;
            JSONArray params;
            JSONObject param;
            for (int i = 0; i < GUIState_wrapper.agentClasses.size(); i++) {
                int finalI = i;
                Object[] agents = AGENTS_clone.entrySet().stream().filter(entry -> entry.getKey().getValue().equals(GUIState_wrapper.agentClasses.get(finalI))).map(wrapperEntry -> wrapperEntry.getValue()).toArray();
                //System.out.print(agents.length + " || ");
                quantitiesBB.putInt(agents.length);                                                                                                                                                     // Agents quantity per class
                for (Object agent : agents){
                    correct_prototype = ((JSONObject) prototypes.stream().filter(p -> ((JSONObject) p).get("class").equals(((SimObject_wrapper) agent).getClass_name())).toArray()[0]);
                    params = (JSONArray) correct_prototype.get("params");
                    //System.out.print("|" + ((SimObject_wrapper)agent).getID() + "|");
                    agentsBB.putInt(((SimObject_wrapper)agent).getID());                                                                                                                                // ID
                    if (GUIState_wrapper.getDIMENSIONS().size() == 2) {
                        Int2D pos = (Int2D)((SimObject_wrapper)agent).getParams().get("position");
                        //System.out.print(pos.x + "," + pos.y + "|");
                        if (is_discrete) {agentsBB.putInt(pos.x); agentsBB.putInt(pos.y);} else { agentsBB.putFloat(pos.x); agentsBB.putFloat(pos.y);}
                    }                                                                                                                             // Position
                    else {
                        Int3D pos = (Int3D)((SimObject_wrapper)agent).getParams().get("position");
                        //System.out.print(pos.x + "," + pos.y + "," + pos.z + "|");
                        if (is_discrete) {agentsBB.putInt(pos.x); agentsBB.putInt(pos.y); agentsBB.putInt(pos.z);} else { agentsBB.putFloat(pos.x); agentsBB.putFloat(pos.y); agentsBB.putFloat(pos.z);}
                    }                                                                                                                                                                          //
                    for (Object param_entry : ((SimObject_wrapper)agent).getParams().entrySet()) {
                        param = (JSONObject) params.stream().filter(p -> ((JSONObject) p).get("name").equals(((Map.Entry<String, Object>) param_entry).getKey())).toArray()[0];
                        switch (((String)param.get("type"))) {
                            case "System.Single":
                                //System.out.print((float)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                                agentsBB.putFloat((float)((Map.Entry<String, Object>) param_entry).getValue());
                                break;
                            case "System.Int32":
                                //System.out.print((int)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                                agentsBB.putInt((int)((Map.Entry<String, Object>) param_entry).getValue());
                                break;
                            case "System.Boolean":
                                //System.out.print(((Boolean)((Map.Entry<String, Object>) param_entry).getValue()) ? (short) 1 : (short) 0 + "|");
                                agentsBB.putShort(((Boolean)((Map.Entry<String, Object>) param_entry).getValue()) ? (short) 1 : (short) 0);
                                break;
                            case "System.String":
                                //System.out.print(((String)((Map.Entry<String, Object>) param_entry).getValue()).getBytes(StandardCharsets.UTF_8) + "|");
                                agentsBB.put(((String)((Map.Entry<String, Object>) param_entry).getValue()).getBytes(StandardCharsets.UTF_8));
                                break;
                            default:
                                break;
                        }
                    }                                                                                                  // Params
                    //System.out.print(" ");
                }

            }
            // GENERICS
            prototypes = (JSONArray) GUIState_wrapper.getPrototype().get("generic_prototypes");
            for (int i = 0; i < GUIState_wrapper.genericClasses.size(); i++) {
                int finalI = i;
                Object[] generics;
                if (Arrays.stream(GUIState_wrapper.dynamicClasses.toArray()).anyMatch(s ->  s.equals(GUIState_wrapper.genericClasses.get(finalI)))) {
                    generics = GENERICS_clone.entrySet().stream().filter(entry -> entry.getKey().getValue().equals(GUIState_wrapper.genericClasses.get(finalI))).map(wrapperEntry -> wrapperEntry.getValue()).toArray();

                    //System.out.print(generics.length + " || ");

                    quantitiesBB.putInt(generics.length);                                                                                                                                                   // Generics quantity per class
                    for (Object generic : generics){
                        correct_prototype = ((JSONObject) prototypes.stream().filter(p -> ((JSONObject) p).get("class").equals(((SimObject_wrapper) generic).getClass_name())).toArray()[0]);
                        params = (JSONArray) correct_prototype.get("params");

                        //System.out.print("|" + ((SimObject_wrapper)generic).getID() + "|");

                        genericsBB.putInt(((SimObject_wrapper)generic).getID());                                                                                                                            // ID

                        if (GUIState_wrapper.getDIMENSIONS().size() == 2) {
                            Int2D pos = (Int2D)((SimObject_wrapper)generic).getParams().get("position");

                            //System.out.print(pos.x + "," + pos.y + "|");

                            if (is_discrete) {genericsBB.putInt(pos.x); genericsBB.putInt(pos.y);} else { genericsBB.putFloat(pos.x); genericsBB.putFloat(pos.y);}

                        }                                                                                                                             // Position
                        else {
                            Int3D pos = (Int3D)((SimObject_wrapper)generic).getParams().get("position");

                            //System.out.print(pos.x + "," + pos.y + "," + pos.z + "|");

                            if (is_discrete) {genericsBB.putInt(pos.x); genericsBB.putInt(pos.y); genericsBB.putInt(pos.z);} else { genericsBB.putFloat(pos.x); genericsBB.putFloat(pos.y); genericsBB.putFloat(pos.z);}

                        }                                                                                                                                                                          //
                        for (Object param_entry : ((SimObject_wrapper)generic).getParams().entrySet()) {
                            param = (JSONObject) params.stream().filter(p -> ((JSONObject) p).get("name").equals(((Map.Entry<String, Object>) param_entry).getKey())).toArray()[0];
                            switch (((String)param.get("type"))) {
                                case "System.Single":
                                    //System.out.print((float)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                                    genericsBB.putFloat((float)((Map.Entry<String, Object>) param_entry).getValue());
                                    break;
                                case "System.Int32":
                                    //System.out.print((int)((Map.Entry<String, Object>) param_entry).getValue() + "|");
                                    genericsBB.putInt((int)((Map.Entry<String, Object>) param_entry).getValue());
                                    break;
                                case "System.Boolean":
                                    //System.out.print(((Boolean)((Map.Entry<String, Object>) param_entry).getValue()) ? (short) 1 : (short) 0 + "|");
                                    genericsBB.putShort(((Boolean)((Map.Entry<String, Object>) param_entry).getValue()) ? (short) 1 : (short) 0);
                                    break;
                                case "System.String":
                                    //System.out.print(((String)((Map.Entry<String, Object>) param_entry).getValue()).getBytes(StandardCharsets.UTF_8) + "|");
                                    genericsBB.put(((String)((Map.Entry<String, Object>) param_entry).getValue()).getBytes(StandardCharsets.UTF_8));
                                    break;
                                default:
                                    break;
                            }
                        }                                                                                                // Params
                        //System.out.print(" ");
                    }
                }
            }

            //System.out.println("");

            quantitiesBB.limit(quantitiesBB.position());
            quantitiesBB.position(0);
            agentsBB.limit(agentsBB.position());
            agentsBB.position(0);
            genericsBB.limit(genericsBB.position());
            genericsBB.position(0);

            rawBB.put(quantitiesBB);                            // Quantities
            rawBB.put(agentsBB);                                // Agents
            rawBB.put(genericsBB);                              // Generics

            int length = rawBB.position();
            rawBB.position(0);
            message = new byte[length];
            rawBB.get(message, 0, length);

            //Sim_Controller.setStep(message);

            rawBB = null;
            quantitiesBB = null;
            agentsBB = null;
            genericsBB = null;

            Comms_Controller.publishStep(step_id, message);


        });


        long time_after = currentTimeMillis();
        //System.out.println(getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + "| " + (time_after-time_before) + " millis.");

    }
}
