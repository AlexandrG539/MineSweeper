
import javax.swing.*;
import java.awt.*;





public class MineSweeper {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        GameResultDialog failDialog =
                new GameResultDialog (frame, "<html><h1><i>Вы проиграли!!!</h1></html>");
        GameResultDialog successDialog =
                new GameResultDialog (frame, "<html><h1><i>Вы выиграли!!!</h1></html>");
        MineSweeperGame game = new MineSweeperGame (15, 10,  40, 10,
                30,
                successDialog, failDialog);
        game.clear ();
        frame.setSize (game.getFieldWidth (), game.getFieldHeight ());
        frame.getContentPane().add(game);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.LIGHT_GRAY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
