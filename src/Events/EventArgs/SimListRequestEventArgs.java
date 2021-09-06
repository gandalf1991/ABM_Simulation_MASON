package Events.EventArgs;

import org.json.simple.JSONObject;

public class SimListRequestEventArgs {

    private JSONObject payload;

    public SimListRequestEventArgs(JSONObject payload) {
        this.setPayload(payload);
    }

    public JSONObject getPayload() {
        return payload;
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }
}
