package eu.over9000.eu.zole;

import java.lang.reflect.Array;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Consumer;

public class ZoleStateMachine<S extends Enum<S>, I extends Enum<I>> {

	private final Class<S> states;
	private final Class<I> inputs;

	private boolean started = false;

	private final S[][] transitionTable;

	private Consumer<S> handlerOnEntry;
	private Consumer<S> handlerOnExit;
	private ZoleTransitionCallback<S, I> handlerOnTransition;

	private final S startState;
	private S currentState;

	private final EnumSet<S> acceptedStates;

	@SuppressWarnings("unchecked")
	public ZoleStateMachine(final Class<S> states, final Class<I> inputs, final S startState, final EnumSet<S> acceptedStates) {
		Objects.requireNonNull(states);
		Objects.requireNonNull(inputs);
		Objects.requireNonNull(startState);
		Objects.requireNonNull(acceptedStates);

		this.states = states;
		this.inputs = inputs;

		if (states.getEnumConstants().length == 0) {
			throw new ZoleException("can't create state machine with empty state enum");
		}

		if (inputs.getEnumConstants().length == 0) {
			throw new ZoleException("can't create state machine with empty input enum");
		}

		transitionTable = (S[][]) Array.newInstance(states, states.getEnumConstants().length, inputs.getEnumConstants().length);

		this.startState = startState;
		this.acceptedStates = acceptedStates;

		currentState = startState;
	}

	public void addTransition(final S fromState, final I withInput, final S toState) {
		Objects.requireNonNull(fromState);
		Objects.requireNonNull(withInput);
		Objects.requireNonNull(toState);

		if (started) {
			throw new ZoleException("can't add a transition after the state machine has been started");
		}
		transitionTable[fromState.ordinal()][withInput.ordinal()] = toState;
	}

	public void addTrapTransitions(final S state) {
		Objects.requireNonNull(state);
		for (final I input : inputs.getEnumConstants()) {
			addTransition(state, input, state);
		}
	}

	public void setStateEntryCallback(final Consumer<S> callback) {
		if (started) {
			throw new ZoleException("can't add a callback after the state machine has been started");
		}

		handlerOnEntry = callback;
	}

	public void setStateExitCallback(final Consumer<S> callback) {
		if (started) {
			throw new ZoleException("can't add a callback after the state machine has been started");
		}

		handlerOnExit = callback;
	}

	public void setTransitionCallback(final ZoleTransitionCallback<S, I> callback) {
		if (started) {
			throw new ZoleException("can't add a callback after the state machine has been started");
		}

		handlerOnTransition = callback;
	}

	public void processInput(final I input) throws ZoleInvalidInputException {
		Objects.requireNonNull(input);

		started = true;

		final S nextState = transitionTable[currentState.ordinal()][input.ordinal()];

		if (nextState == null) {
			throw new ZoleInvalidInputException("No transition in state " + currentState + " for input " + input);
		}

		if (handlerOnExit != null) {
			handlerOnExit.accept(currentState);
		}

		if (handlerOnTransition != null) {
			handlerOnTransition.accept(currentState, input, nextState);
		}

		currentState = nextState;

		if (handlerOnEntry != null) {
			handlerOnEntry.accept(currentState);
		}
	}

	public void reset() {
		currentState = startState;
	}

	public S getCurrentState() {
		return currentState;
	}

	public boolean inAcceptedState() {
		return acceptedStates.contains(currentState);
	}

	public static <S2 extends Enum<S2>, I2 extends Enum<I2>> ZoleStateMachine<S2, I2> buildFrom(final Class<S2> states, final Class<I2> inputs) {
		Objects.requireNonNull(states);
		Objects.requireNonNull(inputs);

		if (states.getEnumConstants().length == 0) {
			throw new ZoleException("can't create state machine with empty state enum");
		}

		return new ZoleStateMachine<>(states, inputs, states.getEnumConstants()[0], EnumSet.noneOf(states));
	}

	public static <S2 extends Enum<S2>, I2 extends Enum<I2>> ZoleStateMachine<S2, I2> buildFrom(final Class<S2> states, final Class<I2> inputs, final EnumSet<S2> acceptedStates) {
		Objects.requireNonNull(states);
		Objects.requireNonNull(inputs);
		Objects.requireNonNull(acceptedStates);

		if (states.getEnumConstants().length == 0) {
			throw new ZoleException("can't create state machine with empty state enum");
		}

		return new ZoleStateMachine<>(states, inputs, states.getEnumConstants()[0], acceptedStates);
	}

	public static <S2 extends Enum<S2>, I2 extends Enum<I2>> ZoleStateMachine<S2, I2> buildFrom(final Class<S2> states, final Class<I2> inputs, final S2 startState) {
		Objects.requireNonNull(states);
		Objects.requireNonNull(inputs);
		Objects.requireNonNull(startState);

		return new ZoleStateMachine<>(states, inputs, startState, EnumSet.noneOf(states));
	}

	public static <S2 extends Enum<S2>, I2 extends Enum<I2>> ZoleStateMachine<S2, I2> buildFrom(final Class<S2> states, final Class<I2> inputs, final S2 startState, final EnumSet<S2> acceptedStates) {
		Objects.requireNonNull(states);
		Objects.requireNonNull(inputs);
		Objects.requireNonNull(startState);
		Objects.requireNonNull(acceptedStates);

		return new ZoleStateMachine<>(states, inputs, startState, acceptedStates);
	}

}
