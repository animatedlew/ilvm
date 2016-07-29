package virtualmachine

import java.io.PrintWriter

import virtualmachine.AST._

import scala.annotation.tailrec
import scala.language.postfixOps

class Translator(namespace: String = "global")(implicit f: PrintWriter) {

  object Util {
    private var guid = 0
    private var muid = 0
    def getId = {
      val nextGUID = s"$namespace.$guid"
      guid += 1
      nextGUID
    }
    def getId(method: String) = {
      val nextGUID = s"$namespace.$method.$muid"
      muid += 1
      nextGUID
    }
  }

  object Writer {
    def emit(s: String)(implicit f: PrintWriter) = f.write(s)
    def emitRule() = emit("//\t" + ("-"*20))
    def storeD(implicit f: PrintWriter) = {
      emit(
        """
         |    @R5
         |    M=D     // store y
        """ stripMargin
      )
    }
  }

  var static = 16 // TODO: make this configurable

  def getLocal() = {
    Writer.emit(
    """
    @R1
    D=M
    @R5
    M=D
    """
    )
  }

  //def local(value: Word) = RAM(1) = value

  def getArg() = {
    Writer.emit(
    """
    @R2
    D=M
    @R5
    M=D
    """
    )
  }

  //def arg(value: Word) = RAM(2) = value

  def getThis() = {
    Writer.emit(
    """
    @R3
    D=M
    @R5
    M=D
    """
    )
  }

  def setThis() = {
    Writer.emit(
    """
    @R3
    M=D
    """
    )
  }

  def getThat() = {
    Writer.emit(
    """
    @R4
    D=M
    @R5
    M=D
    """
    )
  }

  def setThat() = {
    Writer.emit(
    """
    @R4
    M=D
    """
    )
  }

  def getStatic() = {
    Writer.emit(
    s"""
    @$static
    D=A
    @R5
    M=D
    """
    )
  }
  def getTemp() = {
    Writer.emit(
    """
    @5
    D=A
    @R5
    M=D
    """
    )
  }

  private def pop(): Unit = {
    Writer.emit(
    """
    //======= pop() =======//
    @SP
    AM=M-1  // dec stack/memory pointer
    D=M     // return value in 'D'
    """
    )
  }

  private def push(): Unit = {
    Writer.emit(
    """
    //======= push(D) =======//
    @SP
    A=M     // access pointer
    M=D     // store value in stack @ 'A'
    @SP
    M=M+1   // inc stack pointer
    """
    )
  }

  private def push(index: Word): Unit = {
    Writer.emit(
    s"""
    //======= push(bp, index) =======//
    @R5     // used for bp
    D=M
    @$index
    D=D+A   // 'A' register used for index
    @R5     // overwrite bp
    A=D     // sum of bp + index
    D=M     // get value @ pointer
    """
    )
    push()
  }

  private def pop(index: Word): Unit = {
    pop()
    Writer.emit(
    s"""
    //======= pop(R5, R6) =======//
    @R7     // store popped value
    M=D
    @R5     // used for bp
    D=M
    @$index
    D=D+A   // 'A' register used for index
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

  private def bool(condition: Boolean): Short = { if (condition) 0xFFFF else 0x0000 }

  @tailrec private def cpu(ast: List[Command]): Unit = {
    if (ast.nonEmpty) {
      print(Console.YELLOW)
      ast.head match {
        case Push(segment, index) =>
          Writer.emit(f"\n//\tpush\t$segment%-8s $index%-4s\n")
          Writer.emitRule()
          segment match {
            case "constant" =>
              Writer.emit(
                s"""
                |    @$index
                |    D=A     // store constant
                """.stripMargin
              )
              push()
            case "static"   => getStatic(); push(index)
            case "local"    => getLocal();  push(index)
            case "argument" => getArg();    push(index)
            case "this"     => getThis();   push(index)
            case "that"     => getThat();   push(index)
            case "temp"     => getTemp();   push(index)
            case "pointer"  =>
              index match {
                case 0 => getThis(); push()
                case 1 => getThat();   push()
                case _ => throw new IllegalArgumentException
              }
            case _ => throw new IllegalArgumentException
          }
        case Pop(segment, index) =>
          Writer.emit(f"\n//\tpop \t$segment%-8s $index%-4s\n")
          Writer.emitRule()

          segment match {
            case "static"   => getStatic(); pop(index)
            case "local"    => getLocal();  pop(index)
            case "argument" => getArg();    pop(index)
            case "this"     => getThis();   pop(index)
            case "that"     => getThat();   pop(index)
            case "temp"     => getTemp();   pop(index)
            case "pointer"  =>
              index match {
                case 0 => pop(); setThis()
                case 1 => pop(); setThat()
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
      }
      cpu(ast.tail)
    }
  }

  private def gt() = {
    Writer.emit("\n//\tgt\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    val isGreaterThanLabel = s"is.greater.than.${Util.getId}"
    val isNotGreaterThanLabel = s"is.not.greater.than.${Util.getId}"
    val endIsGreaterThanLabel = s"end.is.greater.than.${Util.getId}"
    Writer.emit(
      s"""
      |    @R5
      |    D=D-M    // add x - y
      |    @$isGreaterThanLabel
      |    D; JGT
      |
      |($isNotGreaterThanLabel)
      |    D=0
      |    @$endIsGreaterThanLabel
      |    0; JMP
      |
      |($isGreaterThanLabel)
      |    D=-1
      |
      |($endIsGreaterThanLabel)
      """ stripMargin
    )
    push()
  }

  private def lt() = {
    Writer.emit("\n//\tlt\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    val isLessThanLabel = s"is.less.than.${Util.getId}"
    val isNotLessThanLabel = s"is.not.less.than.${Util.getId}"
    val endIsLessThanLabel = s"end.is.less.than.${Util.getId}"
    Writer.emit(
      s"""
      |    @R5
      |    D=D-M    // add x - y
      |    @$isLessThanLabel
      |    D; JLT
      |
      |($isNotLessThanLabel)
      |    D=0
      |    @$endIsLessThanLabel
      |    0; JMP
      |
      |($isLessThanLabel)
      |    D=-1
      |
      |($endIsLessThanLabel)
      """ stripMargin
    )
    push()
  }

  private def eq() = {
    Writer.emit("\n//\teq\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    val isEqualLabel = s"is.equal.${Util.getId}"
    val isNotEqualLabel = s"is.not.equal.${Util.getId}"
    val endIsEqualLabel = s"end.is.equal.${Util.getId}"
    Writer.emit(
      s"""
      |    @R5
      |    D=D-M    // add x - y
      |    @$isEqualLabel
      |    D; JEQ
      |
      |($isNotEqualLabel)
      |    D=0
      |    @$endIsEqualLabel
      |    0; JMP
      |
      |($isEqualLabel)
      |    D=-1
      |
      |($endIsEqualLabel)
      """ stripMargin
    )
    push()
  }

  private def neg() = {
    Writer.emit("\n//\tneg\n")
    Writer.emitRule()
    pop()
    Writer.emit(
      """
      |    D=-D    // store -x
      """ stripMargin
    )
    push()
  }

  private def and() = {
    Writer.emit("\n//\tand\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    Writer.emit(
      s"""
      |    @R5
      |    D=D&M    // add x & y
      """ stripMargin
    )
    push()
  }

  private def not() = {
    Writer.emit("\n//\tnot\n")
    Writer.emitRule()
    pop()
    Writer.emit(
      s"""
      |    D=-D
      |    D=D-1    // store ~x
      """ stripMargin
    )
    push()
  }

  private def or() = {
    Writer.emit("\n//\tor\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    Writer.emit(
      """
      |    @R5
      |    D=D|M    // add x | y
      """ stripMargin
    )
    push()
  }

  private def sub() = {
    Writer.emit("\n//\tsub\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    Writer.emit(
      """
      |    @R5
      |    D=D-M    // add x - y
      """ stripMargin
    )
    push()
  }

  private def add() = {
    Writer.emit("\n//\tadd\n")
    Writer.emitRule()
    pop()
    Writer.storeD
    pop()
    Writer.emit(
      s"""
      |    @R5
      |    D=D+M    // add x + y
      """ stripMargin
    )
    push()
  }

  def process(ast: List[Command]) = cpu(ast)
}
