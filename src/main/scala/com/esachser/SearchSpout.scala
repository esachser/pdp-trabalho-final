package com.esachser

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

class SearchSpout(freq: Long) extends ProcessFunction[String, (String, Long)]{
  var flag = 0
  var i = 0
  override def processElement(s: String, context: ProcessFunction[String, (String, Long)]#Context, collector: Collector[(String, Long)]): Unit = {
    while(flag < 30){
      flag = flag + 1
      println("waiting:"+flag)
      Thread.sleep(1000)
    }
    println(i+s)
    collector.collect(s, System.currentTimeMillis())
    //collector.collect(s, System.currentTimeMillis())
    i = i + 1
    Thread.sleep(1000 / freq)
  }
}
