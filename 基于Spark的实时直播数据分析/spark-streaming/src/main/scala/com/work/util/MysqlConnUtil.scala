package com.work.util


import java.sql.{Connection, DriverManager}

/**
 * @Author: sanshui
 * @Time: 2022/01/06/13:17
 * @Desc： None
 */
object MysqlConnUtil {
  val url = "jdbc:mysql://localhost:3306/live_analysis?" +
    "useUnicode=true&characterEncoding=utf8&useSSL=false"
  val username = "root"
  val password = "root"
  def getMysqlConn: Connection = {
    Class.forName("com.mysql.cj.jdbc.Driver").newInstance()
    val connection: Connection = DriverManager.getConnection(url, username, password)
    connection
  }
}
