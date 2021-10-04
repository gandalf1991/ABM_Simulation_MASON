/*
 	Written by Pietro Russo
*/

package Events.Handlers;

import Events.JSONIEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class JSONEventHandler<TEventArgs>
{
    ArrayList<JSONObject> responses = new ArrayList<JSONObject>();
    private ArrayList<JSONIEvent<TEventArgs>> eventDelegateArray = new ArrayList<>();
    public void subscribe(JSONIEvent<TEventArgs> methodReference)
    {
        eventDelegateArray.add(methodReference);
    }
    public void unSubscribe(JSONIEvent<TEventArgs> methodReference)
    {
        eventDelegateArray.remove(methodReference);
    }
    public JSONObject invoke(Object source, TEventArgs eventArgs)
    {
        if (eventDelegateArray.size()>0)
            eventDelegateArray.forEach(p -> {
                try {
                    responses.add(p.invoke(source, eventArgs));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        return responses.get(0);
    }
    public void close()
    {
        if (eventDelegateArray.size()>0)
            eventDelegateArray.clear();
    }
}