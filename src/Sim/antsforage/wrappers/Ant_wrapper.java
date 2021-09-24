package Sim.antsforage.wrappers;

import Sim.antsforage.Ant;
import Sim.antsforage.AntsForage;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sim.util.Int2D;

import java.util.Map;
import java.util.SortedSet;


public class Ant_wrapper extends SimObject_wrapper {
    static private final GUIState_wrapper.SimObjectType type = GUIState_wrapper.SimObjectType.AGENT;
    static private final String class_name = "Ant";
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs;
    private Ant ant;

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
    public static void setQuantity(int quantity) {
        Ant_wrapper.quantity = quantity;
    }
    public Ant getAnt() {
        return ant;
    }
    public void setAnt(Ant ant) {
        this.ant = ant;
    }

    public Ant_wrapper(){}

    @Override
    public void map(Object toMap, JSONArray params) {
        ant = (Ant)toMap;
        ID = ant.ID;
        updateInternal(params);
    }
    @Override
    public void init(JSONArray params) {}
    @Override
    public void create(JSONObject params){
        ant = new Ant((Ant_wrapper.empty_IDs.size() > 0) ? Ant_wrapper.empty_IDs.first() : quantity++, 1f);
        ID = ant.ID;
        params.forEach((p_name, p_value) -> {
            if(p_name.equals("position")) {
                ant.last = new Int2D(((Long)((JSONObject)p_value).get("x")).intValue(), ((Long)((JSONObject)p_value).get("y")).intValue());
                ant.x = ant.last.x;
                ant.y = ant.last.y;
                this.params.put("position", new Int2D(((Long)((JSONObject)p_value).get("x")).intValue(), ((Long)((JSONObject)p_value).get("y")).intValue()));
            }
            else if(p_name.equals("hasFoodItem")) {
                ant.hasFoodItem = (boolean)p_value;
                this.params.put("hasFoodItem", (boolean)p_value);
            }
            else if(p_name.equals("reward")) {
                ant.reward = (double)p_value;
                this.params.put("reward", (double)p_value);
            }
        });
    }
    @Override
    public void update(JSONObject params) {
        JSONObject position = ((JSONObject)params.get("position"));
        ant.x = ((Long)position.get("x")).intValue();
        ant.y = ((Long)position.get("y")).intValue();
        ant.last = new Int2D(ant.x, ant.y);
        ant.reward = (double)params.get("reward");
        ant.hasFoodItem = (boolean)params.get("hasFoodItem");
        updateInternal(params);
    }
    @Override
    public void delete() {
        AntsForage.agents_stoppables.remove(ID).stop();
        AntsForage.buggrid.remove(ant);
        empty_IDs.add(ID);
        --quantity;
        ant = null;
    }



    public void updateInternal(JSONArray params) {
        Object[] parameters = params.toArray();
        for (Object p : parameters) {
            if(((JSONObject)p).get("name").equals("position")) {
                this.params.put("position", new Int2D(ant.x, ant.y));
            }
            else if(((JSONObject)p).get("name").equals("hasFoodItem")) {
                this.params.put("hasFoodItem", (boolean)((JSONObject)p).get("default"));
            }
            else if(((JSONObject)p).get("name").equals("reward")) {
                this.params.put("reward", (double)((JSONObject)p).get("default"));
            }
        }
    }
    public void updateInternal(JSONObject params){
        for (Object p: params.entrySet()) {
            this.params.put(((Map.Entry<String, Object>)p).getKey(), ((Map.Entry<String, Object>)p).getValue());
        }
    }
}