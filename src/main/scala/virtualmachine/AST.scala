package virtualmachine
import language.{postfixOps, implicitConversions}
object AST {
  type Word = Short
  implicit def toShort(n: Int): Short = n.toShort

  trait Command
  trait ArithmeticCommand extends Command
  trait MemoryCommand     extends Command
  trait FunctionCommand   extends Command
  trait FlowCommand       extends Command

  case class CLabel(value: String) extends Command
  case class CGoto(label: String)  extends Command
  case class CIf(label: String)    extends Command

  case class  CCall(label: String)                  extends FunctionCommand
  case class  CFunction(label: String, argc: Short) extends FunctionCommand
  case object CReturn                               extends FunctionCommand

  case class CPush(segment: String, index: Word) extends MemoryCommand
  case class CPop(segment: String, index: Word)  extends MemoryCommand

  case object Add extends ArithmeticCommand
  case object Sub extends ArithmeticCommand
  case object Neg extends ArithmeticCommand

  case object And extends ArithmeticCommand
  case object Or  extends ArithmeticCommand
  case object Not extends ArithmeticCommand

  case object Eq  extends ArithmeticCommand
  case object Lt  extends ArithmeticCommand
  case object Gt  extends ArithmeticCommand

  case object Blank extends Command
  case class  Print(address: Word) extends Command
}
