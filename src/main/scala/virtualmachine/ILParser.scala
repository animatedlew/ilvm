package virtualmachine

import AST._

import scala.util.parsing.combinator.RegexParsers
import System.lineSeparator
import language.{postfixOps, implicitConversions}

class ILParser extends RegexParsers {

  override val whiteSpace = """([ \t]|//.*)+""".r
  lazy val boolean = "true" | "false"
  lazy val segment = (
      "local"
    | "argument"
    | "static"
    | "constant"
    | "this"
    | "that"
    | "pointer"
    | "temp"
  )
  lazy val wholeNumber = """-?\d+""".r
  lazy val index: Parser[Short] = wholeNumber ^^ { _.toShort } | boolean ^^ { case b => if (b == "true") 0x0000 else 0xFFFF }
  lazy val identifier = """(?i)[_a-z.][_a-z0-9]*""".r
  lazy val opCode: Parser[Command] = (
      "push" ~ segment ~ index ^^ { case _ ~ s ~ i => Push(s, i) } // push the value of segment[index] onto the stack
    | "pop" ~ segment ~ index ^^ { case _ ~ s ~ i => Pop(s, i) } // store in segment[index]
    | "add" ^^ { case _ => Add }
    | "sub" ^^ { case _ => Sub }
    | "neg" ^^ { case _ => Neg }
    | "and" ^^ { case _ => And }
    | "or"  ^^ { case _ =>  Or }
    | "not" ^^ { case _ => Not }
    | "eq"  ^^ { case _ =>  Eq }
    | "lt"  ^^ { case _ =>  Lt }
    | "gt"  ^^ { case _ =>  Gt }
    | "call" ~ identifier ^^ { case _ ~ label => CCall(label) }
    | "return" ^^ { case _ => CReturn }
    | "function" ~ identifier ~ wholeNumber ^^ { case _ ~ label ~ n => CFunction(label, n.toShort) }
    | "if-goto" ~ identifier ^^ { case _ ~ label => CIf(label) }
    | "goto" ~ identifier ^^ { case _ ~ label => CGoto(label) }
    | "label" ~ identifier ^^ { case _ ~ label => CLabel(label) }
    | "print" ~ wholeNumber ^^ { case _ ~ address => Print(address.toShort) }
  )
  lazy val blank = "" ^^ { case _ => Blank }
  lazy val manyOpCodes = repsep(opCode | blank, lineSeparator | """\r?\n""".r)
  def run(s: String) = parseAll(manyOpCodes, s) match {
    case Success(result, _) => result filterNot { case Blank => true; case _ => false }
    case failure: NoSuccess => scala.sys.error(s">> ${failure.msg}")
  }
}