import virtualmachine._
object Main extends App {
  StackedVM(Vector[Assembly](
    PUSH(), NUMBER(42),
    PRINT(),
    PUSH(), NUMBER(24),
    PRINT(),
    NOOP(), NOOP(), NOOP(),
    PUSH(), NUMBER(1),
    POP(),
    HALT() )).process()
}
