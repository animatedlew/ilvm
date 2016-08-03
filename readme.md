 RAM usage
-=-=-=-=-=-
0-15        | registers
16-255      | static variables
256-2047    | stack
2048-16383  | heap for objects and arrays
16384-24575 | memory mapped I/O
24576-32767 | unused memory space

R0     - SP, topmost location on the stack
R1     - LCL, points to the base of function's current local segment
R2     - ARG, points to the base of function's argument segment
R3     - THIS, points to the base of this segment within heap
R4     - THAT, points to the base of that segment within heap
R5:12  - temp segment
R13:15 - General purpose registers