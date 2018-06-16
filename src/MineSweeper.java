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
    public int size_x;
    public int size_y;
    public int field_width;
    public int field_height;
    public int cell_half_radius;
    public int cell_half_height;
    JDialog success_dialog;
    JDialog fail_dialog;
    public Cell cells [][];
    public MineSweeperGame (int _size_x, int _size_y,
                            int cell_radius_pixels, int border_pixels,
                            JDialog _success_dialog, JDialog _fail_dialog) {
        size_x = _size_x;
        size_y = _size_y;
        success_dialog = _success_dialog;
        fail_dialog = _fail_dialog;
        cell_half_radius = cell_radius_pixels / 2;
        cell_half_height = (int)(cell_half_radius * Math.sqrt (3.0));
        field_width = 2*border_pixels + size_x * 3 * cell_half_radius + cell_half_radius + 10;
        field_height = 2*border_pixels + cell_half_height * (1 + 2 * size_y) + 30;
        cells = new Cell [size_x] [size_y];
        for (int x = 0; x < size_x; x++) {
            for (int y = 0; y < size_y; y++) {
                int center_x = border_pixels + 2 * cell_half_radius + 3 * x * cell_half_radius;
                int center_y = border_pixels + cell_half_height + 2 * cell_half_height * y;
                if (x % 2 == 1)
                    center_y += cell_half_height;
                cells [x] [y] = new Cell (x, y, center_x, center_y, cell_half_radius, cell_half_height);
            }
        }

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                int mouse_x = ev.getX ();
                int mouse_y = ev.getY ();
                Cell c = null;
                for (int x = 0; x < size_x; x++) {
                    for (int y = 0; y < size_y; y++) {
                        if (cells [x] [y].containsPoint (mouse_x, mouse_y)) {
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
    public int getFieldWidth () {
        return field_width;
    }
    public int getFieldHeight () {
        return field_height;
    }

    public void clear () {
        for (int x = 0; x < size_x; x++) {
            for (int y = 0; y < size_y; y++) {
                cells [x] [y].has_mine = false;
                cells [x] [y].neighbours = 0;
                cells [x] [y].opened = false;
                cells [x] [y].flagged = false;
            }
        }
    }

    public void placeMine (int x, int y) {
        cells [x] [y].has_mine = true;
    }

    public void fillWithMines (double probability) {
        for (int x = 0; x < size_x; x++) {
            for (int y = 0; y < size_y; y++) {
                if (Math.random () < probability)
                    placeMine (x, y);
            }
        }
    }

    public void calculateNeighboursCount () {

        for (int x = 0; x < size_x; x++) {
            for (int y = 0; y < size_y; y++) {
                int n = 0;
                if (x != 0) {
                    if (cells [x-1] [y].has_mine)
                        n++;
                }
                if (x != size_x - 1) {
                    if (cells [x+1] [y].has_mine)
                        n++;
                }
                if (y != 0) {
                    if (cells [x] [y-1].has_mine)
                        n++;
                    if (x % 2 == 0) {
                        if (x != 0)
                            if (cells [x-1] [y-1].has_mine)
                                n++;
                        if (x != size_x - 1)
                            if (cells [x+1] [y-1].has_mine)
                                n++;
                    }
                }
                if (y != size_y - 1) {
                    if (cells [x] [y+1].has_mine)
                        n++;
                    if (x % 2 == 1) {
                        if (x != 0)
                            if (cells [x-1] [y+1].has_mine)
                                n++;
                        if (x != size_x - 1)
                            if (cells [x+1] [y+1].has_mine)
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
        if (c.has_mine) {
            fail_dialog.setVisible (true);
        }
        boolean all_opened = true;
        for (int x = 0; x < size_x; x++) {
            for (int y = 0; y < size_y; y++) {
                if (cells [x][y].opened == false)
                    all_opened = false;
            }
        }
        if (all_opened) {
            success_dialog.setVisible (true);
        }
    }

    public void rightClick (Cell c) {
        c.flagged = true;
        c.opened = true;
        repaint ();
        if (c.has_mine == false) {
            fail_dialog.setVisible (true);
        }
        boolean all_opened = true;
        for (int x = 0; x < size_x; x++) {
            for (int y = 0; y < size_y; y++) {
                if (cells [x][y].opened == false)
                    all_opened = false;
            }
        }
        if (all_opened) {
            success_dialog.setVisible (true);
        }
    }

    public void paint(Graphics g) {

        Font font = new Font ("SansSerif", Font.PLAIN, 48);
        g.setFont (font);
        for (int x = 0; x < size_x; x++) {
            for (int y = 0; y < size_y; y++) {
                cells [x] [y].paint (g);
            }
        }
    }
}
class Cell {
    public Cell (int _x, int _y, int _center_x, int _center_y, int _half_radius, int _half_height) {
        x = _x;
        y = _y;
        center_x = _center_x;
        center_y = _center_y;
        half_radius = _half_radius;
        half_height = _half_height;
        opened = false;
        flagged = false;
        has_mine = false;
        neighbours = 0;
        border = new Polygon ();
        border.addPoint (_center_x - 2*_half_radius, _center_y);
        border.addPoint (_center_x - _half_radius, _center_y+_half_height);
        border.addPoint (_center_x + _half_radius, _center_y+_half_height);
        border.addPoint (_center_x + 2*_half_radius, _center_y);
        border.addPoint (_center_x + _half_radius, _center_y-_half_height);
        border.addPoint (_center_x - _half_radius, _center_y-_half_height);
        ch = new char [1];
    }

    public void paint (Graphics g) {
        g.setColor (Color.LIGHT_GRAY);
        g.fillPolygon (border);
        g.setColor (Color.BLACK);
        g.drawPolygon (border);
        ch [0] = '?';
        if (opened) {
            if (has_mine)
                ch [0] = '*';
            else
                ch [0] = (char)('0' + neighbours);
        }
        if (flagged)
            ch [0] = 'F';
        g.drawChars (ch, 0, 1, center_x-12, center_y+18);
    }

    public boolean containsPoint (int x, int y) {
        return border.contains (x, y);
    }
    protected int x;
    protected int y;
    protected int center_x;
    protected int center_y;
    protected int half_radius;
    protected int half_height;
    protected char ch [];
    protected Polygon border;
    protected boolean opened;
    protected boolean flagged;
    protected boolean has_mine;
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
