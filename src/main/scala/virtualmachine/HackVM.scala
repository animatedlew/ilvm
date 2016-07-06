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

  RAM(0) = 256
  RAM(1) = 300
  RAM(2) = 400
  RAM(3) = 3000
  RAM(4) = 3010

  // temp    R05 - R12
  // general R13 - R15

  def sp                  = RAM(0)
  def sp(value: Word)     = RAM(0) = value
  def local               = RAM(1)
  def local(value: Word)  = RAM(1) = value
  def arg                 = RAM(2)
  def arg(value: Word)    = RAM(2) = value
  def `this`              = RAM(3)
  def `this`(value: Word) = RAM(3) = value
  def that                = RAM(4)
  def that(value: Word)   = RAM(4) = value
  val static = 16
  val temp   = 5

  private def pop(): Word = { sp(sp - 1); RAM(sp) }

  private def push(value: Word): Unit = { RAM(sp) = value; sp(sp + 1) }

  private def push(bp: Word, index: Word): Unit = { push(RAM(bp + index)) }

  private def pop(bp: Word, index: Word): Unit = { RAM(bp + index) = pop() }

  private def bool(condition: Boolean): Short = { if (condition) 0xFF else 0x00 }

  @tailrec private def cpu(ast: List[Command]): Unit = {
    if (ast.nonEmpty) {
      print(Console.YELLOW)
      ast.head match {
        case CPush(segment, index) =>
          print(f"\tpush\t$segment%-8s $index%-4s")
          segment match {
            case "constant" => push(index)
            case "static"   => push(index, static)

            case "local"    => push(local,  index)
            case "argument" => push(arg,    index)
            case "this"     => push(`this`, index)
            case "that"     => push(that,   index)
            case "temp"     => push(temp,   index)
            case "pointer"  =>
              index match {
                case 0 => push(`this`)
                case 1 => push(that)
                case _ => push(RAM(3 + index)) // leak
              }
            case _ => throw new IllegalArgumentException
          }
        case CPop(segment, index) =>
          print(f"\tpop \t$segment%-8s $index%-4s")
          segment match {
            case "static"   => pop(index, static)

            case "local"    => pop(local,  index)
            case "argument" => pop(arg,    index)
            case "this"     => pop(`this`, index)
            case "that"     => pop(that,   index)
            case "temp"     => pop(temp,   index)
            case "pointer"  =>
              index match {
                case 0 => `this`(pop())
                case 1 => that(pop())
                case _ => RAM(3 + index) = pop() // leak
              }
            case _ => throw new IllegalArgumentException
          }
        case Add =>
          print("\tadd\t\t\t\t\t")
          push(pop() + pop())
        case Sub =>
          print("\tsub\t\t\t\t\t")
          val y = pop()
          val x = pop()
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
      showStack()
      print(Console.WHITE)
      showStatics(20)
      println()
      showRegisters()
      println()
      cpu(ast.tail)
    }
  }

  def showRAM(r: Range) = r map { i => f"${RAM(i)}%04d" } mkString("[", ", ", "]")
  def showStack() = println(f"\tstack: ${showRAM(256 to (if (sp < 2047) sp - 1 else 2047))}%-16s")
  def showStatics(n: Short = 240) = print(s"\tstatics  : ${showRAM((16 to 255).take(n))}")
  def showRegisters() = print(f"\tregisters: ${showRAM(0 to 16)}")
  def process(ast: List[Command]) = cpu(ast)
}
