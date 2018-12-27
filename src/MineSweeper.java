
import javax.swing.*;
import java.awt.*;





public class MineSweeper {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 4 || Integer.valueOf(args[0]) <= 0 || Integer.valueOf(args[1]) <= 0
                || Integer.valueOf(args[2]) <= 0 || (!args[3].equals("bot") && !args[3].equals("player"))) {
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
        if (args[3].equals("player")) {
            JFrame frame = new JFrame();
            GameResultDialog failDialog =
                    new GameResultDialog(frame, "<html><h1><i>Вы проиграли!!!</h1></html>");
            GameResultDialog successDialog =
                    new GameResultDialog(frame, "<html><h1><i>Вы выиграли!!!</h1></html>");
            MineSweeperGame game = new MineSweeperGame(w, h, 40, 10,
                    m,
                    successDialog, failDialog, false);
            game.clear();
            frame.setSize(game.getFieldWidth(), game.getFieldHeight());
            frame.getContentPane().add(game);
            frame.setLocationRelativeTo(null);
            frame.setBackground(Color.LIGHT_GRAY);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        if (args[3].equals("bot")) {
            JFrame frame = new JFrame();
            GameResultDialog failDialog =
                    new GameResultDialog(frame, "<html><h1><i>Вы проиграли!!!</h1></html>");
            GameResultDialog successDialog =
                    new GameResultDialog(frame, "<html><h1><i>Вы выиграли!!!</h1></html>");
            MineSweeperGame game = new MineSweeperGame(w, h, 40, 10,
                    m,
                    successDialog, failDialog, true);
            game.clear();
            frame.setSize(game.getFieldWidth(), game.getFieldHeight());
            frame.getContentPane().add(game);
            frame.setLocationRelativeTo(null);
            frame.setBackground(Color.LIGHT_GRAY);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            game.play();
        }
    }
}
