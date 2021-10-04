package Wrappers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public abstract class SimObject_wrapper {

    protected GUIState_wrapper.SimObjectType type;
    protected String class_name;
    protected int ID;
    protected HashMap<String, Object> params = new HashMap<>();

    public GUIState_wrapper.SimObjectType getType() {
        return type;
    }
    public String getClass_name() {
        return class_name;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public HashMap<String, Object> getParams() {
        return params;
    }
    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public SimObject_wrapper(){}

    public abstract void map(Object toMap);
    public abstract void create(JSONObject params);
    public abstract void update(JSONObject params);
    public abstract void updateWrapper();
    public abstract void reset();
    public abstract void delete();
}
