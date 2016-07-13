import virtualmachine._

object Main extends App {
  //bootStackedVM
  bootILParser()

  def bootILParser(): Unit = {
    val p = new ILParser
    val s =
      """
        |push constant 5
        |push constant 5
        |eq
        |pop static 0
        |push constant 7
        |push constant 7
        |eq
        |pop static 0
        |push constant 3
        |push constant 5
        |eq
        |pop static 0
        |push constant 5
        |push constant 3
        |eq
        |pop static 0
        |push constant 3030
        |pop pointer 0
        |print 3
        |push constant 3040
        |pop pointer 1
        |print 4
        |push constant 32
        |pop this 2
        |print 3032 // should be 32
        |push constant 46
        |pop that 6
        |print 3046 // should be 46
        |push pointer 0
        |push pointer 1
        |add
        |push this 2
        |sub
        |push that 6
        |add
        |push constant 111
        |push constant 333
        |push constant 888
        |pop static 8
        |print 24 // 888
        |pop static 3
        |print 19 // 333
        |pop static 1
        |print 17 // 111
        |push static 3
        |push static 1
        |sub
        |push static 8
        |add
        |push constant 10
        |pop local 0
        |push constant 21
        |push constant 22
        |pop argument 2
        |pop argument 1
        |push constant 36
        |pop this 6
        |push constant 42
        |push constant 45
        |pop that 5
        |pop that 2
        |push constant 510
        |pop temp 6
        |push local 0
        |push that 5
        |add
        |push argument 1
        |sub
        |push this 6
        |push this 6
        |add
        |sub
        |push temp 6
        |add
      """.stripMargin
    val vm = new HackVM()
    vm.process(p.run(s))
  }

  def bootStackedVM: Unit = {
    StackedVM(Vector[Assembly](
      PUSH(), NUMBER(42),
      PRINT(),
      PUSH(), NUMBER(24),
      PRINT(),
      NOOP(), NOOP(), NOOP(),
      PUSH(), NUMBER(1),
      POP(),
      HALT())).process()
  }
}
