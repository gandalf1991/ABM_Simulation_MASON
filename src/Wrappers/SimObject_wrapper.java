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
    protected boolean is_new = false;
    protected int steps_to_live_as_new = 0;

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
    public boolean Is_new() {
        return is_new;
    }
    public void setIs_new(boolean is_new) {
        this.is_new = is_new;
    }
    public int STLN(){
        return steps_to_live_as_new;
    }
    public void setSTLN(int steps_to_live_as_new){
        this.steps_to_live_as_new = steps_to_live_as_new;
    }
    public void IncrementSTLN(){
        ++this.steps_to_live_as_new;
    }

    public SimObject_wrapper(){}

    public abstract void map(Object toMap);
    public abstract void create(JSONObject params);
    public abstract void update(JSONObject params);
    public abstract boolean updateWrapper();
    public abstract void reset();
    public abstract void delete();
}
