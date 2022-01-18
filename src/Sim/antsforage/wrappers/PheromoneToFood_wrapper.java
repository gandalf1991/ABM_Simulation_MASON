package Sim.antsforage.wrappers;

import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.*;
import java.util.stream.Collectors;

public class PheromoneToFood_wrapper extends SimObject_wrapper {
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs = new TreeSet<>();;

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) { PheromoneToFood_wrapper.quantity = quantity; }

    public PheromoneToFood_wrapper() {
        type = GUIState_wrapper.SimObjectType.GENERIC;
        class_name = "PheromoneToFood";
    }

    static public void Reset(){
        ArrayList<SimObject_wrapper> wrappersToReset = new ArrayList<>();
        wrappersToReset.addAll(GUIState_wrapper.getGENERICS().values().stream().filter(simObject_wrapper -> simObject_wrapper.getClass_name().equals("PheromoneToFood")).collect(Collectors.toList()));
        wrappersToReset.forEach(SimObject_wrapper::delete);
        empty_IDs.clear();
        quantity = 0;
    }

    @Override
    public void map(Object toMap) {
        is_new = true;
        if (PheromoneToFood_wrapper.empty_IDs.size() > 0) {
            ID = PheromoneToFood_wrapper.empty_IDs.first();
            PheromoneToFood_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ++quantity;
        ArrayList<Int2D> mapping = (ArrayList<Int2D>) toMap;
        this.params.put("position", mapping);
        this.params.put("intensity", AntsForage.toFoodGrid.field[mapping.get(0).x][mapping.get(0).y]);
    }
    @Override
    public void create(JSONObject params) {
        is_new = true;
        if (PheromoneToFood_wrapper.empty_IDs.size() > 0) {
            ID = PheromoneToFood_wrapper.empty_IDs.first();
            PheromoneToFood_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ++quantity;
        float intensity = ((Number)params.get("intensity")).floatValue();
        ArrayList<Int2D> cells = new ArrayList<Int2D>();
        Int2D cell = new Int2D(((Long)(((JSONObject)((JSONArray)params.get("position")).get(0))).get("x")).intValue(), ((Long)(((JSONObject)((JSONArray)params.get("position")).get(0))).get("y")).intValue());
        cells.add(cell);
        AntsForage.toFoodGrid.field[cell.x][cell.y] = intensity;
        this.params.put("position", cells);
        this.params.put("intensity", intensity);
    }
    @Override
    public void update(JSONObject params) {

    }
    @Override
    public boolean updateWrapper() {
        ArrayList<Int2D> cells = (ArrayList<Int2D>) params.get("position");
        params.put("intensity", AntsForage.toFoodGrid.field[cells.get(0).x][cells.get(0).y]);
        return ((float) params.get("intensity")) <= 0.001f;
    }
    @Override
    public void reset() {
        is_new = false;
        steps_to_live_as_new = 0;
        ArrayList<Int2D> cells = (ArrayList<Int2D>) params.get("position");
        AntsForage.toFoodGrid.field[cells.get(0).x][cells.get(0).y] = 0f;
        GUIState_wrapper.getGENERICS().remove(new Pair<>(this.ID, this.getClass_name()));
        --quantity;
    }
    @Override
    public void delete() {
        empty_IDs.add(ID);
        reset();
    }
}