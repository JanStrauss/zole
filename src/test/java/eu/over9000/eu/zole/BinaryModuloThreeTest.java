package eu.over9000.eu.zole;

import junit.framework.Assert;
import org.junit.Test;

import java.util.EnumSet;

/**
 * Adding a bit b to the end of a binary number multiplies the existing number by two and then adds b
 * see https://math.stackexchange.com/questions/140283/
 */
public class BinaryModuloThreeTest {


	enum States {A, B, C}

	enum Input {ZERO, ONE}

	final Input[][] numbers = {
			{Input.ZERO},
			{Input.ONE},
			{Input.ONE, Input.ZERO},
			{Input.ONE, Input.ONE},
			{Input.ONE, Input.ZERO, Input.ZERO},
			{Input.ONE, Input.ZERO, Input.ONE},
			{Input.ONE, Input.ONE, Input.ZERO},
			{Input.ONE, Input.ONE, Input.ONE},
			{Input.ONE, Input.ZERO, Input.ZERO, Input.ZERO},
			{Input.ONE, Input.ZERO, Input.ZERO, Input.ONE}};

	@Test
	public void runBinaryModuloThreeTest() throws ZoleInvalidInputException {


		// Build state machine
		ZoleStateMachine<States, Input> fsm = ZoleStateMachine.buildFrom(States.class, Input.class, States.A, EnumSet.of(States.A));

		fsm.addTransition(States.A, Input.ZERO, States.A);
		fsm.addTransition(States.A, Input.ONE, States.B);
		fsm.addTransition(States.B, Input.ZERO, States.C);
		fsm.addTransition(States.B, Input.ONE, States.A);
		fsm.addTransition(States.C, Input.ZERO, States.B);
		fsm.addTransition(States.C, Input.ONE, States.C);

		// test numbers
		for (int i = 0; i < numbers.length; i++) {
			Input[] number = numbers[i];

			for (Input input : number) {
				fsm.processInput(input);
			}

			Assert.assertEquals(i % 3 == 0, fsm.inAcceptedState());
			fsm.reset();
		}

	}

}