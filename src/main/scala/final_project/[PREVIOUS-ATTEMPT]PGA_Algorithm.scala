package final_project

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object GraphMatchingApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Graph Matching Application")
    val sc = new SparkContext(conf)

    val graphPaths = List(
      //"/Users/rafael/Desktop/BigData/final_project/data/twitter_original_edges.csv",
      //"/Users/rafael/Desktop/BigData/final_project/data/soc-LiveJournal1.csv",

      "/Users/rafael/Desktop/BigData/final_project/data/log_normal_100.csv",
      //"/Users/rafael/Desktop/BigData/final_project/data/soc-pokec-relationships.csv",
      //"/Users/rafael/Desktop/BigData/final_project/data/musae_ENGB_edges.csv",
    )

    graphPaths.foreach { path =>
      println("\n\n\n=============================== Starting processing for file: " + path)

      val rawEdges: RDD[(Long, Long)] = sc.textFile(path)
        .map(_.split(","))
        .map(parts => (parts(0).trim.toLong, parts(1).trim.toLong))
        .distinct()

      println(s"Raw edges count: ${rawEdges.count()}")

      var vertices: RDD[(Long, Boolean)] = rawEdges
        .flatMap { case (src, dst) => Seq(src, dst) }
        .distinct()
        .map(vertexId => (vertexId, false)) // All vertices are initially unmatched

      println(s"Initial unmatched vertices count: ${vertices.filter(!_._2).count()}")

      var matchedEdges = sc.emptyRDD[(Long, Long)]
      var active = true

      while (active) {
        val unmatchedVertices = vertices.filter(!_._2).collectAsMap()
        val broadcastUnmatched = sc.broadcast(unmatchedVertices)

        println(s"Unmatched vertices available for matching: ${broadcastUnmatched.value.size}")

        val eligibleEdges = rawEdges
          .filter { case (src, dst) =>
            broadcastUnmatched.value.contains(src) && broadcastUnmatched.value.contains(dst) && src != dst
          }
          .distinct()

        println(s"Eligible edges count: ${eligibleEdges.count()}")
        eligibleEdges.take(10).foreach(println)

        if (eligibleEdges.isEmpty()) {
          println("No eligible edges found, stopping iteration.")
          active = false
        } else {
          val firstMatch = eligibleEdges.first()
          println(s"Selecting edge to match: $firstMatch")
          matchedEdges = matchedEdges.union(sc.parallelize(Seq(firstMatch)))

          vertices = vertices.map {
            case (id, matched) if id == firstMatch._1 || id == firstMatch._2 => (id, true)
            case (id, matched) => (id, matched)
          }
        }
      }

      val matchedCount = matchedEdges.count()
      println(s"Total number of matched edges: $matchedCount")

      val outputFilePath = path.replace(".csv", "_matching.csv")
      matchedEdges.map { case (src, dst) => s"$src,$dst" }.saveAsTextFile(outputFilePath)
      println(s"Matching saved to: $outputFilePath")
    }

    sc.stop()
  }
}
