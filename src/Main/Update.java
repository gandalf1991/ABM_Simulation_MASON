package Main;

import org.json.simple.JSONObject;

public class Update {

    boolean is_new;
    JSONObject update;

    public Update(boolean is_new, JSONObject update){
        this.is_new = is_new;
        this.update = update;
    }

    public boolean is_new() {
        return is_new;
    }
    public void setIs_new(boolean is_new) {
        this.is_new = is_new;
    }
    public JSONObject getUpdate() {
        return update;
    }
    public void setUpdate(JSONObject update) {
        this.update = update;
    }
}
