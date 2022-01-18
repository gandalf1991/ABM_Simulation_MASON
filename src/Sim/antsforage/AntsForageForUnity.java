/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.antsforage;

import Events.EventArgs.SimUpdateEventArgs;
import Sim.antsforage.wrappers.*;
import Utils.CustomController;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.javatuples.Triplet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.engine.*;
import sim.display.*;
import sim.util.Int2D;
import java.util.*;
import static java.lang.System.currentTimeMillis;

public class AntsForageForUnity extends GUIState_wrapper {

    public static void main(String[] args) {}
    public AntsForageForUnity() {
        super(new AntsForage(currentTimeMillis()));
        c = new CustomController(this);
    }
    public AntsForageForUnity(SimState state) { super(state); }
    public static String getName() { return "Ants Forage"; }
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

        agentClasses = new ArrayList<String>();
        genericClasses = new ArrayList<String>();
        obstacleClasses = new ArrayList<String>();
        dynamicClasses = new ArrayList<String>();

        for(Object a_p : ((JSONArray) prototype.get("agent_prototypes")).toArray()){
            agentClasses.add((String) ((JSONObject)a_p).get("class"));
            if((boolean)((JSONObject)a_p).get("is_in_step")) dynamicClasses.add((String)((JSONObject)a_p).get("class"));
        }
        for(Object g_p : ((JSONArray) prototype.get("generic_prototypes")).toArray()){
            genericClasses.add((String) ((JSONObject)g_p).get("class"));
            if((boolean)((JSONObject)g_p).get("is_in_step")) dynamicClasses.add((String)((JSONObject)g_p).get("class"));
        }
        for(Object o_p : ((JSONArray) prototype.get("obstacle_prototypes")).toArray()){
            obstacleClasses.add((String) ((JSONObject)o_p).get("class"));
            if((boolean)((JSONObject)o_p).get("is_in_step")) dynamicClasses.add((String)((JSONObject)o_p).get("class"));
        }

        /*agentClasses = new ArrayList<String>(Arrays.asList("Ant"));
        genericClasses = new ArrayList<String>(Arrays.asList("Home", "Food", "PheromoneToHome", "PheromoneToFood"));
        obstacleClasses = new ArrayList<String>(Arrays.asList("Home", "Food", "PheromoneToHome", "PheromoneToFood"));
        dynamicClasses = new ArrayList<String>(Arrays.asList("Ant", "PheromoneToHome", "PheromoneToFood"));*/

        DIMENSIONS = new HashMap<>();
        SIM_PARAMS = new HashMap<>();
        AGENTS = new HashMap<>();
        GENERICS = new HashMap<>();
        OBSTACLES = new HashMap<>();

        // Init wrapped/raw sim params
        JSONArray params = (JSONArray)prototype.get("sim_params");
        AntsForage.GRID_WIDTH = ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(0)).get("default")).intValue();
        AntsForage.GRID_HEIGHT = ((Number)((JSONObject)((JSONArray)prototype.get("dimensions")).get(1)).get("default")).intValue();
        for (Object d : ((JSONArray)prototype.get("dimensions")).toArray()) {
            DIMENSIONS.put((String) ((JSONObject)d).get("name"), ((Number)((JSONObject)d).get("default")).intValue());
        }
        AntsForage.numAnts = ((Number)((JSONObject)((JSONArray)prototype.get("agent_prototypes")).get(0)).get("default")).intValue();
        params.forEach(o -> {
            // wrapped
            SIM_PARAMS.put((String)((JSONObject)o).get("name"), ((JSONObject)o).get("default"));
            // raw
            if(((JSONObject)o).get("name").equals("evaporationConstant")) AntsForage.evaporationConstant = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("reward")) AntsForage.reward = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("updateCutDown")) AntsForage.updateCutDown = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("momentumProbability")) AntsForage.momentumProbability = ((Number)((JSONObject)o).get("default")).floatValue();
            else if(((JSONObject)o).get("name").equals("randomActionProbability")) AntsForage.randomActionProbability = ((Number)((JSONObject)o).get("default")).floatValue();
        });

        // Init SimObjects wrappers
        JSONArray g_protos = (JSONArray)prototype.get("generic_prototypes");
        for (int i = 0; i<g_protos.size(); i++) {
            JSONObject g = (JSONObject) g_protos.get(i);
            params = (JSONArray) g.get("params");
            switch ((String) g.get("class")) {
                case "Home":
                    // set quantity
                    //Home_wrapper.setQuantity(((Number) g.get("default")).intValue());
                    // set params
                    for (int j = 0; j < ((Number) g.get("default")).intValue(); j++) {
                        Home_wrapper hw = new Home_wrapper();
                        ArrayList<Int2D> cells = new ArrayList<>();
                        for (Object c: (JSONArray)(((JSONObject)params.get(0)).get("default"))) {
                            Int2D cell = new Int2D(((Number)((JSONObject)c).get("x")).intValue(), ((Number)((JSONObject)c).get("y")).intValue());
                            cells.add(cell);
                            AntsForage.HOME_POS.add(cell);
                        }
                        hw.map(cells);
                        GENERICS.put(new Pair<>(hw.getID(), hw.getClass_name()), hw);
                    }
                    break;
                case "Food":
                    // set quantity
                    //Food_wrapper.setQuantity(((Number) g.get("default")).intValue());
                    // set params
                    for (int j = 0; j < ((Number) g.get("default")).intValue(); j++) {
                        Food_wrapper fw = new Food_wrapper();
                        ArrayList<Int2D> cells = new ArrayList<>();
                        for (Object c: (JSONArray)(((JSONObject)params.get(0)).get("default"))) {
                            Int2D cell = new Int2D(((Number)((JSONObject)c).get("x")).intValue(), ((Number)((JSONObject)c).get("y")).intValue());
                            cells.add(cell);
                            AntsForage.FOOD_POS.add(cell);
                        }
                        fw.map(cells);
                        GENERICS.put(new Pair<>(fw.getID(), fw.getClass_name()), fw);
                    }
                    break;
            }
        }

        // Init raw Simulation
        this.start();

        // Init raw Ants hasFoodItem param
        params = ((JSONArray)((JSONObject)((JSONArray)prototype.get("agent_prototypes")).get(0)).get("params"));
        Object[] p1 = params.stream().filter(o -> ((String) ((JSONObject)o).get("name")).equals("hasFoodItem")).toArray();
        for (Object a : AntsForage.buggrid.getAllObjects()) {
            ((Ant) a).hasFoodItem = (boolean) ((JSONObject)p1[0]).get("default");
        }

        // Init Ant wrappers
        Ant_wrapper.setQuantity(AntsForage.numAnts);
        for (int i=0; i<AntsForage.numAnts; i++){
            Ant_wrapper aw = new Ant_wrapper(AntsForage.buggrid.getAllObjects().objs[i], params);
            AGENTS.put(new Pair<>(i, agentClasses.get(0)), aw);
        }
        return true;
    }
    @Override
    public boolean updateSimulationState(JSONObject update) {
        // SIM PARAMS
        if(update.containsKey("sim_params")) {
            for (Object s_p: ((JSONObject)update.get("sim_params")).entrySet()) {
                String key = ((Map.Entry<String, Object>) s_p).getKey();
                Object value = ((Map.Entry<String, Object>) s_p).getValue();
                SIM_PARAMS.put(key, value);
                if (key.equals("evaporationConstant")) AntsForage.evaporationConstant = ((Number) value).floatValue();
                else if (key.equals("reward")) AntsForage.reward = ((Number) value).floatValue();
                else if (key.equals("updateCutDown")) AntsForage.updateCutDown = ((Number) value).floatValue();
                else if (key.equals("momentumProbability")) AntsForage.momentumProbability = ((Number) value).floatValue();
                else if (key.equals("randomActionProbability")) AntsForage.randomActionProbability = ((Number) value).floatValue();
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
            for (Object a_c : (JSONArray) update.get("agents_create")) {
                for (int i = 0; i < ((Number) ((JSONObject) a_c).get("quantity")).intValue(); i++) {
                    Ant_wrapper a_wrapper = new Ant_wrapper();
                    a_wrapper.create((JSONObject) ((JSONObject) a_c).get("params"));
                    AntsForage.buggrid.setObjectLocation(a_wrapper.getAnt(), a_wrapper.getAnt().last.x, a_wrapper.getAnt().last.y);
                    AntsForage.agents_stoppables.put(a_wrapper.getID(), this.state.schedule.scheduleRepeating(a_wrapper.getAnt()));
                    AGENTS.put(new Pair<>(a_wrapper.getID(), (String) ((JSONObject) a_c).get("class")), a_wrapper);
                    AntsForage.numAnts++;
                }
            }
        }
        // delete
        if(update.containsKey("agents_delete")) {
            for (Object a_d : (JSONArray) update.get("agents_delete")) {
                AGENTS.get(new Pair<>(((Number) ((JSONObject) a_d).get("id")).intValue(), (String) ((JSONObject) a_d).get("class"))).delete();
            }
        }

        // GENERICS
        // update
        if(update.containsKey("generics_update")) {
            for (Object g_u : (JSONArray) update.get("generics_update")) {
                so_wrapper = GENERICS.get(new Pair<>(((Number) ((JSONObject) g_u).get("id")).intValue(), (String) ((JSONObject) g_u).get("class")));
                so_wrapper.update((JSONObject) ((JSONObject) g_u).get("params"));
            }
        }
        // create
        if(update.containsKey("generics_create")) {
            for (Object g_c : (JSONArray) update.get("generics_create")) {
                for (int i = 0; i < ((Number) ((JSONObject) g_c).get("quantity")).intValue(); i++) {
                    switch ((String) ((JSONObject) g_c).get("class")) {
                        case "Food":
                            so_wrapper = new Food_wrapper();
                            break;
                        case "Home":
                            so_wrapper = new Home_wrapper();
                            break;
                        case "PheromoneToFood":
                            so_wrapper = new PheromoneToFood_wrapper();
                            break;
                        case "PheromoneToHome":
                            so_wrapper = new PheromoneToHome_wrapper();
                            break;
                        default:
                            return false;
                    }
                    so_wrapper.create((JSONObject) ((JSONObject) g_c).get("params"));
                    GENERICS.put(new Pair<>(so_wrapper.getID(), (String) ((JSONObject) g_c).get("class")), so_wrapper);
                }
            }
        }
        // delete
        if(update.containsKey("generics_delete")) {
            for (Object g_d : (JSONArray) update.get("generics_delete")) {
                GENERICS.get(new Pair<>(((Number) ((JSONObject) g_d).get("id")).intValue(), (String) ((JSONObject) g_d).get("class"))).delete();
            }
        }

        // OBSTACLES
        // create
        if(update.containsKey("obstacles_create")) {
            for (Object o_c : (JSONArray) update.get("obstacles_create")) {
                so_wrapper = new Obstacle_wrapper((String) ((JSONObject) o_c).get("class"));
                so_wrapper.create((JSONObject) ((JSONObject) o_c).get("params"));
                OBSTACLES.put(new Pair<>(so_wrapper.getID(), (String) ((JSONObject) o_c).get("class")), so_wrapper);
            }
        }
        // delete
        if(update.containsKey("obstacles_delete")) {
            for (Object o_d : (JSONArray) update.get("obstacles_delete")) {
                OBSTACLES.get(new Pair<>(((Number) ((JSONObject) o_d).get("id")).intValue(), (String) ((JSONObject) o_d).get("class"))).delete();
            }
        }

        //simUpdatedEventHandler.invoke(this, new SimUpdateEventArgs(update));

        return true;
    }
    @Override
    public boolean updateSimulationWrapper(SimState state) {

        long time_before = currentTimeMillis();
        ArrayList<SimObject_wrapper> toDelete = new ArrayList<>();

        // AGENTS
        for (SimObject_wrapper a_w : AGENTS.values()) {
            if (a_w.updateWrapper()) {
                toDelete.add(a_w);
            }
        }
        // GENERICS
        for (SimObject_wrapper g_w : GENERICS.values()) {
            if (g_w.updateWrapper()) {
                toDelete.add(g_w);
            }
        }
        // OBSTACLES
        for (SimObject_wrapper o_w : OBSTACLES.values()) {
            if (o_w.updateWrapper()) {
                toDelete.add(o_w);
            }
        }

        toDelete.forEach(SimObject_wrapper::delete);

        long time_after = currentTimeMillis();
        //System.out.println(getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + "| " + (time_after-time_before) + " millis.");
        return true;
    }
    @Override
    public boolean resetSimulation(){

        // Reschedule ants
        ((AntsForage)state).scheduleAgain();

        // AGENTS
        Ant_wrapper.Reset();
        // GENERICS
        PheromoneToFood_wrapper.Reset();
        PheromoneToHome_wrapper.Reset();

        return true;
    }
}
    
    
    
    
