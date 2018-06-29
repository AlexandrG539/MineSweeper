import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

class MineSweeperGame extends JPanel {
    public int sizeX;
    public int sizeY;
    public int fieldWidth;
    public int fieldHeight;
    public int cellHalfRadius;
    public int cellHalfHeight;
    JDialog successDialog;
    JDialog failDialog;
    public Cell cells [][];
    boolean minesPlaced;
    int numMines;

    public MineSweeperGame (int size_x, int size_y,
                            int cellRadiusPixels, int borderPixels,
                            int num_mines,
                            JDialog success_dialog, JDialog fail_dialog) {
        sizeX = size_x;
        sizeY = size_y;
        numMines = num_mines;
        minesPlaced = false;
        successDialog = success_dialog;
        failDialog = fail_dialog;
        cellHalfRadius = cellRadiusPixels / 2;
        cellHalfHeight = (int)(cellHalfRadius * Math.sqrt (3.0));
        fieldWidth = 2 * borderPixels + sizeX * 3 * cellHalfRadius + cellHalfRadius + 10;
        fieldHeight = 2 * borderPixels + cellHalfHeight * (1 + 2 * sizeY) + 30;
        cells = new Cell [sizeX] [sizeY];
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                int centerX = borderPixels + 2 * cellHalfRadius + 3 * x * cellHalfRadius;
                int centerY = borderPixels + cellHalfHeight + 2 * cellHalfHeight * y;
                if (x % 2 == 1)
                    centerY += cellHalfHeight;
                cells [x] [y] = new Cell (x, y, centerX, centerY, cellHalfRadius, cellHalfHeight);
            }
        }

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                int mouseX = ev.getX ();
                int mouseY = ev.getY ();
                Cell c = null;
                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        if (cells [x] [y].containsPoint (mouseX, mouseY)) {
                            c = cells [x] [y];
                        }
                    }
                }
                if (c != null) {
                    if (ev.getButton() == MouseEvent.BUTTON1)
                        leftClick (c);
                    else if (ev.getButton() == MouseEvent.BUTTON3)
                        rightClick (c);
                }
            }
        });
    }
    public int getFieldWidth () { return fieldWidth; }
    public int getFieldHeight () { return fieldHeight; }

    public void clear () {
        minesPlaced = false;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                cells [x] [y].hasMine = false;
                cells [x] [y].neighbours = 0;
                cells [x] [y].opened = false;
                cells [x] [y].flagged = false;
            }
        }
    }

    public void placeMine (int x, int y) {
        cells [x] [y].hasMine = true;
    }

    public void fillWithMines (int dontplace_x, int dontplace_y) {
        java.util.Random rnd = new java.util.Random ();
        int placed = 0;
        while (placed < numMines) {
            int x = rnd.nextInt (sizeX);
            int y = rnd.nextInt (sizeY);
            if (!(cells [x] [y].hasMine) && (x != dontplace_x) && (y != dontplace_y)) {
                placeMine (x, y);
                placed++;
            }
        }
    }

    public ArrayList<Cell> getNeighbours (int x, int y) {
        ArrayList<Cell> s = new ArrayList<Cell> ();
        if (x > 0)
            s.add (cells [x-1] [y]);
        if (x < sizeX - 1)
            s.add (cells [x+1] [y]);
        if (y > 0) {
            s.add (cells [x] [y-1]);
            if (x % 2 == 0) {
                if (x > 0)
                    s.add (cells [x-1] [y-1]);
                if (x < sizeX - 1)
                    s.add (cells [x+1] [y-1]);
            }
        }
        if (y < sizeY - 1) {
            s.add (cells [x] [y+1]);
            if (x % 2 == 1) {
                if (x > 0)
                    s.add (cells [x-1] [y+1]);
                if (x < sizeX - 1)
                    s.add (cells [x+1] [y+1]);
            }
        }
        return s;
    }

    public void openZeros () {
        while (true) {
            int changes = 0;
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    Cell c = cells [x] [y];
                    if (!c.opened) {
                        for (Cell n: getNeighbours (x, y)) {
                            if (n.opened && !n.flagged && n.neighbours == 0) {
                                c.opened = true;
                                changes++;
                            }
                        }
                    }
                }
            }
            if (changes == 0)
                break;
        }
    }

    public void calculateNeighboursCount () {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                int n = 0;
                for (Cell c: getNeighbours (x, y)) {
                    if (c.hasMine)
                        n++;
                }
                cells [x] [y].neighbours = n;
            }
        }
    }
    public void leftClick (Cell c) {
        if (!minesPlaced) {
            fillWithMines (c.x, c.y);
            calculateNeighboursCount ();
            minesPlaced = true;
        }
        if (c.opened)
            return;
        c.opened = true;
        openZeros ();
        repaint ();
        if (c.hasMine) {
            failDialog.setVisible (true);
        }
        boolean all_opened = true;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (cells [x][y].opened == false)
                    all_opened = false;
            }
        }
        if (all_opened) {
            successDialog.setVisible (true);
        }
    }

    public void rightClick (Cell c) {
        if (!minesPlaced) {
            fillWithMines (c.x, c.y);
            calculateNeighboursCount ();
            minesPlaced = true;
        }
        if (c.opened)
            return;
        c.flagged = true;
        c.opened = true;
        repaint ();
        if (c.hasMine == false) {
            failDialog.setVisible (true);
        }
        boolean all_opened = true;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (cells [x][y].opened == false)
                    all_opened = false;
            }
        }
        if (all_opened) {
            successDialog.setVisible (true);
        }
    }

    public void paint(Graphics g) {
        Font font = new Font ("SansSerif", Font.PLAIN, 48);
        g.setFont (font);
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                cells [x] [y].paint (g);
            }
        }
    }
}