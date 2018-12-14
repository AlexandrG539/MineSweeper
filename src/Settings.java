import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Settings {
    class FieldSize extends JDialog {
        public  FieldSize (JFrame owner, String msg) {
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
}
