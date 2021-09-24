package Wrappers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Dictionary;
import java.util.Hashtable;

public abstract class SimObject_wrapper {

    protected GUIState_wrapper.SimObjectType type;
    protected String class_name;
    protected int ID;
    protected Dictionary<String, Object> params = new Hashtable<>();

    public GUIState_wrapper.SimObjectType getType() {
        return type;
    }
    public void setType(GUIState_wrapper.SimObjectType type) {
        this.type = type;
    }
    public String getClass_name() {
        return class_name;
    }
    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }

    public SimObject_wrapper(){}

    public abstract void map(Object toMap, JSONArray params);
    public abstract void init(JSONArray params);
    public abstract void create(JSONObject params);
    public abstract void update(JSONObject params);
    public abstract void delete();
}
