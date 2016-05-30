/*
 * Copyright (c) 2016 Jan Strauß <jan[at]over9000.eu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.over9000.eu.zole;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class ZoleStateMachineTest {
	enum CallbackType {ENTRY, EXIT, TRANS}

	private class CallbackRecord {
		CallbackType type;
		StatesValid state;
		StatesValid target;
		InputValid input;

		public CallbackRecord(final CallbackType type, final StatesValid state, final StatesValid target, final InputValid input) {
			this.type = type;
			this.state = state;
			this.target = target;
			this.input = input;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final CallbackRecord that = (CallbackRecord) o;
			return type == that.type &&
					state == that.state &&
					target == that.target &&
					input == that.input;
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, state, target, input);
		}
	}

	enum StatesEmpty {}

	enum InputEmpty {}

	enum StatesValid {S1, S2, S3, S4}

	enum InputValid {I1, I2, I3}

	EnumSet<StatesValid> accepted = EnumSet.of(StatesValid.S2, StatesValid.S4);


	@org.junit.Test
	public void testTrapAssert() throws Exception, ZoleInvalidInputException {
		final ZoleStateMachine<StatesValid, InputValid> fsm = ZoleStateMachine.buildFrom(StatesValid.class, InputValid.class, accepted);

		fsm.addTrapTransitions(StatesValid.S1);

		fsm.processInput(InputValid.I1);
		fsm.processInput(InputValid.I2);
		fsm.processInput(InputValid.I3);

		Assert.assertFalse(fsm.inAcceptedState());
	}


	@org.junit.Test(expected = ZoleInvalidInputException.class)
	public void testErrorInput() throws Exception, ZoleInvalidInputException {
		final ZoleStateMachine<StatesValid, InputValid> fsm = ZoleStateMachine.buildFrom(StatesValid.class, InputValid.class, StatesValid.S1);

		fsm.addTransition(StatesValid.S1, InputValid.I1, StatesValid.S2);

		fsm.processInput(InputValid.I2);
	}

	@org.junit.Test
	public void testCallbacks() throws Exception, ZoleInvalidInputException {
		final List<CallbackRecord> recordsActual = new ArrayList<>();
		final List<CallbackRecord> recordsExpected = new ArrayList<>();

		final ZoleStateMachine<StatesValid, InputValid> fsm = ZoleStateMachine.buildFrom(StatesValid.class, InputValid.class, StatesValid.S1, accepted);

		fsm.addTransition(StatesValid.S1, InputValid.I1, StatesValid.S2);
		fsm.addTransition(StatesValid.S2, InputValid.I1, StatesValid.S3);
		fsm.addTransition(StatesValid.S3, InputValid.I1, StatesValid.S1);

		fsm.setStateEntryCallback(s -> recordsActual.add(new CallbackRecord(CallbackType.ENTRY, s, null, null)));

		fsm.setStateExitCallback(s -> recordsActual.add(new CallbackRecord(CallbackType.EXIT, s, null, null)));

		fsm.setTransitionCallback((f, i, t) -> recordsActual.add(new CallbackRecord(CallbackType.TRANS, f, t, i)));


		recordsExpected.add(new CallbackRecord(CallbackType.EXIT, StatesValid.S1, null, null));
		recordsExpected.add(new CallbackRecord(CallbackType.TRANS, StatesValid.S1, StatesValid.S2, InputValid.I1));
		recordsExpected.add(new CallbackRecord(CallbackType.ENTRY, StatesValid.S2, null, null));

		recordsExpected.add(new CallbackRecord(CallbackType.EXIT, StatesValid.S2, null, null));
		recordsExpected.add(new CallbackRecord(CallbackType.TRANS, StatesValid.S2, StatesValid.S3, InputValid.I1));
		recordsExpected.add(new CallbackRecord(CallbackType.ENTRY, StatesValid.S3, null, null));

		recordsExpected.add(new CallbackRecord(CallbackType.EXIT, StatesValid.S3, null, null));
		recordsExpected.add(new CallbackRecord(CallbackType.TRANS, StatesValid.S3, StatesValid.S1, InputValid.I1));
		recordsExpected.add(new CallbackRecord(CallbackType.ENTRY, StatesValid.S1, null, null));


		fsm.processInput(InputValid.I1);
		fsm.processInput(InputValid.I1);
		fsm.processInput(InputValid.I1);

		Assert.assertEquals(StatesValid.S1, fsm.getCurrentState());
		Assert.assertEquals(recordsExpected, recordsActual);

		fsm.reset();
		Assert.assertEquals(StatesValid.S1, fsm.getCurrentState());
		recordsActual.clear();

		fsm.processInput(InputValid.I1);
		fsm.processInput(InputValid.I1);
		fsm.processInput(InputValid.I1);

		Assert.assertEquals(StatesValid.S1, fsm.getCurrentState());
		Assert.assertEquals(recordsExpected, recordsActual);
	}

	@org.junit.Test(expected = ZoleException.class)
	public void testEmptyBoth() throws Exception {
		ZoleStateMachine.buildFrom(StatesEmpty.class, InputEmpty.class);
	}

	@org.junit.Test(expected = ZoleException.class)
	public void testEmptyStates() throws Exception {
		ZoleStateMachine.buildFrom(StatesEmpty.class, InputValid.class);
	}

	@org.junit.Test(expected = ZoleException.class)
	public void testEmptyInputs() throws Exception {
		ZoleStateMachine.buildFrom(StatesValid.class, InputEmpty.class);
	}

}