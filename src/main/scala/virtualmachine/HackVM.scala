package virtualmachine

import virtualmachine.AST._

import scala.annotation.tailrec
import scala.language.postfixOps

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

  def getLocal = RAM(1)

  //def local(value: Word) = RAM(1) = value

  def getArg = RAM(2)

  //def arg(value: Word) = RAM(2) = value

  def getThis = RAM(3)

  def setThis(value: Word) = { RAM(3) = value }

  def getThat = RAM(4)

  def setThat(value: Word) = { RAM(4) = value }

  def getStatic = 16 // TODO: make this configurable
  def getTemp = 5

  private def pop(): Word = {
    sp(sp - 1)
    RAM(sp)
  }

  private def push(value: Word): Unit = {
    RAM(sp) = value
    sp(sp + 1)
  }

  private def push(bp: Word, index: Word): Unit = {
    val D = RAM(bp + index)
    push(D)
  }

  private def pop(bp: Word, index: Word): Unit = {
    val D = pop()
    RAM(bp + index) = D
  }

  private def bool(condition: Boolean): Short = { if (condition) 0xFFFF else 0x0000 }

  @tailrec private def cpu(ast: List[Command]): Unit = {
    if (ast.nonEmpty) {
      print(Console.YELLOW)
      ast.head match {
        case Push(segment, index) =>
          println(f"\tpush\t$segment%-8s $index%-4s")
          print("\t" + ("-"*20))
          segment match {
            case "constant" => push(index)
            case "static"   => push(getStatic, index)
            case "local"    => push(getLocal,  index)
            case "argument" => push(getArg,    index)
            case "this"     => push(getThis,   index)
            case "that"     => push(getThat,   index)
            case "temp"     => push(getTemp,   index)
            case "pointer"  =>
              index match {
                case 0 => push(getThis)
                case 1 => push(getThat)
                case _ => throw new IllegalArgumentException
              }
            case _ => throw new IllegalArgumentException
          }
        case Pop(segment, index) =>
          println(f"\tpop \t$segment%-8s $index%-4s")
          print("\t" + ("-"*20))
          segment match {
            case "static"   => pop(getStatic, index)
            case "local"    => pop(getLocal,  index)
            case "argument" => pop(getArg,    index)
            case "this"     => pop(getThis,   index)
            case "that"     => pop(getThat,   index)
            case "temp"     => pop(getTemp,   index)
            case "pointer"  =>
              index match {
                case 0 => setThis(pop())
                case 1 => setThat(pop())
                case _ => throw new IllegalArgumentException
              }
            case _ => throw new IllegalArgumentException
          }
        case Add => add()
        case Sub => sub()
        case Neg => neg()
        case And => and()
        case Or  => or()
        case Not => not()
        case Eq  => eq()
        case Lt  => lt()
        case Gt  => gt()
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

  private def gt() = {
    print("\tgt  \t\t\t\t\t")
    val y = pop()
    val x = pop()
    push(bool(x < y))
  }

  private def lt() = {
    print("\tlt  \t\t\t\t\t")
    val y = pop()
    val x = pop()
    push(bool(x > y))
  }

  private def eq() = {
    print("\teq  \t\t\t\t\t")
    val y = pop()
    val x = pop()
    push(bool(x - y == 0))
  }

  private def neg() = {
    print("\tneg \t\t\t\t\t")
    val x = pop()
    push(-x)
  }

  private def and() = {
    print("\tand\t\t\t\t\t")
    val x = pop()
    val y = pop()
    push(x & y)
  }

  private def not() = {
    print("\tnot \t\t\t\t\t")
    val D = pop()
    push(~D)
  }

  private def or() = {
    print("\tor  \t\t\t\t\t")
    val x = pop()
    val y = pop()
    push(x | y)
  }

  private def sub() = {
    print("\tsub\t\t\t\t\t")
    val y = pop()
    val x = pop()
    push(x - y)
  }

  private def add() = {
    print("\tadd\t\t\t\t\t")
    val x = pop()
    val y = pop()
    push(x + y)
  }

  def showRAM(r: Range) = r map { i => f"${RAM(i)}%04d" } mkString("[", ", ", "]")
  def showStack() = println(f"\tstack    : ${showRAM(256 to (if (sp < 2047) sp - 1 else 2047))}%-16s")
  def showStatics(n: Short = 240) = print(s"\tstatics  : ${showRAM((16 to 255).take(n))}")
  def showRegisters() = print(f"\tregisters: ${showRAM(0 to 16)}")
  def process(ast: List[Command]) = cpu(ast)
}
