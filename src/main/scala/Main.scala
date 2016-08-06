import java.io.{File, FileReader, PrintWriter}
import scala.io.Source
import language.postfixOps
import virtualmachine._

object Main {

  def process(arg: String, f: File) = {
    if (f.isFile && arg.contains(".vm")) {

      val vmFileName = arg
      val asmFileName = vmFileName.replace("vm", "asm")
      val fileName = vmFileName.split("/").reverse.head.replaceAll("""\.vm|\/""", "")

      val p = new ILParser()
      val fileReader = new FileReader(vmFileName)
      val ast = p.run(fileReader)
      fileReader.close()

      val runtime = new ILVM()
      runtime.process(ast)

      implicit val vm = new PrintWriter(asmFileName)
      try new Translator(fileName).process(ast)
      finally vm.close()

    } else if (f.isDirectory) {

      val outFileName = s"${f.getAbsolutePath}/${f.getName}.asm"
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
      val asmFiles = new File(arg).listFiles.filter { f =>
        f.isFile && f.getName.endsWith("asm") && !outFileName.endsWith(f.getName)
      }

      new Translator("bootstrap")(outFile).bootstrap()

      println("out: ")
      asmFiles foreach { println(_) }
      val asm = asmFiles.foldLeft(""){ case (out, file) =>
        val handle = Source.fromFile(file)
        val result = out + s"\n//\t${file.getName}\t//\n${handle.mkString}"
        handle.close()
        result
      }
      outFile.write(asm)
      outFile.close()
    } else println(s"${Console.YELLOW}Please provide a valid vm file or a directory with valid vm files.")
  }

  def main(args: Array[String]) = {
    println("IL Translator \u00A9 2016\n")
    if (args.length == 1) process(args.head, new File(args.head))
    else println(s"${Console.YELLOW}Usage: translator [file.vm|directory]")
  }

}
