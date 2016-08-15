
//	function 	sub      2   
//	--------------------
(sub)

    @0
    D=A     // store constant
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
    @0
    D=A     // store constant
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	push	argument 0   
//	--------------------
    @ARG
    D=M
    @R13
    M=D
    
    //======= push(bp, index) =======//
    @R13    // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp                      --
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	local    0   
//	--------------------
    @LCL
    D=M
    @R13
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R13) =======//
    @R14    // store popped value
    M=D
    @R13    // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp
    M=D     // sum of bp + index
    @R14
    D=M     // popped value
    @R13
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	push	argument 1   
//	--------------------
    @ARG
    D=M
    @R13
    M=D
    
    //======= push(bp, index) =======//
    @R13    // used for bp
    D=M
    @1
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp                      --
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	local    1   
//	--------------------
    @LCL
    D=M
    @R13
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R13) =======//
    @R14    // store popped value
    M=D
    @R13    // used for bp
    D=M
    @1
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp
    M=D     // sum of bp + index
    @R14
    D=M     // popped value
    @R13
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	sub
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R13
    M=D     // store y
      
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R13
    D=D-M   // add x - y
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	return
//	--------------------
    @LCL    // frame = local
    D=M
    @R13    // frame register
    M=D

    @5
    D=A     // return address = frame - 5
    @R13
    A=M-D
    D=M

    @R14    // return address register
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @ARG
    A=M     // dereference
    M=D     // replace arg with return value
    D=A     // get arg pointer

    @SP
    M=D+1   // set SP to arg + 1

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @THAT   // set *THAT to frame - 1
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @THIS   // set *THIS to frame - 2
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @ARG    // set *ARG to frame - 3
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @LCL    // set *LCL to frame - 4
    M=D

    @R14    // jump to return address
    A=M
    0; JMP
    
//	function 	add      2   
//	--------------------
(add)

    @0
    D=A     // store constant
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
    @0
    D=A     // store constant
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	push	argument 0   
//	--------------------
    @ARG
    D=M
    @R13
    M=D
    
    //======= push(bp, index) =======//
    @R13    // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp                      --
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	local    0   
//	--------------------
    @LCL
    D=M
    @R13
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R13) =======//
    @R14    // store popped value
    M=D
    @R13    // used for bp
    D=M
    @0
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp
    M=D     // sum of bp + index
    @R14
    D=M     // popped value
    @R13
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	push	argument 1   
//	--------------------
    @ARG
    D=M
    @R13
    M=D
    
    //======= push(bp, index) =======//
    @R13    // used for bp
    D=M
    @1
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp                      --
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	pop 	local    1   
//	--------------------
    @LCL
    D=M
    @R13
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    //======= pop(R13) =======//
    @R14    // store popped value
    M=D
    @R13    // used for bp
    D=M
    @1
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp
    M=D     // sum of bp + index
    @R14
    D=M     // popped value
    @R13
    A=M     // grab RAM pointer
    M=D     // store value into heap
    
//	add
//	--------------------
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R13
    M=D     // store y
      
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @R13
    D=D+M   // add x + y
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
//	return
//	--------------------
    @LCL    // frame = local
    D=M
    @R13    // frame register
    M=D

    @5
    D=A     // return address = frame - 5
    @R13
    A=M-D
    D=M

    @R14    // return address register
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @ARG
    A=M     // dereference
    M=D     // replace arg with return value
    D=A     // get arg pointer

    @SP
    M=D+1   // set SP to arg + 1

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @THAT   // set *THAT to frame - 1
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @THIS   // set *THIS to frame - 2
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @ARG    // set *ARG to frame - 3
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @LCL    // set *LCL to frame - 4
    M=D

    @R14    // jump to return address
    A=M
    0; JMP
    
//	function 	Sys.init 0   
//	--------------------
(Sys.init)

//	push	constant 8   
//	--------------------
    @8
    D=A     // store constant
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
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
    
//	call 	sub      2   
//	--------------------
    @return.function.0
    D=A
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
    @LCL
    D=M
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
    @ARG
    D=M
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
    @THIS
    D=M
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
    @THAT
    D=M
    
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    
    @SP     // ARG = SP - 2 - 5
    D=M
    @2
    D=D-A
    @5      // offset stacked env
    D=D-A
    @ARG
    M=D
    
    @SP     // LCL = SP
    D=M
    @LCL
    M=D
    
//	goto 	sub     
//	--------------------
    @sub
    0; JMP
    
(return.function.0)

//	return
//	--------------------
    @LCL    // frame = local
    D=M
    @R13    // frame register
    M=D

    @5
    D=A     // return address = frame - 5
    @R13
    A=M-D
    D=M

    @R14    // return address register
    M=D
    
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    
    @ARG
    A=M     // dereference
    M=D     // replace arg with return value
    D=A     // get arg pointer

    @SP
    M=D+1   // set SP to arg + 1

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @THAT   // set *THAT to frame - 1
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @THIS   // set *THIS to frame - 2
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @ARG    // set *ARG to frame - 3
    M=D

    @R13    // grab frame value
    D=M

    AM=D-1
    D=M
    @LCL    // set *LCL to frame - 4
    M=D

    @R14    // jump to return address
    A=M
    0; JMP
    