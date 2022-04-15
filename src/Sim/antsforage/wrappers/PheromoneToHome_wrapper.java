/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.antsforage.wrappers;

import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class PheromoneToHome_wrapper extends SimObject_wrapper {
    static private int quantity = 0;
    static public SortedSet<Integer> empty_IDs = new TreeSet<>();;

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) { PheromoneToHome_wrapper.quantity = quantity; }

    public PheromoneToHome_wrapper() {
        type = GUIState_wrapper.SimObjectType.GENERIC;
        class_name = "PheromoneToHome";
    }

    static public void Reset(){
        ArrayList<SimObject_wrapper> wrappersToReset = new ArrayList<>();
        wrappersToReset.addAll(GUIState_wrapper.getGENERICS().values().stream().filter(simObject_wrapper -> simObject_wrapper.getClass_name().equals("PheromoneToHome")).collect(Collectors.toList()));
        wrappersToReset.forEach(SimObject_wrapper::delete);
        empty_IDs.clear();
        quantity = 0;
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
        ArrayList<Int2D> mapping = (ArrayList<Int2D>) toMap;
        this.params.put("position", mapping);
        this.params.put("intensity", AntsForage.toHomeGrid.field[mapping.get(0).x][mapping.get(0).y]);
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
        ArrayList<Int2D> cells = new ArrayList<Int2D>();
        Int2D cell = new Int2D(((Long)(((JSONObject)((JSONArray)params.get("position")).get(0))).get("x")).intValue(), ((Long)(((JSONObject)((JSONArray)params.get("position")).get(0))).get("y")).intValue());
        cells.add(cell);
        AntsForage.toHomeGrid.field[cell.x][cell.y] = intensity;
        this.params.put("position", cells);
        this.params.put("intensity", intensity);
    }
    @Override
    public void update(JSONObject params) {

    }
    @Override
    public boolean updateWrapper() {
        ArrayList<Int2D> cells = (ArrayList<Int2D>) params.get("position");
        params.put("intensity", AntsForage.toHomeGrid.field[cells.get(0).x][cells.get(0).y]);
        return ((float) params.get("intensity")) <= 0.001f;
    }
    @Override
    public void reset() {
        is_new = true;
        steps_to_live_as_new = 0;
        ArrayList<Int2D> cells = (ArrayList<Int2D>) params.get("position");
        AntsForage.toHomeGrid.field[cells.get(0).x][cells.get(0).y] = 0f;
        GUIState_wrapper.getGENERICS().remove(new Pair<>(this.ID, this.getClass_name()));
        --quantity;
    }
    @Override
    public void delete() {
        empty_IDs.add(ID);
        reset();
    }
}