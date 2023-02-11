package com.work.service

import com.work.util.{InitSpark, MysqlConnUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

import java.sql.PreparedStatement

/**
 * @Author: sanshui
 * @Time: 2022/01/06/14:06
 * @Desc： 在该时间段内统一用户发送的弹幕条数
 */
object ActiveUser {
  def getActiveUser(ssc: StreamingContext): Unit ={
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      InitSpark.getDStream(ssc)
    val kafkaValue: DStream[String] = kafkaDStream.map(_.value())
    val activeData: DStream[((String, String, String), Int)] =
      kafkaValue.map(_.split("\t")).
        map(t => ((t(0), t(2), t(6)), 1))
        .reduceByKeyAndWindow((x: Int, y: Int) => x + y, Seconds(40), Seconds(10))

    val res: DStream[((String, String, String), Int)] =
      activeData.transform(_.sortBy(_._2, ascending = false))

    res.foreachRDD(rdd => {
      rdd.foreach(t => {
        val sql = "insert into active_user(rid,uname,dt,num) values(?,?,?,?) on duplicate key update " +
          "num=num+?"
        val connection = MysqlConnUtil.getMysqlConn
        val pstmt: PreparedStatement = connection.prepareStatement(sql)
        pstmt.setString(1, t._1._1)
        pstmt.setString(2, t._1._2)
        pstmt.setString(3, t._1._3)
        pstmt.setInt(4, t._2)
        pstmt.setInt(5, t._2)
        pstmt.executeUpdate()
        connection.close()
      })
    })
    print("获取用户活跃度：")
    res.print()
  }
}
