/*
  Copyright 2009 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package Sim.antsforage;

import Sim.antsforage.wrappers.*;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.engine.*;
import sim.display.*;
import sim.util.Int2D;

import java.util.Hashtable;
import java.util.Map;


public class AntsForageForUnity extends GUIState_wrapper {

    public static void main(String[] args) {}
    public AntsForageForUnity() {
        super(new AntsForage(System.currentTimeMillis()));
        c = new SimpleController(this, false);
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

    public enum agentClasses {
        Ant
    }
    public enum genericClasses {
        Home,
        Food,
        PheromoneToHome,
        PheromoneToFood
    }

    @Override
    public boolean initSimulationState() {
        SIM_PARAMS = new Hashtable<>();
        AGENTS = new Hashtable<>();
        GENERICS = new Hashtable<>();
        OBSTACLES = new Hashtable<>();

        // Init wrapped/raw sim params
        JSONArray params = (JSONArray)prototype.get("sim_params");
        AntsForage.GRID_HEIGHT = ((Long)((JSONObject)((JSONArray)prototype.get("dimensions")).get(0)).get("default")).intValue();
        AntsForage.GRID_WIDTH = ((Long)((JSONObject)((JSONArray)prototype.get("dimensions")).get(1)).get("default")).intValue();
        AntsForage.numAnts = ((Long)((JSONObject)((JSONArray)prototype.get("agent_prototypes")).get(0)).get("default")).intValue();
        params.forEach(o -> {
            // wrapped
            SIM_PARAMS.put((String)((JSONObject)o).get("name"), ((JSONObject)o).get("default"));
            // raw
            if(((JSONObject)o).get("name").equals("evaporationConstant")) AntsForage.evaporationConstant = (double)((JSONObject)o).get("default");
            else if(((JSONObject)o).get("name").equals("reward")) AntsForage.reward = (double)((JSONObject)o).get("default");
            else if(((JSONObject)o).get("name").equals("updateCutDown")) AntsForage.updateCutDown = (double)((JSONObject)o).get("default");
            else if(((JSONObject)o).get("name").equals("momentumProbability")) AntsForage.momentumProbability = (double)((JSONObject)o).get("default");
            else if(((JSONObject)o).get("name").equals("randomActionProbability")) AntsForage.randomActionProbability = (double)((JSONObject)o).get("default");
        });

        // Init SimObjects wrappers
        JSONArray g_protos = (JSONArray)prototype.get("generic_prototypes");
        for (int i = 0; i<g_protos.size(); i++) {
            JSONObject g = (JSONObject) g_protos.get(i);
            params = (JSONArray) g.get("params");
            switch ((String) g.get("class")) {
                case "Home":
                    // set quantity
                    Home_wrapper.setQuantity(((Long) g.get("default")).intValue());
                    // set params
                    for (int j = 0; j < Home_wrapper.getQuantity(); j++) {
                        Home_wrapper hw = new Home_wrapper();
                        AntsForage.HOME_POS.add(new Int2D(guirandom.nextInt(j+11), guirandom.nextInt((j+11)^2)));
                        hw.map(new Pair<>(j, AntsForage.HOME_POS.get(j)), params);
                        GENERICS.put(new Pair<>(j, genericClasses.Home.ordinal()), hw);
                    }
                    break;
                case "Food":
                    // set quantity
                    Food_wrapper.setQuantity(((Long) g.get("default")).intValue());
                    // set params
                    for (int j = 0; j < Food_wrapper.getQuantity(); j++) {
                        Food_wrapper fw = new Food_wrapper();
                        AntsForage.FOOD_POS.add(new Int2D(guirandom.nextInt(j+11), guirandom.nextInt((j+11)^2)));
                        fw.map(new Pair<>(j, AntsForage.FOOD_POS.get(j)), params);
                        GENERICS.put(new Pair<>(j, genericClasses.Food.ordinal()), fw);
                    }
                    break;
            }
        }

        // Init raw Simulation
        state.start();

        // Init raw Ants hasFoodItem param
        params = ((JSONArray)((JSONObject)((JSONArray)prototype.get("agent_prototypes")).get(0)).get("params"));
        Object[] p1 = params.stream().filter(o -> ((String) ((JSONObject)o).get("name")).equals("hasFoodItem")).toArray();
        for (Object a : AntsForage.buggrid.getAllObjects()) {
            ((Ant) a).hasFoodItem = (boolean) ((JSONObject)p1[0]).get("default");
        }

        // Init Ant wrappers
        Ant_wrapper.setQuantity(AntsForage.numAnts);
        for (int i=0; i<AntsForage.numAnts; i++){
            Ant_wrapper aw = new Ant_wrapper();
            aw.map(AntsForage.buggrid.getAllObjects().toArray()[i], params);
            AGENTS.put(new Pair<>(i, agentClasses.Ant.ordinal()), aw);
        }
        return true;
    }
    @Override
    public boolean updateSimulationState(JSONObject update) {
        // SIM PARAMS
        for (Object s_p: ((JSONObject)update.get("sim_params")).entrySet()) {
            String key = ((Map.Entry<String, Object>)s_p).getKey();
            Object value = ((Map.Entry<String, Object>)s_p).getValue();
            SIM_PARAMS.put(key, value);
            if(key.equals("evaporationConstant")) AntsForage.evaporationConstant = (int)value;
            else if(key.equals("reward")) AntsForage.reward = (double)value;
            else if(key.equals("updateCutDown")) AntsForage.updateCutDown = (double)value;
            else if(key.equals("momentumProbability")) AntsForage.momentumProbability = (double)value;
            else if(key.equals("randomActionProbability")) AntsForage.randomActionProbability = (double)value;
        }

        SimObject_wrapper so_wrapper;
        // AGENTS
        // update
        for (Object a_u: (JSONArray)update.get("agents_update")) {
            so_wrapper = AGENTS.get(new Pair<>(((Long)((JSONObject)a_u).get("id")).intValue(), (String)((JSONObject)a_u).get("class")));
            so_wrapper.update((JSONObject)((JSONObject)a_u).get("params"));
        }
        // create
        for (Object a_c: (JSONArray)update.get("agents_create")) {
            for (int i = 0; i<((Long)((JSONObject)a_c).get("quantity")).intValue(); i++){
                Ant_wrapper a_wrapper = new Ant_wrapper();
                a_wrapper.create((JSONObject)((JSONObject)a_c).get("params"));
                AntsForage.buggrid.setObjectLocation(a_wrapper.getAnt(), a_wrapper.getAnt().x, a_wrapper.getAnt().y);
                AntsForage.agents_stoppables.put(a_wrapper.getID(), this.state.schedule.scheduleRepeating(a_wrapper.getAnt()));
                AGENTS.put(new Pair<>(a_wrapper.getID(), agentClasses.Ant.ordinal()), a_wrapper);
            }
        }
        // delete
        for (Object a_d: (JSONArray)update.get("agents_delete")) {
            AGENTS.remove(new Pair<>(((Long)((JSONObject)a_d).get("id")).intValue(), agentClasses.Ant.ordinal())).delete();
        }

        // GENERICS
        // update
        for (Object g_u: (JSONArray)update.get("generics_update")){
            so_wrapper = GENERICS.get(new Pair<>(((Long)((JSONObject)g_u).get("id")).intValue(), (String)((JSONObject)g_u).get("class")));
            so_wrapper.update((JSONObject)((JSONObject)g_u).get("params"));
        }
        // create
        for (Object g_c: (JSONArray)update.get("generics_create")) {
            for (int i = 0; i<((Long)((JSONObject)g_c).get("quantity")).intValue(); i++) {
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
                GENERICS.put(new Pair<>(so_wrapper.getID(), genericClasses.Food.ordinal()), so_wrapper);
            }
        }
        // delete
        for (Object g_d: (JSONArray)update.get("generics_delete")) {
            GENERICS.remove(new Pair<>(((Long)((JSONObject)g_d).get("id")).intValue(), genericClasses.Food.ordinal())).delete();
        }

        // OBSTACLES
        // create
        for (Object o_c: (JSONArray)update.get("obstacles_create")) {
            so_wrapper = new Obstacle_wrapper((String)((JSONObject)o_c).get("class"));
            so_wrapper.create((JSONObject)((JSONObject)o_c).get("params"));
            OBSTACLES.put(new Pair<>(so_wrapper.getID(), (String)((JSONObject)o_c).get("class")), so_wrapper);
        }
        // delete
        for (Object o_d: (JSONArray)update.get("obstacles_delete")) {
            OBSTACLES.remove(new Pair<>(((Long)((JSONObject)o_d).get("id")).intValue(), (String)((JSONObject)o_d).get("class"))).delete();
        }
        return true;
    }
}
    
    
    
    
