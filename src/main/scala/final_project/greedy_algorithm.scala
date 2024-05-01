package final_project

import scala.io.Source
import scala.util.Random
import java.io.{File, PrintWriter}

object greedy_algorithm {
  def main(args: Array[String]): Unit = {
    val filename = "input/file/here"
    val outputFilename = "output/file/here"
    val samplingRate = 1 // we do not sample anymore

    val startTime = System.nanoTime()

    val matching = findMaximalMatching(filename, samplingRate)

    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1e9d
    println(s"Algorithm runtime: $duration seconds")

    println("Saving Matching to csv")
    saveMatchingToFile(matching, outputFilename)
  }

  def findMaximalMatching(filename: String, samplingRate: Double): Set[(Int, Int)] = {
    val random = new Random()
    var matching = Set.empty[(Int, Int)]
    var visited = Set.empty[Int]
    var iteration = 0 

    val bufferedSource = Source.fromFile(filename)
    for (line <- bufferedSource.getLines()) {
      iteration += 1 
      if (random.nextDouble() < samplingRate) {
        val parts = line.split(",").map(_.trim.toInt)
        val u = parts(0)
        val v = parts(1)
        if (!visited.contains(u) && !visited.contains(v)) {
          matching += ((u, v))
          visited += u
          visited += v
        }
      }
    }
    bufferedSource.close()
    println(s"Total iterations: $iteration") 
    matching
  }

  def saveMatchingToFile(matching: Set[(Int, Int)], filename: String): Unit = {
    val writer = new PrintWriter(new File(filename))
    matching.foreach {
      case (u, v) => writer.println(s"$u,$v")
    }
    writer.close()
  }
}
