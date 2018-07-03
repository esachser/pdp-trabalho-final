package com.esachser

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

import scala.collection.mutable

class IntactMergeBolt(bolt_num: Int)
  extends ProcessFunction[(String, Long, String, Long, Int), (String, String, Long)]{

  lazy val result: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
  lazy val cal_num: mutable.HashMap[String, Int] = mutable.HashMap[String, Int]()

  override def processElement(input: (String, Long, String, Long, Int), context: ProcessFunction[(String, Long, String, Long, Int), (String, String, Long)]#Context, collector: Collector[(String, String, Long)]): Unit = {
    val (word, bolt_time, urls_score, start_time, search_num) = input

    val url = result.getOrElseUpdate(word, urls_score)
    val urls = url + (if (urls_score.equals("")) "" else ";"+urls_score)
    val cal = cal_num.getOrElseUpdate(word, 0)
    // println(cal_num.size)
    if (cal == bolt_num) {
      collector.collect(word, urls, start_time)
      println("search_word:" + word + ",url:" + urls + ",starttime" + start_time)
      result -= word
      cal_num -= word
    } else {
      result += (word -> urls)
      cal_num += (word -> (cal + 1))
    }
  }
}
