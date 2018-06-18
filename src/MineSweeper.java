import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


class GameResultDialog extends JDialog {
    public GameResultDialog (JFrame owner, String msg) {
        super(owner, "MineSweeper", true);
        add(new JLabel(msg), BorderLayout.CENTER);
        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit (0);
            }
        });
        JPanel panel = new JPanel();
        panel.add(ok);
        add(panel, BorderLayout.SOUTH);
        setSize(260, 160);
    }
}

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

    public MineSweeperGame (int size_x, int size_y,
                            int cellRadiusPixels, int borderPixels,
                            JDialog success_dialog, JDialog fail_dialog) {
        sizeX = size_x;
        sizeY = size_y;
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

    public void fillWithMines (double probability) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (Math.random () < probability)
                    placeMine (x, y);
            }
        }
    }

    public void calculateNeighboursCount () {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                int n = 0;
                if (x != 0) {
                    if (cells [x-1] [y].hasMine)
                        n++;
                }
                if (x != sizeX - 1) {
                    if (cells [x+1] [y].hasMine)
                        n++;
                }
                if (y != 0) {
                    if (cells [x] [y-1].hasMine)
                        n++;
                    if (x % 2 == 0) {
                        if (x != 0)
                            if (cells [x-1] [y-1].hasMine)
                                n++;
                        if (x != sizeX - 1)
                            if (cells [x+1] [y-1].hasMine)
                                n++;
                    }
                }
                if (y != sizeY - 1) {
                    if (cells [x] [y+1].hasMine)
                        n++;
                    if (x % 2 == 1) {
                        if (x != 0)
                            if (cells [x-1] [y+1].hasMine)
                                n++;
                        if (x != sizeX - 1)
                            if (cells [x+1] [y+1].hasMine)
                                n++;
                    }
                }
                cells [x] [y].neighbours = n;
            }
        }
    }
    public void leftClick (Cell c) {
        c.opened = true;
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
class Cell {
    public Cell (int _x, int _y, int center_x, int center_y, int half_radius, int half_height) {
        x = _x;
        y = _y;
        centerX = center_x;
        centerY = center_y;
        halfRadius = half_radius;
        halfHeight = half_height;
        opened = false;
        flagged = false;
        hasMine = false;
        neighbours = 0;
        border = new Polygon ();
        border.addPoint (center_x - 2 * half_radius, center_y);
        border.addPoint (center_x - half_radius, center_y + half_height);
        border.addPoint (center_x + half_radius, center_y + half_height);
        border.addPoint (center_x + 2 * half_radius, center_y);
        border.addPoint (center_x + half_radius, center_y - half_height);
        border.addPoint (center_x - half_radius, center_y - half_height);
        ch = new char [1];
    }

    public void paint (Graphics g) {
        Color backgroundColor = Color.LIGHT_GRAY;
        ch [0] = '?';
        if (opened) {
            if (hasMine) {
                backgroundColor = Color.RED;
                ch[0] = '*';
            }
            else {
                ch[0] = (char) ('0' + neighbours);
                backgroundColor = Color.cyan;
            }
        }
        if (flagged) {
            ch[0] = 'F';
            backgroundColor = Color.YELLOW;
        }
        g.setColor (backgroundColor);
        g.fillPolygon (border);
        g.setColor (Color.BLACK);
        g.drawPolygon (border);
        g.drawChars (ch, 0, 1, centerX -12, centerY +18);
    }

    public boolean containsPoint (int x, int y) {
        return border.contains (x, y);
    }
    protected int x;
    protected int y;
    protected int centerX;
    protected int centerY;
    protected int halfRadius;
    protected int halfHeight;
    protected char ch [];
    protected Polygon border;
    protected boolean opened;
    protected boolean flagged;
    protected boolean hasMine;
    protected int neighbours;
}

public class MineSweeper {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        GameResultDialog fail_dialog =
                new GameResultDialog (frame, "<html><h1><i>Вы проиграли!!!</h1></html>");
        GameResultDialog success_dialog =
                new GameResultDialog (frame, "<html><h1><i>Вы выиграли!!!</h1></html>");
        MineSweeperGame game = new MineSweeperGame (15, 10, 40, 10,
                success_dialog, fail_dialog);
        game.clear ();
        game.fillWithMines (0.2);
        game.calculateNeighboursCount ();
        frame.setSize (game.getFieldWidth (), game.getFieldHeight ());
        frame.getContentPane().add(game);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.LIGHT_GRAY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
