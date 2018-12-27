import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class MineSweeperGame extends JPanel {
    public int sizeX;
    public int sizeY;
    public int fieldWidth;
    public int fieldHeight;
    public int cellHalfRadius;
    public int cellHalfHeight;
    JDialog successDialog;
    JDialog failDialog;
    int mines;
    public Cell cells [][];
    boolean minesPlaced;
    int numMines;
    int freeCells;
    boolean bot;

    public MineSweeperGame (int size_x, int size_y,
                            int cellRadiusPixels, int borderPixels,
                            int num_mines,
                            JDialog success_dialog, JDialog fail_dialog, boolean botFlag) {
        sizeX = size_x;
        sizeY = size_y;
        numMines = num_mines;
        bot = botFlag;
        minesPlaced = false;
        mines = numMines;
        successDialog = success_dialog;
        failDialog = fail_dialog;
        freeCells = sizeX * sizeY;
        cellHalfRadius = cellRadiusPixels / 2;
        cellHalfHeight = (int) (cellHalfRadius * Math.sqrt(3.0));
        fieldWidth = 2 * borderPixels + sizeX * 3 * cellHalfRadius + cellHalfRadius + 10;
        fieldHeight = 2 * borderPixels + cellHalfHeight * (1 + 2 * sizeY) + 30;
        cells = new Cell[sizeX][sizeY];
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                int centerX = borderPixels + 2 * cellHalfRadius + 3 * x * cellHalfRadius;
                int centerY = borderPixels + cellHalfHeight + 2 * cellHalfHeight * y;
                if (x % 2 == 1)
                    centerY += cellHalfHeight;
                cells[x][y] = new Cell(x, y, centerX, centerY, cellHalfRadius, cellHalfHeight);
            }
        }
        if (!bot) {
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent ev) {
                    int mouseX = ev.getX();
                    int mouseY = ev.getY();
                    Cell c = null;
                    for (int x = 0; x < sizeX; x++) {
                        for (int y = 0; y < sizeY; y++) {
                            if (cells[x][y].containsPoint(mouseX, mouseY)) {
                                c = cells[x][y];
                            }
                        }
                    }
                    if (c != null) {
                        if (ev.getButton() == MouseEvent.BUTTON1) {
                            try {
                                leftClick(c);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else if (ev.getButton() == MouseEvent.BUTTON3) {
                            try {
                                rightClick(c);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }
    List<Cell> noClosedCells = new ArrayList<>();


    public void play() throws InterruptedException {
        if (!minesPlaced)
            firstClick();
        noClosedCells = getNoClosed();
        closeCells(noClosedCells);
        noClosedCells = getNoClosed();
        int d;
        while (true) {
            d = 0;
            for (Cell cell: noClosedCells) {
                if (freeMines(cell) == 0) {
                    leftClicks(cell.x, cell.y);
                    d++;
                }
            }
            for (Cell cell: noClosedCells) {
                if (freeMines(cell) == notOpenedCellsNear(cell.x, cell.y)) {
                    rightClicks(cell.x, cell.y);
                    d++;
                }
            }
            if (d == 0 && noClosedCells.size() == 0) {
                enclaveProblem();
            }
            if (d == 0 && noClosedCells.size() != 0) {
                probabilityProblem();
            }
            noClosedCells = getNoClosed();
            closeCells(getNoClosed());
            noClosedCells = getNoClosed();
        }
    }

    public void probabilityProblem() throws InterruptedException {
        Set<Cell> p = new HashSet();
        for (Cell cell: noClosedCells) {
            List<Cell> n = getNeighbours(cell.x, cell.y);
            for (Cell c: n) {
                if (!c.opened) {
                    if (c.probability == 0.000) {
                        c.probability = (double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y);
                        p.add(c);
                    } else {
                        c.probability = 1 - (1 - c.probability) * (1 - ((double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y)));
                        p.add(c);
                    }
                }
            }
        }
        double maxP = 0.000;
        double minP = 10000000.000;
        Cell min = null;
        Cell max = null;
        for (Cell cell: p) {
            if (cell.probability >= maxP) {
                maxP = cell.probability;
                max = cell;
            }
            if (cell.probability < minP) {
                minP = cell.probability;
                min = cell;
            }
        }
        if ((double)mines/freeCells < 1 - maxP && (double)mines/freeCells < minP) {
            randomClick();
        }
        else {
            if (1 - minP > maxP)
                leftClick(min);
            else rightClick(max);
            for (Cell cell : p) {
                cell.probability = 0.000;
            }
            noClosedCells = getNoClosed();
            closeCells(getNoClosed());
            noClosedCells = getNoClosed();
        }
    }

    public void enclaveProblem() throws InterruptedException {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (!cells[x][y].opened) {
                    if (freeCells - mines > mines) {
                        leftClick(cells[x][y]);
                        noClosedCells = getNoClosed();
                        closeCells(getNoClosed());
                        noClosedCells = getNoClosed();
                        return;
                    }
                    else {
                        rightClick(cells[x][y]);
                        noClosedCells = getNoClosed();
                        closeCells(getNoClosed());
                        noClosedCells = getNoClosed();
                        return;
                    }
                }
            }
        }
    }

    public void firstClick() throws InterruptedException {
        int x = rnd(sizeX);
        int y = rnd(sizeY);
        noClosedCells.add(cells[x][y]);
        leftClick(cells[x][y]);
        if (cells[x][y].neighbours > 1)
            randomClick();
    }

    public static int rnd(int max)
    {
        return (int) (Math.random() * max);
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

    public void rightClicks(int x, int y) throws InterruptedException {
        List<Cell> n = getNeighbours(x, y);
        for (Cell cell: n) {
            rightClick(cell);
        }
    }

    public void leftClicks(int x, int y) throws InterruptedException {
        List<Cell> n = getNeighbours(x, y);
        for (Cell cell: n) {
            leftClick(cell);
        }
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
                                break;
                            }
                        }
                    }
                }
            }
            freeCells -= changes;
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

    public void randomClick() throws InterruptedException {
        List<Cell> m = new ArrayList<>();
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (!cells[x][y].opened) {
                    int n = 0;
                    for (Cell c : getNeighbours(x, y)) {
                        if (c.opened && !c.hasMine)
                            break;
                        n++;
                    }
                    if (n == getNeighbours(x, y).size())
                        m.add(cells[x][y]);
                }
            }
        }
        if (m.size() == 0) {
            probabilityProblem();
            return;
        }
        leftClick(m.get(rnd(m.size())));
        noClosedCells = getNoClosed();
        closeCells(getNoClosed());
        noClosedCells = getNoClosed();
    }

    public void leftClick (Cell c) throws InterruptedException {
        if (!minesPlaced) {
            fillWithMines (c.x, c.y);
            calculateNeighboursCount ();
            minesPlaced = true;
        }
        if (c.opened)
            return;
        freeCells--;
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
        if (bot) {
            TimeUnit.MILLISECONDS.sleep(300);
        }
    }

    public int notOpenedCellsNear(int x, int y) {
        List<Cell> n = getNeighbours(x, y);
        int ans = 0;
        for (Cell cell: n) {
            if (!cell.opened)
                ans++;
        }
        return ans;
    }

    public int openedMinesNear(int x, int y) {
        List<Cell> n = getNeighbours(x, y);
        int ans = 0;
        for (Cell cell: n) {
            if (cell.opened && cell.hasMine)
                ans++;
        }
        return ans;
    }

    public int freeMines(Cell cell) {
        return cell.neighbours - openedMinesNear(cell.x,cell.y);
    }

    public boolean isClosed(int x, int y) {
        List<Cell> n = getNeighbours(x, y);
        List<Cell> s = new ArrayList<Cell> ();
        for (Cell cell: n) {
            s.add(cell);
        }
        for (Cell ce: s) {
            if (!ce.opened)
                return false;
        }
        return true;
    }

    public void closeCells (List<Cell> c) {
        for (Cell cell: c) {
            if (isClosed(cell.x,cell.y))
                cell.closed = true;
            if (cell.hasMine && cell.opened)
                cell.closed = true;
        }
    }
    public List<Cell> getNoClosed () {
        List<Cell> c = new ArrayList<>();
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (!cells[x][y].closed && cells[x][y].opened)
                    c.add(cells[x][y]);
            }
        }
        return c;
    }
    public void rightClick (Cell c) throws InterruptedException {
        if (!minesPlaced) {
            fillWithMines (c.x, c.y);
            calculateNeighboursCount ();
            minesPlaced = true;
        }
        if (c.opened)
            return;
        c.flagged = true;
        c.opened = true;
        freeCells--;
        mines--;
        repaint ();
        if (!c.hasMine) {
            failDialog.setVisible (true);
        }
        boolean all_opened = true;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (!cells[x][y].opened)
                    all_opened = false;
            }
        }
        if (all_opened) {
            successDialog.setVisible (true);
        }
        if (bot) {
            TimeUnit.MILLISECONDS.sleep(300);
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
