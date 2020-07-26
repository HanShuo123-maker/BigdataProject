package cn.edu.xmu
import java.text.SimpleDateFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scala.util.matching.Regex
/* 用户在线时长和登录次数统计*/
object UserOnlineAnalysis {
  def main(args: Array[String])
  {
    if (args.length != 1) {
      System.err.println("Usage: UserOnlineAnalysis <input>")
      System.exit(1)
    }
    val conf = new SparkConf().setAppName("UserOnlineAnalysis").setMaster("local[4]")
    val sc = new SparkContext(conf)
    println("输入文件的路径:"+args(0))
    val data = sc.textFile(args(0))
    println("剔除type等于3的数据 imei为Unknown 为\"\" 为\"000000000000000\"的数据")

    //定义notContainsType3
    val notContainsType3 = data.filter(!_.contains("\\\"type\\\":\\\"3\\\""))
      .filter(!_.contains("\\\"imei\\\":\\\"\\\""))
      .filter(!_.contains("000000000000000"))
      .filter(!_.contains("Unknown"))
    println("过滤logid或imei不存在的数据 \\\"imei\\\":\\\"\\\"")
    val cleanData = notContainsType3.filter(_.contains("logid")).filter(_.contains("imei"))


    println("****************")
    println("****************")
    println("****************")
    println("****************")
    println("****************")

    cleanData.collect().foreach(println)          //*****cleanData  ******数据集经过滤后打印每条信息（原始信息）*******************
    println("****************")
    println("****************")
    println("****************")
    println("****************")
    println("****************")


    val cleanMap = cleanData.map {
      line => val data = formatLine(line).split(",")     //以','进行拆分   formatLine处理函数最后返回用 逗号  分割每个数据
        (data(0), data(1),data(2) ,data(3),data(4),data(5))     // ,data(6))
    }


    println("打印出cleanMap数据："+"共有："+cleanMap.count())
    cleanMap.collect().foreach(println)

    println("明细数据插入数据库")
//    cleanMap.foreachPartition(MysqlUtil.cleanMap)                       //这里用foreachPartition来写数据库
    println("明细数据插入数据库_结束")


    val cleanMapFilter = cleanData.map {
      line => val data = formatLine(line).split(",")
        (data(0), data(1) )
    }
    println("输出时每行分组的第2个元素列表按照时间排序sortByKey()")
    val rdd = cleanMapFilter.groupByKey().map(x => (x._1, x._2.toList.sorted))

    println("****************")
    println("****************")
    println("****************")
    println("****************")
    println("****************")
    rdd.foreach(println)
    println("****************")
    println("****************")
    println("****************")
    println("****************")
    println("****************")


    rdd.cache()

    println("导出明细数据:")
    exportDetailData(rdd)
    println("导出统计数据:")

    exportSumData(rdd)
    rdd.unpersist()
    sc.stop()
    println("全部结束")
  }




  /* 导出用户在线时长和首次登录时间
   * 存储结构:(IMEI,首次登录时间,在线时长(秒))*/
  def exportDetailData(map: RDD[(String, List[String])]): Unit = {
    val result:RDD[(String, String,Int)] = map.flatMap {
      x =>
        val len = x._2.length                                            //登陆次数数组的长度
        val array = new Array[(String, String, Int)](len)
        for (i <- 0 until len)
        {
          if (i + 1 < len)     //len=1,直接置0
          {
            val nowTime = getTimeByString(x._2(i))
            val nextTime = getTimeByString(x._2(i + 1))
            val intervalTime = nextTime - nowTime
            if (intervalTime < 60 * 10)        //算时间
            {
              array(i) = (x._1, x._2(i), intervalTime.toInt)
            }
                else
                {
                  array(i) = (x._1, x._2(i), 0)
                }
          }
          else
          {
            array(i) = (x._1, x._2(i), 0)
          }
        }
        array
    }
    println("输出detail数据：")
    //result.collect().foreach(println)

    println("***5555555555*************")
    println("****************")
    println("*******555555*********")
    println("******555**********")
    println("******55555 **********")
    result.foreach(println)
    println("******5555555**********")
    println("******5555**********")
    println("*****5555***********")
    println("****************")
    println("******5555**********")


    println("detail插入mysql数据库_开始")
    result.foreachPartition(MysqlUtil.addsdetail)
    println("detail插入mysql数据库_结束")
  }
  /* 导出用户在线时长和登录次数统计结果
   * 存储结构:(IMEI,登录次数,在线时长(秒)) */
  def exportSumData(map: RDD[(String, List[String])]): Unit = {
    val result:RDD[(String, Int, Int)] = map.map {
      x =>
        //登录次数,默认登录1次
        var logNum: Int = 1
        //在线时长(秒)
        var totalTime: Long = 0
        val len = x._2.length

        println("*******************************************")
        println("x._2.length="+len)
        println("*******************************************")


        for (i <- 0 until len) {
          if (i + 1 < len) {
            val nowTime = getTimeByString(x._2(i))
            val nextTime = getTimeByString(x._2(i + 1))
            val intervalTime = nextTime - nowTime
            if (intervalTime < 60 * 10) {
              totalTime += intervalTime
            } else {
              logNum += 1
            }
          }
        }
        //输出ime,登录次数,总时长(秒)
        (x._1, logNum.toInt, totalTime.toInt)
    }
    println("输出sum数据：")
    //result.collect().foreach(println)
    println("sum插入mysql数据库_开始")
    result.foreachPartition(MysqlUtil.addsum)
    println("sum插入mysql数据库_结束")
  }
//  登录时间logid
//  用户编号imei（A0001~A1000）
//  IP地址requestip（192.168.0.1、192.168.0.2、192.168.0.3）
//  区域areacode（浙江省丽水市、福建省南平市、福建省福州市）
//  渠道channelno（0:app；1:PC机；2:平板电脑）
//  请求类型requesttype（0:GET；1:POST）
//  请求结果responsedata（"无查询结果"；"查询结果  成功"）
  def formatLine(line: String): String = {
    val imeiRegex = """\\"imei\\":\\"([A-Za-z0-9]+)\\"""".r  //12345678900987654321
    val logIdRegex = """"logid":"([A-Za-z0-9]+)",""".r //201803192035079865882995
    val requestipRegex = """"requestip":"([0-9.]+)",""".r
    val areacodeRegex = """"areacode":"([0-9]+)",""".r
    val requesttypeRegex = """"requesttype":"([0-9]+)",""".r
    val channelnoRegex = """\\"channelno\\":\\"([0-9]+)\\"""".r
    val responsedataRegex = """\\"responsedata\\":\\"([0-9]+)\\"""".r
    val logId = getDataByPattern(logIdRegex, line)
    val requestip = getDataByPattern(requestipRegex, line)
    val areacode = getDataByPattern(areacodeRegex, line)
    val requesttype = getDataByPattern(requesttypeRegex, line)
    val imei = getDataByPattern(imeiRegex, line)
    val channelno = getDataByPattern(channelnoRegex, line)
    val responsedata = getDataByPattern(responsedataRegex, line)
    //输出数据
//    imei+","+logId.substring(0, 14)+","+requestip+","+areacode+","+requesttype+","+channelno +","+responsedata
     imei+","+logId.substring(0, 14)+","+requestip+","+areacode+","+requesttype+","+channelno +","+responsedata
  }
  /* 根据正则表达式,查找相应值*/
  def getDataByPattern(p: Regex, line: String): String = {
    val result = (p.findFirstMatchIn(line)).map(item => {
      val s = item group 1 //返回匹配上正则的第一个字符串。
      s
    })
    result.getOrElse("NULL")
  }
  def getTimeByString(timeString: String): Long = {
    val sf: SimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
    sf.parse(timeString).getTime / 1000
  }
}