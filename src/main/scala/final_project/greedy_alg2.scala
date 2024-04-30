package final_project

import scala.io.Source
import java.io.{File, PrintWriter}

object greedy_alg2 {
  def main(args: Array[String]): Unit = {
    val filename = "C:\\Users\\Peter\\OneDrive\\Documents\\Data-Processing-Final-Project\\data\\soc-LiveJournal1.csv"
    val outputFilename = "C:\\Users\\Peter\\OneDrive\\Documents\\Data-Processing-Final-Project\\output\\soc-LiveJournal1_matching.csv"
    val graph = loadGraphFromFile(filename)

    // Start time
    val startTime = System.nanoTime()

    val matching = findMaximalMatching(graph)

    // End time and calculate duration
    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1e9d // Convert to seconds
    println(s"Algorithm runtime: $duration seconds")

    println("Saving Matching to csv")
    saveMatchingToFile(matching, outputFilename)
  }

  // Function to load graph from a CSV file
  def loadGraphFromFile(filename: String): Array[(Int, Int)] = {
    val bufferedSource = Source.fromFile(filename)
    val edges = bufferedSource.getLines().map { line =>
      val parts = line.split(",").map(_.trim.toInt)
      (parts(0), parts(1))
    }.toArray
    bufferedSource.close()
    edges
  }

  // Function to find a maximal matching in a graph
  def findMaximalMatching(graph: Array[(Int, Int)]): Set[(Int, Int)] = {
    var matching = Set.empty[(Int, Int)]
    var visited = Set.empty[Int]
    for ((u, v) <- graph if !visited.contains(u) && !visited.contains(v)) {
      matching += ((u, v))
      visited += u
      visited += v
    }
    matching
  }

  // Function to save the matching to a CSV file
  def saveMatchingToFile(matching: Set[(Int, Int)], filename: String): Unit = {
    val writer = new PrintWriter(new File(filename))
    matching.foreach {
      case (u, v) => writer.println(s"$u,$v")
    }
    writer.close()
  }
}
