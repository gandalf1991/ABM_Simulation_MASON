/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Wrappers;

import javafx.util.Pair;
import org.json.simple.JSONObject;
import sim.display.GUIState;
import sim.display.SimpleController;
import sim.engine.SimState;

import java.util.Dictionary;

public abstract class GUIState_wrapper extends GUIState {

    protected JSONObject prototype;                                     // to init simulation
    protected JSONObject simulationStateJSON;                           // to let others join

    protected Dictionary<String, Object> SIM_PARAMS;
    protected Dictionary<Pair<Integer, Integer>, SimObject_wrapper> AGENTS;
    protected Dictionary<Pair<Integer, Integer>, SimObject_wrapper> GENERICS;
    protected Dictionary<Pair<Integer, String>, SimObject_wrapper> OBSTACLES;

    public static enum SimType {
        CONTINUOUS,
        DISCRETE
    }
    public static enum SimObjectType {
        AGENT,
        GENERIC,
        OBSTACLE
    }

    public static SimpleController c;
    public GUIState_wrapper(SimState state) {
        super(state);
    }
    public static void main(String[] args) { }


    public void initPrototype(JSONObject initialized_prototype){
        this.prototype = initialized_prototype;
    }
    public JSONObject getPrototype() {
        return prototype;
    };
    public JSONObject getSimulationStateJSON() {
        return simulationStateJSON;
    };
    public void updateSimulationStateJSON(){

        // TODO

    }


    public abstract boolean initSimulationState();
    public abstract boolean updateSimulationState(JSONObject update);
}
