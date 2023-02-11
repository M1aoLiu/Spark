package com.work.service

import com.work.util.{InitSpark, MysqlConnUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

import java.sql.PreparedStatement

/**
 * @Author: sanshui
 * @Time: 2022/01/06/14:05
 * @Desc： 在该时间内的弹幕条数
 */
object BarrageNum {
  def getBarrageNum(ssc: StreamingContext): Unit = {
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      InitSpark.getDStream(ssc)
    val kafkaValue: DStream[String] = kafkaDStream.map(_.value())
    val words: DStream[((String, String), Int)] =
      kafkaValue.map(_.split("\t")).map(t => ((t(0), t(6)), 1))

    val res: DStream[((String, String), Int)] = words.reduceByKeyAndWindow((x:Int,y:Int)=>x+y,
      Seconds(20),Seconds(10)
    )
    res.foreachRDD(rdd => {
      rdd.foreach(t => {
        val sql = "insert into barrage_num(rid,dt,num) values(?,?,?) on duplicate key update " +
          "num=num+?"
        val connection = MysqlConnUtil.getMysqlConn
        val pstmt: PreparedStatement = connection.prepareStatement(sql)
        pstmt.setString(1, t._1._1)
        pstmt.setString(2, t._1._2)
        pstmt.setInt(3, t._2)
        pstmt.setInt(4, t._2)
        pstmt.executeUpdate()
        connection.close()
      })
    })
    print("获取弹幕数量：")
    res.print()
  }
}
