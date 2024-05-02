# Large Scale Data Processing: Final Project

## Overview
In this project, we tackled the problem of finding large matchings in various undirected graphs provided as part of the course requirements. We explored several algorithms, faced challenges related to system limitations, and implemented solutions that combined traditional and innovative approaches to achieve optimal results.

## Team Members
- Rafael Espinoza
- George Arianas
- Nathan French


## Algorithms Implemented

### Path Growing Algorithm

The Path Growing Algorithm was our initial choice for smaller datasets due to its conceptual simplicity and efficiency in less complex graph structures. This algorithm works by incrementally building a matching. It selects paths and cycles within the graph and adds edges to the matching that are not adjacent to already chosen edges, continuing this process until no further additions can be made.

#### Theoretical Accuracy and Runtime

The accuracy of the Path Growing Algorithm in terms of matching size can be approximated by an efficiency factor (ε), where the matching produced is at least a fraction ε of the maximum possible matching size. In typical implementations, ε often approaches 1/2. This means that in the best-case scenario, the algorithm can guarantee a matching that is at least half the size of the maximum matching. However, this is highly dependent on the structure of the graph and the distribution of its edges. Sparse and less complex graphs tend to yield closer approximations to this theoretical upper bound, while denser and more complex graphs might see a reduction in efficiency.

The runtime of the algorithm largely depends on the number of vertices and edges in the graph. Since each iteration of the algorithm attempts to find and add non-adjacent edges to the matching, the process is bound by the operations of checking adjacency and updating the set of unmatched vertices. These operations can become computationally expensive as the size of the graph increases, leading to higher memory and processing time requirements.

#### Implementation in Spark

We implemented the Path Growing Algorithm using Apache Spark to leverage its capabilities in handling large datasets through distributed computing. Here’s a simplified breakdown of how our Spark implementation processes a graph:

1. **Initialization**: We start by reading the graph's edges from a CSV file into an RDD (Resilient Distributed Dataset). Each vertex is initially marked as unmatched.

2. **Processing Loop**:
    - In each iteration, we identify the unmatched vertices and filter the edges to find those that connect two unmatched vertices.
    - From these eligible edges, we select the first available edge (for simplicity in this implementation) and mark the involved vertices as matched.
    - We update our list of matched edges and unmatched vertices accordingly.

3. **Termination**: The loop continues until no more eligible edges are found, indicating that no further matching can be done without violating the algorithm's constraints.

4. **Output**: The matched edges are then saved back to a CSV file, providing the output required for verification.

Our implementation includes debug statements that output key metrics at each step, such as the number of raw edges, unmatched vertices, and eligible edges. These logs are crucial for monitoring the algorithm's progress and for debugging purposes.

#### Memory Constraints and Limitations

While the Path Growing Algorithm is efficient for smaller or simpler graphs, we encountered significant memory constraints when applying it to larger datasets. The main challenge was the collection and broadcasting of unmatched vertices across iterations, which became increasingly memory-intensive as the size of the graph grew. This limitation prompted us to explore alternative methods for the larger datasets, leading to the development of our enhanced greedy algorithm with streaming capabilities.

In summary, the Path Growing Algorithm serves as a robust starting point for graph matching problems, particularly for smaller datasets. However, its scalability is limited in the context of very large or dense graphs, necessitating more sophisticated approaches to manage computational and memory efficiency effectively.


#### Blossom Algorithm

# Approach
Graph Representation: Start by representing the problem as a graph. Each node in the graph represents an element, and each edge represents a potential match between two elements.
Initialization: Begin with an empty matching.
Augmenting Paths: Iteratively find augmenting paths, which are paths in the graph that start and end at unmatched vertices, alternating between matched and unmatched edges. These paths are used to increase the size of the matching.
Blossom Contraction: When searching for augmenting paths, use blossom contraction to efficiently handle cycles in the graph. Blossom contraction merges an odd length cycle into a single vertex, reducing the size of the graph and meaning that we can employ a simpler (Hopcroft-Karp) bipartite graph matching algorithm on the now even length cycle.
Matching Update: Update the matching based on the augmenting paths found (flip the status of matched and unmatched edges along the augmenting paths).
Repeat: Continue the process until no more augmenting paths can be found.
# Complexity
Time Complexity: The time complexity of the Blossom algorithm, depending on implementation, is O(V^3) or O(V^2*E), where V is the number of vertices in the graph, and E is the number of edges. 
Space Complexity: The space complexity depends on the data structures used to represent the graph. In our case, its O(V^2) due to the adjacency list representation and need to store the matching
### Greedy Algorithm with Enhancements

After discussing our initial results with the professor, we opted to adopt a greedy algorithm. Our first implementation of this algorithm proved successful for the smaller datasets, particularly for `log_normal_100.csv` and `musae_ENGB_edges.csv`. This approach, straightforward in its attempt to pair unmatched vertices greedily, achieved quick and efficient matchings due to the manageable size and lower complexity of these graphs.

#### First Implementation and Initial Success

The greedy algorithm began by attempting to match each vertex with an adjacent vertex that had not yet been paired, ensuring that no vertex was part of more than one matched pair. This method worked well within the memory and computational constraints of our local environment for the initial datasets, demonstrating both the efficiency and simplicity of the greedy approach in less demanding scenarios.

#### Encountering Memory Limitations

As we scaled our approach to larger datasets, our local machine's memory constraints were quickly reached. The subsequent datasets, beginning with `soc-pokec-relationships.csv`, prompted us to incorporate a sampling method. By randomly selecting edges based on a predefined sampling rate, we managed to process larger portions of the data without overloading our system's memory. This technique allowed us to extend our greedy algorithm's applicability to slightly larger datasets, successfully handling `soc-pokec-relationships.csv`.

#### Code Insight: Sampling Implementation

Here's how we implemented the sampling within our greedy algorithm:

```scala
val random = new Random()
var matching = Set.empty[(Int, Int)]
var visited = Set.empty[Int]
var iteration = 0 // Counter to keep track of iterations

val bufferedSource = Source.fromFile(filename)
for (line <- bufferedSource.getLines()) {
  iteration += 1 // Increment iteration counter
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
println(s"Total iterations: $iteration") // Output the total iterations
```
#### Transition to Streaming

However, as we moved to even larger datasets, it became clear that sampling alone would not suffice due to the immense scale of the data. To address this, we shifted our strategy to stream data, processing edges as they were read without loading the entire graph into memory. This transition marked a significant improvement in our capability to process vast datasets efficiently. Notably, this streaming approach enabled us to handle the largest dataset, `com-orkut.ungraph.csv`, which has over 117 million edges, and run it sequentially in just 84.24 seconds on a locally run Mac. This implementation removed the need for sampling and allowed for continuous data processing, leading to optimal results.

#### Code Snippet
```scala
    val bufferedSource = Source.fromFile(filename)
    for (line <- bufferedSource.getLines()) {...
```

#### Theoretical Bounds and Efficiency

The theoretical efficiency of our greedy algorithm can be described in terms of an efficiency factor (ε), where ε typically approaches 1 in ideal conditions but may vary depending on the dataset's complexity and density. Under the conditions of our enhanced greedy approach, we observed that the efficiency was consistently high, managing near-optimal matchings especially as we optimized our approach with streaming.

#### Summary

In summary, our enhanced greedy algorithm started with straightforward implementations for smaller datasets and evolved through adaptations like sampling and streaming to overcome significant computational challenges. This evolution not only showcased the adaptability of greedy techniques in graph matching problems but also highlighted the importance of tailoring data processing strategies to the available computational resources and the specific demands of each dataset.

## Algorithm Comparisons

In this project, we employed multiple algorithms to address graph matching challenges across various datasets. Here's how they stacked up against each other:

- **Path Growing vs. Greedy Algorithm**: For smaller datasets like `log_normal_100.csv`, both algorithms performed efficiently. However, as dataset size increased, the Greedy algorithm, enhanced with streaming, proved more scalable than the Path Growing approach.
- **Blossom Algorithm**: 
This is the least scalable algorithm of the 3, running in O(∣E∣∣V∣^2) time. The implementation is also much more difficult. However, if the graph isn't too big, it will return an exact maximum matching correctly, so we wouldn't have to worry about suboptimal solutions. 

## System Performance Summary
- **CPU Load and Usage**: Load Average: 1.99 (1 min), 3.06 (5 min), 5.08 (15 min); CPU Utilization: 9.52% user, 5.5% sys
- **Memory Utilization**: 8134 MB used, with 2123 MB wired; significant swap activity during heavy processing loads
- **Disk and Network Activity**: 174 GB read and 116 GB written; Network traffic of 7955 MB outgoing and 1580 MB incoming

## Results

| Graph File                 | Number of Edges | Size of Matching | Sampling Rate | Ratio (Optimal/Our) | Iterations   | Time             |
|----------------------------|-----------------|------------------|---------------|---------------------|--------------|------------------|
| com-orkut.ungraph.csv      | 117,185,083     | 1,323,954        | 1             | 0.939               | 117,185,083  | 84.24 seconds    |
| twitter_original_edges.csv | 63,555,749      | 91,636           | 1             | 0.974               | 63,555,749   | 28.81 seconds    |
| soc-LiveJournal1.csv       | 42,851,237      | 1,543,228        | 1             | 0.867               | 42,851,237   | 36.01 seconds    |
| soc-pokec-relationships.csv| 22,301,964      | 587,601          | 1             | 0.880               | 22,301,964   | 17.32 seconds    |
| musae_ENGB_edges.csv       | 35,324          | 2,424            | 1             | 0.830               | 35,324       | 0.115 seconds    |
| log_normal_100.csv         | 2,671           | 48               | 1             | 0.960               | 2,671        | 0.045 seconds    |

## Discussion
Our project's core achievement was developing a scalable solution that adapts to graph size and complexity. The transition from in-memory data handling to a streaming data approach proved critical, allowing our algorithms to perform efficiently without the constraints of hardware limitations. Our results demonstrate a high degree of efficiency and competitiveness with the best outcomes in the previous class.

## Future Work

Our exploration into graph matching algorithms revealed several avenues for future research:

- **Algorithm Optimization**: Further tuning of the greedy and path growing algorithms could enhance their efficiency and scalability.
- **Hybrid Approaches**: Combining elements of different algorithms could yield better performance across diverse dataset characteristics.
- **Real-World Applications**: Applying our findings to real-world datasets in social networking or bioinformatics could validate their practical utility and reveal new challenges.
- **Issues with Streaming Approach, try Blossom Algorithm**: Our current streaming approach leads can lead to suboptimal solutions in certain cases (thank you Professor Su for pointing this out). For example, consider a test case involving edges {2,3}, {1,2}, {3,4}. Our current method might would only make a suboptimal matching of size 1, where the optimal matching clearly has length 2. A way we could circumvent this drawback is by changing from a streaming algorithm to an algorithm like the Blossom Algorithm we considered using. With a Blossom Algorithm, we would generate better quality matches, but the tradeoff is in time complexity, where we would have to pay a steep price, as the algorithm runs in O(|E||V^2|) time. However, if we use the same Blossom Algorithm while only considering paths of length 3 (so it wouldn't actually be a blossom algorithm at all since it wouldn't find blossoms, but it could still be used as such regardless), this would run very fast, not have issues with cases like the streaming algorithm, but like the streaming algorithm, generates a worse matching than the full Blossom algorithm would.

## Submission Details
This project was submitted via GitHub. The complete codebase, including the algorithm implementation and output files, can be found at the repository link provided in the Canvas assignment submission. The output files were also validated using the provided verifier to ensure correctness of the matchings.
