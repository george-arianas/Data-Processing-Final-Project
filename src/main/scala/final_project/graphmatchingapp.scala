package final_project

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import scala.util.Try

object GraphMatchingApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Graph Matching Application")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder.config(conf).getOrCreate()

    val graphPaths = List(
      "/Users/rafael/Desktop/BigData/final_project/data/log_normal_100.csv"
    )

    graphPaths.foreach { path =>
      println("\n\n\n=============================== Starting processing for file: " + path)

      val edges = sc.textFile(path)
        .map(_.split(","))
        .map(parts => (Try(parts(0).trim.toLong).getOrElse(-1L), Try(parts(1).trim.toLong).getOrElse(-1L)))
        .filter { case (src, dst) => src != -1L && dst != -1L }
        .map { case (src, dst) => if (src < dst) (src, dst) else (dst, src) }
        .distinct()

      println("\n\n\n=============================== Number of edges loaded: " + edges.count())

      val matched = edges
        .flatMap { case (src, dst) => Seq((src, dst), (dst, src)) }
        .reduceByKey((a, b) => if (a < b) a else b)
        .filter { case (src, dst) => src < dst }
        .map { case (src, dst) => s"$src,$dst" }

      println("\n\n\n=============================== Number of matching edges: " + matched.count())

      val outputFilePath = path.replace(".csv", "_matching.csv")
      matched.saveAsTextFile(outputFilePath)
      println("\n\n\n=============================== Matching saved to: " + outputFilePath)
    }

    sc.stop()
    spark.stop()
  }
}
