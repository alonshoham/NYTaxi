package nyctaxi

import java.time.{DayOfWeek, LocalDate}
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.{Column, SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all._

/**
  * Created by evgeny on 7/5/17.
  */
object LoadData {

  val goldmanSacksLocation = new Point( -74.013961, 40.714672 )
  val dateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy")
  val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
  val locationPrecision = 0.001 // about 100 meters

  //first parameter is link to csv file
  def main(args: Array[String]): Unit = {

    if (args.length == 0) {
      System.err.println("At least one parameter with link to csv file should be passed")
      System.exit(1)
    }

    val settings = if (args.length > 1) args else Array("spark://127.0.0.1:7077", "insightedge-space", "insightedge", "127.0.0.1:4174", args(0))
    if (settings.length < 5) {
      System.err.println("Usage: LoadDataFrame <spark master url> <space name> <space groups> <space locator>, first parameter must be url to csv file")
      System.exit(1)
    }
    val Array(master, space, groups, locators) = settings.slice( 0, 4 )
    val ieConfig = InsightEdgeConfig(space, Some(groups), Some(locators))

    val conf = new SparkConf().setMaster("local[2]").setAppName("NYC taxi")
    val sc = new SparkContext(conf)
    val sparkSession = SparkSession.builder
      .config(conf = conf)
      .appName("Taxi")
      .insightEdgeConfig(ieConfig)
      .getOrCreate()

    val time1 = System.currentTimeMillis()
    val path = args(0)

    println( "Path:" + path )

    val startTime = System.currentTimeMillis()

    val df = sparkSession.read.option("header","true").csv(path)

    val filteredDf = df.select( "vendor_id",
      "pickup_datetime",
      "dropoff_datetime",
      "passenger_count",
      "trip_distance",
      "pickup_longitude",
      "pickup_latitude" ).
      filter(
        df("dropoff_longitude") >= ( goldmanSacksLocation.longitude - locationPrecision ) &&
          df("dropoff_longitude") <= ( goldmanSacksLocation.longitude + locationPrecision ) &&
          df("dropoff_latitude") >= ( goldmanSacksLocation.latitude - locationPrecision ) &&
          df("dropoff_latitude") <= ( goldmanSacksLocation.latitude + locationPrecision )

        /*df( "tpep_dropoff_datetime" )......*/ )
    df.printSchema()
    filteredDf.show()

    filteredDf.write.mode(SaveMode.Overwrite).grid("nytaxi")

    val endTime = System.currentTimeMillis()

    sparkSession.stopInsightEdgeContext()

    println( "DataFrame load took " + ( endTime - startTime ) + " msec." )
  }

  def isWeekday(column:Column):Boolean = {
    val dateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy")
    val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")

//    val day=LocalDate.parse(column.as[String].toString(),dateTimeFormat).getDayOfWeek()

//    if (day.compareTo(DayOfWeek.SATURDAY)==1) return false

//    if (day.compareTo(DayOfWeek.SUNDAY)==1) return false

    return true
  }
}