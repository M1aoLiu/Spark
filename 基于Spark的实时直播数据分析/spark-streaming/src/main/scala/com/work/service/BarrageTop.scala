package com.work.service

import com.work.util.{InitSpark, KafkaConnUtil, MysqlConnUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

import java.sql.{Connection, PreparedStatement}

/**
 * @Author: sanshui
 * @Time: 2022/01/06/14:03
 * @Desc： 该时间内相同弹幕的条数
 */
object BarrageTop {
  def getBarrageTop(ssc: StreamingContext): Unit = {
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      InitSpark.getDStream(ssc)
    val kafkaValue: DStream[String] = kafkaDStream.map(_.value())
    var sql = ""
    val wordsToOne: DStream[((String, String, String), Int)] =
      kafkaValue.map(_.split("\t")).map(t => ((t(0), t(4), t(6)), 1))
    val wordCount: DStream[((String, String, String), Int)] =
      wordsToOne.reduceByKeyAndWindow((x: Int, y: Int) => x + y,
        Seconds(40), Seconds(10))

    val sortCount: DStream[((String, String, String), Int)] =
      wordCount.transform(_.sortBy(_._2, ascending = false))

    //写入数据库
    sortCount.foreachRDD(rdd => {
      rdd.foreach(record => {
        val connection: Connection = MysqlConnUtil.getMysqlConn
        sql = "insert into barrage_top(rid,content,dt,num) values(?,?,?,?) on duplicate key update " +
          "num=num+?"
        val pstmt: PreparedStatement = connection.prepareStatement(sql)
        pstmt.setString(1, record._1._1)
        pstmt.setString(2, record._1._2)
        pstmt.setString(3, record._1._3)
        pstmt.setInt(4, record._2)
        pstmt.setInt(5, record._2)
        pstmt.executeUpdate()
        connection.close()
      })
    })
    print("获取弹幕Top：")
    sortCount.print(10)
  }
}
