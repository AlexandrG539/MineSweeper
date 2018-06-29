import java.awt.*;

class Cell {
    public Cell (int x, int y, int center_x, int center_y, int half_radius, int half_height) {
        this.x = x;
        this.y = y;
        centerX = center_x;
        centerY = center_y;
        halfRadius = half_radius;
        halfHeight = half_height;
        opened = false;
        flagged = false;
        hasMine = false;
        neighbours = 0;
        border = new Polygon();
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
