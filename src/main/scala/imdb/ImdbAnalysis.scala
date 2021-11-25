package imdb

import math._
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
  val titleBasicsRDD: RDD[TitleBasics] = sc.textFile(ImdbData.titleBasicsPath).map(ImdbData.parseTitleBasics(_))

  // Hint: use a combination of `ImdbData.titleRatingsPath` and `ImdbData.parseTitleRatings`
  val titleRatingsRDD: RDD[TitleRatings] = sc.textFile(ImdbData.titleRatingsPath).map(ImdbData.parseTitleRatings(_))

  // Hint: use a combination of `ImdbData.titleCrewPath` and `ImdbData.parseTitleCrew`
  val titleCrewRDD: RDD[TitleCrew] = sc.textFile(ImdbData.titleCrewPath).map(ImdbData.parseTitleCrew(_))

  // Hint: use a combination of `ImdbData.nameBasicsPath` and `ImdbData.parseNameBasics`
  val nameBasicsRDD: RDD[NameBasics] = sc.textFile(ImdbData.nameBasicsPath).map(ImdbData.parseNameBasics(_))

  



  def task1(rdd: RDD[TitleBasics]): RDD[(Float, Int, Int, String)] = {
  	val filteredRDD = rdd
  	.filter(x => x.runtimeMinutes.getOrElse(0) != 0 && x.genres.getOrElse(List())!= List())
  	
  	// val filteredRDD = rdd
  	// .filter(x => x.runtimeMinutes.getOrElse(0) != 0 && x.genres.getOrElse(List())!= List())
  	
  	val genreRuntime = filteredRDD
  	.flatMap(x => x.genres.getOrElse(List()).map(y=> (y,x.runtimeMinutes.getOrElse(-1))))
  	
  	// genreRuntime.take(5).foreach(println)
  	// val test =genreRuntime.flatMap(x => x)
  	// test.take(5).foreach(println)
  	// println(genreRuntime)

  	// (sumTime,numMovies), minTime,MaxTime
  	type GenreCollector = (Int,Int,Int,Int)
  	// (sumTime,numMovies), minTime,MaxTime , GENRE
  	type GenreData = (String,(Int,Int,Int,Int))


  	val createGenreCombiner = (runtime:Int) => (runtime,1,runtime,runtime)

  	
  	val genreCombiner = (collector: GenreCollector, runtime: Int) => {
  		val (cumTime,numMovies,minTime,maxTime) = collector
  		(cumTime+runtime,numMovies+1,math.min(runtime,minTime),math.max(runtime,maxTime))
  	}

  	val genreMerger = (c1: GenreCollector, c2: GenreCollector) => {
  		val (cumTime1,numMovies1,minTime1,maxTime1) = c1
  		val (cumTime2,numMovies2,minTime2,maxTime2) = c2

  		(cumTime1+cumTime2,numMovies1+numMovies2,math.min(minTime1,minTime2),math.max(maxTime1,maxTime2))
  	}


  	val results: RDD[(GenreData)] = genreRuntime.combineByKey(createGenreCombiner,genreCombiner,genreMerger)

  	// println(results)

  	val formatResults = (aGenre: GenreData) => {
  		val (genre, (cumTime,numMovies,minTime,maxTime)) = aGenre

  		(cumTime.toFloat/numMovies,minTime,maxTime,genre)
  	}

  	val formattedResults = results.map(formatResults)

  	// print(formattedResults)	

  	// formattedResults.foreach(println)
  	
    val data = List((1.23f, 1,1,"bruh"))
    val rdd2 = sc.parallelize(data)
    // println(rdd2)
    // return rdd2
    return formattedResults
  }

  def task2(l1: RDD[TitleBasics], l2: RDD[TitleRatings]): RDD[String] = {
    // val filtered  =  l1.filter(_.runtimeMinutes.getOrElse(-1)>= 0) 

 	val filteredRating = l2.filter(x => (x.numVotes >= 500000 && x.averageRating >= 7.5)).map( x => (x.tconst,true))
 	// val filteredRating = l2.map(x => (x.tconst,x.numVotes >= 500000 && x.averageRating >= 7.5))
 	// println(filteredRating)
 	// println(filteredRating.lookup("t01"))
 	val filteredTitles = l1.filter( x => (x.runtimeMinutes.getOrElse(-1)>=0 && (1990 <= x.startYear.getOrElse(0) && x.startYear.getOrElse(0) <= 2018) && x.titleType.getOrElse("") == "movie"))
 	 .map(x => (x.tconst,x.primaryTitle.getOrElse("")))
 	// val filteredTitles = l1.map(x => (x.tconst,(x.runtimeMinutes.getOrElse(-1)>=0 && (1990 <= x.startYear.getOrElse(0) && x.startYear.getOrElse(0) <= 2018) && x.titleType.getOrElse("") == "movie")))

	val joined = filteredTitles.join(filteredRating).map(x => x._2._1)

	return joined

    // return rdd2



  }

  def task3(l1: RDD[TitleBasics], l2: RDD[TitleRatings]): RDD[(Int, String, String)] = {
    ???
  }

  // Hint: There could be an input RDD that you do not really need in your implementation.
  def task4(l1: RDD[TitleBasics], l2: RDD[TitleCrew], l3: RDD[NameBasics]): RDD[(String, Int)] = {
    // val validMovies = l1
    // .filter(x => ((1990 <= x.startYear.getOrElse(0) && x.startYear.getOrElse(0) <= 2018) && x.titleType.getOrElse("") == "movie"))
    // .map(x => (x.tconst,1))
  	
  	val validMovies = l1.map(x => { if((2010 <= x.startYear.getOrElse(0) && x.startYear.getOrElse(0) <= 2021) && x.titleType.getOrElse("") == "movie") (x.tconst,1) else (x.tconst,0)
     }) 
  	println("bruh1")
  	val movies = sc.broadcast(validMovies.collectAsMap())
	
	def param0= (accu:Int, v:String) => accu + movies.value.getOrElse(v,0)
  	def param1= (accu1:Int, accu2:Int) => accu1 + accu2
	val Dudes = l3.map(x => (x.primaryName.getOrElse(""),x.knownForTitles.getOrElse(List()).aggregate(0)(param0,param1)))
	val validDudes = Dudes.filter(x => x._2 >1)
	
	println("bruh")
	// validMovies.foreach(println)



	// val ans = validDudes.map(x => x.aggregate(0)(param0,param1))
    validDudes.foreach(x=>println(x)) 
    // ans.foreach(println)
    println("bruh2")
    // val validDudes = l2.map(x => (x.))

    val data = List(("bruh",1))
    val rdd2 = sc.parallelize(data)
    // println(rdd2)
    // return rdd2
    return validDudes 
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
