package com.work.service

import com.work.util.{InitSpark, MysqlConnUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

import java.sql.PreparedStatement

/**
 * @Author: sanshui
 * @Time: 2022/01/06/14:12
 * @Desc： 在改时间内用户的等级数量
 */
object UserLevel {

  def getUserLevel(ssc: StreamingContext): Unit = {
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      InitSpark.getDStream(ssc)
    val kafkaValue: DStream[String] = kafkaDStream.map(_.value())
    val levelData: DStream[((String, Int, String), Int)] =
      kafkaValue.map(_.split("\t")).map(t => ((t(0), t(5).toInt, t(6)), 1))
        .reduceByKeyAndWindow((x: Int, y: Int) => x + y, Seconds(20), Seconds(10))

    val sortLevel: DStream[((String, Int, String), Int)] =
      levelData.transform(_.sortBy(_._2, ascending = false))
    sortLevel.foreachRDD(rdd => {
      rdd.foreach(t => {
        val sql = "insert into user_level(rid,level,dt,num) values(?,?,?,?) on duplicate key " +
          "update num=num+?"
        val connection = MysqlConnUtil.getMysqlConn
        val pstmt: PreparedStatement = connection.prepareStatement(sql)
        pstmt.setString(1, t._1._1)
        pstmt.setInt(2, t._1._2.toInt)
        pstmt.setString(3, t._1._3)
        pstmt.setInt(4, t._2)
        pstmt.setInt(5, t._2)
        pstmt.executeUpdate()
        connection.close()
      })
    })
    print("获取用户等级：")
    sortLevel.print()
  }
}
