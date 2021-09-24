package Sim.antsforage.wrappers;

import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.Dictionary;

public class PheromoneToFood_wrapper extends SimObject_wrapper {
    static private final GUIState_wrapper.SimObjectType type = GUIState_wrapper.SimObjectType.AGENT;
    static private final String class_name = "PheromoneToFood";
    static private int quantity = 0;
    static private int[] empty_IDs;
    static private Dictionary<Integer, Int2D> IDs;

    @Override
    public GUIState_wrapper.SimObjectType getType() {
        return type;
    }
    public String getClass_name() {
        return class_name;
    }
    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) { PheromoneToFood_wrapper.quantity = quantity; }

    @Override
    public void map(Object toMap, JSONArray params) {}
    @Override
    public void init(JSONArray params) {}
    @Override
    public void create(JSONObject params) {

//        for (Object c : (JSONArray)((JSONObject)((JSONObject)g_c).get("params")).get("position")) {
//            AntsForage.toFoodGrid.field[(int)((JSONObject)c).get("x")][(int)((JSONObject)c).get("y")] = (double)((JSONObject)((JSONObject)g_u).get("params")).get("intensity");  /// Intensità del feromone
//        }

    }
    @Override
    public void update(JSONObject params) {

    }
    @Override
    public void delete() {

    }
}