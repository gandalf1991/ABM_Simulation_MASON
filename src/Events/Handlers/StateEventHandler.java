/*
 	Written by Pietro Russo
*/

package Events.Handlers;

import Events.StateIEvent;
import Main.Sim_Controller;

import java.util.ArrayList;

public class StateEventHandler<TEventArgs> {

    ArrayList<Sim_Controller.SimStateEnum> responses = new ArrayList<Sim_Controller.SimStateEnum>();
    private ArrayList<StateIEvent<TEventArgs>> eventDelegateArray = new ArrayList<>();
    public void subscribe(StateIEvent<TEventArgs> methodReference)
    {
        eventDelegateArray.add(methodReference);
    }
    public void unSubscribe(StateIEvent<TEventArgs> methodReference)
    {
        eventDelegateArray.remove(methodReference);
    }
    public Sim_Controller.SimStateEnum invoke(Object source, TEventArgs eventArgs)
    {
        if (eventDelegateArray.size()>0)
            eventDelegateArray.forEach(p -> responses.add(p.invoke(source, eventArgs)));
        return responses.get(0);
    }
    public void close()
    {
        if (eventDelegateArray.size()>0)
            eventDelegateArray.clear();
    }
}
