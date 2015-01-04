from pyspark import SparkContext
import Sliding, argparse

def bfs_map(curr_position):
    """
    Takes in a key-value pair of a board position and its level
    """
    if (curr_position[1] != level):
        return [curr_position]
    return [curr_position] + [(Sliding.board_to_hash(WIDTH, HEIGHT, neighbor), level+1) for neighbor in Sliding.children(WIDTH, HEIGHT, Sliding.hash_to_board(WIDTH, HEIGHT, curr_position[0]))]

def bfs_reduce(level1, level2):
    """
    Gathers levels for every neighbor reachable from original position,
    and emits a key-value pair containing the neighbor and the shortest path
    to it
    """
    return min(level1, level2)


# def bfs_filter((position, curr_level)):
#     """
#     Filters key-value pairs to just the levels that match with global level
#     """
#     return curr_level == level

def solve_puzzle(master, output, height, width, slaves):
    """
    Solves a sliding puzzle of the provided height and width.
     master: specifies master url for the spark context
     output: function that accepts string to write to the output file
     height: height of puzzle
     width: width of puzzle
    """
    # Set up the spark context. Use this to create your RDD
    sc = SparkContext(master, "python")

    # Global constants that will be shared across all map and reduce instances.
    # You can also reference these in any helper functions you write.
    global HEIGHT, WIDTH, level

    # Initialize global constants
    HEIGHT=height
    WIDTH=width
    level = 0 # this "constant" will change, but it remains constant for every MapReduce job

    # The solution configuration for this sliding puzzle. You will begin exploring the tree from this node
    sol = Sliding.board_to_hash(WIDTH, HEIGHT, Sliding.solution(WIDTH, HEIGHT))


    """ YOUR MAP REDUCE PROCESSING CODE HERE """
    sol = sc.parallelize([(sol, level)])
    post_num_levels = 0
    pre_num_levels = -1
    while (post_num_levels > pre_num_levels):
        if (level%5 == 0):
            pre_num_levels = sol.count()
        sol = sol.repartition(16).flatMap(bfs_map).reduceByKey(bfs_reduce)
        if (level%5 == 0):
            post_num_levels = sol.count()
        level = level+1

    """ YOUR OUTPUT CODE HERE """
 #for line in sol.collect():
        #output(str(line))

    sol.coalesce(slaves).saveAsTextFile(output)

    sc.stop()



""" DO NOT EDIT PAST THIS LINE

You are welcome to read through the following code, but you
do not need to worry about understanding it.
"""

def main():
    """
    Parses command line arguments and runs the solver appropriately.
    If nothing is passed in, the default values are used.
    """
    parser = argparse.ArgumentParser(
            description="Returns back the entire solution graph.")
    parser.add_argument("-M", "--master", type=str, default="local[8]",
            help="url of the master for this job")
    parser.add_argument("-O", "--output", type=str, default="solution-out",
            help="name of the output file")
    parser.add_argument("-H", "--height", type=int, default=2,
            help="height of the puzzle")
    parser.add_argument("-W", "--width", type=int, default=2,
            help="width of the puzzle")
    parser.add_argument("-S", "--slaves", type=int, default=6,
            help="number of slaves executing the job")
    args = parser.parse_args()

    global PARTITION_COUNT
    PARTITION_COUNT = args.slaves * 16

    # call the puzzle solver
    solve_puzzle(args.master, args.output, args.height, args.width, args.slaves)

# begin execution if we are running this file directly
if __name__ == "__main__":
    main()
