
//	pop 	static   0   
//	--------------------
    @16
    D=A
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
    
//	push	constant 5   
//	--------------------
    @5
    D=A     // store constant
                
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	push	constant 3   
//	--------------------
    @3
    D=A     // store constant
                
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	eq
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
    @is.equal.0
    D; JEQ

(is.not.equal.0)
    D=0
    @end.is.equal.0
    0; JMP

(is.equal.0)
    D=-1

(end.is.equal.0)
      
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    