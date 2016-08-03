
//	push	argument 1   
//	--------------------
    @R2
    D=M
    @R5
    M=D
    
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @1
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	pointer  1   
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R4
    M=D
    
//	push	constant 0   
//	--------------------
    @0
    D=A     // store constant
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	that     0   
//	--------------------
    @R4
    D=M
    @R5
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R5, R6) =======//
    @R7     // store popped value
    M=D
    @R5     // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    M=D     // sum of bp + index
    @R7
    D=M     // popped value
    @R5
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	push	constant 1   
//	--------------------
    @1
    D=A     // store constant
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	that     1   
//	--------------------
    @R4
    D=M
    @R5
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R5, R6) =======//
    @R7     // store popped value
    M=D
    @R5     // used for bp
    D=M
    @1
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    M=D     // sum of bp + index
    @R7
    D=M     // popped value
    @R5
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	push	argument 0   
//	--------------------
    @R2
    D=M
    @R5
    M=D
    
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	push	constant 2   
//	--------------------
    @2
    D=A     // store constant
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	sub
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    M=D     // store y
        
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    D=D-M    // add x - y
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	argument 0   
//	--------------------
    @R2
    D=M
    @R5
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R5, R6) =======//
    @R7     // store popped value
    M=D
    @R5     // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    M=D     // sum of bp + index
    @R7
    D=M     // popped value
    @R5
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
(MAIN_LOOP_START)

//	push	argument 0   
//	--------------------
    @R2
    D=M
    @R5
    M=D
    
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	goto 	COMPUTE_ELEMENT
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @COMPUTE_ELEMENT
    D; JNE      // jump if D is not zero
      
//	goto 	END_PROGRAM
//	--------------------
    @END_PROGRAM
    0; JMP
      
(COMPUTE_ELEMENT)

//	push	that     0   
//	--------------------
    @R4
    D=M
    @R5
    M=D
    
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	push	that     1   
//	--------------------
    @R4
    D=M
    @R5
    M=D
    
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @1
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	add
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    M=D     // store y
        
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    D=D+M    // add x + y
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	that     2   
//	--------------------
    @R4
    D=M
    @R5
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R5, R6) =======//
    @R7     // store popped value
    M=D
    @R5     // used for bp
    D=M
    @2
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    M=D     // sum of bp + index
    @R7
    D=M     // popped value
    @R5
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	push	pointer  1   
//	--------------------
    @R4
    D=M
    @R5
    M=D
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	push	constant 1   
//	--------------------
    @1
    D=A     // store constant
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	add
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    M=D     // store y
        
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    D=D+M    // add x + y
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	pointer  1   
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R4
    M=D
    
//	push	argument 0   
//	--------------------
    @R2
    D=M
    @R5
    M=D
    
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	push	constant 1   
//	--------------------
    @1
    D=A     // store constant
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	sub
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    M=D     // store y
        
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R5
    D=D-M    // add x - y
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	argument 0   
//	--------------------
    @R2
    D=M
    @R5
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R5, R6) =======//
    @R7     // store popped value
    M=D
    @R5     // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    M=D     // sum of bp + index
    @R7
    D=M     // popped value
    @R5
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	goto 	MAIN_LOOP_START
//	--------------------
    @MAIN_LOOP_START
    0; JMP
      
(END_PROGRAM)
