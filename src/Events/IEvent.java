/*
 	Written by Pietro Russo
*/

package Events;


@FunctionalInterface
public interface IEvent<TEventArgs extends Object> {
    boolean invoke(Object source, TEventArgs eventArgs) throws InstantiationException, IllegalAccessException;
}