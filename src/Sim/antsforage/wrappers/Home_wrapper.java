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

public class Home_wrapper extends SimObject_wrapper {
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs = new TreeSet<>();

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
        Int2D mapping = (Int2D)toMap;
        if (Home_wrapper.empty_IDs.size() > 0) {
            ID = Home_wrapper.empty_IDs.first();
            Home_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        this.params.put("position", new Int2D(mapping.x, mapping.y));
    }
    @Override
    public void create(JSONObject params) {
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
            AntsForage.sites.field[c.x][c.y] = 0;
        }
        GUIState_wrapper.getGENERICS().remove(new Pair<>(this.ID, this.getClass_name()));
        empty_IDs.add(ID);
        --quantity;
    }
}