/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Wrappers;

import Events.EventArgs.CheckStatusEventArgs;
import Events.EventArgs.SimUpdateEventArgs;
import Events.Handlers.StateEventHandler;
import Utils.CustomController;
import javafx.util.Pair;
import org.javatuples.Triplet;
import org.json.simple.JSONObject;
import sim.display.GUIState;
import sim.engine.SimState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GUIState_wrapper extends GUIState {

    protected static JSONObject prototype;                                     // to init simulation

    protected static HashMap<String, Object> DIMENSIONS;
    protected static HashMap<String, Object> SIM_PARAMS;
    protected static HashMap<Pair<Integer, String>, SimObject_wrapper> AGENTS;
    protected static HashMap<Pair<Integer, String>, SimObject_wrapper> GENERICS;
    protected static HashMap<Pair<Integer, String>, SimObject_wrapper> OBSTACLES;

    public static ArrayList<String> agentClasses;
    public static ArrayList<String> genericClasses;
    public static ArrayList<String> obstacleClasses;
    public static ArrayList<String> dynamicClasses;                                         // classes to consider while producing steps

    // EVENTS
    public StateEventHandler<SimUpdateEventArgs> simUpdatedEventHandler = new StateEventHandler<>();

    public static enum SimType {
        CONTINUOUS,
        DISCRETE
    }
    public static enum SimObjectType {
        AGENT,
        GENERIC,
        OBSTACLE
    }

    public static CustomController c;
    public GUIState_wrapper(SimState state) {
        super(state);
    }
    public static void main(String[] args) { }

    public static void initPrototype(JSONObject initialized_prototype){
        GUIState_wrapper.prototype = initialized_prototype;
    }
    public static JSONObject getPrototype() {
        return prototype;
    };
    public static void updateSimulationStateJSON(){

        // TODO

    }

    public static HashMap<String, Object> getDIMENSIONS() {
        return DIMENSIONS;
    }
    public static void setDIMENSIONS(HashMap<String, Object> DIMENSIONS) {
        GUIState_wrapper.DIMENSIONS = DIMENSIONS;
    }
    public static HashMap<String, Object> getSIM_PARAMS() {
        return SIM_PARAMS;
    }
    public static void setSIM_PARAMS(HashMap<String, Object> SIM_PARAMS) {
        GUIState_wrapper.SIM_PARAMS = SIM_PARAMS;
    }
    public static HashMap<Pair<Integer, String>, SimObject_wrapper> getAGENTS() {
        return AGENTS;
    }
    public static void setAGENTS(HashMap<Pair<Integer, String>, SimObject_wrapper> AGENTS) {
        GUIState_wrapper.AGENTS = AGENTS;
    }
    public static HashMap<Pair<Integer, String>, SimObject_wrapper> getGENERICS() {
        return GENERICS;
    }
    public static void setGENERICS(HashMap<Pair<Integer, String>, SimObject_wrapper> GENERICS) {
        GUIState_wrapper.GENERICS = GENERICS;
    }
    public static HashMap<Pair<Integer, String>, SimObject_wrapper> getOBSTACLES() {
        return OBSTACLES;
    }
    public static void setOBSTACLES(HashMap<Pair<Integer, String>, SimObject_wrapper> OBSTACLES) {
        GUIState_wrapper.OBSTACLES = OBSTACLES;
    }

    public abstract boolean initSimulationState();
    public abstract boolean updateSimulationState(JSONObject update);
    public abstract boolean updateSimulationWrapper(SimState state);
    public abstract boolean resetSimulation();
    public abstract boolean stopSimulation();

    @Override
    public String toString(){
        return "Simulation " + prototype.get("id") + " " + prototype.get("name") + "\nAGENTS: " + getAGENTS().keySet().stream().map(Pair::toString).collect(Collectors.joining(" ")) + "\nGENERICS: " + String.join(" ", getGENERICS().keySet().stream().map(Pair::toString).collect(Collectors.joining("\n"))) + ".";
    }
}
