package com.work.service

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import java.lang.System.exit

/**
 * @Author: sanshui
 * @Time: 2022/01/03/15:19
 * @Desc： None
 */
object KafkaConsumer {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Please input <topic>")
      exit(1)
    }
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("KafkaConsumer")
    val ssc = new StreamingContext(conf, Seconds(10))
    //设置检查点
    ssc.checkpoint("./cp")

    //获取用户等级
//    UserLevel.getUserLevel(ssc)
    //获取该时间内的弹幕条数
//        BarrageNum.getBarrageNum(ssc)
    //获取活跃的用户
    ActiveUser.getActiveUser(ssc)
    //时间内相同弹幕的条数
//    BarrageTop.getBarrageTop(ssc)
    ssc.start()
    ssc.awaitTermination()
  }
}
