package com.esachser

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector

import scala.collection.mutable

class SearchMergeBolt
  extends ProcessFunction[(String, String, Long), (String, Int)]{

  lazy val result: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
  lazy val cal_num: mutable.HashMap[String, Int] = mutable.HashMap[String, Int]()

  var outputCount = 0

  override def processElement(input: (String, String, Long), context: ProcessFunction[(String, String, Long), (String, Int)]#Context, collector: Collector[(String,Int)]): Unit = {
    val (word, urls_score, start_time) = input

    val url = result.getOrElseUpdate(word, urls_score)
    val urls = url + (if (urls_score.equals("")) "" else ";"+urls_score)
    val cal = cal_num.getOrElseUpdate(word, 0) + 1
    collector.collect(word, cal)
    if (cal == 6) {
      val totaltime = System.currentTimeMillis()-start_time
      outputCount = outputCount + 1
      //collector.collect(word, outputCount)
      println(word+":"+totaltime+":"+urls_score)
      result -= word
      cal_num -= word
    }else {
      result += (word -> urls)
      cal_num += (word -> cal)
    }
  }
}
