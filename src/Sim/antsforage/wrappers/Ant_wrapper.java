/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.antsforage.wrappers;

import Sim.antsforage.Ant;
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


public class Ant_wrapper extends SimObject_wrapper {

    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs = new TreeSet<>();;
    private Ant ant;

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) {
        Ant_wrapper.quantity = quantity;
    }
    public Ant getAnt() {
        return ant;
    }

    public Ant_wrapper(){}
    public Ant_wrapper(Object toMap, JSONArray params){
        type = GUIState_wrapper.SimObjectType.AGENT;
        class_name = "Ant";
        map(toMap);
    }

    static public void Reset(){
        ArrayList<SimObject_wrapper> wrappersToReset = new ArrayList<>();
        wrappersToReset.addAll(GUIState_wrapper.getAGENTS().values());
        wrappersToReset.forEach(SimObject_wrapper::reset);
    }

    @Override
    public void map(Object toMap) {
        is_new = true;
        ant = (Ant)toMap;
        ID = ant.ID;
        ArrayList<Int2D> cells = new ArrayList<>();
        cells.add(ant.last);
        this.params.put("position", cells);
        this.params.put("rotation", new Quaternion(0,0,0,1));
        this.params.put("hasFoodItem", ant.hasFoodItem);
        this.params.put("reward", ant.reward);
    }
    @Override
    public void create(JSONObject params){
        is_new = true;
        if (Ant_wrapper.empty_IDs.size() > 0) {
            ID = Ant_wrapper.empty_IDs.first();
            Ant_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        ant = new Ant(ID, 1f);
        ++quantity;
        params.forEach((p_name, p_value) -> {
            if(p_name.equals("position")) {
                ant.last = new Int2D(((Long)((JSONObject)((JSONArray)p_value).get(0)).get("x")).intValue(), ((Long)((JSONObject)((JSONArray)p_value).get(0)).get("y")).intValue());
                ArrayList<Int2D> cells = new ArrayList<>();
                cells.add(ant.last);
                this.params.put("position", cells);
            }
            else if(p_name.equals("rotation")) {
                this.params.put("rotation", new Quaternion(((Number)((JSONObject)p_value).get("x")).floatValue(), ((Number)((JSONObject)p_value).get("y")).floatValue(), ((Number)((JSONObject)p_value).get("z")).floatValue(), ((Number)((JSONObject)p_value).get("w")).floatValue()));
            }
            else if(p_name.equals("hasFoodItem")) {
                ant.hasFoodItem = (boolean)p_value;
                this.params.put("hasFoodItem", ant.hasFoodItem);
            }
            else if(p_name.equals("reward")) {
                ant.reward = ((Number)p_value).floatValue();
                this.params.put("reward", ant.reward);
            }
        });
    }
    @Override
    public void update(JSONObject params) {
        JSONArray position = ((JSONArray)params.get("position"));
        ant.last = new Int2D(((Long)((JSONObject)position.get(0)).get("x")).intValue(), ((Long)((JSONObject)position.get(0)).get("y")).intValue());
        ant.reward = ((Number)params.get("reward")).floatValue();
        ant.hasFoodItem = (boolean)params.get("hasFoodItem");
    }
    @Override
    public boolean updateWrapper() {
        ArrayList<Int2D> cells = new ArrayList<>();
        cells.add(ant.last);
        params.put("position", cells);
        params.put("hasFoodItem", ant.hasFoodItem);
        params.put("reward", ant.reward);
        return false;
    }
    @Override
    public void reset(){
        is_new = false;
        steps_to_live_as_new = 0;
        updateWrapper();
    }
    @Override
    public void delete() {
        AntsForage.agents_stoppables.remove(ID).stop();
        AntsForage.buggrid.remove(ant);
        ant = null;
        GUIState_wrapper.getAGENTS().remove(new Pair<>(this.ID, this.getClass_name()));
        empty_IDs.add(ID);
        --quantity;
    }

}