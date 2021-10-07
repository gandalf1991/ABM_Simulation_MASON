/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.antsforage.wrappers;

import Sim.antsforage.Ant;
import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.Map;
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

    @Override
    public void map(Object toMap) {
        ant = (Ant)toMap;
        ID = ant.ID;
        this.params.put("position", ant.last);
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
                ant.last = new Int2D(((Long)((JSONObject)p_value).get("x")).intValue(), ((Long)((JSONObject)p_value).get("y")).intValue());
                this.params.put("position", ant.last);
            }
            else if(p_name.equals("hasFoodItem")) {
                ant.hasFoodItem = (boolean)p_value;
                this.params.put("hasFoodItem", ant.hasFoodItem);
            }
            else if(p_name.equals("reward")) {
                ant.reward = (float)p_value;
                this.params.put("reward", ant.reward);
            }
        });
    }
    @Override
    public void update(JSONObject params) {
        JSONObject position = ((JSONObject)params.get("position"));
        ant.last = new Int2D(((Long)position.get("x")).intValue(), ((Long)position.get("y")).intValue());
        ant.reward = ((Number)params.get("reward")).floatValue();
        ant.hasFoodItem = (boolean)params.get("hasFoodItem");
    }
    @Override
    public boolean updateWrapper() {
        params.put("position", ant.last);
        params.put("hasFoodItem", ant.hasFoodItem);
        params.put("reward", ant.reward);
        return false;
    }
    @Override
    public void reset(){
        is_new = false;
        Int2D old_pos = new Int2D(AntsForage.HOME_POS.get((ID%AntsForage.HOME_POS.size())).x, AntsForage.HOME_POS.get((ID%AntsForage.HOME_POS.size())).y);
        float old_reward = ((Number)((JSONObject)((JSONArray)((JSONObject)((JSONArray)GUIState_wrapper.getPrototype().get("agent_prototypes")).get(0)).get("params")).get(1)).get("default")).floatValue();
        boolean old_hasFoodItem = (boolean)((JSONObject)((JSONArray)((JSONObject)((JSONArray)GUIState_wrapper.getPrototype().get("agent_prototypes")).get(0)).get("params")).get(2)).get("default");
        ant.last = old_pos;
        ant.reward = old_reward;
        ant.hasFoodItem = old_hasFoodItem;
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