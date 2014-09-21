package ru.simplesys.relasca
package macros

//import shapeless.{::, HNil, HList}
import shapeless._

trait DataSource


trait SQLColumnExpression[T] {
  type TSimpleType = T
  val sqlExpression: String
}

trait SQLColumnNamed[T] {
  val sqlColumnName: String
}

trait TableColumn[T] extends SQLColumnExpression[T] with SQLColumnNamed[T] {
  val sqlExpression = sqlColumnName
}

trait AliasedSQLColumnExpression[T] extends SQLColumnExpression[T] with SQLColumnNamed[T]

class BaseTableColumn[T](val sqlColumnName: String) extends TableColumn[T]

class TableColumnLong(sqlColumnName: String) extends BaseTableColumn[Long](sqlColumnName)

object TableColumnLong {
  def apply(sqlColumnName: String): TableColumnLong = new TableColumnLong(sqlColumnName)//new BaseTableColumn[Long](sqlColumnName)
}

object TableColumnLongOption {
  def apply(sqlColumnName: String): TableColumn[Option[Long]] = new BaseTableColumn[Option[Long]](sqlColumnName)
}

object TableColumnString {
  def apply(sqlColumnName: String): TableColumn[String] = new BaseTableColumn[String](sqlColumnName)
}

object TableColumnStringOption {
  def apply(sqlColumnName: String): TableColumn[Option[String]] = new BaseTableColumn[Option[String]](sqlColumnName)
}

trait SQLRelation {
  val ds: DataSource
  def columns: IndexedSeq[TableColumn[_]]
}

trait Table extends SQLRelation {
  type ProjectionType <: HList
  type SelectType <: HList
  val sqlTableName: String
  def * : ProjectionType
  def columns: IndexedSeq[TableColumn[_]]// = IndexedSeq()

//  val test: SelectType
}

trait TableImpl[CL <: HList, VL <: HList] {
  self: Table =>
  type ProjectionType = CL
  type SelectType = VL
  //val allColumns: CL
}

abstract class TableQuery(val ds: DataSource) extends SQLRelation
