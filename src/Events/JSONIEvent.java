package Events;

import org.json.simple.JSONObject;

@FunctionalInterface
public interface JSONIEvent<TEventArgs extends Object> {
    JSONObject invoke(Object source, TEventArgs eventArgs);
}