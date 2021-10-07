/*
 	Written by Pietro Russo
*/

package Events.Handlers;

import Events.IEvent;
import java.util.ArrayList;

public class EventHandler<TEventArgs>
{
    ArrayList<Boolean> responses = new ArrayList<Boolean>();
    private ArrayList<IEvent<TEventArgs>> eventDelegateArray = new ArrayList<>();
    public void subscribe(IEvent<TEventArgs> methodReference)
    {
        eventDelegateArray.add(methodReference);
    }
    public void unSubscribe(IEvent<TEventArgs> methodReference)
    {
        eventDelegateArray.remove(methodReference);
    }
    public boolean invoke(Object source, TEventArgs eventArgs)
    {
        if (eventDelegateArray.size()>0)
            eventDelegateArray.forEach(p -> {
                try {
                    responses.add(p.invoke(source, eventArgs));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        return !responses.contains(false);
    }
    public void close()
    {
        if (eventDelegateArray.size()>0)
            eventDelegateArray.clear();
    }
}