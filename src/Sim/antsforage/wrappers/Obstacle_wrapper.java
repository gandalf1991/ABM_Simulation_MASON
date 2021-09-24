package Sim.antsforage.wrappers;


import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.SortedSet;

public class Obstacle_wrapper extends SimObject_wrapper {
    static private final GUIState_wrapper.SimObjectType type = GUIState_wrapper.SimObjectType.OBSTACLE;
    private String class_name;
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs;

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
    public static void setQuantity(int quantity) { Obstacle_wrapper.quantity = quantity;
    }

    public Obstacle_wrapper(String class_name){
        this.class_name = class_name;
    }

    @Override
    public void map(Object toMap, JSONArray params) {}
    @Override
    public void init(JSONArray params) {}
    @Override
    public void create(JSONObject params){
        ID = (Obstacle_wrapper.empty_IDs.size() > 0) ? Obstacle_wrapper.empty_IDs.first() : quantity++;
        ArrayList<Int2D> cells = new ArrayList<>();
        for (Object c : (JSONArray)params.get("position")) {
            AntsForage.obstacles.field[((Long)((JSONObject)c).get("x")).intValue()][((Long)((JSONObject)c).get("y")).intValue()] = 1;
            cells.add(new Int2D(((Long)((JSONObject)c).get("x")).intValue(), ((Long)((JSONObject)c).get("y")).intValue()));
        }
        this.params.put("position", cells);
    }
    @Override
    public void update(JSONObject params) {




    }
    @Override
    public void delete() {
        for (Int2D c: (ArrayList<Int2D>)params.get("position")) {
            AntsForage.obstacles.field[c.x][c.y] = 0;
        }
        empty_IDs.add(ID);
        --quantity;
    }
}