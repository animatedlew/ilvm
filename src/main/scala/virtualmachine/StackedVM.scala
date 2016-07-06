package virtualmachine

import scala.annotation.tailrec
import language.postfixOps

sealed trait Assembly { val opcode = 0L }
case class NOOP(mnemonic: String = "NOOP") extends Assembly
case class PUSH(override val opcode: Long = 1, mnemonic: String = "PUSH") extends Assembly
case class POP(override val opcode: Long = 2, mnemonic: String = "POP") extends Assembly
case class NUMBER(override val opcode: Long = 3) extends Assembly
case class PRINT(override val opcode: Long = 4, mnemonic: String = "PRINT") extends Assembly
case class HALT(override val opcode: Long = 5, mnemonic: String = "HALT") extends Assembly

case class StackedVM(
  code: Vector[Assembly] = Vector.empty[Assembly],
  data: Vector[Long] = Vector.empty[Long],
  var stack: Array[Long] = new Array[Long](1024),
  ip: Long = 0, sp: Long = -1, fp: Long = 0) {

  def process() = cpu(ip, sp)

  @tailrec
  final def cpu(ip: Long = 0, sp: Long = 0): Unit = {
    if (ip < code.length) {
      code(ip.toInt) match {
        case PUSH(opcode, _) =>
          stack(sp.toInt + 1) = code(ip.toInt + 1).opcode
          cpu(ip + 2, sp + 1) // push
        case POP(_, _) =>
          println(s"popping off: ${stack(sp.toInt)}")
          cpu(ip + 1, sp - 1)
        case NUMBER(n) =>
          println(s"found $n")
          cpu(ip + 1, sp)
        case PRINT(_, _) =>
          println(s"stack: ${stack(sp.toInt)}")
          cpu(ip + 1, sp - 1) // pop
        case NOOP(_) =>
          println("noop")
          cpu(ip + 1, sp)
        case HALT(_, _) =>
          println("end")
      }
    } else stack.toSeq.foreach { println }
  }
}
