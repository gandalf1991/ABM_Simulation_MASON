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

public class Home_wrapper extends SimObject_wrapper {
    static private final GUIState_wrapper.SimObjectType type = GUIState_wrapper.SimObjectType.GENERIC;
    static private final String class_name = "Home";
    static private int quantity = 0;
    static private SortedSet<Integer> empty_IDs;

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
        Home_wrapper.quantity = quantity;
    }

    @Override
    public void map(Object toMap, JSONArray params) {
        Pair<Integer, Int2D> mapping = (Pair<Integer, Int2D>)toMap;
        ID = mapping.getKey();
        Object[] parameters = params.toArray();
        for (Object p : parameters) {
            if(((JSONObject)p).get("name").equals("position")) {
                this.params.put("position", new Int2D(mapping.getValue().x, mapping.getValue().y));
            }
        }
    }
    @Override
    public void init(JSONArray params) {}
    @Override
    public void create(JSONObject params) {
        ID = (Home_wrapper.empty_IDs.size() > 0) ? Home_wrapper.empty_IDs.first() : quantity++;
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
    public void delete() {



    }
}


//if(((JSONObject)p).get("name").equals("position")) {
//        JSONArray home_pos = (JSONArray)((JSONObject)p).get("position");
//        home_pos.forEach(pos -> {
//        AntsForage.HOME_POS.add(new Int2D((int)((JSONObject)pos).get("x"), (int)((JSONObject)pos).get("y")));
//        });
//        }