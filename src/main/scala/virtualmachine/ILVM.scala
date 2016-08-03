package virtualmachine

import virtualmachine.AST._
import annotation.tailrec
import collection.mutable
import language.postfixOps

class ILVM {

  val LOG = true

  var RAM  = new Array[Word](16384 + 8192 + 1) // 16k + 8k + 1reg
  var ROM  = new Array[Word](32768) // 32k RESERVED

  object Writer {
    def emit(s: String) = if (LOG) print(s"${Console.CYAN}$s")
    def emitLn() = emit("\n")
    def emitRule() = emit(s"\t${Console.YELLOW}" + ("-"*24))
  }

  // TODO: configure these in constructor
  RAM(0) = 256
  RAM(1) = 300
  RAM(2) = 400
  RAM(3) = 3000
  RAM(4) = 3010

  RAM(400) = 3

  val labels = mutable.Map[String, Short]() // used for branching

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

  @tailrec private def cpu(ast: List[Command], ip: Short = 0): Short = {
    if (ip < ast.size) {
      var nextip = ip + 1
      ast(ip) match {
        case Push(segment, index) =>
          Writer.emit(f"\t${Console.YELLOW}push\t$segment%-8s $index%-4s\n")
          Writer.emitRule()
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
          Writer.emit(f"\t${Console.YELLOW}pop \t$segment%-8s $index%-4s\n")
          Writer.emitRule()
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
        case Label(value) =>
          Writer.emit(f"\t($value)\n")
          Writer.emitRule()
          labels(value) = ip + 1
        case If(label) =>
          Writer.emit(f"\t${Console.YELLOW}if-goto \t$label%-8s\n")
          Writer.emitRule()
          nextip = if (pop() != 0.toShort) labels(label) else ip + 1
        case Goto(label) =>
          Writer.emit(f"\t${Console.YELLOW}goto \t$label%-8s\n")
          Writer.emitRule()
          nextip = labels(label)
        case Call(name, argc) =>
          println(f"\t${Console.YELLOW}call \t$name%-8s $argc%-4s")
          Writer.emitRule()
        case Function(name, argc) =>
          Writer.emit(f"\t${Console.YELLOW}function \t$name%-8s $argc%-4s\n")
          Writer.emitRule()
        case Return =>
          Writer.emit(f"\t${Console.YELLOW}return\n")
          Writer.emitRule()
        case Print(n) =>
          Writer.emit(s"\t${Console.RED} @$n: ${RAM(n)}\t\t\t")
      }
      Writer.emit(Console.BLUE)
      Writer.emitLn()
      showStack()
      Writer.emit(Console.WHITE)
      showStatics(20)
      Writer.emitLn()
      showRegisters()
      Writer.emitLn()
      Writer.emitLn()
      cpu(ast, nextip)
    } else 0
  }

  private def gt() = {
    Writer.emit(s"\t${Console.YELLOW}gt\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(bool(x < y))
  }

  private def lt() = {
    Writer.emit(s"\t${Console.YELLOW}lt\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(bool(x > y))
  }

  private def eq() = {
    Writer.emit(s"\t${Console.YELLOW}eq\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(bool(x - y == 0))
  }

  private def neg() = {
    Writer.emit(s"\t${Console.YELLOW}neg\n")
    Writer.emitRule()
    val x = pop()
    push(-x)
  }

  private def and() = {
    Writer.emit(s"\t${Console.YELLOW}and\n")
    Writer.emitRule()
    val x = pop()
    val y = pop()
    push(x & y)
  }

  private def not() = {
    Writer.emit(s"\t${Console.YELLOW}not\n")
    Writer.emitRule()
    val D = pop()
    push(~D)
  }

  private def or() = {
    Writer.emit(s"\t${Console.YELLOW}or\n")
    Writer.emitRule()
    val x = pop()
    val y = pop()
    push(x | y)
  }

  private def sub() = {
    Writer.emit(s"\t${Console.YELLOW}sub\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(x - y)
  }

  private def add() = {
    Writer.emit(s"\t${Console.YELLOW}add\n")
    Writer.emitRule()
    val x = pop()
    val y = pop()
    push(x + y)
  }

  def showRAM(r: Range) = r map { i => f"${RAM(i)}%04d" } mkString("[", ", ", "]")
  def showStack() = Writer.emit(f"\tstack    : ${showRAM(256 to (if (sp < 2047) sp - 1 else 2047))}%-16s\n")
  def showStatics(n: Short = 240) = Writer.emit(s"\tstatics  : ${showRAM((16 to 255).take(n))}")
  def showRegisters() = Writer.emit(f"\tregisters: ${showRAM(0 to 16)}")
  def process(ast: List[Command]) = cpu(ast)
}
