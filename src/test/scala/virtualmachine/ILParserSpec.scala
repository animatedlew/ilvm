package virtualmachine


import org.scalatest._

class ILParserSpec extends FlatSpec with Matchers {
  "ILParser" should "parse all vm commands" in {
    val p = new ILParser
    val result = p.run("""
      pop local 0
      pop local 1
      label xyz
      goto xyz
      if-goto xyz
      function mult 2 // two local vars
      call mult 2 // call identifier
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
      List(Pop(local,0), Pop(local,1), Label(xyz), Goto(xyz), If(xyz), Function(mult,2), Call(mult,2), Return, Push(constant,2), Push(constant,3), Add, Pop(local,0), Eq, Lt, Gt, Sub, And, Or, Not, Pop(argument,0), Push(argument,0), Push(local,0), Pop(local,0), Push(static,0), Pop(static,0), Push(constant,0), Pop(constant,0), Push(this,0), Pop(this,0), Push(this,0), Pop(that,0), Push(pointer,0), Pop(pointer,0), Push(temp,0), Pop(temp,0))
    """.trim)
  }
}
