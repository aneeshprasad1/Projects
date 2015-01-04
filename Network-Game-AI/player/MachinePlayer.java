/* MachinePlayer.java */

package player;
import list.*;

/**
 *  An implementation of an automatic Network player.  Keeps track of moves
 *  made by both players.  Can select a move for itself.
 */
public class MachinePlayer extends Player {
    private int color;
    private int searchDepth;
    private GameBoard board;

    /**
     *  Creates a machine player with the given color.  Color is either 
     *  0 (black) or 1 (white).  (White has the first move.)
     **/
    public MachinePlayer(int color) {
        this.color = color;
	searchDepth = 2; // default value
	board = new GameBoard();
    }

    /**
     *  Creates a machine player with the given color and search depth.  Color 
     *  is either 0 (black) or 1 (white).  (White has the first move.)
     **/
    public MachinePlayer(int color, int searchDepth) {
	this.color = color;    
	this.searchDepth = searchDepth;
	board = new GameBoard();
    }

    /**
     *  Returns a new move by "this" player.  Internally records the move 
     *  (updates the internal game board) as a move by "this" player.
     **/
    public Move chooseMove() {
    	Best myBest = bestMove(color, 0, 0, 0);
        board.makeMove(myBest.move, color);
        System.out.println(board);
    	return myBest.move;
    }

    /**
     *  Employs alpha-beta pruning technique to search to a given depth for 
     *  the best moves the Machine Player can make
     *  @return the Best move that the Machine Player can make on the current 
     *          board based on this technique
     **/
    // I didn't take the full time to understand the algorithm ... 
    // someone needs to explain it to
    // me. -Mike
    public Best bestMove(int color, float alpha, float beta, int depth) {
        Best myBest = new Best();
        Best reply;
        try {
            if (board.hasValidNetwork(1)) {
                myBest.move = new Move();
                myBest.score = 1;
                return myBest;
            } else if (board.hasValidNetwork(0)) {
                myBest.move = new Move();
                myBest.score = -1;
                return myBest;
            }

            if (color != this.color) {
                myBest.score = -1;
            } else {
                myBest.score = 1;
            }

            DList validMoves = board.listValidMoves(color);
            Move m;

            ListNode d = validMoves.front();
            myBest.move = (Move) d.item();

            if (depth == searchDepth) {
                System.out.println("depth has been reached!");
                myBest.score = board.evaluation();
                return myBest;
            }

            while (d.isValidNode()) {
                m = (Move) d.item();
                //System.out.println("Move: " + m + " board: ");
                board.makeMove(m, color);
                //System.out.println(board);
                reply = bestMove(1 - color, alpha, beta, depth + 1);
                board.undoMove(m);
                //System.out.println(board);
                if (color != this.color && reply.score > myBest.score) {
                    myBest.move = m;
                    myBest.score = reply.score;
                    alpha = reply.score;
                } else if ((color == this.color) && 
			   reply.score < myBest.score) {
                    myBest.move = m;
                    myBest.score = reply.score;
                    beta = reply.score;
                }
                if (alpha >= beta) {return myBest; }
                d = d.next();
            }
        } catch (InvalidNodeException e) {
            System.out.println(e);
        }
	return myBest;
    }

    /**
     *  If the Move m is legal, records the move as a move by the opponent
     *  (updates the internal game board) and returns true.  If the move is
     *  illegal, returns false without modifying the internal state of "this"
     *  player.  This method allows your opponents to inform you of their 
     *  moves.
     **/
    public boolean opponentMove(Move m) {
	if (((!board.isValidMove(m, 1 - color)) || 
	     (!board.isRightTurn(1 - color)))) {
	    return false;
	}
    	board.makeMove(m, 1 - color);
        System.out.println(board);
    	return true;
    }

    /**
     *  If the Move m is legal, records the move as a move by "this" player
     *  (updates the internal game board) and returns true.  If the move is
     *  illegal, returns false without modifying the internal state of "this"
     *  player.  This method is used to help set up "Network problems" for your
     *  player to solve.
     **/
    public boolean forceMove(Move m) {
	if ((!board.isValidMove(m, color)) || (!board.isRightTurn(color))) {
	    return false;
	}
	board.makeMove(m, color);
	return true;
    }
}

