# zole

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.over9000/zole/badge.svg)](https://maven-badges.herokuapp.com/maven-central/eu.over9000/zole)

Zole is a simple FSM (finite state machine) implementation.
States and inputs are defined as enums. Callbacks can be registered for state entry/exit as well as on transitions.
An exception will be thrown when no transition is given for the current input/state.

## Usage

Add zole as a dependency using Maven:
```xml
<dependency>
  <groupId>eu.over9000</groupId>
  <artifactId>zole</artifactId>
  <version>1.0</version>
</dependency>
```
Or see [releases](https://github.com/s1mpl3x/zole/releases).

## Example

Usage example of a FSM accepting binary numbers divisible by three: 

```java
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
```
