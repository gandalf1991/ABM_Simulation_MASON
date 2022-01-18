/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.flockers;

import Sim.flockers.wrappers.Flocker_wrapper;
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

public class FlockersForUnity extends GUIState_wrapper {

    public static void main(String[] args) {}
    public FlockersForUnity() {
        super(new Flockers(currentTimeMillis()));
        c = new CustomController(this);
    }
    public FlockersForUnity(SimState state) { super(state); }
    public static String getName() { return "Flockers"; }
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
        Flockers.width = ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(0)).get("default")).intValue();             // X in Unity -> width
        Flockers.height = ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(1)).get("default")).intValue();            // Y in Unity -> height
        Flockers.lenght =  ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(2)).get("default")).intValue();           // Z in Unity -> lenght
        Flockers.flockers = new Continuous3D(Flockers.neighborhood/1.5d,Flockers.width,Flockers.height,Flockers.lenght);
        for (Object d : ((JSONArray)prototype.get("dimensions")).toArray()) {
            DIMENSIONS.put((String) ((JSONObject)d).get("name"), ((Number)((JSONObject)d).get("default")).intValue());
        }
        Flockers.numFlockers = ((Number)((JSONObject)((JSONArray)prototype.get("agent_prototypes")).get(0)).get("default")).intValue();
        params.forEach(o -> {
            // wrapped
            SIM_PARAMS.put((String)((JSONObject)o).get("name"), ((JSONObject)o).get("default"));
            // raw
            if(((JSONObject)o).get("name").equals("cohesion")) Flockers.cohesion = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("avoidance")) Flockers.avoidance = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("randomness")) Flockers.randomness = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("consistency")) Flockers.consistency = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("momentum")) Flockers.momentum = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("deadFlockerProbability")) Flockers.deadFlockerProbability = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("neighborhood")) Flockers.neighborhood = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("jump")) Flockers.jump = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("AVOID_DISTANCE")) Flockers.AVOID_DISTANCE = ((Number)((JSONObject)o).get("default")).floatValue();
        });

        // Init raw Simulation
        this.start();

        // Init Ant wrappers
        Flocker_wrapper.setQuantity(Flockers.numFlockers);
        for (int i=0; i<Flockers.numFlockers; i++){
            Flocker_wrapper aw = new Flocker_wrapper(Flockers.flockers.getAllObjects().objs[i], params);
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
                if (key.equals("cohesion")) Flockers.cohesion = ((Number) value).floatValue();
                else if (key.equals("avoidance")) Flockers.avoidance = ((Number) value).floatValue();
                else if (key.equals("randomness")) Flockers.randomness = ((Number) value).floatValue();
                else if (key.equals("consistency")) Flockers.consistency = ((Number) value).floatValue();
                else if (key.equals("momentum")) Flockers.momentum = ((Number) value).floatValue();
                else if (key.equals("deadFlockerProbability")) Flockers.deadFlockerProbability = ((Number) value).floatValue();
                else if (key.equals("neighborhood")) Flockers.neighborhood = ((Number) value).floatValue();
                else if (key.equals("jump")) Flockers.jump = ((Number) value).floatValue();
                else if (key.equals("AVOID_DISTANCE")) Flockers.AVOID_DISTANCE = ((Number) value).floatValue();
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
                    Flocker_wrapper a_wrapper = new Flocker_wrapper();
                    a_wrapper.create((JSONObject)((JSONObject)a_c).get("params"));
                    Flockers.flockers.setObjectLocation(a_wrapper.getFlocker(), new Double3D(a_wrapper.getFlocker().loc.x, a_wrapper.getFlocker().loc.y, a_wrapper.getFlocker().loc.z));
                    a_wrapper.getFlocker().flockers = Flockers.flockers;
                    a_wrapper.getFlocker().theFlock = (Flockers)this.state;
                    Flockers.agents_stoppables.put(a_wrapper.getID(), this.state.schedule.scheduleRepeating(a_wrapper.getFlocker()));
                    AGENTS.put(new Pair<>(a_wrapper.getID(), agentClasses.get(0)), a_wrapper);
                    Flockers.numFlockers++;
                }
            }
        }
        // delete
        if(update.containsKey("agents_delete")) {
            for (Object a_d : (JSONArray) update.get("agents_delete")) {
                AGENTS.get(new Pair<>(((Number) ((JSONObject) a_d).get("id")).intValue(), agentClasses.get(0))).delete();
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
        ((Flockers)state).scheduleAgain();

        // AGENTS
        wrappersToReset.addAll(AGENTS.values());

        for (SimObject_wrapper w : wrappersToReset) w.reset();

        return true;
    }
}




