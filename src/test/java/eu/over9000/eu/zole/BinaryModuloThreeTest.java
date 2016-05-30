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