package com.esachser

import java.io.{BufferedReader, File, FileReader}

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.search.{BooleanClause, IndexSearcher, TopDocs}
import org.apache.lucene.store.FSDirectory

class IntactSearchBolt(index_dir:String, indexid_dir:String, plan:String, search_time:Long)
  extends ProcessFunction[(String, Long), (String, Long, String, Long, Int)]{

  val fields = Array("title", "content")
  val flags = Array(BooleanClause.Occur.SHOULD, BooleanClause.Occur.MUST)

  lazy val indexSearcher1 = {
    try {
      val br = new BufferedReader(new FileReader(indexid_dir))
      val index_id = br.readLine()
      br.close()

      val indexDir1 = FSDirectory.open(new File(index_dir + index_id + "/index"))
      val indexreader1 = DirectoryReader.open(indexDir1)
      Some(new IndexSearcher(indexreader1))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  override def processElement(input: (String, Long), context: ProcessFunction[(String, Long), (String, Long, String, Long, Int)]#Context, collector: Collector[(String, Long, String, Long, Int)]): Unit = {
    indexSearcher1.foreach { idxSearcher =>
      val word = input._1
      val start_time = input._2

      val search_num = 0
      val urls_score = {
        try {
          val query = MultiFieldQueryParser.parse(word, fields, flags, new SmartChineseAnalyzer())
          val docs = idxSearcher.search(query, 10000)
          val scoreDocs = docs.scoreDocs
          val numhits = scoreDocs.length
          val length = if (numhits >= 10) 10 else numhits
          val subDocs = scoreDocs.take(length)
          numhits + "+" + subDocs.mkString("[", ", ", "]")
        } catch {
          case e:
            Exception => e.printStackTrace()
            ""
        }
      }

      val bolt_time = System.currentTimeMillis() - start_time

      collector.collect(word, bolt_time, urls_score, start_time, search_num)
      println(word+","+bolt_time+","+urls_score+","+start_time+","+search_num)
    }
  }

}
