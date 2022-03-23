package Sim.antsforage.wrappers;


import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import com.jogamp.opengl.math.Quaternion;
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
    static public SortedSet<Integer> empty_IDs = new TreeSet<>();;

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
        is_new = true;
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
            AntsForage.toHomeGrid.field[((Long)((JSONObject)c).get("x")).intValue()][((Long)((JSONObject)c).get("y")).intValue()] = 0f;
            AntsForage.toFoodGrid.field[((Long)((JSONObject)c).get("x")).intValue()][((Long)((JSONObject)c).get("y")).intValue()] = 0f;
            cells.add(new Int2D(((Long)((JSONObject)c).get("x")).intValue(), ((Long)((JSONObject)c).get("y")).intValue()));
            AntsForage.OBST_POS.add(new Int2D(((Long)((JSONObject)c).get("x")).intValue(), ((Long)((JSONObject)c).get("y")).intValue()));
        }
        this.params.put("position", cells);
        this.params.put("rotation", new Quaternion(((Number)((JSONObject)params.get("rotation")).get("x")).floatValue(), ((Number)((JSONObject)params.get("rotation")).get("y")).floatValue(), ((Number)((JSONObject)params.get("rotation")).get("z")).floatValue(), ((Number)((JSONObject)params.get("rotation")).get("w")).floatValue()));
    }
    @Override
    public void update(JSONObject params) {

    }
    @Override
    public boolean updateWrapper() {return false;}
    @Override
    public void reset() {}
    @Override
    public void delete() {
        for (Int2D c: (ArrayList<Int2D>)params.get("position")) {
            AntsForage.obstacles.field[c.x][c.y] = 0;
            AntsForage.OBST_POS.remove(c);
        }
        GUIState_wrapper.getOBSTACLES().remove(new Pair<>(this.ID, class_name));
        empty_IDs.add(ID);
        --quantity;
    }
}