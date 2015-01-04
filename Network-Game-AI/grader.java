/* grader.java */
/* By Aneesh Prasad */


=========
OVERVIEW
=========

/**
 *  GameBoard
 *    Chip[][] board;
 *    DList[] placedChips;
 *    int[] directions;
 *    int whoseTurn;
 *
 *    GameBoard(); // constructor
 *    makeMove(Move m, int color); // @return void
 *    undoMove(Move m); // @return void
 *    isValidSquare(int x, int y); // @return boolean
 *    hasChip(int x, int y); // @return boolean
 *    getChip(int x, int y); // @return Chip
 *    setChip(int x, int y, int color); // @return void
 *    removeChip(int x, int y); // @return void
 *    moveChip(int x1, int y1,
 *             int x2, int y2); // @return void
 *    makeConnections(Chip c); // @return void
 *    removeConnections(Chip c); // @return void
 *
 *    hasValidNetwork(int color); // @return boolean
 *    longestNetworkN(int color); // @return int[]
 *    numberAdjacentChips(int x, int y, int color,
 *                        int avoidX, int avoidY); // @return int
 *    isRightTurn(int color); // @return boolean
 *
 *    isValidMove(Move m, int color); // @return boolean
 *    listValidMoves(int color); // @return DList
 *    evaluation(); // @return float
 *
 *
 *  MachinePlayer
 *    int color;
 *    int searchDepth;
 *    GameBoard board;
 *
 *    MachinePlayer(int color); // constructor
 *    MachinePlayer(int color, int searchDepth); // constructor
 *    chooseMove(); // @return Move
 *    bestMove(int color, int alpha,
 *             int beta, int depth); // @return Best
 *    opponentMove(Move m); // @return boolean
 *
 *
 *  Direction
 *    int SW; // 1
 *    int S; // 2
 *    int SE; // 3
 *    int W; // 4
 *    int E; // 6
 *    int NW; // 7
 *    int N; // 8
 *    int NE; // 9
 *    int[] directions; {SW, S, SE, W, E, NW, N, NE};
 *
 *    incrementX(int dir); // @return int
 *    incrementY(int dir); // @return int
 *    indexX(int dir); // @return int
 *    indexY(int dir); // @return int
 *    opposite(int dir); // @return int
 *
 *
 *  Chip
 *    int x, y;
 *    int color;
 *    GameBoard board;
 *    Chip[][] connections;
 *
 *    Chip(GameBoard board,
 *         int x, int y, int color); // constructor
 *    getColor(); // @return int
 *    setPosition(int x, int y); // @return void
 *    getPosition(); // @return int[]
 *    hasConnection(int dir); // @return boolean
 *    setConnection(int dir, Chip c); // @return void
 *    getConnection(int dir); // @return Chip
 *    removeConnection(int dir); // @return void
 *    toString(); // @return String
 *
 *
 *  Best
 *    Move move;
 *    float score;
 **/

=======
MODULES
=======

/**
 *  Responsibility: David
 *  hasValidNetwork() determines whether "this" GameBoard has a valid network
 *  for player "side".  (Does not check whether the opponent has a network.)
 *  A full description of what constitutes a valid network appears in the
 *  project "readme" file.
 *
 *  Unusual conditions:
 *    If side is neither MachinePlayer.COMPUTER nor MachinePlayer.OPPONENT,
 *          returns false.
 *    If GameBoard squares contain illegal values, the behavior of this
 *          method is undefined (i.e., don't expect any reasonable behavior).
 *
 *  @param color is 0 (black) or 1 (white)
 *  @return true if player "side" has a winning network in "this" GameBoard;
 *          false otherwise.
 **/
protected boolean hasValidNetwork(int color);


/**
 *  Responsibility: David
 *  isValidMove() determines whether Move "m" is a valid move on "this" 
 *  GameBoard.
 *  
 *  A full description of what constitutes a valid move appears in the project
 *  "readme" file.
 *
 *  @param m is Move
 *  @param color is 0 (black) or 1 (white)
 *  @return true if Move "m" is valid on "this" GameBoard. false otherwise.
 **/
protected boolean isValidMove(Move m, int color);


/**
 *  Responsibility: Mike and Aneesh
 *  evaluation() is a static evaluation heuristic that assigns a score to "this"
 *  GameBoard estimating how well your MachinePlayer is doing. It is impossible
 *  to search the entire depth of the game, so this heuristic is necessary to
 *  estimate the odds of winning if the MachinePlayer makes a particular move.
 *  This function assigns a maximum positive score of 1 to a win by the
 *  MachinePlayer and a minimum negative score to a win by the opponent. An
 *  intermediate score will be assigned to a board where neither player has
 *  completed a network. 
 *
 *  A slightly higher score will be assigned to a win in fewer moves than
 *  otherwise.
 *
 *  @return float from -1 to 1 based on how good a board is for the given color,
 *          aka the MachinePlayer
 **/
protected float evaluation();


/**
 *  Responsibility: David
 *  listValidMoves() retrieves an array of all the valid moves
 *  as determined by looping through the "this" GameBoard's
 *  isValidMove() method across all unfilled spaces of the
 *  GameBoard.
 *
 *  @param color is 0 (black) or 1 (white)
 *  @return moves is an array of Moves that are valid
 **/
protected DList listValidMoves(int color);


/**
 *  Responsibility: Mike and Aneesh
 *  bestMove() uses alpha-beta pruning search techniques in
 *  order to speed up the search through a game tree.
 *  
 *  @param color is 0 (black) or 1 (white)
 *  @param alpha is the lower-bound
 *  @param beta is the upper bound
 *  @param depth is the search depth
 *  @return Best is contains both the move and the score
 **/
protected bestMove(int color, int alpha,
		   int beta, int depth);

/**
 *  Responsibility: Aneesh
 *  getConnection() gets the nearest Chip in the given Direction
 *  "dir".
 *  @param dir is a number representing the desired direction
 *  @return the nearest Chip in the Direction "dir"
 **/
public Chip getConnection(int dir)



