# flowquiz

The idea is to be able to generate simple programs, display them to
the user and ask the user what they output and keep score of how many
correct and incorrect answers are given.

The programs will be in a simple language resembling BASIC.

Single lowercase letters are variable names; they are all integers.

Lines are labeled by numbers.

There are the usual arithmetic operators.
/ means integer division (quotient) e.g. 12 / 5 = 2.

There is conditional and unconditional branching.

If I have time I will write something that turns these programs into flowcharts.

For now, the programs are presented to the user who will be asked for
the final value of the variables in the program. Enter these values
seperated by spaces in the text field provided.

## Installation

	git clone https://github.com/mikeyreilly/flowquiz
	cd flowquiz
	boot build

## Usage

Build an uberjar from the project:

$ boot build

Run the uberjar:

$ java -jar target/flowquiz-0.1.jar


## License

Copyright © 2017 Michael Reilly

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
