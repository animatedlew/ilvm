package virtualmachine

import virtualmachine.AST._
import annotation.tailrec

class HackVM {

  /*
   *  RAM usage
   * -=-=-=-=-=-
   * 0-15        | registers
   * 16-255      | static variables
   * 256-2047    | stack
   * 2048-16383  | heap for objects and arrays
   * 16384-24575 | memory mapped I/O
   * 24576-32767 | unused memory space
   *
   * R0     - SP, topmost location on the stack
   * R1     - LCL, points to the base of function's current local segment
   * R2     - ARG, points to the base of function's argument segment
   * R3     - THIS, points to the base of this segment within heap
   * R4     - THAT, points to the base of that segment within heap
   * R5:12  - temp segment
   * R13:15 - General purpose registers
   */

  var RAM  = new Array[Word](16384 + 8192 + 1) // 16k + 8k + 1reg
  var ROM  = new Array[Word](32768) // 32k RESERVED

  object Writer { def emit(s: String) = print(s"${Console.CYAN}$s") }

  RAM(0) = 256
  RAM(1) = 300
  RAM(2) = 400
  RAM(3) = 3000
  RAM(4) = 3010

  // temp    R05 - R12
  // general R13 - R15

  def sp = RAM(0)
  def sp(value: Word) = RAM(0) = value

  def local = {
    Writer.emit(
    """
    @R1
    D=M
    @R5
    M=D
    """
    )
    RAM(1)
  }

  //def local(value: Word) = RAM(1) = value

  def arg = {
    Writer.emit(
    """
    @R2
    D=M
    @R5
    M=D
    """
    )
    RAM(2)
  }

  //def arg(value: Word)    = RAM(2) = value

  def `this` = {
    Writer.emit(
    """
    @R3
    D=M
    @R5
    M=D
    """
    )
    RAM(3)
  }

  def `this`(value: Word) = {
    Writer.emit(
    """
    @R3
    M=D
    """
    )
    RAM(3) = value
  }

  def that = {
    Writer.emit(
    """
    @R4
    D=M
    @R5
    M=D
    """
    )
    RAM(4)
  }

  def that(value: Word) = {
    Writer.emit(
    """
    @R4
    M=D
    """
    )
    RAM(4) = value
  }

  def static = {
    Writer.emit(
    """
    @16
    D=A
    @R5
    M=D
    """
    )
    16
  }
  def temp   = {
    Writer.emit(
    """
    @5
    D=A
    @R5
    M=D
    """
    )
    5
  }

  private def pop(): Word = {
    Writer.emit(
    """
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in D
    """
    )
    sp(sp - 1); RAM(sp)
  }

  private def push(value: Word): Unit = {
    Writer.emit(
    s"""
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ A
    @SP
    M=M+1   // inc stack pointer
    """
    )
    RAM(sp) = value; sp(sp + 1)
  }

  private def push(bp: Word, index: Word): Unit = {
    val D = RAM(bp + index)
    Writer.emit(
    s"""
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @$index
    D=D+A   // A register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    """
    )
    push(D)
  }

  private def pop(bp: Word, index: Word): Unit = {
    val D = pop()
    RAM(bp + index) = D
    Writer.emit(
    s"""
    //======= pop(R5, R6) =======//
    @R7     // store popped value
    M=D
    @R5     // used for bp
    D=M
    @$index
    D=D+A   // A register used for index
    @R5     // overwrite bp
    M=D     // sum of bp + index
    @R7
    D=M     // popped value
    @R5
    A=M     // grab RAM pointer
    M=D     // store value into heap
    """
    )
  }

  private def bool(condition: Boolean): Short = { if (condition) 0xFF else 0x00 }

  @tailrec private def cpu(ast: List[Command]): Unit = {
    if (ast.nonEmpty) {
      print(Console.YELLOW)
      ast.head match {
        case CPush(segment, index) =>
          println(f"\tpush\t$segment%-8s $index%-4s")
          print("\t" + ("-"*20))
          segment match {
            case "constant" =>
              Writer.emit(
                s"""
                  |    @$index
                  |    D=A    // store constant
                """.stripMargin
              )
              push(index)
            case "static"   => push(static, index)
            case "local"    => push(local,  index)
            case "argument" => push(arg,    index)
            case "this"     => push(`this`, index)
            case "that"     => push(that,   index)
            case "temp"     => push(temp,   index)
            case "pointer"  =>
              index match {
                case 0 => push(`this`)
                case 1 => push(that)
                case _ => throw new IllegalArgumentException
              }
            case _ => throw new IllegalArgumentException
          }
        case CPop(segment, index) =>
          println(f"\tpop \t$segment%-8s $index%-4s")
          print("\t" + ("-"*20))
          segment match {
            case "static"   => pop(static, index)
            case "local"    => pop(local,  index)
            case "argument" => pop(arg,    index)
            case "this"     => pop(`this`, index)
            case "that"     => pop(that,   index)
            case "temp"     => pop(temp,   index)
            case "pointer"  =>
              index match {
                case 0 => `this`(pop())
                case 1 => that(pop())
                case _ => throw new IllegalArgumentException
              }
            case _ => throw new IllegalArgumentException
          }
        case Add =>
          print("\tadd\t\t\t\t\t")
          val x = pop()
          Writer.emit(
          s"""
          |    @R5
          |    M=D    // store x
          """.stripMargin
          )
          val y = pop()
          Writer.emit(
          s"""
          |    @R5
          |    D=D+M    // add x + y
          """.stripMargin
          )
          push(x + y)
        case Sub =>
          print("\tsub\t\t\t\t\t")
          val y = pop()
          Writer.emit(
          s"""
          |    @R5
          |    M=D    // store y
          """.stripMargin
          )
          val x = pop()
          Writer.emit(
          s"""
             |    @R5
             |    D=D-M    // add x - y
          """.stripMargin
          )
          push(x - y)
        case Neg =>
          print("\tneg \t\t\t\t\t")
          push(-pop())
        case And =>
          print("\tand\t\t\t\t\t")
          push(pop() & pop())
        case Or =>
          print("\tor  \t\t\t\t\t")
          push(pop() | pop())
        case Not =>
          print("\tnot \t\t\t\t\t")
          push(~pop())
        case Eq =>
          print("\teq  \t\t\t\t\t")
          push(bool(pop() == pop()))
        case Lt =>
          print("\tlt  \t\t\t\t\t")
          push(bool(pop() > pop()))
        case Gt =>
          print("\tgt  \t\t\t\t\t")
          push(bool(pop() < pop()))
        case Print(n) =>
          print(Console.RED)
          print(s"\t@$n: ${RAM(n)}\t\t\t")
      }
      print(Console.BLUE)
      println()
      showStack()
      print(Console.WHITE)
      showStatics(20)
      println()
      showRegisters()
      println()
      println()
      cpu(ast.tail)
    }
  }

  def showRAM(r: Range) = r map { i => f"${RAM(i)}%04d" } mkString("[", ", ", "]")
  def showStack() = println(f"\tstack    : ${showRAM(256 to (if (sp < 2047) sp - 1 else 2047))}%-16s")
  def showStatics(n: Short = 240) = print(s"\tstatics  : ${showRAM((16 to 255).take(n))}")
  def showRegisters() = print(f"\tregisters: ${showRAM(0 to 16)}")
  def process(ast: List[Command]) = cpu(ast)
}
