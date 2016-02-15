package eu.over9000.eu.zole;

/**
 * Created by Jan on 15.02.2016.
 */
@FunctionalInterface
public interface ZoleTransitionCallback<S, I> {

	void accept(S fromState, I withInput, S toState);
}