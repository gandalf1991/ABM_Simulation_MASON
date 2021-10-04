/*
 	Written by Pietro Russo
*/

package Events;

import org.json.simple.JSONObject;

@FunctionalInterface
public interface IEvent<TEventArgs extends Object> {
    boolean invoke(Object source, TEventArgs eventArgs) throws InstantiationException, IllegalAccessException;
}