import static org.junit.jupiter.api.Assertions.*;

class MineSweeperGameTest {
    @org.junit.jupiter.api.Test
    void getFieldWidth() {
        MineSweeperGame gameTest = new MineSweeperGame (15, 10, 40, 10, 0,
                null, null);
        assertEquals(gameTest.getFieldWidth(),950);
    }
    @org.junit.jupiter.api.Test
    void getFieldHeight() {
        MineSweeperGame gameTest = new MineSweeperGame (15, 10, 40, 10, 0,
                null, null);
        assertEquals(gameTest.getFieldHeight(),764);
    }

    @org.junit.jupiter.api.Test
    void fillWithMines() {
        int mines = 0;
        MineSweeperGame gameTest = new MineSweeperGame (15, 10, 40, 10, 30,
                null, null);
        gameTest.fillWithMines(0,0);
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 10; y++) {
                if (gameTest.cells[x][y].hasMine)
                    mines++;
            }
        }
        if (mines == 30)
            assertEquals(true, true);
        else assertEquals(true, false);
    }

    @org.junit.jupiter.api.Test
    void calculateNeighboursCount() {
        MineSweeperGame gameTest = new MineSweeperGame (15, 10, 40, 10, 0,
                null, null);
        gameTest.placeMine(0,1);
        gameTest.placeMine(0,2);
        gameTest.placeMine(1,0);
        gameTest.placeMine(2,1);
        gameTest.placeMine(1,2);
        gameTest.placeMine(2,2);
        gameTest.calculateNeighboursCount();
        assertEquals(gameTest.cells[1][1].neighbours,6);
    }

    @org.junit.jupiter.api.Test
    void leftClick() {
        MineSweeperGame gameTest = new MineSweeperGame (15, 10, 40, 10, 0,
                null, null);
        gameTest.calculateNeighboursCount();
        gameTest.leftClick(gameTest.cells[1][1]);
        if (gameTest.cells[1][1].opened)
            assertEquals(true, true);
        else assertEquals(true, false);
    }

    @org.junit.jupiter.api.Test
    void rightClick() {
        MineSweeperGame gameTest = new MineSweeperGame (15, 10, 40, 10, 0,
                null, null);
        gameTest.placeMine(1,1);
        gameTest.rightClick(gameTest.cells[1][1]);
        if (gameTest.cells[1][1].flagged)
            assertEquals(true, true);
        else assertEquals(true, false);
    }

}