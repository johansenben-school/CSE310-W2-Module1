package Checkers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;

import javax.swing.JPanel;

import java.awt.event.MouseEvent;

class Board extends JPanel {
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
  public int selected = -1;
  int squareWidth = 100;
  int pieceWidth = 80;
  public Board(GameManager game) {
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

    //panel size
    setPreferredSize(new Dimension(squareWidth * 8, squareWidth * 8));

    //click event listener
    addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          int x = e.getX() / squareWidth;
          int y = e.getY() / squareWidth;
          game.click(y, x);
          repaint();
        }
      });
  }

  //render function
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        if (selected == y * 8 + x)//selected
          g.setColor(new Color(0x88aaff));
        else if ((x + y) % 2 == 0) //white/light brown square
          g.setColor(new Color(0xe6af67));
        else //black/dark brown square
          g.setColor(new Color(0x633901));
        
        //draw square
        g.fillRect(x * squareWidth, y * squareWidth, squareWidth, squareWidth);

        //skip drawing a piece if square is empty
        if (pieces[y * 8 + x] == PieceType.EMPTY)
          continue;

        //draw piece
        g.setColor(isPieceWhite(y * 8 + x) ? Color.white : Color.black);
        g.fillOval(x * squareWidth + (squareWidth - pieceWidth) / 2, y * squareWidth + (squareWidth - pieceWidth) / 2, pieceWidth, pieceWidth);
        
        //add crown if piece is a king
        if (pieces[y * 8 + x] == PieceType.WHITE_KING || pieces[y * 8 + x] == PieceType.BLACK_KING) {
          int[] xPoints = { x * squareWidth + 30, x * squareWidth + 30, x * squareWidth + 40, x * squareWidth + 50, (x + 1) * squareWidth - 40, (x + 1) * squareWidth - 30, (x + 1) * squareWidth - 30 };
          int[] yPoints = { (y + 1) * squareWidth - 30, y * squareWidth + 30, y * squareWidth + 50, y * squareWidth + 30, y * squareWidth + 50, y * squareWidth + 30, (y + 1) * squareWidth - 30 };

          g.setColor(Color.gray);
          g.fillPolygon(xPoints, yPoints, 7);
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
  
}