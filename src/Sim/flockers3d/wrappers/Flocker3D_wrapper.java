package Sim.flockers3d.wrappers;


import Utils.Float3D;
import Wrappers.GUIState_wrapper;
import Wrappers.SimObject_wrapper;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Sim.flockers3d.*;
import sim.util.Double3D;
import java.util.SortedSet;
import java.util.TreeSet;

public class Flocker3D_wrapper extends SimObject_wrapper {

    static private int quantity = 0;
    static public SortedSet<Integer> empty_IDs = new TreeSet<>();;
    private Flocker3D flocker;

    public static int getQuantity() {
        return quantity;
    }
    public static void setQuantity(int quantity) {
        Flocker3D_wrapper.quantity = quantity;
    }
    public Flocker3D getFlocker() {
        return flocker;
    }

    public Flocker3D_wrapper(){
        type = GUIState_wrapper.SimObjectType.AGENT;
        class_name = "Flocker";
    }
    public Flocker3D_wrapper(Object toMap, JSONArray params){
        type = GUIState_wrapper.SimObjectType.AGENT;
        class_name = "Flocker";
        map(toMap);
    }

    @Override
    public void map(Object toMap) {
        flocker = (Flocker3D) toMap;
        ID = flocker.ID;
        this.params.put("position", new Float3D((float)flocker.loc.x, (float)flocker.loc.y, (float)flocker.loc.z));
        this.params.put("dead", flocker.dead);
    }
    @Override
    public void create(JSONObject params){
        is_new = true;
        if (Flocker3D_wrapper.empty_IDs.size() > 0) {
            ID = Flocker3D_wrapper.empty_IDs.first();
            Flocker3D_wrapper.empty_IDs.remove(ID);
        }
        else {
            ID = quantity;
        }
        flocker = new Flocker3D(ID, new Double3D());
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
        steps_to_live_as_new = 0;
        updateWrapper();
    }
    @Override
    public void delete() {
        Flockers3D.agents_stoppables.remove(ID).stop();
        Flockers3D.flockers.remove(flocker);
        flocker = null;
        GUIState_wrapper.getAGENTS().remove(new Pair<>(this.ID, this.getClass_name()));
        empty_IDs.add(ID);
        --quantity;
    }
}