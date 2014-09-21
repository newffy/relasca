package ru.simplesys.relasca

import macros._

import org.scalatest.FunSuite

@TableDef class UserTable(val ds: DataSource) extends Table {
  //import shapeless.{::, HNil, HList}

  val sqlTableName = "A_USER"

  val id = TableColumnLong("id")
  val caption = TableColumnString("sCaption")
  val description = TableColumnStringOption("sDescription")

  //override val test: SelectType = 1L :: "Vasya" :: None :: HNil


/*
  @TableDef class UserTableNested/*(val ds: DataSource)*/ extends Table {
    val ds = UserTable.this.ds
    //import shapeless.{::, HNil, HList}

    val sqlTableName = "A_USER2"

    val id = TableColumnLong("id2")
    val caption = TableColumnString("sCaption2")
    val description = TableColumnStringOption("sDescription2")

    //override val test: SelectType = 2L :: "Vasya2" :: None :: HNil
  }
*/

}


class SimpleTest extends FunSuite {
  test("Table A_USER") {

    val testTable = new UserTable(new DataSource {})
    println(testTable.*)
    println(testTable.columns)
    //println(classOf[testTable.ProjectionType])
    //println(classOf[testTable.SelectType])

  }
}
