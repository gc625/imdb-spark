package imdb

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

case class TitleBasics(tconst: String, titleType: Option[String], primaryTitle: Option[String],
                      originalTitle: Option[String], isAdult: Int, startYear: Option[Int], endYear: Option[Int],
                      runtimeMinutes: Option[Int], genres: Option[List[String]]) {
  def getGenres(): List[String] = genres.getOrElse(List[String]())
}
case class TitleRatings(tconst: String, averageRating: Float, numVotes: Int)
case class TitleCrew(tconst: String, directors: Option[List[String]], writers: Option[List[String]])
case class NameBasics(nconst: String, primaryName: Option[String], birthYear: Option[Int], deathYear: Option[Int],
                      primaryProfession: Option[List[String]], knownForTitles: Option[List[String]])

object ImdbAnalysis {

  val conf: SparkConf = new SparkConf().setAppName("IMDB Analysis").setMaster("local") ;
  val sc: SparkContext = new SparkContext(conf);


  // Hint: use a combination of `ImdbData.titleBasicsPath` and `ImdbData.parseTitleBasics`
  val titleBasicsRDD: RDD[TitleBasics] = sc.textFile(ImdbData.titleBasicsPath).map(ImdbData.parseTitleBasics)

  // Hint: use a combination of `ImdbData.titleRatingsPath` and `ImdbData.parseTitleRatings`
  val titleRatingsRDD: RDD[TitleRatings] = ???

  // Hint: use a combination of `ImdbData.titleCrewPath` and `ImdbData.parseTitleCrew`
  val titleCrewRDD: RDD[TitleCrew] = ???

  // Hint: use a combination of `ImdbData.nameBasicsPath` and `ImdbData.parseNameBasics`
  val nameBasicsRDD: RDD[NameBasics] = ???

  println("bruh")
  titleBasicsRDD.map(println)

  def task1(rdd: RDD[TitleBasics]): RDD[(Float, Int, Int, String)] = {
  	println("in here?")
    ???
  }

  def task2(l1: RDD[TitleBasics], l2: RDD[TitleRatings]): RDD[String] = {
    ???
  }

  def task3(l1: RDD[TitleBasics], l2: RDD[TitleRatings]): RDD[(Int, String, String)] = {
    ???
  }

  // Hint: There could be an input RDD that you do not really need in your implementation.
  def task4(l1: RDD[TitleBasics], l2: RDD[TitleCrew], l3: RDD[NameBasics]): RDD[(String, Int)] = {
    ???
  }

  def main(args: Array[String]) {
    val durations = timed("Task 1", task1(titleBasicsRDD).collect().toList)
    val titles = timed("Task 2", task2(titleBasicsRDD, titleRatingsRDD).collect().toList)
    val topRated = timed("Task 3", task3(titleBasicsRDD, titleRatingsRDD).collect().toList)
    val crews = timed("Task 4", task4(titleBasicsRDD, titleCrewRDD, nameBasicsRDD).collect().toList)
    println(durations)
    println(titles)
    println(topRated)
    println(crews)
    println(timing)
    sc.stop()
  }

  val timing = new StringBuffer
  def timed[T](label: String, code: => T): T = {
    val start = System.currentTimeMillis()
    val result = code
    val stop = System.currentTimeMillis()
    timing.append(s"Processing $label took ${stop - start} ms.\n")
    result
  }
}
