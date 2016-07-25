import java.io.{File, FileReader, PrintWriter}

import virtualmachine._

object Main {
  def getFiles(dir: File) = dir.listFiles.filter(_.isFile).toList

  def main(args: Array[String]) = {
    println("Hack Translator \u00A9 2016" + args.length)
    if (args.length == 1) {
      if (args.head.contains(".vm")) {
        val file = args.head
        val p = new ILParser()
        //val vm = new HackVM()
        val ast = p.run(new FileReader(file))
        println(s"ast: $ast")
        implicit val vm = new PrintWriter(file.replace("vm", "asm"))
        val translator = new Translator
        //vm.process(p.run(s))
        translator.process(ast)

        //val pw = new PrintWriter(out)
        //try ml foreach { case (address, op) => pw.println(op) }
        //finally pw.close()

      } else {
        val p = new ILParser()
        val files = getFiles(new File(args.head))
        files filter { _.getName.endsWith("vm") } foreach { file =>
          val ast = p.run(new FileReader(file))
          implicit val vm = new PrintWriter(s"${file.getAbsolutePath.stripSuffix("vm")}asm")
          val translator = new Translator
          try translator.process(ast)
          finally vm.close()
        }

        // TODO: concat all files into one asm named after parent directory
      }
    } else println("Please provide a vm file or directory with vm files.")
  }

  def bootStackedVM() = {
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
