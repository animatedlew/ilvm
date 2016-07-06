package virtualmachine


import org.scalatest._

class ILParserSpec extends FlatSpec with Matchers {
  "ILParser" should "parse all vm commands" in {
    val p = new ILParser
    val result = p.run("""
      pop local true
      pop local false
      label xyz
      goto xyz
      if-goto xyz
      function mult 2 // two local vars
      call mult // call identifier
      return // return what's on the stack
      push constant 2
      push constant 3
      add
      pop local 0
      eq
      lt
      gt
      sub
      and
      or
      not
      pop argument 0
      push argument 0
      push local 0
      pop local 0
      push static 0
      pop static 0
      push constant 0
      pop constant 0
      push this 0
      pop this 0
      push this 0
      pop that 0
      push pointer 0
      pop pointer 0
      push temp 0
      pop temp 0
    """)
    result.toString should equal("""
      List(CPop(local,0), CPop(local,-1), CLabel(xyz), CGoto(xyz), CIf(xyz), CFunction(mult,2), CCall(mult), CReturn, CPush(constant,2), CPush(constant,3), Add, CPop(local,0), Eq, Lt, Gt, Sub, And, Or, Not, CPop(argument,0), CPush(argument,0), CPush(local,0), CPop(local,0), CPush(static,0), CPop(static,0), CPush(constant,0), CPop(constant,0), CPush(this,0), CPop(this,0), CPush(this,0), CPop(that,0), CPush(pointer,0), CPop(pointer,0), CPush(temp,0), CPop(temp,0))
    """.trim)
  }
}
