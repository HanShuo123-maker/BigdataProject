package cn.edu.xmu

import org.apache.spark.{SparkConf, SparkContext}
import java.io.StringReader
import au.com.bytecode.opencsv.CSVReader
import java.util.Date

object Earthquake {
  def main(args: Array[String]) {
    val inputFile = args(0) //第1个参数是输入文件路径
    val conf = new SparkConf().setAppName("WordCount").setMaster("local")
    val sc = new SparkContext(conf)
    val textFile = sc.textFile(inputFile)
    //    val wordCount = textFile.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey((a, b) => a + b)
    //    val wordCount = textFile.flatMap(line => line.split(","))
    //    val wordCount = textFile.flatMap(line => line.split(","))
    //    println(textFile.first())


    //    textFile.foreach(println)
    //    print("*******")
    //    println(textFile.count())

    //    print(textFile.map(line => line.split("")).collect.length)
    //    print("*******")
    //    println(textFile.count())


    //    wordCount.foreach(println)
    //    println(wordCount.count())


//    case class Person(name: String, favoriteAnimal: String)
    //    val input = sc.wholeTextFiles(inputFile)
    val result = textFile.map { line =>
      val reader = new CSVReader(new StringReader(line));
      reader.readNext()
    }
    println(result.getClass)
    println(result.count())    //*******输出一共有多少行**********
//    result.foreach(x =>
//    {x.foreach(println);
//      println("======")})

//    result.foreach(x =>
//    {x(0).foreach(println);
//      println("======")})

    result.foreach(x =>
    { println(x.length);   //************每行有7个元素**********************
      println(x(0));
      println(x(1));
      println(x(5));
//      println(x(7));
      println("======")})

    val cleanData = result.map {
      line => val a=line
        (a(0),a(1),a(2),a(3),a(4),a(5),a(6))
    }

    println("cleanData")
    cleanData.foreach(println)


    println("明细数据插入数据库")
    val start_time =new Date().getTime
    cleanData.foreachPartition(MysqlUtil.earthMap)                       //这里用foreachPartition来写数据库
    val end_time =new Date().getTime
    println("插入共用时"+(end_time-start_time)+"ms")                            //单位ms
    println("明细数据插入数据库_结束")


  }
}
