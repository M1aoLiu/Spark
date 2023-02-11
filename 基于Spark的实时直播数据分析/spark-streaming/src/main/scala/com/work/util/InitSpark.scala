package com.work.util

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

/**
 * @Author: sanshui
 * @Time: 2022/01/06/14:07
 * @Desc： 初始化Spark，连接Kafka获取Dsteam数据流
 */
object InitSpark {
  def getDStream(ssc: StreamingContext): InputDStream[ConsumerRecord[String, String]] = {

    //设置 Kafka 参数
    val kafkaPara: Map[String, Object] = KafkaConnUtil.getKafkaProp

    //4.读取 Kafka 数据创建 DStream
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](Set("first2"), kafkaPara))
    kafkaDStream
  }
}
