
import javax.swing.*;
import java.awt.*;





public class MineSweeper {
    public static void main(String[] args) {
        if (args.length != 3 || Integer.valueOf(args[0]) <= 0 || Integer.valueOf(args[1]) <= 0
                || Integer.valueOf(args[2]) <= 0) {
            System.out.println("неверный аргумент");
            return;
        }
        int w = Integer.valueOf(args[0]);
        if (w > 25) {
            System.out.println("слишком широкое поле");
            return;
        }
        int h = Integer.valueOf(args[1]);
        if (h > 11) {
            System.out.println("Слишком высокое поле");
            return;
        }
        int m = Integer.valueOf(args[2]);
        if (m >= w * h) {
            System.out.println("неправильно задано количество мин");
            return;
        }
        JFrame frame = new JFrame();
        GameResultDialog failDialog =
                new GameResultDialog (frame, "<html><h1><i>Вы проиграли!!!</h1></html>");
        GameResultDialog successDialog =
                new GameResultDialog (frame, "<html><h1><i>Вы выиграли!!!</h1></html>");
        MineSweeperGame game = new MineSweeperGame (w, h,  40, 10,
                m,
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
