package ru.simplesys.relasca
package macros



import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.annotation.StaticAnnotation


trait Helper[+C <: Context] {

  val ctx: C
  import ctx.universe._
  import shapeless.{::, HNil, HList}


/*
  def typeCheckClassDef(cd: ClassDef): c.Type = {
    val block = if (cd.tparams.isEmpty)
      q"(null: ${cd.name})"
    else {
      val tp = cd.tparams//.map { _ => "_" }.mkString(",", "", "")
      q"(null: ${cd.name}[..$tp])"
    }

    //      c.typeCheck(Block(List(cd), Literal(Constant((null)))).children.head.symbol.typeSignature
    c.typecheck(block, withMacrosDisabled = true).tpe
  }
*/

  // code from shapeless
  def mkCompoundTpe[Parent, Nil <: Parent, Cons[_, _ <: Parent] <: Parent](
    items: List[Type])(implicit
                       nil: ctx.WeakTypeTag[Nil],
                       cons: ctx.WeakTypeTag[Cons[Any, Nothing]]
                      ): Type = {
    items.foldRight(nil.tpe) {
      case (tpe, acc) =>
        appliedType(cons.tpe, List(tpe, acc))
    }
  }

  def mkHListTpe(items: List[Type]): Type = mkCompoundTpe[HList, HNil, ::](items)
  // code from shapeless
}


class TableDef extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro TableDefMacro.implAdd
}

object TableDefMacro {

  def implAdd(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def modifiedClass(classDecl: ClassDef/*, compDeclOpt: Option[ModuleDef]*/) = {
      val helper = new Helper[c.type] {
        val ctx: c.type = c
      }

      classDecl match {
        case origCl @ q"class $name(..$args) extends $parent with ..$traits { ..$body }" =>
          val dupCl = origCl.duplicate//.asInstanceOf[ClassDef]
          //val resultType = c.typecheck(q"(??? : $dupCl)").tpe
          val resultType = c.typecheck(Block(List(dupCl), Literal(Constant(())))).children.head.symbol.typeSignature
          //for whatever reason simple typecheck doesn't work...

          val itlMembers = resultType.members.filter(m => m.typeSignature <:< typeOf[TableColumn[_]]).toList.reverse
          val itlTypeList = itlMembers.map {m => m.typeSignature}

          val itlTypeParamList = itlMembers.map {t =>
            val typeParam = t.typeSignature.baseType(typeOf[TableColumn[_]].typeSymbol) match {
              case TypeRef(_, _, targ :: Nil) => targ
              case NoType => c.abort(c.enclosingPosition, "call this method with known type parameter only.")
            }
            typeParam
          }

          val resultColumnsType = helper.mkHListTpe(itlTypeList)
          val resultValuesType = helper.mkHListTpe(itlTypeParamList)

          val resTypeHList = TypeTree(resultColumnsType)
          val resTypeResHList = TypeTree(resultValuesType)

          val addingType = typeOf[TableImpl[_, _]].typeSymbol
          val addingTypeParameters = List(resTypeHList, resTypeResHList)
          val addedType = tq"$addingType[..$addingTypeParameters]"
          val addedTypeList: List[Tree] = List(addedType)

          val autoProjectionList = itlMembers.map(_.asTerm.name).foldRight[c.Tree](q"shapeless.HNil: shapeless.HNil")(
            (h, t) => q"shapeless.::($h, $t)"
          )

          val allColumns = q"""val * = $autoProjectionList"""
          val allColumnsSeq = q"""val columns = IndexedSeq(..${itlMembers.map(_.asTerm.name)})"""
          //val allColumns = q"""val * = shapeless.HNil"""
          val res = q"class $name(..$args) extends $parent with ..${(traits ++ addedTypeList).toList} { ..${(body ++ Seq(allColumns, allColumnsSeq)).toList} }"
          println(res)
//          res
          c.Expr(q"""
                      $res
                  """)




        case _ =>  c.abort(c.enclosingPosition, "can't unquote classDecl?")
      }
    }

    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: tail => modifiedClass(classDecl/*, None*/)
/*
      case (classDecl: ClassDef) :: Nil => modifiedClass(classDecl, None)
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifiedClass(classDecl, Some(compDecl))
*/
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }
  }


}