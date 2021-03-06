package com.esachser


import java.io._
import java.util.Properties

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}

object Search{
  def main(args: Array[String]): Unit = {
    // Inicia recebendo os valores de configuração
    val fis = new FileInputStream(args(0))
    val configProps = new Properties()
    configProps.load(fis)

    // Parâmetros de execução
    val plan = configProps.getProperty("search.plan")
    val freq = configProps.getProperty("search.freq").toInt
    val per = configProps.getProperty("search.boltratio").toFloat
    val boltnum = configProps.getProperty("search.group.boltnum").toInt
    val pro_num = (boltnum * per).toInt

    val wordsdir = configProps.getProperty("searchwords.dir")
    val indexid_dir = configProps.getProperty("indexid.dir")
    val index_dir = configProps.getProperty("index.dir")
    val search_time = 0L

    val spoutThreads = configProps.getProperty("search.searchSpout.threads").toInt
    val intactSearchBoltThreads = configProps.getProperty("search.IntactSearchBolt.threads").toInt
    val intactMergeBoltThreads = configProps.getProperty("search.IntactMergeBolt.threads").toInt
    val finalMergeBoltThreads = configProps.getProperty("search.FinalMergeBolt.threads").toInt

    val num_workers = configProps.getProperty("num.workers").toInt



    // Inicia o Flink e sua execução em si
    //val env = StreamExecutionEnvironment.createLocalEnvironment(num_workers)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(num_workers)
    // env.setMaxParallelism(num_workers)

    val text = env.readTextFile(wordsdir).setParallelism(1)

    val searchspout = text
      .process(new SearchSpout(freq))
      .setParallelism(spoutThreads)
      .broadcast
    val intactSearchBolt = searchspout
      .process(new IntactSearchBolt(index_dir, indexid_dir, plan, search_time))
      .setParallelism(intactSearchBoltThreads)
      .shuffle
    val intactMergeBolt = intactSearchBolt
      .process(new IntactMergeBolt(pro_num))
      .setParallelism(intactMergeBoltThreads)
      .shuffle
    val searchBolt = intactMergeBolt
      .process(new SearchMergeBolt)
      .setParallelism(finalMergeBoltThreads)

    // intactSearchBolt.addSink(v => out.println(v._1+","+v._2+","+v._3+","+v._4+","+v._5))

    env.execute("BigDataBench-Flink-Search")
  }
}
