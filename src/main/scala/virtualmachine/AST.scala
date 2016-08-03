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

  case class Label(value: String) extends Command
  case class Goto(label: String)  extends Command
  case class If(label: String)    extends Command

  case class  Call(label: String, argc: Short)     extends FunctionCommand
  case class  Function(label: String, argc: Short) extends FunctionCommand
  case object Return                               extends FunctionCommand

  case class Push(segment: String, index: Word) extends MemoryCommand
  case class Pop(segment: String, index: Word)  extends MemoryCommand

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
