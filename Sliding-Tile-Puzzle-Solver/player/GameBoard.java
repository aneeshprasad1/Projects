/* GameBoard.java */

package player;
import list.*;

/**
 *  GameBoard is a representation of the gameboard that is the main interface
 *  that the MachinePlayer uses. It contains a representation of the board as
 *  well as the placed Chips. GameBoard can make and undo Moves. It also 
 *  interfaces with Chips, by getting, setting, moving, and checking for Chips 
 *  ar given coordinates. Contains methods to cheseck for valid networks and 
 *  also search for longest networks. Can check whether a move is valid and 
 *  list all valid moves on a given board. Finally, contains heuristic to 
 *  evaluate the score for a board between -1 and 1 representing how likely it 
 *  is to win from this board.
 **/

public class GameBoard { // default ... or private?    
   
    Chip[][] board;
    DList[] placedChips; // whiteChips and blackChips
    int[] directions = Direction.directions; // {SW, S, SE, W, E, NW, N, NE}
    int whoseTurn;

    public GameBoard() {
        board = new Chip[8][8]; // 8 by 8 board
        placedChips = new DList[2];
        placedChips[0] = new DList();
        placedChips[1] = new DList();
        whoseTurn = 1;
    }

    /** 
     * performs the specified move on the GameBoard and updates the Gameboard
     * @param m is the specified move
     * @param color specifies what kind of chip will be placed
     **/
    public void makeMove(Move m, int color) {
        whoseTurn = 1 - whoseTurn;
        if (m.moveKind == 0) {
	    System.out.println("Good game!"); // quit?
        } else if (m.moveKind == 1) {
            setChip(m.x1, m.y1, color);
        } else { // m.moveKind == 2
            moveChip(m.x1, m.y1, m.x2, m.y2);
        }
    }

    /**
     * undos a performed move on the GameBoard and updates the GameBoard
     * @param m specifies the kind of move that was performed
     */
    public void undoMove(Move m) { // undos move! 
        // for add move, use removeChip
        // for step moves, use moveChip with reversed x1, y1
        whoseTurn = 1 - whoseTurn;
        if (m.moveKind == 1) {
            removeChip(m.x1, m.y1);
        } else {
            moveChip(m.x2, m.y2, m.x1, m.y1);
        }
    }

    /**
     *  checks to make sure that the square is on the board and isn't any of 
     *  the four corners where it is illegal to place a chip
     *  @param x, y are the coordinates of the square on the gameboard
     **/
    private boolean isValidSquare(int x, int y) {
        return ((x >= 0) && (x < 8) && (y >= 0) && (y < 8) &&
                !( ((x == 0) || (x == 7)) && ((y == 0) || (y == 7)) ));
    }

    /**
     *  checks if the GameBoard has a chip placed at the specified coordinates
     *  @param x, y are the coordinates of the square on the gameboard
     **/
    private boolean hasChip(int x, int y) {
        if (!isValidSquare(x, y)) {
            return false;
        }
    	return (board[x][y] != null);
    }

    /**
     *  returns the chip that is at the specified coordinates on the Gameboard
     *  @param x, y are the coordinates of the square on the gameboard
     **/
    private Chip getChip(int x, int y) {
        if (!hasChip(x, y)) {
            return null;
        }
        return board[x][y];
    }

    /**
     *  places a chip of a specific color at the specified coordinates on the 
     *  Gameboard
     *  @param x, y are the coordinates of the square on the gameboard
     *  @param color, is the color of the chip to be placed
     **/
    private void setChip(int x, int y, int color) {
        Chip c = new Chip(this, x, y, color);
        placedChips[color].insertBack(c);
        board[x][y] = c;
        makeConnections(c);
    }

    /**
     *  removes a chip at the specified coordinates on the Gameboard used for 
     *  undoing moves
     *  @param x, y are the coordinates of the square on the gameboard
     **/
    private void removeChip(int x, int y) { // this is solely for undoing moves
        try {
	    Chip c = getChip(x, y);
	    removeConnections(c);
	    board[x][y] = null;
	    placedChips[c.getColor()].back().remove();
        } catch (InvalidNodeException e) {
            System.out.println("Oh no!");
        }
    }

    /**
     *  moves a chip at the specified coordinates on the gameboard to a new 
     *  position on the Gameboard
     *  @param x1, y1 are the original coordinates of the chip
     *  @param x2, y2 are the new coordinates of the chip
     **/
    private void moveChip(int x1, int y1, int x2, int y2) {
        Chip c = getChip(x2, y2);
        removeConnections(c);
        board[x2][y2] = null;
        c.setPosition(x1, y1);
        board[x1][y1] = c;
        makeConnections(c);
    }

    /** 
     *  makeConnections() is a helper function that crawls in every
     *  direction and makes the necessary connections both to and from
     *  the discovered Chips and Chip "c"
     *  @param c is the Chip that the connections are being made onto
     **/
    private void makeConnections(Chip c) {
        Chip tempC;
        int dir;
        int tempX;
        int tempY;

        for (int i = 0; i < 8; i++) {
            dir = directions[i];
            tempX = c.getPosition()[0] + Direction.incrementX(dir);
            tempY = c.getPosition()[1] + Direction.incrementY(dir);
            while (isValidSquare(tempX, tempY)) {
                if (hasChip(tempX, tempY)) {
                    tempC = getChip(tempX, tempY);
                    c.setConnection(dir, tempC);
                    tempC.setConnection(Direction.opposite(dir), c); 
                    break;
                }
                tempX += Direction.incrementX(dir);
                tempY += Direction.incrementY(dir);
            }
        }
    }

    /**
     *  crawls in all directions and removes connections that this Chip had 
     *  with other Chips
     *  @param c is the Chip that removes its current connections
     **/
    private void removeConnections(Chip c) {
        Chip tempC;
        int dir;
        for (int i = 0; i < 8; i++) {
            dir = directions[i];
            if (c.hasConnection(dir)) {
		tempC = c.getConnection(dir);
                tempC.setConnection(Direction.opposite(dir),
                                    c.getConnection(Direction.opposite(dir)));
            }
        }
        for (int i = 0; i < 8; i++) {
            c.removeConnection(directions[i]);
        }
    }

    /**
     *  hasValidNetwork checks for a certain color of chip if a network is found
     *  @param color is the color that all the chips in the network must be
     **/
    public boolean hasValidNetwork(int color) {
        // System.out.println("hasValidNetwork for color: " + color);
        boolean outcome = false;
        DList checked = new DList();
        if (color == 0) {
            for (int x = 1; x < 7; x++) {
                if (hasChip(x, 0)) {
                    if (validNetworkHelper(getChip(x, 0), checked, 
					   directions[3])) {
                        return true;
                    }
                }
            }
        } else {
            for (int y = 1; y < 7; y++) {
                if (hasChip(0, y)) {
                    if (validNetworkHelper(getChip(0, y), checked, 
					   directions[1])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *  helper method for hasValidNetwork that recurses through the several 
     *  connections that the current Chip has 
     *  @param c is the current Chip being evaluated for its connections
     *  @param checked is the list of checked chips that the network has gone 
     *         through
     * @param dir is the current direction that entered the current chip being 
     *        evaluated
     **/
    private boolean validNetworkHelper(Chip c, DList checked, int dir) {
        try {
            ListNode d;
            Chip connected;
            int dir1;
            if (c == null) {return false; } // this should no longer be necessary to check.
            d = checked.front();
            while (d.isValidNode()) {
                if (d.item() == c) {
                    return false;
                }
                d = d.next();
            }  

            checked.insertBack(c);

            // if chip is in the endzone ... it's over!
            if (c.getPosition()[1 - c.getColor()] == 7) {
                if (checked.length() >= 6) {
                    System.out.println("!");
                    return true; 
                }
                checked.back().remove();
                return false;
            }

            if ((checked.length() > 1) && 
		(c.getPosition()[1 - c.getColor()] == 0)) {
                checked.back().remove();
                return false;
            }

            for (int i = 0; i < 8; i++) {
                dir1 = directions[i];
                if ((dir1 == dir) || (dir1 == Direction.opposite(dir))) 
		    {continue; } // skip same direction
                if (!c.hasConnection(dir1)) {continue; }
                connected = c.getConnection(dir1);
                if (connected.getColor() != c.getColor()) {continue; }
                if (validNetworkHelper(connected, checked, dir1)) 
		    {return true; }
            }
            checked.back().remove();
            return false;
        } catch (InvalidNodeException e) {
            System.out.println("Oh no!");
        }
        return false;
    }

    /**
     *  finds the longest string of Chips that are connected of a certain color
     *  @param color is the color of the chips that form the string
     *  @return an array of length two containing first the length of the 
     *          longest network, and second the number of times it occurs
     **/

    // this should be implemented last (only part of evaluation)
    // returnvalue[0] = length of longest network
    // returnvalue[1] = number of times it occurs

    private int[] longestNetworkN(int color) {
        // System.out.println("longestNetworkN for color: " + color);
        int[] current = new int[2];
        DList checked = new DList();
        int n = 1;
        int maxLength = 0;
        if (color == 0) {
            for (int x = 1; x < 7; x++) {
                if (hasChip(x, 0)) {
                    current = longestNetworkHelper(getChip(x, 0), checked, 
						   directions[3], 0, 1);
                    if (current[0] > maxLength) {
                        maxLength = current[0];
                        n = current[1];
                    } else if (current[0] == maxLength) {
                        n += current[1];
                    }
                }
            }
        } else {
            for (int y = 1; y < 7; y++) {
                if (hasChip(0, y)) {
                    current = longestNetworkHelper(getChip(0, y), checked, 
						   directions[1], 0, 1);
                    if (current[0] > maxLength) {
                        maxLength = current[0];
                        n = current[1];
                    } else if (current[0] == maxLength) {
                        n += current[1];
                    }
                }
            }
        }
        current[0] = maxLength;
        current[1] = n;
        return current;
    }


    /**
     *  implemented similarly to validNetworkHelper
     *  recurses Chips and their connections to find the longest uninteruppted 
     *  string of Chips as well as find out how many times this length of 
     *  network appears on the board
     *  @param c is the current chip being evaluated for its connections
     *  @param checked is the list of checked chips
     *  @param dir is the direction that the current chip was entered from
     *  @param n is the number of times the network of a certain length has 
     *         been found
     *  @return an array of the length of the longest Network and the number
     *          of times it occurs
     **/
    private int[] longestNetworkHelper(Chip c, DList checked, 
                                       int dir, int maxLength, int n) {
        int[] current = {maxLength, n};
        try {
            ListNode d;
            Chip connected;
            int dir1;
            d = checked.front();
            while (d.isValidNode()) {
                if (d.item() == c) {
                    return current;
                }
                d = d.next();
            }

            checked.insertBack(c);

            if (checked.length() > maxLength) {
                maxLength = checked.length();
                n = 1;
            } else if (checked.length() == maxLength) {
                n += 1;
            }

            if (c.getPosition()[1 - c.getColor()] == 7) {
                current[0] = maxLength;
                current[1] = n;
                return current;
            }

            for (int i = 0; i < 8; i++) {
                dir1 = directions[i];
                if ((dir1 == dir) || (dir1 == Direction.opposite(dir))) 
		    {continue; }
                if (!c.hasConnection(dir1)) {continue; }
                connected = c.getConnection(dir1);
                if (connected.getColor() != c.getColor()) {continue; }
                current = longestNetworkHelper(connected, checked, dir1, 
					       maxLength, n);
                if (current[0] > maxLength) {
                    maxLength = current[0];
                    n = current[1];
                } else if (current[0] == maxLength) {
                    n += current[1];
                }
            }
            checked.back().remove();
            current[0] = maxLength;
            current[1] = n;
        } catch (InvalidNodeException e) {
            System.out.println("Oh no!");
        }
        return current;
    }

    /*

      DList checked = new DList();
      int n = 1;
      int maxLength = 0;
      int[] current = new int[2];
      if (color == 0) {
      for (int x = 1; x < 7; x++) {
      if (hasChip(x, 0)) {
      current = longestNetworkHelper(getChip(x, 0), checked, directions[3], 1);
      if (current[0] > maxLength) {
      maxLength = current[0];
      n = current[1];
      } else if (current[0] == maxLength) {
      n += current[1];
      }
      }
      }
      } else {
      for (int y = 1; y < 7; y++) {
      if (hasChip(0, y)) {
      current = longestNetworkHelper(getChip(0, y), checked, directions[1], 1);
      if (current[0] > maxLength) {
      maxLength = current[0];
      n = current[1];
      } else if (current[0] == maxLength) {
      n += current[1];
      }
      }
      }
      }
      current[0] = maxLength;
      current[1] = n;
      return current;
      }

      // this should be implement last (only part of evaulation)
      private int[] longestNetworkHelper(Chip c, DList checked, int dir, int n) {

      Chip connected;
      int[] current = {checked.length(), n};
      int maxLength = checked.length();
      int maxN = n;
      ListNode d;

      try {

      // if (c == null) {return current; } // this should no longer be necessary to check.
      d = checked.front();
      while (d.isValidNode()) {
      if (d.item() == c) {
      return current;
      }
      d = d.next();
      }

      checked.insertBack(c);

      if ((checked.length() > 1) && (c.getPosition()[1 - c.getColor()] == 0)) { // endzone
      current[1] = n;
      checked.back().remove();
      return current;
      }

      System.out.println("" + checked + maxLength + maxN);

      for (int i = 0; i < 8; i++) {
      if ((i == dir) || (i == Direction.opposite(dir))) {continue; } // skip same direction
      if (!c.hasConnection(dir)) {continue; }
      connected = c.getConnection(dir);
      if (connected.getColor() != c.getColor()) {continue; }
      current = (longestNetworkHelper(connected, checked, dir, n));
      if (current[0] > maxLength) {
      maxLength = current[0];
      n = current[1];
      } else if (current[0] == maxLength) {
      n += current[1];
      }
      }
      current[0] = maxLength;
      current[1] = n;
      checked.back().remove();
      return current;
      } catch (InvalidNodeException e) {
      System.out.println("Oh no!");
      } 
      return current;
      }
    */

    /**
     *  finds the number of chips adjacent to a position on the Gameboard that 
     *  contains a chip and takes into account the previous position of a 
     *  Chip in the situation of a step move
     *  @param x, y is the position on the gameboard that is being checked if 
     *         it has chips adjacent to it of the same color
     * @param color is the color of the chip placed at x, y
     * @param avoidX, avoidY are usedin the case of a step move but in the 
     *        situation of an add move they are set to x, y
     **/
    private int numberAdjacentChips(int x, int y, int color, int avoidX, int avoidY) {
        int dir;
        int count = 0;
        for (int i = 0; i < 8; i++) {
            dir = directions[i];
            //System.out.print("|" + (x+Direction.incrementX(dir)) + 
	    //(y+Direction.incrementY(dir)));
            if (!isValidSquare(x + Direction.incrementX(dir), 
			       y + Direction.incrementY(dir))) {
                continue; }
            if ((x + Direction.incrementX(dir) == avoidX) && 
		(y + Direction.incrementY(dir) == avoidY)) {
                //System.out.println("" + avoidX + avoidY);
                //System.out.println("Oh ....!");
                continue; }
            // System.out.print(getChip(x + Direction.incrementX(dir), 
	    //y + Direction.incrementY(dir)));
            if ((hasChip(x + Direction.incrementX(dir), 
			 y + Direction.incrementY(dir))) &&
                (getChip(x + Direction.incrementX(dir), 
			 y + Direction.incrementY(dir)).getColor() == color)) {
                count += 1;
            }
        }
        return count;
    }

    /**
     *  this checks if it's the right turn given a color!
     **/    
    public boolean isRightTurn(int color) {
        return (color == whoseTurn);
    }

    /**
     *  this method checks if a given add or step or quit move is valid on 
     *  "this" GameBoard
     *  @param m is a given Move (can be add, step, or quit)
     *  @param color is the color of the chip to be placed in the event of an 
     *         add or step move or the side that quits
     **/
    // isValidMove does NOT check for right turns. (we need it use it to list possible moves for evaluation)
    public boolean isValidMove(Move m, int color) {
        int dir;

        if (m.moveKind == 0) {return true; }

        else if (m.moveKind == 1) {
            if (color == 0) {
                if ((m.x1 == 0) || (m.x1 == 7)) {return false; }
            } else {
                if ((m.y1 == 0) || (m.y1 == 7)) {return false; }
            }

            if (placedChips[color].length() == 10) {return false; }

            if (!isValidSquare(m.x1, m.y1)) {return false; }

            if (hasChip(m.x1, m.y1)) {return false; }

            // checks for 3+ adjacent 'islands', if there are 2 next to the square to which chip is placed
            if (numberAdjacentChips(m.x1, m.y1, color, m.x1, m.y1) > 1) 
		{return false; }


            // checks for 3+ adjacent 'islands', if an adjacent chip already has another adjacent chip
            for (int i = 0; i < 8; i++) {
                dir = directions[i];
                if (hasChip(m.x1 + Direction.incrementX(dir), 
			    m.y1 + Direction.incrementY(dir)) && 
                    (getChip(m.x1 + Direction.incrementX(dir), 
			     m.y1 + 
			     Direction.incrementY(dir)).getColor() == color)) {
                    if (numberAdjacentChips(m.x1 + Direction.incrementX(dir), 
                                            m.y1 + Direction.incrementY(dir), 
					    color, m.x1, m.y1) > 0) 
			{return false; }
                }
            }

            return true;


            // step moves
        } else if (m.moveKind == 2) {

            if (color == 0) {
                if ((m.x1 == 0) || (m.x1 == 7)) {return false; }
            } else {
                if ((m.y1 == 0) || (m.y1 == 7)) {return false; }
            }

            if (!isValidSquare(m.x1, m.y1)) {return false; }
            if (!isValidSquare(m.x2, m.y2)) {return false; }

            if (placedChips[color].length() != 10) {return false; };

            if ((!hasChip(m.x2, m.y2)) || 
		getChip(m.x2, m.y2).getColor() != color) 
		{return false; } // color doesn't match

            if (hasChip(m.x1, m.y1)) {return false; } // there's chip in step-to square

            if (numberAdjacentChips(m.x1, m.y1, color, m.x2, m.y2) > 1) 
		{return false; } // step-to square already would form 2 adjacencies

            for (int i = 0; i < 8; i++) {
                dir = directions[i];
                if ((m.x1 + Direction.incrementX(dir) == m.x2) &&
                    (m.y1 + Direction.incrementY(dir) == m.y2)) {continue; }
                if (hasChip(m.x1 + Direction.incrementX(dir), 
			    m.y1 + Direction.incrementY(dir)) && 
                    (getChip(m.x1 + Direction.incrementX(dir), 
			     m.y1 + 
			     Direction.incrementY(dir)).getColor() == color)) {
                    if (numberAdjacentChips(m.x1 + Direction.incrementX(dir), 
					    m.y1 + Direction.incrementY(dir), 
                                            color, m.x2, m.y2) > 0) 
			{return false; }                
                }
            }

            return true;

        } else { // moveKind != 2 ... 
            return false;
        }
    }

    /**
     *  this method returns a DList of Valid Moves possible on "this" 
     *  Gameboard for a given color
     *  @param color is the color/side that can perform the returned list of 
     *         Moves
     **/
    // this doesn't account if it's the wrong turn. (evaluation heuristic breaks this)
    public DList listValidMoves(int color) {

        int dir;
        int x2;
        int y2;
        DList validMoves = new DList();
        Move current;
        Chip c;
        ListNode d;

        try {
            if (placedChips[color].length() == 10) { // step move
                d = placedChips[color].front();
                while (d.isValidNode()) {
                    c = (Chip) d.item();
                    x2 = c.getPosition()[0];
                    y2 = c.getPosition()[1];
                    for (int x1 = 0; x1 < 8; x1++) {
                        for (int y1 = 0; y1 < 8; y1++) {
                            current = new Move(x1, y1, x2, y2);
                            if (isValidMove(current, color)) {
                                validMoves.insertBack(current);
                            }
                        }
                    }
                    d = d.next();
                }
            } else { // add move
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        current = new Move(x, y);
                        if (isValidMove(current, color)) {
                            validMoves.insertBack(current);
                        }
                    }
                }
            }
        } catch (InvalidNodeException e) {
            System.out.println("Oh no!");
        }
        return validMoves;
    }

    /**
     *  A heuristic function that tries to evaluate "this"  board for a given 
     *  color judging its winnability
     **/
    public float evaluation() { // also needs to be completed
        System.out.println("For black: " + longestNetworkN(0)[0] + ", " + 
			   longestNetworkN(0)[1]);
        System.out.println("For white: " + longestNetworkN(1)[0] + ", " + 
			   longestNetworkN(1)[1]);
        float result = 0;
        float A = 10;
        float B = 1000;

        result += Math.log(1 + longestNetworkN(1)[0]*longestNetworkN(1)[1]);
        result -= Math.log(1+ longestNetworkN(0)[0]*longestNetworkN(0)[1]);
        result *= A;
        result += Math.log( ((float) listValidMoves(1).length()) / 
			    listValidMoves(0).length());
        result /= B;
        System.out.println(result);
        if (Math.abs(result) > 1) {
            System.out.println("Warning -- result is out of range! Result: " + 
			       result);
        }
        return result;
    }

    public String toString() {
        String s = "";
        Chip c;
        try {
            DList listChips;
            ListNode d;
            s += "Black chips at: ";
            listChips = placedChips[0];
            d = listChips.front();
            while (d.isValidNode()) {
                c = (Chip) d.item();
                s += "("  + c.getPosition()[0] + ", " + 
		    c.getPosition()[1] + "), ";
                d = d.next();
            }
            s += "hasValidNetwork: " + hasValidNetwork(0);
            s += "\nWhite chips at: ";
            listChips = placedChips[1];
            d = listChips.front();
            while (d.isValidNode()) {
                c = (Chip) d.item();
                s += "(" + c.getPosition()[0] + ", " + 
		    c.getPosition()[1] + "), ";
                d = d.next();
            }
            s += "hasValidNetwork: " + hasValidNetwork(1);
            return s;
        } catch (InvalidNodeException e) {
            System.out.println("Oh no!");
        }
        return s;
    }
}
