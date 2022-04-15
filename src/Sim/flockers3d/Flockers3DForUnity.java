/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.flockers3d;

import Sim.flockers3d.wrappers.Flocker3D_wrapper;
import Utils.CustomController;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.display.Controller;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.util.Double3D;

import java.util.*;

import static java.lang.System.currentTimeMillis;

public class Flockers3DForUnity extends GUIState_wrapper {

    public static void main(String[] args) {}
    public Flockers3DForUnity() {
        super(new Flockers3D(currentTimeMillis()));
        c = new CustomController(this);
    }
    public Flockers3DForUnity(SimState state) { super(state); }
    public static String getName() { return "Flockers3D"; }
    public void start() {
        super.start();  // set up everything
    }
    public void load(SimState state) {
        super.load(state);
    }
    public void init(Controller c) {
        super.init(c);
    }
    public void quit() {
        super.quit();
    }

    @Override
    public boolean initSimulationState() {

        agentClasses = new ArrayList<String>(Collections.singletonList("Flocker"));
        genericClasses = new ArrayList<String>();
        dynamicClasses = new ArrayList<String>(Collections.singletonList("Flocker"));

        DIMENSIONS = new HashMap<>();
        SIM_PARAMS = new HashMap<>();
        AGENTS = new HashMap<>();
        GENERICS = new HashMap<>();
        OBSTACLES = new HashMap<>();

        // Init wrapped/raw sim params
        JSONArray params = (JSONArray)prototype.get("sim_params");
        Flockers3D.width = ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(0)).get("default")).intValue();             // X in Unity -> width
        Flockers3D.height = ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(1)).get("default")).intValue();            // Y in Unity -> height
        Flockers3D.lenght =  ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(2)).get("default")).intValue();           // Z in Unity -> lenght
        Flockers3D.flockers = new Continuous3D(Flockers3D.neighborhood/1.5d, Flockers3D.width, Flockers3D.height, Flockers3D.lenght);
        for (Object d : ((JSONArray)prototype.get("dimensions")).toArray()) {
            DIMENSIONS.put((String) ((JSONObject)d).get("name"), ((Number)((JSONObject)d).get("default")).intValue());
        }
        Flockers3D.numFlockers = ((Number)((JSONObject)((JSONArray)prototype.get("agent_prototypes")).get(0)).get("default")).intValue();
        params.forEach(o -> {
            // wrapped
            SIM_PARAMS.put((String)((JSONObject)o).get("name"), ((JSONObject)o).get("default"));
            // raw
            if(((JSONObject)o).get("name").equals("cohesion")) Flockers3D.cohesion = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("avoidance")) Flockers3D.avoidance = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("randomness")) Flockers3D.randomness = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("consistency")) Flockers3D.consistency = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("momentum")) Flockers3D.momentum = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("deadFlockerProbability")) Flockers3D.deadFlockerProbability = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("neighborhood")) Flockers3D.neighborhood = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("jump")) Flockers3D.jump = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("AVOID_DISTANCE")) Flockers3D.AVOID_DISTANCE = ((Number)((JSONObject)o).get("default")).floatValue();
        });

        // Init raw Simulation
        this.start();

        // Init raw Flockers dead param
        //params = ((JSONArray)((JSONObject)((JSONArray)prototype.get("agent_prototypes")).get(0)).get("params"));
        //Object[] p1 = params.stream().filter(o -> ((String) ((JSONObject)o).get("name")).equals("dead")).toArray();
        //for (Object a : Flockers.flockers.getAllObjects()) {
        //    ((Flocker) a).dead = (boolean) ((JSONObject)p1[0]).get("default");
        //}

        // Init Flocker wrappers
        Flocker3D_wrapper.setQuantity(Flockers3D.numFlockers);
        for (int i = 0; i< Flockers3D.numFlockers; i++){
            Flocker3D_wrapper aw = new Flocker3D_wrapper(Flockers3D.flockers.getAllObjects().objs[i], params);
            AGENTS.put(new Pair<>(i, agentClasses.get(0)), aw);
        }
        return true;
    }
    @Override
    public boolean updateSimulationState(JSONObject update) {
        // SIM PARAMS
        if(update.containsKey("sim_params")) {
            for (Object s_p : ((JSONObject) update.get("sim_params")).entrySet()) {
                String key = ((Map.Entry<String, Object>) s_p).getKey();
                Object value = ((Map.Entry<String, Object>) s_p).getValue();
                SIM_PARAMS.put(key, value);
                if (key.equals("cohesion")) Flockers3D.cohesion = ((Number) value).floatValue();
                else if (key.equals("avoidance")) Flockers3D.avoidance = ((Number) value).floatValue();
                else if (key.equals("randomness")) Flockers3D.randomness = ((Number) value).floatValue();
                else if (key.equals("consistency")) Flockers3D.consistency = ((Number) value).floatValue();
                else if (key.equals("momentum")) Flockers3D.momentum = ((Number) value).floatValue();
                else if (key.equals("deadFlockerProbability")) Flockers3D.deadFlockerProbability = ((Number) value).floatValue();
                else if (key.equals("neighborhood")) Flockers3D.neighborhood = ((Number) value).floatValue();
                else if (key.equals("jump")) Flockers3D.jump = ((Number) value).floatValue();
                else if (key.equals("AVOID_DISTANCE")) Flockers3D.AVOID_DISTANCE = ((Number) value).floatValue();
            }
        }

        SimObject_wrapper so_wrapper;
        // AGENTS
        // update
        if(update.containsKey("agents_update")) {
            for (Object a_u : (JSONArray) update.get("agents_update")) {
                so_wrapper = AGENTS.get(new Pair<>(((Number) ((JSONObject) a_u).get("id")).intValue(), (String) ((JSONObject) a_u).get("class")));
                so_wrapper.update((JSONObject) ((JSONObject) a_u).get("params"));
            }
        }
        // create
        if(update.containsKey("agents_create")) {
            for (Object a_c: (JSONArray)update.get("agents_create")) {
                for (int i = 0; i<((Number)((JSONObject)a_c).get("quantity")).intValue(); i++){
                    Flocker3D_wrapper a_wrapper = new Flocker3D_wrapper();
                    a_wrapper.create((JSONObject)((JSONObject)a_c).get("params"));
                    Flockers3D.flockers.setObjectLocation(a_wrapper.getFlocker(), new Double3D(a_wrapper.getFlocker().loc.x, a_wrapper.getFlocker().loc.y, a_wrapper.getFlocker().loc.z));
                    a_wrapper.getFlocker().flockers = Flockers3D.flockers;
                    //a_wrapper.getFlocker().theFlock = (Flockers3D)this.state;
                    Flockers3D.agents_stoppables.put(a_wrapper.getID(), this.state.schedule.scheduleRepeating(a_wrapper.getFlocker()));
                    AGENTS.put(new Pair<>(a_wrapper.getID(), agentClasses.get(0)), a_wrapper);
                    Flockers3D.numFlockers++;
                }
            }
        }
        // delete
        if(update.containsKey("agents_delete")) {
            for (Object a_d : (JSONArray) update.get("agents_delete")) {
                if(AGENTS.containsKey(new Pair<>(((Number) ((JSONObject) a_d).get("id")).intValue(), agentClasses.get(0)))) {
                    AGENTS.get(new Pair<>(((Number) ((JSONObject) a_d).get("id")).intValue(), agentClasses.get(0))).delete();
                }
            }
        }
        return true;
    }
    @Override
    public boolean updateSimulationWrapper(SimState state) {

        long time_before = currentTimeMillis();

        // AGENTS
        for (SimObject_wrapper a_w : AGENTS.values()) {
            a_w.updateWrapper();
        }

        long time_after = currentTimeMillis();
        //System.out.println(getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + "| " + (time_after-time_before) + " millis.");
        return true;
    }
    @Override
    public boolean resetSimulation(){

        ArrayList<SimObject_wrapper> wrappersToReset = new ArrayList<>();

        // reschedule ants
        ((Flockers3D)state).scheduleAgain();

        // AGENTS
        wrappersToReset.addAll(AGENTS.values());

        for (SimObject_wrapper w : wrappersToReset) w.reset();

        return true;
    }
    @Override
    public boolean stopSimulation(){
        ArrayList<SimObject_wrapper> wrappersToDelete = new ArrayList<>();

        wrappersToDelete.addAll(AGENTS.values());

        for (SimObject_wrapper w : wrappersToDelete) w.delete();

        Flocker3D_wrapper.empty_IDs.clear();

        return true;
    }
}




