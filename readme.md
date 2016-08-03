-=-=-=-=-=- Heap Suggested Usage -=-=-=-=-=-

-	0-15, registers
-	16-255, static variables
-	256-2047, stack
-	2048-16383, heap for objects and arrays
-	16384-24575, memory mapped I/O
-	24576-32767, unused memory space

First 5 registers are *sp*, *lcl*, *arg*, *this*, and *that*. R5:12 are temp registers. R13:15 are general purpose.
