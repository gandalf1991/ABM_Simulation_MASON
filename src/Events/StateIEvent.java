/*
 	Written by Pietro Russo
*/

package Events;

import Main.Sim_Controller;

@FunctionalInterface
public interface StateIEvent<TEventArgs extends Object> {
    Sim_Controller.SimStateEnum invoke(Object source, TEventArgs eventArgs);
}