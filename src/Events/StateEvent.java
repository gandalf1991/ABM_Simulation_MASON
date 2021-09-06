package Events;

import Main.Sim_Controller;

@FunctionalInterface
public interface StateEvent<TEventArgs extends Object> {
    Sim_Controller.SimStateEnum invoke(Object source, TEventArgs eventArgs);
}