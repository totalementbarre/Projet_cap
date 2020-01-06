import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomFrame extends JFrame {
    private JPanel contentPane;
    private ImageProcessing ip;
    public CustomFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, 1280, 720);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        ip = new ImageProcessing();

        new MyThread().start();
    }





    public void paint(Graphics g) {
        g = contentPane.getGraphics();
        //capture camera



        g.drawImage(ip.Traitement(), 0, 0, this);
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            for (; ; ) {
                repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
