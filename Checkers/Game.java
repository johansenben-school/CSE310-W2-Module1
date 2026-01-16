package Checkers;

import java.util.Scanner;

import Checkers.Board.PieceType;


class Game {
  //for inputs when running with console
  static Scanner scanner = new Scanner(System.in);

  //possible states
  public static enum State {
    TURN_WHITE,
    TURN_BLACK,
    WIN_WHITE,
    WIN_BLACK,
    UNINITIALIZED,
    STOPPED
  }
  State state = State.UNINITIALIZED;
  //extra state variables to handle double jump (its probably possible to use only 1 for just double jump)
  boolean jumpOnly = false;
  boolean canSelectDifferentPiece = true;

  //data
  Board board;
  int selected = -1;

  //constructor
  public Game() {
    board = new Board();
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
  //use console inputs to select a piece
  int selectPieceWithConsole() {
    int selected = -1;

    while (selected == -1) {
      System.out.print("Select square (format: xy) or enter 'q' to quit: ");
      String in = scanner.next();
      //quit
      if (in.equals("q")) {
        state = State.STOPPED;
        break;
      }
      //is input 2 numbers from 0 to 7? (col and row)
      if (!in.matches("^[0-7]{2}$"))
        continue;
      int i = in.charAt(0) - '0' + (in.charAt(1) - '0') * 8;
      if (!canSelect(i)) //don't select if the square can't be selected
        continue;
      selected = i;
      
    }
    return selected;
  }
  //is a 1 square move valid?
  boolean isValidMove(int startIndex, int endIndex) {//todo check if selected is at the edge of the board; a piece on the right side shouldnt be able to move to a square on the left side
    if (jumpOnly)
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
    move(startIndex, endIndex);
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
          selected = endIndex;
          jumpOnly = true;
          canSelectDifferentPiece = false;
          return;
        }
      }
    }
  }
  void move(int startIndex, int endIndex) {
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

  //check if there are 0 white pieces or 0 black pieces -> set state
  void checkForWin() {
    int whiteCount = 0;
    int blackCount = 0;
    for (int i = 0; i < 64; i++) {
      if (board.isPieceWhite(i))
        whiteCount++;
      else if (board.isPieceBlack(i))
        blackCount++;
    }
    if (whiteCount == 0)
      state = State.WIN_BLACK;
    else if (blackCount == 0)
      state = State.WIN_WHITE;

  }
  void movePieceWithConsole() {
    if (selected == -1)
      return;
    
    while (true) {
      //different message if the user can't select a different piece
      if (canSelectDifferentPiece)
        System.out.print("Enter 'q' to quit or 's' to select a different piece or enter a square to move to (format: xy)");
      else if (jumpOnly)
        System.out.print("Enter 'q' to quit or enter 'e' to end turn or enter a square to move to (format: xy)");

      //input
      String in = scanner.next();

      //quit
      if (in.equals("q")) {
        state = State.STOPPED;
        break;
      }
      //select a differnt piece
      else if (canSelectDifferentPiece && in.equals("s")) {
        selected = -1;
        break;
      }
      //end turn when able to double jump
      else if (jumpOnly && in.equals("e")) {
        selected = -1;
        switchTurn();
        canSelectDifferentPiece = true;
        jumpOnly = false;
      }
      //is input 2 numbers from 0 to 7?
      if (!in.matches("^[0-7]{2}$"))
        continue;

      //calculate index from input
      int index = in.charAt(0) - '0' + (in.charAt(1) - '0') * 8;

      //if valid move -> move
      if (isValidMove(selected, index)) {
        move(selected, index);
        switchTurn();
        selected = -1;
      } 
      //if valid jump -> jump
      else if (isValidJump(selected, index)) {
        jump(selected, index);
        jumpOnly = false;
        canSelectDifferentPiece = true;
        selected = -1;
        switchTurn();
      }
      break;
    }
  }

  public void runWithConsole() {
    state = State.TURN_WHITE;
    //game loop
    while (state != State.STOPPED) {
      //draw
      board.draw(selected);

      //white or black wins?
      if (state == State.WIN_WHITE) {
        System.out.println("White Wins!");
        state = State.STOPPED;
        break;
      } else if (state == State.WIN_BLACK) {
        System.out.println("Black Wins!");
        state = State.STOPPED;
        break;
      }

      //piece already selected
      if (selected != -1) {
        movePieceWithConsole();
        continue;
      } 
      //piece not selected
      else {
        selected = selectPieceWithConsole();
        continue;
      }
    }
  }
}