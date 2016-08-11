package virtualmachine

import java.io.PrintWriter
import virtualmachine.AST._
import scala.annotation.tailrec
import scala.language.postfixOps

class Translator(namespace: String = "global")(implicit f: PrintWriter) {

  private object Util {
    private var guid = 0
    def getId = {
      val nextGUID = s"$namespace.$guid"
      guid += 1
      nextGUID
    }
  }

  private object Writer {
    def emit(s: String)(implicit f: PrintWriter) = f.write(s)
    def emitRule() = emit("//\t" + ("-"*20))
    def storeD(implicit f: PrintWriter) = {
      emit(
      """
      |    @R13
      |    M=D     // store y
      """ stripMargin
      )
    }
  }

  object Registers extends Enumeration {
    val SP   = Value("SP")
    val LCL  = Value("LCL")
    val ARG  = Value("ARG")
    val THIS = Value("THIS")
    val THAT = Value("THAT")
  }

  private def base(reg: Registers.Value) = {
    Writer emit
    s"""
    @$reg
    D=M
    @R13
    M=D
    """
  }

  private def updateThis() = {
    Writer emit
    """
    @THIS
    M=D
    """
  }

  private def updateThat() = {
    Writer emit
    """
    @THAT
    M=D
    """
  }

  private def baseTemp() = {
    Writer emit
    """
    @5
    D=A
    @R13
    M=D
    """
  }

  private def pop(): Unit = {
    Writer emit
    """
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    """
  }

  private def push(): Unit = {
    Writer emit
    """
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    """
  }

  private def push(index: Word): Unit = {
    Writer emit
    s"""
    //======= push(bp, index) =======//
    @R13    // used for bp
    D=M
    @$index
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    """
    push()
  }

  private def pop(index: Word): Unit = {
    pop()
    Writer emit
    s"""
    //======= pop(R13) =======//
    @R14    // store popped value
    M=D
    @R13    // used for bp
    D=M
    @$index
    D=D+A   // 'A' register used for index
    @R13    // overwrite bp
    M=D     // sum of bp + index
    @R14
    D=M     // popped value
    @R13
    A=M     // grab RAM pointer
    M=D     // store value into heap
    """
  }

  private def popStatic(index: Word): Unit = {
    Writer emit
    s"""
    @$namespace.$index
    D=A
    @R13
    M=D
    """
    pop(0) // TODO: expand this to avoid addition
  }

  private def pushStatic(index: Word): Unit = {
    Writer emit
    s"""
    @$namespace.$index
    D=A
    @R13
    M=D
    """
    push(0) // TODO: expand this to avoid subtraction
  }


  def pushConstant(n: Short) = {
    Writer emit
    s"""
    @$n
    D=A     // store constant
    """
    push()
  }

  private def setLabel(value: String) = {
    Writer emit s"\n($value)\n"
  }

  def goto(address: String) = {
    Writer emit f"\n//\tgoto \t$address%-8s\n"
    Writer emitRule()
    Writer emit
    s"""
    @$address
    0; JMP
    """
  }

  private def ifGoto(address: String) = {
    Writer emit f"\n//\tgoto \t$address%-8s\n"
    Writer emitRule()
    pop()
    Writer emit
    s"""
    @$address
    D; JNE  // jump if D is not zero
    """
  }

  def bootstrap() = {
    Writer emit
    s"""
    @256
    D=A
    @SP
    M=D
    """
    call("Sys.init", 0)
    Writer emit
    s"""
    (sys.end.loop)
    @sys.end.loop
    0; JMP
    """
  }

  private def call(function: String, argc: Short) = {
    Writer emit f"\n//\tcall \t$function%-8s $argc%-4s\n"
    Writer emitRule()
    val returnAddress = s"return.${Util.getId}"
    Writer emit
    s"""
    @$returnAddress
    D=A
    """
    push()
    Writer emit
    """
    @LCL
    D=M
    """
    push()
    Writer emit
    """
    @ARG
    D=M
    """
    push()
    Writer emit
    """
    @THIS
    D=M
    """
    push()
    Writer emit
    """
    @THAT
    D=M
    """
    push()
    Writer emit
    s"""
    @SP     // ARG = SP - $argc - 5
    D=M
    @$argc
    D=D-A
    @5      // offset stacked env
    D=D-A
    @ARG
    M=D
    """
    Writer emit
    s"""
    @SP     // LCL = SP
    D=M
    @LCL
    M=D
    """
    goto(function)
    setLabel(returnAddress)
  }

  def function(name: String, argc: Short) = {
    Writer emit f"\n//\tfunction \t$name%-8s $argc%-4s\n"
    Writer emitRule()
    setLabel(name)
    (0 until argc).foreach { _ => pushConstant(0) }
  }

  private def ret() = {
    Writer emit f"\n//\treturn\n"
    Writer emitRule()
    Writer emit
    """
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
    """
    pop()
    Writer emit
    """
    @ARG
    A=M     // dereference
    M=D     // replace first arg with return value
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
    """
  }

  private def gt() = {
    Writer emit "\n//\tgt\n"
    Writer emitRule()
    pop()
    Writer.storeD
    pop()
    val isGreaterThanLabel = s"is.greater.than.${Util.getId}"
    val isNotGreaterThanLabel = s"is.not.greater.than.${Util.getId}"
    val endIsGreaterThanLabel = s"end.is.greater.than.${Util.getId}"
    Writer emit
    s"""
    @R13
    D=D-M   // add x - y
    @$isGreaterThanLabel
    D; JGT

    ($isNotGreaterThanLabel)
    D=0
    @$endIsGreaterThanLabel
    0; JMP

    ($isGreaterThanLabel)
    D=-1

    ($endIsGreaterThanLabel)
    """
    push()
  }

  private def lt() = {
    Writer emit "\n//\tlt\n"
    Writer emitRule()
    pop()
    Writer.storeD
    pop()
    val isLessThanLabel = s"is.less.than.${Util.getId}"
    val isNotLessThanLabel = s"is.not.less.than.${Util.getId}"
    val endIsLessThanLabel = s"end.is.less.than.${Util.getId}"
    Writer emit
    s"""
    @R13
    D=D-M   // add x - y
    @$isLessThanLabel
    D; JLT

    ($isNotLessThanLabel)
    D=0
    @$endIsLessThanLabel
    0; JMP

    ($isLessThanLabel)
    D=-1

    ($endIsLessThanLabel)
    """
    push()
  }

  private def eq() = {
    Writer emit "\n//\teq\n"
    Writer emitRule()
    pop()
    Writer.storeD
    pop()
    val isEqualLabel = s"is.equal.${Util.getId}"
    val isNotEqualLabel = s"is.not.equal.${Util.getId}"
    val endIsEqualLabel = s"end.is.equal.${Util.getId}"
    Writer emit
    s"""
    @R13
    D=D-M   // add x - y
    @$isEqualLabel
    D; JEQ

    ($isNotEqualLabel)
    D=0
    @$endIsEqualLabel
    0; JMP

    ($isEqualLabel)
    D=-1

    ($endIsEqualLabel)
    """
    push()
  }

  private def neg() = {
    Writer emit "\n//\tneg\n"
    Writer emitRule()
    pop()
    Writer emit
    """
    D=-D    // store -x
    """
    push()
  }

  private def and() = {
    Writer emit "\n//\tand\n"
    Writer emitRule()
    pop()
    Writer.storeD
    pop()
    Writer emit
    """
    @R13
    D=D&M   // add x & y
    """
    push()
  }

  private def not() = {
    Writer.emit("\n//\tnot\n")
    Writer.emitRule()
    pop()
    Writer emit
    s"""
    D=-D
    D=D-1   // store ~x
    """
    push()
  }

  private def or() = {
    Writer.emit("\n//\tor\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    Writer emit
    """
    @R13
    D=D|M   // add x | y
    """
    push()
  }

  private def sub() = {
    Writer emit "\n//\tsub\n"
    Writer emitRule()
    pop()
    Writer.storeD
    pop()
    Writer emit
    """
    @R13
    D=D-M   // add x - y
    """
    push()
  }

  private def add() = {
    Writer emit "\n//\tadd\n"
    Writer emitRule()
    pop()
    Writer.storeD
    pop()
    Writer emit
    """
    @R13
    D=D+M   // add x + y
    """
    push()
  }

  private def bool(condition: Boolean): Short = { if (condition) 0xFFFF else 0x0000 }

  @tailrec private def cpu(ast: List[Command]): Unit = {
    if (ast.nonEmpty) {
      print(Console.YELLOW)
      ast.head match {
        case Push(segment, index) =>
          Writer emit f"\n//\tpush\t$segment%-8s $index%-4s\n"
          Writer emitRule()
          import Registers._
          segment match {
            case "constant" => pushConstant(index)
            case "static"   => pushStatic(index)
            case "local"    => base(LCL);  push(index)
            case "argument" => base(ARG);  push(index)
            case "this"     => base(THIS); push(index)
            case "that"     => base(THAT); push(index)
            case "temp"     => baseTemp(); push(index)
            case "pointer"  =>
              index match {
                case 0 => base(THIS); push()
                case 1 => base(THAT); push()
                case _ => throw new IllegalArgumentException
              }
            case _ => throw new IllegalArgumentException
          }
        case Pop(segment, index) =>
          Writer emit f"\n//\tpop \t$segment%-8s $index%-4s\n"
          Writer emitRule()
          import Registers._
          segment match {
            case "static"   => popStatic(index)
            case "local"    => base(LCL);  pop(index)
            case "argument" => base(ARG);  pop(index)
            case "this"     => base(THIS); pop(index)
            case "that"     => base(THAT); pop(index)
            case "temp"     => baseTemp(); pop(index)
            case "pointer"  =>
              index match {
                case 0 => pop(); updateThis()
                case 1 => pop(); updateThat()
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
        case Label(value) => setLabel(s"$namespace.$value")
        case If(label) => ifGoto(s"$namespace.$label")
        case Goto(label) => goto(s"$namespace.$label")
        case Call(name, argc) => call(name, argc)
        case Function(name, argc) => function(name, argc)
        case Return => ret()
      }
      cpu(ast.tail)
    }
  }

  def process(ast: List[Command]): Unit = cpu(ast)
}
