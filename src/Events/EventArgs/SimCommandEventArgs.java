package Events.EventArgs;

import org.json.simple.JSONObject;

public class SimCommandEventArgs {

    private JSONObject payload;

    public SimCommandEventArgs(JSONObject payload) {
        this.setPayload(payload);
    }

    public JSONObject getPayload() {
        return payload;
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }
}
