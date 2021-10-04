/*
 	Written by Pietro Russo
*/

package Events.EventArgs;

import org.json.simple.JSONObject;

public class SimUpdateEventArgs {

    private JSONObject payload;

    public SimUpdateEventArgs(JSONObject payload) {
        this.setPayload(payload);
    }

    public JSONObject getPayload() {
        return payload;
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }
}
