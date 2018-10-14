package spark10.access

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext, sql}
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
/**
  * Created by hadoop on 下午10:01.
  * 
  */
object AccesslogETL {
  def main(args: Array[String]): Unit = {

    val sc = new SparkContext(new SparkConf().setAppName("access").setMaster("local[3]"))
    val sqlContext = new HiveContext(sc)
    

   // doJob(sqlContext, fileSystem, appName, inputPath, outputPath,
   //   loadTime, accessTable, ifRefreshPartiton, tryTime)

  }

  /**
    *
    * @param sqlContext
    * @param fileSystem
    * @param batchID           batchID, 格式:201803221505-g1
    * @param inputPath
    * @param outputPath
    * @param accessTable       清洗后外部表
    * @param ifRefreshPartiton 是否刷新分区: 0-不刷新, 1-刷新
    * @param tryTime           重试次数
    * @return
    */
  @throws(classOf[Exception])
  def doJob(sqlContext: SQLContext, fileSystem: FileSystem, batchID: String,
            inputPath: String, outputPath: String, dataTime:String,
            accessTable: String, ifRefreshPartiton: String, tryTime: Int): String = {

    try{
      var result = "batchID:" + batchID
      val logger = Logger.getLogger("org")
      var begin = new Date().getTime
      
      val inputLocation = inputPath + batchID

      val inputDoingLocation = inputPath + "/" + batchID + "_doing"

      val dirExists = fileSystem.exists(new Path(inputLocation))
      if (!dirExists && tryTime == 0) {
        logger.info(s"$inputLocation not exists")
        result = result + s" $inputLocation not exists"
        return result
      } else if (!dirExists && tryTime > 0) {
        if (!fileSystem.exists(new Path(inputDoingLocation))) {
          logger.info(s" $inputDoingLocation not exists")
          result = result + s" $inputDoingLocation not exists"
          return result
        }
      } else {
        val isDoingRenamed = renameHDFSDir(fileSystem, inputLocation, inputDoingLocation)
        if (!isDoingRenamed) {
          logger.info(s" $inputLocation move to $inputDoingLocation failed")
          result = result + s" $inputLocation move to $inputDoingLocation failed"
          return result
        }
        logger.info(s" $inputLocation move to $inputDoingLocation success")
      }

      val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val endTime = sdf.parse(dataTime)
      val beginTime = sdf.format(endTime.getTime() - 2*60*60*1000)
      val curHourTime = sdf.format(endTime.getTime() - 1*60*60*1000)

      var resultDF:DataFrame = null

      // 遍历目录下的所有house子目录  
      fileSystem.globStatus(new Path(inputDoingLocation+"/*")).foreach(p=>{
        val hLoc = p.getPath.toString
        // 调用计算分区大小的代码 
        val getPartitionNum = (houseLoc:String)=>{
          //计算逻辑略 
          1
        } 
        val partitionNum = getPartitionNum(hLoc)
        
        logger.info("hLoc:" + hLoc + ", partitionNum:" + partitionNum)
        
        // 根据每个house目录生成DataFrame, 过程略
        val hDF:DataFrame = null // 调用生成DataFrame的逻辑，这里使用null占位
        
        //最近1个小时的log 
        val curHourDF = hDF.filter(s"acctime>='$curHourTime'")
        // 1个小时之前的log 
        val preHourDF = hDF.filter(s"acctime>'$beginTime' and acctime<'$curHourTime' ")

        // 1个小时之前的数据， 数据量小， 分区数设置为1/3分区大小 
        val preHourPartNum = if(partitionNum/3 == 0) 1 else partitionNum/3
        
        val newDF = curHourDF.coalesce(partitionNum).unionAll(preHourDF.coalesce(preHourPartNum))

        if(resultDF != null){
          resultDF = resultDF.unionAll(newDF)
        }else{
          resultDF = newDF
        }
      })

      // 将DataFrame保存到HDFS（代码略）

   ""
    }catch {
      case e:Exception =>{
        e.printStackTrace()
        e.getMessage
      }
    }

  }

  def renameHDFSDir(fileSystem: FileSystem, srcLocation: String, destLocation: String): Boolean = {
    val srcPath = new Path(srcLocation)
    val destPath = new Path(destLocation)
    val isRename = fileSystem.rename(srcPath, destPath)
    isRename
  }

}
