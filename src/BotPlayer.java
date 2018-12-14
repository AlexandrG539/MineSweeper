import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

class BotPlayer extends JPanel {
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
    int mines;
    int freeCells;

    public BotPlayer (int size_x, int size_y,
                            int cellRadiusPixels, int borderPixels,
                            int num_mines,
                            JDialog success_dialog, JDialog fail_dialog) {
        sizeX = size_x;
        sizeY = size_y;
        numMines = num_mines;
        minesPlaced = false;
        successDialog = success_dialog;
        failDialog = fail_dialog;
        mines = numMines;
        freeCells = sizeX * sizeY;
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
            if (cell.x > 0)
                if (!cells[cell.x-1][cell.y].opened) {
                    if (cells[cell.x-1][cell.y].probability == 0.000) {
                        cells[cell.x-1][cell.y].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y);
                        p.add(cells[cell.x-1][cell.y]);
                    }
                    else {
                        cells[cell.x-1][cell.y].probability = 1 - (1 - cells[cell.x-1][cell.y].probability) * (1 - ((double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y)));
                        p.add(cells[cell.x-1][cell.y]);
                    }
                }
            if (cell.x < sizeX - 1)
                if (!cells[cell.x+1][cell.y].opened) {
                    if (cells[cell.x+1][cell.y].probability == 0.000) {
                        cells[cell.x+1][cell.y].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y);
                        p.add(cells[cell.x+1][cell.y]);
                    }
                    else {
                        cells[cell.x+1][cell.y].probability = 1 - (1 - cells[cell.x+1][cell.y].probability) * (1 - ((double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y)));
                        p.add(cells[cell.x+1][cell.y]);
                    }
                }
            if (cell.y > 0) {
                if (!cells[cell.x][cell.y-1].opened) {
                    if (cells[cell.x][cell.y-1].probability == 0.000) {
                        cells[cell.x][cell.y-1].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y);
                        p.add(cells[cell.x][cell.y-1]);
                    }
                    else {
                        cells[cell.x][cell.y-1].probability = 1 - (1 - cells[cell.x][cell.y-1].probability) * (1 - (double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y));
                        p.add(cells[cell.x][cell.y-1]);
                    }
                }
                if (cell.x % 2 == 0) {
                    if (cell.x > 0)
                        if (!cells[cell.x-1][cell.y-1].opened) {
                            if (cells[cell.x-1][cell.y-1].probability == 0.000) {
                                cells[cell.x-1][cell.y-1].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y);
                                p.add(cells[cell.x-1][cell.y-1]);
                            }
                            else {
                                cells[cell.x-1][cell.y-1].probability = 1 - (1 - cells[cell.x-1][cell.y-1].probability) * (1 - (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y));
                                p.add(cells[cell.x-1][cell.y-1]);
                            }
                        }
                    if (cell.x < sizeX - 1)
                        if (!cells[cell.x+1][cell.y-1].opened) {
                            if (cells[cell.x+1][cell.y-1].probability == 0.000) {
                                cells[cell.x+1][cell.y-1].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y);
                                p.add(cells[cell.x+1][cell.y-1]);
                            }
                            else {
                                cells[cell.x+1][cell.y-1].probability = 1 - (1 - cells[cell.x+1][cell.y-1].probability) * (1 - (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y));
                                p.add(cells[cell.x+1][cell.y-1]);
                            }
                        }
                }
            }
            if (cell.y < sizeY - 1) {
                if (!cells[cell.x][cell.y+1].opened) {
                    if (cells[cell.x][cell.y+1].probability == 0.000) {
                        cells[cell.x][cell.y+1].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y);
                        p.add(cells[cell.x][cell.y+1]);
                    }
                    else {
                        cells[cell.x][cell.y+1].probability = 1 - (1 - cells[cell.x][cell.y+1].probability) * (1 - (double) freeMines(cells[cell.x][cell.y]) /
                                (double) notOpenedCellsNear(cell.x, cell.y));
                        p.add(cells[cell.x][cell.y+1]);
                    }
                }
                if (cell.x % 2 == 1) {
                    if (cell.x > 0)
                        if (!cells[cell.x-1][cell.y+1].opened) {
                            if (cells[cell.x-1][cell.y+1].probability == 0.000) {
                                cells[cell.x-1][cell.y+1].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y);
                                p.add(cells[cell.x-1][cell.y+1]);
                            }
                            else {
                                cells[cell.x-1][cell.y+1].probability = 1 - (1 - cells[cell.x-1][cell.y+1].probability) * (1 - (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y));
                                p.add(cells[cell.x-1][cell.y+1]);
                            }
                        }
                    if (cell.x < sizeX - 1)
                        if (!cells[cell.x+1][cell.y+1].opened) {
                            if (cells[cell.x+1][cell.y+1].probability == 0.000) {
                                cells[cell.x+1][cell.y+1].probability = (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y);
                                p.add(cells[cell.x+1][cell.y+1]);
                            }
                            else {
                                cells[cell.x+1][cell.y+1].probability = 1 - (1 - cells[cell.x+1][cell.y+1].probability) * (1 - (double) freeMines(cells[cell.x][cell.y]) /
                                        (double) notOpenedCellsNear(cell.x, cell.y));
                                p.add(cells[cell.x+1][cell.y+1]);
                            }
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
        if (x > 0)
            rightClick (cells [x-1] [y]);
        if (x < sizeX - 1)
            rightClick(cells [x+1] [y]);
        if (y > 0) {
            rightClick (cells [x] [y-1]);
            if (x % 2 == 0) {
                if (x > 0)
                    rightClick(cells [x-1] [y-1]);
                if (x < sizeX - 1)
                    rightClick(cells [x+1] [y-1]);
            }
        }
        if (y < sizeY - 1) {
            rightClick(cells [x] [y+1]);
            if (x % 2 == 1) {
                if (x > 0)
                    rightClick(cells [x-1] [y+1]);
                if (x < sizeX - 1)
                    rightClick(cells [x+1] [y+1]);
            }
        }
    }

    public void leftClicks(int x, int y) throws InterruptedException {
        if (x > 0)
            leftClick (cells [x-1] [y]);
        if (x < sizeX - 1)
            leftClick(cells [x+1] [y]);
        if (y > 0) {
            leftClick (cells [x] [y-1]);
            if (x % 2 == 0) {
                if (x > 0)
                    leftClick(cells [x-1] [y-1]);
                if (x < sizeX - 1)
                    leftClick(cells [x+1] [y-1]);
            }
        }
        if (y < sizeY - 1) {
            leftClick(cells [x] [y+1]);
            if (x % 2 == 1) {
                if (x > 0)
                   leftClick(cells [x-1] [y+1]);
                if (x < sizeX - 1)
                    leftClick(cells [x+1] [y+1]);
            }
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
                        if (c.opened)
                            break;
                        n++;
                    }
                    if (n == getNeighbours(x, y).size())
                        m.add(cells[x][y]);
                }
            }
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
        TimeUnit.SECONDS.sleep(1);
    }

    public int notOpenedCellsNear(int x, int y) {
        int ans = 0;
        if (x > 0)
            if (!cells[x-1][y].opened)
                ans++;
        if (x < sizeX - 1)
            if (!cells[x+1][y].opened)
                ans++;
        if (y > 0) {
            if (!cells[x][y-1].opened)
                ans++;
            if (x % 2 == 0) {
                if (x > 0)
                    if (!cells[x-1][y-1].opened)
                        ans++;
                if (x < sizeX - 1)
                    if (!cells[x+1][y-1].opened)
                        ans++;
            }
        }
        if (y < sizeY - 1) {
            if (!cells[x][y+1].opened)
                ans++;
            if (x % 2 == 1) {
                if (x > 0)
                    if (!cells [x-1][y+1].opened)
                        ans++;
                if (x < sizeX - 1)
                    if (!cells[x+1][y+1].opened)
                        ans++;
            }
        }
        return ans;
    }

    public int openedMinesNear(int x, int y) {
        int ans = 0;
        if (x > 0)
            if (cells[x-1][y].opened && cells[x-1][y].hasMine)
                ans++;
        if (x < sizeX - 1)
            if (cells[x+1][y].opened && cells[x+1][y].hasMine)
                ans++;
        if (y > 0) {
            if (cells[x][y-1].opened && cells[x][y-1].hasMine)
                ans++;
            if (x % 2 == 0) {
                if (x > 0)
                    if (cells[x-1][y-1].opened && cells[x-1][y-1].hasMine)
                        ans++;
                if (x < sizeX - 1)
                    if (cells[x+1][y-1].opened && cells[x+1][y-1].hasMine)
                        ans++;
            }
        }
        if (y < sizeY - 1) {
            if (cells[x][y+1].opened && cells[x][y+1].hasMine)
                ans++;
            if (x % 2 == 1) {
                if (x > 0)
                    if (cells [x-1][y+1].opened && cells[x-1][y+1].hasMine)
                        ans++;
                if (x < sizeX - 1)
                   if (cells[x+1][y+1].opened && cells[x+1][y+1].hasMine)
                       ans++;
            }
        }
        return ans;
    }

    public int freeMines(Cell cell) {
        return cell.neighbours - openedMinesNear(cell.x,cell.y);
    }

    public boolean isClosed(int x, int y) {
        ArrayList<Cell> s = new ArrayList<Cell> ();
        if (x > 0)
            s.add (cells [x-1][y]);
        if (x < sizeX - 1)
            s.add (cells [x+1][y]);
        if (y > 0) {
            s.add (cells [x][y-1]);
            if (x % 2 == 0) {
                if (x > 0)
                    s.add (cells [x-1][y-1]);
                if (x < sizeX - 1)
                    s.add (cells [x+1] [y-1]);
            }
        }
        if (y < sizeY - 1) {
            s.add (cells [x][y+1]);
            if (x % 2 == 1) {
                if (x > 0)
                    s.add (cells [x-1][y+1]);
                if (x < sizeX - 1)
                    s.add (cells [x+1][y+1]);
            }
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
        TimeUnit.SECONDS.sleep(1);
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