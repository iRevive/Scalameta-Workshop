package workshop

import auto.loggable
import auto.loggable.ignore

object Main {

  def main(args: Array[String]): Unit = {
    import Loggable.ops._

    println(" ".print)

    val user = User(0, "Paolo", "qwerty123")
    println(user.print)
  }

  @loggable type A = String

}

@loggable
case class User(id: Int, name: String, @ignore password: String)

object User {

}

trait Loggable[A] {

  def print(value: A): String

}

object Loggable extends LoggableInstances {

  def apply[A](implicit instance: Loggable[A]): Loggable[A] = instance

  def instance[A](op: A => String): Loggable[A] = (value: A) => op(value)

  trait Ops[A] {
    def typeClassInstance: Loggable[A]

    def self: A

    def print: String = typeClassInstance.print(self)
  }

  object ops {
    import scala.language.implicitConversions

    implicit def toOps[A](target: A)(implicit tc: Loggable[A]): Ops[A] = new Ops[A] {
      val self: A = target
      val typeClassInstance: Loggable[A] = tc
    }

  }

}

trait LoggableInstances {

  implicit val string: Loggable[String] = Loggable.instance(identity)
  implicit val int: Loggable[Int] = Loggable.instance(_.toString)
  implicit val long: Loggable[Long] = Loggable.instance(_.toString)
  implicit val double: Loggable[Double] = Loggable.instance(_.toString)

  implicit def option[A: Loggable]: Loggable[Option[A]] = Loggable.instance {
    case Some(v) => s"Some(${Loggable[A].print(v)})"
    case None => "Empty"
  }

}