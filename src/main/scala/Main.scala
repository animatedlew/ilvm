import java.io.{File, FileReader, PrintWriter}
import scala.io.Source
import virtualmachine._

object Main {

  def main(args: Array[String]) = {
    println("Hack Translator \u00A9 2016")
    if (args.length == 1) {
      if (args.head.contains(".vm")) {
        val file = args.head
        val p = new ILParser()
        val fileReader = new FileReader(file)
        //val vm = new HackVM()
        val ast = p.run(fileReader)
        fileReader.close()
        implicit val vm = new PrintWriter(file.replace("vm", "asm"))
        val translator = new Translator(file.replace(".vm", ""))
        //vm.process(p.run(s))
        try translator.process(ast)
        finally vm.close()

      } else {
        val f = new File(args.head)
        if (f.isDirectory) {
          val outFileName =s"${f.getAbsolutePath}/${f.getName}.asm"
          val vmFiles = f.listFiles.filter { f => f.isFile && f.getName.endsWith("vm") }
          println("in:"); vmFiles foreach { println(_) }
          val p = new ILParser()
          vmFiles foreach { file =>
            val f = new FileReader(file)
            val ast = try p.run(f) finally f.close()
            implicit val vm = new PrintWriter(s"${file.getAbsolutePath.stripSuffix("vm")}asm")
            val translator = new Translator(file.getName.replace(".vm", ""))
            try translator.process(ast)
            finally vm.close()
          }
          val outFile = new PrintWriter(outFileName)
          val asmFiles = new File(args.head).listFiles.filter { f =>
            f.isFile && f.getName.endsWith("asm") && !outFileName.endsWith(f.getName)
          }
          println("out: ")
          asmFiles foreach { println(_) }
          val asm = asmFiles.foldLeft(""){ case (out, file) =>
            val f = Source.fromFile(file)
            val data = f.mkString
            val result = out + s"\n//\t${file.getName}\t//\n$data"
            f.close()
            result
          }
          outFile.write(asm)
          outFile.close()
        } else println("Please provide valid directory.")
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
