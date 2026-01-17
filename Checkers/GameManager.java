package Checkers;

import Checkers.Board.PieceType;
import javax.swing.JFrame;

class GameManager extends JFrame {
  //possible states
  public static enum State {
    TURN_WHITE,
    TURN_BLACK,
    UNINITIALIZED,
    STOPPED
  }
  State state = State.TURN_WHITE;
  //extra state variable to handle double jump
  boolean canDoubleJump = false;

  //data
  Board board;

  //constructor
  public GameManager() {
    board = new Board(this);

    //JFrame setup
    setTitle("Checkers");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    add(board);
    pack();
    setVisible(true);
  }

  //switch turn; could be simplified if turn was stored as a separate int; for example turn = (turn + 1) % 2, where turn is 0 or 1
  void switchTurn() {
    if (state == State.TURN_WHITE) state = State.TURN_BLACK;
    else if (state == State.TURN_BLACK) state = State.TURN_WHITE;
  }
  //user can only select a piece that is the correct color for the turn
  boolean canSelect(int index) {
    return (state == State.TURN_WHITE && board.isPieceWhite(index)) || (state == State.TURN_BLACK && board.isPieceBlack(index));
  }
  
  //is a 1 square move valid?
  boolean isValidMove(int startIndex, int endIndex) {
    if (canDoubleJump)
      return false;
    for (int y = -1; y <= 1; y += 2) {
      for (int x = -1; x <= 1; x += 2) {
        //are x and y correct for the start and end indexes given?
        if (startIndex - endIndex == -x - y * 8) {
          //pieces can't move backwards if they arent a king
          if (y == -1 && board.getPieceType(startIndex) == Board.PieceType.BLACK)
            continue;
          if (y == 1 && board.getPieceType(startIndex) == Board.PieceType.WHITE)
            continue;

          //is index within bounds?
          if (startIndex + x + y * 8 < 0 || startIndex + x + y * 8 >= 64)
            continue;
          if (startIndex % 8 + x < 0 || startIndex % 8 + x >= 8)
            continue;

          //is target square empty?
          if (board.getPieceType(endIndex) == Board.PieceType.EMPTY)
            return true;
          return false;
        }
      }
    }
    return false;
  }
  //is a jump valid?
  boolean isValidJump(int startIndex, int endIndex) {
    for (int y = -1; y <= 1; y += 2) {
      for (int x = -1; x <= 1; x += 2) {
        //are x and y correct for the start and end indexes given?
        if (startIndex - endIndex == -2 * x + -2 * y * 8) {
          //pieces can't move backwards if they arent a king
          if (y == -1 && board.getPieceType(startIndex) == Board.PieceType.BLACK)
            continue;
          if (y == 1 && board.getPieceType(startIndex) == Board.PieceType.WHITE)
            continue;

          //is index within bounds?
          if (startIndex + x * 2 + y * 8 * 2 < 0 || startIndex + x * 2 + y * 8 * 2 >= 64)
            continue;
          if (startIndex % 8 + x * 2 < 0 || startIndex % 8 + x * 2 >= 8)
            continue;

          PieceType jumpOver = board.getPieceType(startIndex + x + y * 8);
          //if square to jump over isnt empty and has a piece of the opposite color
          if (jumpOver != Board.PieceType.EMPTY && 
            (
              (state == State.TURN_WHITE && board.isPieceBlack(startIndex + x + y * 8)) || 
              (state == State.TURN_BLACK && board.isPieceWhite(startIndex + x + y * 8))
            ) && 
            //is target square empty?
            board.getPieceType(startIndex + x * 2 + y * 2 * 8) == Board.PieceType.EMPTY) {
            return true;
          }
          return false;
        }
      }
    }
    return false;
  }
  void jump(int startIndex, int endIndex) {
    movePiece(startIndex, endIndex);
    int delta = startIndex - endIndex;
    //find the piece that is being jumped over and remove it
    switch (delta) {
      case -14:
        board.setPiece(startIndex + 7, Board.PieceType.EMPTY);
        break;
      case 14:
        board.setPiece(startIndex - 7, Board.PieceType.EMPTY);
        break;
      case -18:
        board.setPiece(startIndex + 9, Board.PieceType.EMPTY);
        break;
      case 18:
        board.setPiece(startIndex - 9, Board.PieceType.EMPTY);
        break;
    }
    //can the piece do a double/triple jump?
    for (int y = -2; y <= 2; y += 4) {
      for (int x = -2; x <= 2; x += 4) {
        if (isValidJump(endIndex, endIndex + y * 8 + x)) {
          board.selected = endIndex;
          canDoubleJump = true;
          return;
        }
      }
    }
    canDoubleJump = false;
  }
  void movePiece(int startIndex, int endIndex) {
    //set new piece
    board.setPiece(endIndex, board.getPieceType(startIndex));
    //set old piece
    board.setPiece(startIndex, Board.PieceType.EMPTY);

    //make piece a king if it reaches the opposite side
    if ((int)(endIndex / 8) == 0 && board.isPieceWhite(endIndex)) {
      board.setPiece(endIndex, Board.PieceType.WHITE_KING);
    } else if ((int)(endIndex / 8) == 7 && board.isPieceBlack(endIndex)) {
      board.setPiece(endIndex, Board.PieceType.BLACK_KING);
    }
  }
  
  public void click(int row, int col) {
    int i = row * 8 + col;

    //select square
    if (canSelect(i) && (board.selected == -1 || !canDoubleJump)) {
        board.selected = i;
    } 
    //move
    else {
      //regular move
      if (isValidMove(board.selected, i)) {
        movePiece(board.selected, row * 8 + col);
        switchTurn();
        board.selected = -1;
      } 
      //jump
      else if (isValidJump(board.selected, i)) {
        jump(board.selected, i);
        if (!canDoubleJump) {//switch turn if not double jump
          board.selected = -1;
          switchTurn();
        }
      } 
      //end turn if able to double jump
      else if (canDoubleJump) {
        board.selected = -1;
        switchTurn();
        canDoubleJump = false;
      }
    }
  }
}