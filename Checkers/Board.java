package Checkers;
class Board {
  public static enum PieceType {
    EMPTY("   "),
    WHITE(" w "),
    BLACK(" b "),
    WHITE_KING("-W-"),
    BLACK_KING("-B-");

    public final String c; //display char for console

    PieceType(String c) {
      this.c = c;
    }
  }

  PieceType[] pieces = new PieceType[64];
  public Board() {
    //initialize board
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        int i = y * 8 + x;
        if ((x + y) % 2 != 0) {
          if (y < 2)
            pieces[i] = PieceType.BLACK;
          else if (y >= 6)
            pieces[i] = PieceType.WHITE;
          else
            pieces[i] = PieceType.EMPTY;
        } else {
          pieces[i] = PieceType.EMPTY;
        }
      }
    }
  }
  public PieceType getPieceType(int index) {
    return pieces[index];
  }
  public void setPiece(int index, PieceType type) {
    pieces[index] = type;
  }
  public boolean isPieceWhite(int index) {
    return pieces[index] == PieceType.WHITE || pieces[index] == PieceType.WHITE_KING;
  }
  public boolean isPieceBlack(int index) {
    return pieces[index] == PieceType.BLACK || pieces[index] == PieceType.BLACK_KING;
  }

  //draw the board in the console
  public void draw(int selectedIndex) {
    System.out.print("  ");
    for (int x = 0; x < 8; x++)
      System.out.print("   " + x + "  ");
    System.out.println();
    for (int y = 0; y < 8; y++) {
      System.out.println("  " + "-".repeat(6 * 8 + 1));
      System.out.print(y + " ");
      for (int x = 0; x < 8; x++) {
        int i = y * 8 + x;
        System.out.print("| " + (i == selectedIndex ? "\u001B[33m" : "") +pieces[i].c + (i == selectedIndex ? "\u001B[0m " : " "));
      }
      System.out.println("|");
    }
    System.out.println("  " + "-".repeat(6 * 8 + 1));

  }
  
}