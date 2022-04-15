/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.antsforage.wrappers;

import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import com.jogamp.opengl.math.Quaternion;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class Home_wrapper extends SimObject_wrapper {
    static private int quantity = 0;
    static public SortedSet<Integer> empty_IDs = new TreeSet<>();

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) {
        Home_wrapper.quantity = quantity;
    }

    public Home_wrapper() {
        type = GUIState_wrapper.SimObjectType.GENERIC;
        class_name = "Home";
    }

    @Override
    public void map(Object toMap) {
        is_new = true;
        ArrayList<Int2D> mapping = (ArrayList<Int2D>)toMap;
        if (Home_wrapper.empty_IDs.size() > 0) {
            ID = Home_wrapper.empty_IDs.first();
            Home_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ++quantity;
        this.params.put("position", mapping);
        this.params.put("rotation", new Quaternion(0,0,0,1));
    }
    @Override
    public void create(JSONObject params) {
        is_new = true;
        if (Home_wrapper.empty_IDs.size() > 0) {
            ID = Home_wrapper.empty_IDs.first();
            Home_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ++quantity;
        ArrayList<Int2D> cells = new ArrayList<>();
        for (Object c : (JSONArray)params.get("position")) {
            AntsForage.sites.field[((Long)((JSONObject)c).get("x")).intValue()][((Long)((JSONObject)c).get("y")).intValue()] = AntsForage.HOME;
            cells.add(new Int2D(((Long)((JSONObject)c).get("x")).intValue(), ((Long)((JSONObject)c).get("y")).intValue()));
            AntsForage.HOME_POS.add(new Int2D(((Long)((JSONObject)c).get("x")).intValue(), ((Long)((JSONObject)c).get("y")).intValue()));
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
            AntsForage.sites.field[c.x][c.y] = 0;
            AntsForage.HOME_POS.remove(c);
        }
        GUIState_wrapper.getGENERICS().remove(new Pair<>(this.ID, this.getClass_name()));
        empty_IDs.add(ID);
        --quantity;
    }
}