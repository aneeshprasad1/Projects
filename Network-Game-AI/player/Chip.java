/* Chip.java */

package player;

/**
 *  An implementation of a single Chip on the GameBoard. Keeps track of
 *  its position on the GameBoard, its color, and connected Chips. Can
 *  get Chip's colo, position, and connections as well as set Chip's
 *  position, and connections. Can check whether or not it has a
 *  connection in a direction, and even remove a connection.
 **/

public class Chip { // default ... or private?    
    int x, y;
    int color;
    GameBoard board;
    Chip[][] connections;

    public Chip(GameBoard board, int x, int y, int color) {
    	this.board = board;
    	this.x = x;
    	this.y = y;
    	this.color = color;
        connections = new Chip[3][3];
    }

    /**
     *  getColor() gets color of "this" Chip
     *  @return either 0 (black) or 1 (white)
     **/
    public int getColor() {
        return color;
    }

    /**
     *  setPosition() sets "this" Chip's position to the given coordinates
     *  @param x which is the x-coordinate
     *  @param y which is the y-coordinate
     **/

    public void setPosition(int x, int y) {
      	this.x = x;
        this.y = y;
    }

    /**
     *  getPosition() gets "this" Chip's position
     *  @return an array of coordinates (x, y)
     **/    
    public int[] getPosition() {
      	int[] position = {x, y};
      	return position;
    }

    /**
     *  hasConnection() returns true if "this" Chip has a connection to any
     *  other Chip in the Direction "dir"
     *  @param is a number representing the desired direction
     *  @return true if connection exists, false otherwise
     **/
    public boolean hasConnection(int dir) {
        int x1 = Direction.indexX(dir);
        int y1 = Direction.indexY(dir);
        return (connections[x1][y1] != null);
    }

    /**
     *  setConnection() sets "this" Chip's connection in the given Direction
     *  "dir" to the passed in Chip "c".
     *  @param dir is a number representing the desired direction
     *  @param c is the nearest Chip in the given Direction "dir"
     **/

    public void setConnection(int dir, Chip c) {
        int x1 = Direction.indexX(dir);
        int y1 = Direction.indexY(dir);
        //System.out.println("Connection set from chip " + this + "to " + c);
        connections[x1][y1] = c;
    }

    /**
     *  getConnection() gets the nearest Chip in the given Direction
     *  "dir".
     *  @param dir is a number representing the desired direction
     *  @return the nearest Chip in the Direction "dir"
     **/
    public Chip getConnection(int dir) {
        int x1 = Direction.indexX(dir);
        int y1 = Direction.indexY(dir);
        //System.out.println("Connection of chip " + this + " at direction " + dir + " is " + connections[x1][y1]);
        return connections[x1][y1];
    }

    /**
     *  removeConnection() changes the Chip's connection in the given
     *  Direction "dir" to null.
     *  @param dir is a number representing the desired Direction
     **/
    public void removeConnection(int dir) {
        int x1 = Direction.indexX(dir);
        int y1 = Direction.indexY(dir);
        connections[x1][y1] = null;
    }
    
    /**
     *  toString() returns a String representation of this Chip
     *  @return a String representation of this Chip
     **/
    public String toString() {
        String s = "(" + x + ", " + y + ")";
        return s;
    }
}
