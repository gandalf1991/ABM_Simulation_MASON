package Sim.antsforage.wrappers;

import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.Dictionary;
import java.util.SortedSet;
import java.util.TreeSet;

public class PheromoneToHome_wrapper extends SimObject_wrapper {
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs = new TreeSet<>();;

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) { PheromoneToHome_wrapper.quantity = quantity; }

    public PheromoneToHome_wrapper() {
        type = GUIState_wrapper.SimObjectType.GENERIC;
        class_name = "PheromoneToHome";
    }

    @Override
    public void map(Object toMap) {
        is_new = true;
        if (PheromoneToHome_wrapper.empty_IDs.size() > 0) {
            ID = PheromoneToHome_wrapper.empty_IDs.first();
            PheromoneToHome_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ++quantity;
        Int2D mapping = (Int2D)toMap;
        this.params.put("position", new Int2D(mapping.x, mapping.y));
        this.params.put("intensity", AntsForage.toHomeGrid.field[mapping.x][mapping.y]);
    }
    @Override
    public void create(JSONObject params) {
        is_new = true;
        if (PheromoneToHome_wrapper.empty_IDs.size() > 0) {
            ID = PheromoneToHome_wrapper.empty_IDs.first();
            PheromoneToHome_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ++quantity;
        float intensity = ((Number)params.get("intensity")).floatValue();
        Int2D cell = new Int2D(((Long)((JSONObject)params.get("position")).get("x")).intValue(), ((Long)((JSONObject)params.get("position")).get("y")).intValue());
        AntsForage.toHomeGrid.field[cell.x][cell.y] = intensity;
        this.params.put("position", cell);
        this.params.put("intensity", intensity);
    }
    @Override
    public void update(JSONObject params) {

    }
    @Override
    public boolean updateWrapper() {
        Int2D cell = (Int2D)params.get("position");
        params.put("intensity", AntsForage.toHomeGrid.field[cell.x][cell.y]);
        if ((float)params.get("intensity") <= 0.001f){
            return true;
        }
        return false;
    }
    @Override
    public void reset() {
        is_new = false;
        Int2D cell = (Int2D)params.get("position");
        AntsForage.toHomeGrid.field[cell.x][cell.y] = 0;
        GUIState_wrapper.getGENERICS().remove(new Pair<>(this.ID, this.getClass_name()));
        --quantity;
    }
    @Override
    public void delete() {
        empty_IDs.add(ID);
        reset();
    }
}