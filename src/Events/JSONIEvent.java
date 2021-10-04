/*
 	Written by Pietro Russo
*/

package Events;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;

@FunctionalInterface
public interface JSONIEvent<TEventArgs extends Object> {
    JSONObject invoke(Object source, TEventArgs eventArgs) throws IOException, ParseException;
}