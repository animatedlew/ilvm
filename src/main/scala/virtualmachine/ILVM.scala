package virtualmachine

import virtualmachine.AST._
import annotation.tailrec
import collection.mutable
import language.postfixOps

class ILVM(val LOG: Boolean = true) {

  private val RAM = new Array[Word](16384 + 8192 + 1) // 16k + 8k + 1reg

  private object Writer {
    def emit(s: String) = if (LOG) print(s"${Console.CYAN}$s")
    def emitLn() = emit("\n")
    def emitRule() = emit(s"\t${Console.YELLOW}" + ("-"*24))
  }

  private val labels = mutable.Map[String, Short]() // used for branching
  private val functions = mutable.Map[String, Short]()

  private def sp = RAM(0)
  private def sp(value: Word) = RAM(0) = value

  private def lcl = RAM(1)
  private def lcl(value: Word) = RAM(1) = value

  private def arg = RAM(2)
  private def arg(value: Word) = RAM(2) = value

  private def getThis = RAM(3)
  private def setThis(value: Word) = { RAM(3) = value }

  private def getThat = RAM(4)
  private def setThat(value: Word) = { RAM(4) = value }

  private def static = 16
  private def temp = 5

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

  private def _goto(label: String): Word = {
    Writer emit f"\t${Console.YELLOW}goto \t$label%-8s\n"
    Writer emitRule()
    labels(label)
  }

  private def _if(ip: Word, label: String): Int = {
    Writer emit f"\t${Console.YELLOW}if-goto \t$label%-8s\n"
    Writer emitRule()
    if (pop() != 0.toShort) labels(label) else ip + 1
  }

  private def _label(ip: Word, value: String): Unit = {
    Writer emit f"\t($value)\n"
    Writer emitRule()
    labels(value) = ip + 1
  }

  private def _call(ip: Word, name: String, argc: Word): Word = {
    Writer emit f"\t${Console.YELLOW}call \t$name%-8s $argc%-4s\n"
    Writer emitRule()
    push(ip + 1)
    push(lcl)
    push(arg)
    push(getThis)
    push(getThat)
    arg(sp - argc - 5)
    lcl(sp)
    functions(name)
  }

  private def _function(name: String, argc: Word): Unit = {
    Writer.emit(f"\t${Console.YELLOW}function \t$name%-8s $argc%-4s\n")
    Writer.emitRule()
    (0 until argc).foreach { _ => push(lcl, 0) }
  }

  private def _return: Word = {
    Writer.emit(f"\t${Console.YELLOW}return\n")
    Writer.emitRule()
    val frame = lcl
    val nextip = RAM(frame - 5)
    RAM(arg) = pop()
    sp(arg + 1)
    setThat(RAM(frame - 1))
    setThis(RAM(frame - 2))
    arg(RAM(frame - 3))
    lcl(RAM(frame - 4))
    nextip
  }

  private def _gt() = {
    Writer.emit(s"\t${Console.YELLOW}gt\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(bool(x < y))
  }

  private def _lt() = {
    Writer.emit(s"\t${Console.YELLOW}lt\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(bool(x > y))
  }

  private def _eq() = {
    Writer.emit(s"\t${Console.YELLOW}eq\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(bool(x - y == 0))
  }

  private def _neg() = {
    Writer.emit(s"\t${Console.YELLOW}neg\n")
    Writer.emitRule()
    push(-pop())
  }

  private def _and() = {
    Writer.emit(s"\t${Console.YELLOW}and\n")
    Writer.emitRule()
    push(pop() & pop())
  }

  private def _not() = {
    Writer.emit(s"\t${Console.YELLOW}not\n")
    Writer.emitRule()
    push(~pop())
  }

  private def _or() = {
    Writer.emit(s"\t${Console.YELLOW}or\n")
    Writer.emitRule()
    push(pop() | pop())
  }

  private def _sub() = {
    Writer.emit(s"\t${Console.YELLOW}sub\n")
    Writer.emitRule()
    val y = pop()
    val x = pop()
    push(x - y)
  }

  private def _add() = {
    Writer.emit(s"\t${Console.YELLOW}add\n")
    Writer.emitRule()
    push(pop() + pop())
  }

  def showRAM(r: Range) = r map { i => f"${RAM(i)}%04d" } mkString("[", ", ", "]")
  def showStack() = Writer.emit(f"\tstack    : ${showRAM(256 to (if (sp < 2047) sp - 1 else 2047))}%-16s\n")
  def showStatics(n: Short = 240) = Writer.emit(s"\tstatics  : ${showRAM((16 to 255).take(n))}")
  def showRegisters() = Writer.emit(f"\tregisters: ${showRAM(0 to 16)}")

  private def labelPass(ast: List[Command]) = {
    var count = 0
    // remove labels and store them in a map
    val cachedLabels = ast.zipWithIndex.filterNot { case (command, index) =>
      command match {
        case Label(value) =>
          labels(value) = index - count
          count += 1
          true
        case n => false
      }
    }.map { _._1 }.toVector

    // store address of each function
    cachedLabels.zipWithIndex.foreach { case (command, index) =>
      command match {
        case Function(name, argc) =>
          functions(name) = index
        case _ =>
      }
    }
    cachedLabels
  }

  @tailrec private def cpu(ROM: Vector[Command], ip: Short = 0): Short = {
    if (ip < ROM.size) {
      var nextip: Word = ip + 1
      ROM(ip) match {
        case Push(segment, index) =>
          Writer.emit(f"\t${Console.YELLOW}push\t$segment%-8s $index%-4s\n")
          Writer.emitRule()
          segment match {
            case "constant" => push(index)
            case "static"   => push(static, index)
            case "local"    => push(lcl, index)
            case "argument" => push(arg, index)
            case "this"     => push(getThis, index)
            case "that"     => push(getThat, index)
            case "temp"     => push(temp, index)
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
            case "static"   => pop(static, index)
            case "local"    => pop(lcl, index)
            case "argument" => pop(arg, index)
            case "this"     => pop(getThis, index)
            case "that"     => pop(getThat, index)
            case "temp"     => pop(temp, index)
            case "pointer"  =>
              index match {
                case 0 => setThis(pop())
                case 1 => setThat(pop())
                case _ => throw new IllegalArgumentException
              }
            case _ => throw new IllegalArgumentException
          }
        case Add => _add()
        case Sub => _sub()
        case Neg => _neg()
        case And => _and()
        case Or  => _or()
        case Not => _not()
        case Eq  => _eq()
        case Lt  => _lt()
        case Gt  => _gt()
        case Label(value) => _label(ip, value)
        case If(label) => nextip = _if(ip, label)
        case Goto(label) if label == "Sys.end" => return 0
        case Goto(label) => nextip = _goto(label)
        case Call(name, argc) => nextip = _call(ip, name, argc)
        case Function(name, argc) => _function(name, argc)
        case Return => nextip = _return
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
      cpu(ROM, nextip)
    } else 0
  }

  def process(ast: List[Command]) = {
    RAM(0) = 256
    val header = List(Call("Sys.init", 0), Label("Sys.end"), Goto("Sys.end"))
    val ROM = labelPass(header ++ ast)
    cpu(ROM)
  }
}
