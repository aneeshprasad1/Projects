/* Direction.java */

package player;

/**
 *  Direction is a repository for moving in directions
 *  
 *       NW  N  NE                           7  8  9
 *       W       E corresponds to the ints:  4     6
 *       SW  S  SE                           1  2  3
 *
 *  This class creates an easy interface to increment coordinates
 *  as well as access the nearest index on 2D arrays in a certain
 *  direction. Can also find the opposite of a direction
 **/

public class Direction {

    public static final int SW = 1;
    public static final int S = 2;
    public static final int SE = 3;
    public static final int W = 4;
    public static final int E = 6;
    public static final int NW = 7;
    public static final int N = 8;
    public static final int NE = 9;
    public static final int[] directions = {SW, S, SE, W, E, NW, N, NE};

    /**
     *  incrementX() increments across the board in the x-direction
     *  based on the direction
     *  @param dir is a number representing the Direction
     *  @return a number to add to the current x-coordinate to
     *          move in the given direction on an array
     **/
    public static int incrementX(int dir) {
        if ((dir % 3 == 0)) { // 3, 6, 9
            return 1;
        }
        else if ((dir % 3 == 1)) { // 1, 4, 7
            return -1;
        }
        else { // 2, 8
            return 0;
        }
    }

    /**
     *  incrementY() increments across the board in the y-direction
     *  based on the direction
     *  @param dir is a number representing the Direction
     *  @return a number to add to the current y-coordinate to
     *          move in the given direction on an array
     **/
    public static int incrementY(int dir) {
        if ((dir > 6)) { //7, 8, 9
            return -1;
        }
        else if ((dir < 4)) { // 1, 2, 3
            return 1;
        }
        else { // 4, 6
            return 0;
        }
    }

    /**
     *  indexX() returns a number representing the index
     *  in an array centered around the current x, y values
     *  @param dir is a number representing the Direction
     *  @return a number of the x-coordinate that corresponds
     *          to the entry in the given Direction
     **/
    public static int indexX(int dir) {
        return 1 + incrementX(dir);
    }

    /**
     *  indexY() returns a number representing the index
     *  in an array centered around the current x, y values
     *  @param dir is a number representing the Direction
     *  @return a number of the y-coordinate that corresponds
     *          to the entry in the given Direction
     **/
    public static int indexY(int dir) {
        return 1 + incrementY(dir);
    }

    /**
     *  opposite() returns a number representing the Direction
     *  corresponding to the entry opposite the given Direction
     *  "dir".
     *  @param dir is a number representing a Direction
     *  @return a number representing the Direction
     *          opposite to "dir"
     **/
    public static int opposite(int dir) {
        return (10 - dir);
    }
}
