package Sim.antsforage.wrappers;


import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class Obstacle_wrapper extends SimObject_wrapper {
    private final String class_name;
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs = new TreeSet<>();;

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) { Obstacle_wrapper.quantity = quantity; }

    public Obstacle_wrapper(String class_name){
        type = GUIState_wrapper.SimObjectType.OBSTACLE;
        this.class_name = class_name;
    }

    @Override
    public void map(Object toMap) {}
    @Override
    public void create(JSONObject params){
        if (Obstacle_wrapper.empty_IDs.size() > 0) {
            ID = Obstacle_wrapper.empty_IDs.first();
            Obstacle_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ++quantity;
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
    public void updateWrapper() {}
    @Override
    public void reset() {}
    @Override
    public void delete() {
        for (Int2D c: (ArrayList<Int2D>)params.get("position")) {
            AntsForage.obstacles.field[c.x][c.y] = 0;
        }
        GUIState_wrapper.getOBSTACLES().remove(new Pair<>(this.ID, GUIState_wrapper.genericClasses.get(3)));
        empty_IDs.add(ID);
        --quantity;
    }
}