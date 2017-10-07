package auto

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.collection.immutable.Seq
import scala.meta._

/**
  * @author Maksim Ochenashko
  */
@compileTimeOnly("loggable annotation should have been removed by compiler but was not")
class loggable extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    def extractParamType(param: Term.Param): Type =
      param.decltpe match {
        case Some(t: Type)  => t
        case _              => abort(param.pos, "Field without type declaration")
      }

    def generate(className: Type.Name, cParams: Seq[Term.Param]): Seq[Stat] = {
      val imports: Seq[Import] = Seq(
        q"import _root_.workshop.Loggable.ops._"
      )

      val chain = cParams map { param =>
        val propName: Term.Name = Term.Name(param.name.value)
        val paramType: Type.Name = Type.Name(extractParamType(param).toString())

        val propNameLiteral: Lit.String = Lit.String(param.name.value)
        val delimiterLiteral: Lit.String = Lit.String(" = ")
        val loggableCall: Term.Apply = q"Loggable[$paramType].print(value.$propName)"

        val result = Term.ApplyInfix(
          lhs = Term.ApplyInfix(
            lhs = propNameLiteral,
            op = Term.Name("+"),
            targs = Nil,
            args = Seq(delimiterLiteral)
          ),
          op = Term.Name("+"),
          targs = Nil,
          args = Seq(loggableCall)
        )

        //"id" + " = " + Loggable[Int].print(id)

        result
      }

      val start = Lit.String(className.value + "(")

      val loggableInstance =
        q"""
            implicit val loggableInstance: Loggable[$className] =
              new Loggable[$className] {

                override def print(value: $className): String = {
                  List(..$chain).mkString($start, ", ", ")")
                }

              }
        """

      imports :+ loggableInstance
    }

    def filterParams(cParams: Seq[Term.Param]): Seq[Term.Param] = {
      def isAnnotated(mods: Seq[Mod]): Boolean = {
        mods exists {
          case Mod.Annot(Ctor.Ref.Name("ignore")) => true
          case _                                  => false
        }
      }

      for {
        param <- cParams
        if !isAnnotated(param.mods)
      } yield param
    }

    defn match {
      case c@Defn.Class(mods, className, tParams, ctor, templ) if mods.contains(Mod.Case()) =>
        val generatedBody = generate(className, filterParams(ctor.paramss.flatten))

        val companionObject = Defn.Object(
          Nil,
          Term.Name(className.value),
          Template(
            Nil, Nil, Term.Param(Nil, Name.Anonymous(), None, None),
            Some(generatedBody)
          )
        )

        Term.Block(c :: companionObject :: Nil)

        defn

      case Term.Block((c: Defn.Class) :: (o: Defn.Object) :: Nil) =>
        val stats = o.templ.stats.getOrElse(Nil)

        val generatedBody = generate(c.name, filterParams(c.ctor.paramss.flatten))

        val updatedStats = stats ++ generatedBody

        val updatedObject = o.copy(templ = o.templ.copy(stats = Some(updatedStats)))

        Term.Block(c :: updatedObject :: Nil)

      case _ =>
        defn
    }
  }

}

object loggable {

  class ignore extends StaticAnnotation

}