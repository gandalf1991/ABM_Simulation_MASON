package Events.EventArgs;

import org.json.simple.JSONObject;

public class SimInitializeEventArgs {

    private JSONObject payload;

    public SimInitializeEventArgs(JSONObject payload) {
        this.setPayload(payload);
    }

    public JSONObject getPayload() {
        return payload;
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }
}
