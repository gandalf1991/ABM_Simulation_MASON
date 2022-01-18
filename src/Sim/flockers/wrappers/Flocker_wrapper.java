package Sim.flockers.wrappers;


import Utils.Float3D;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Sim.flockers.*;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import java.util.SortedSet;
import java.util.TreeSet;

public class Flocker_wrapper extends SimObject_wrapper {

    MersenneTwisterFast random = new MersenneTwisterFast();
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs = new TreeSet<>();;
    private Flocker flocker;

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) {
        Sim.flockers.wrappers.Flocker_wrapper.quantity = quantity;
    }
    public Flocker getFlocker() {
        return flocker;
    }

    public Flocker_wrapper(){}
    public Flocker_wrapper(Object toMap, JSONArray params){
        type = GUIState_wrapper.SimObjectType.AGENT;
        class_name = "Flocker";
        map(toMap);
        is_new = true;
    }

    @Override
    public void map(Object toMap) {
        flocker = (Flocker) toMap;
        ID = flocker.ID;
        this.params.put("position", new Float3D((float)flocker.loc.x, (float)flocker.loc.y, (float)flocker.loc.z));
        this.params.put("dead", flocker.dead);
    }
    @Override
    public void create(JSONObject params){
        is_new = true;
        if (Sim.flockers.wrappers.Flocker_wrapper.empty_IDs.size() > 0) {
            ID = Sim.flockers.wrappers.Flocker_wrapper.empty_IDs.first();
            Sim.flockers.wrappers.Flocker_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        flocker = new Flocker(ID, new Double3D());
        ++quantity;
        params.forEach((p_name, p_value) -> {
            if(p_name.equals("position")) {
                flocker.loc = new Double3D(((Number)((JSONObject) p_value).get("x")).doubleValue(), ((Number)((JSONObject) p_value).get("y")).doubleValue(), ((Number)((JSONObject) p_value).get("z")).doubleValue());
                this.params.put("position", new Float3D((float)flocker.loc.x, (float)flocker.loc.y, (float)flocker.loc.z));
            }
            else if(p_name.equals("dead")) {
                flocker.dead = (boolean) p_value;
                this.params.put("dead", flocker.dead);
            }
        });
    }
    @Override
    public void update(JSONObject params) {
        JSONObject position = ((JSONObject)params.get("position"));
        flocker.loc = new Double3D(((Number)position.get("x")).doubleValue(), ((Number)position.get("y")).doubleValue(), ((Number)position.get("z")).doubleValue());
        flocker.dead = (boolean)params.get("dead");
    }
    @Override
    public boolean updateWrapper() {
        this.params.put("position", new Float3D((float)flocker.loc.x, (float)flocker.loc.y, (float)flocker.loc.z));
        params.put("dead", flocker.dead);
        return false;
    }
    @Override
    public void reset(){
        is_new = true;
        updateWrapper();
    }
    @Override
    public void delete() {
        Flockers.agents_stoppables.remove(ID).stop();
        Flockers.flockers.remove(flocker);
        flocker = null;
        GUIState_wrapper.getAGENTS().remove(new Pair<>(this.ID, this.getClass_name()));
        empty_IDs.add(ID);
        --quantity;
    }

}