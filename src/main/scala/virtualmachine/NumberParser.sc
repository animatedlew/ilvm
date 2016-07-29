import scala.util.parsing.combinator._
import language.postfixOps
//
class NumberParser extends RegexParsers {
  def buildNumber(d: String): Int = d.foldLeft(0) { (m: Int, n: Char) => 10 * m + (n - '0') }
  def power(base: Int, exp: Int): Double = {
    var result: Double = base // assume positive exp most of the time
    var e = exp
    while (e > 1) { result *= base; e -= 1 }
    while (e < 1) { result /= base; e += 1 }
    result
  }
  lazy val number: Parser[Double] = (
    int ~ frac ~ exp ^^ { case (s, i) ~ f ~ power =>
      val mantissa = i + f.getOrElse(0D)
      s * mantissa * power
    }
      | int ~ exp ^^ { case (s, i) ~ power => s * i * power }
      | int ~ frac ^^ { case (s, i) ~ f => s * (i + f.getOrElse(0D)) }
      | int ^^ { case (s, d) => s * d.toDouble })
  lazy val int: Parser[(Int, Int)] = sign ~ (digits ^^ { n => buildNumber(n) } | digit ^^ { _.head - '0' }) ^^ {
    case s ~ d => s.fold(1 -> d) {
      case neg if neg == "-" => -1 -> d
      case _ => 1 -> d
    }
  }
  lazy val frac = ("." ~ digits ^^ { case _ ~ d =>
    var place = 1
    d.foldLeft(0D) { (m, n) =>
      place *= 10
      val d: Double = n - '0'
      m + d / place
    }
  })?
  lazy val digits: Parser[String] = (digit+) ^^ { _.mkString("") }
  lazy val digit = """[0-9]""".r
  lazy val exp = e ~ digits ^^ {
    case Some(o) ~ d if o == "-" => power(10, -buildNumber(d))
    case _ ~ d => power(10, buildNumber(d))
  }
  lazy val e = """[eE]""".r ~> sign
  lazy val sign = """[+-]""".r?
  def run(s: String) = parseAll(number, s) match {
    case Success(result, _) => result
    case failure: NoSuccess => scala.sys.error(failure.msg)
  }
}
//
val p = new NumberParser
//
p.power(10, 1) == 10
p.power(10, -1) == 0.1
// test out number = int
p.run("0") == 0.0
p.run("01") == 1.0
p.run("1") == 1.0
p.run("10") == 10.0
p.run("24") == 24.0
p.run("3") == 3.0
p.run("123") == 123.0
p.run("+3") == 3.0
p.run("-3") == -3.0
//------------------------------
// test out number = int ~ frac
p.run("3.1") == 3.1
p.run("3.01") == 3.01
p.run("3.14") == 3.14
p.run("+3.14") == 3.14
p.run("-3.14") == -3.14
//------------------------------
p.run("+3.14e-3") == 0.00314
p.run("-3.14E3") == -3140
p.run("-3.14E-3") == -0.00314
p.run("+3.14e+3") == 3140
p.run("3e3") == 3000
p.run("3e6") == 3000000
//------------------------------------