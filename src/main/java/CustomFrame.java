import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

public class CustomFrame extends JFrame {
    private JPanel contentPane;
    private ImageProcessingOpti imageProcessing;
    public CustomFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, ImageProcessing.IMG_WIDTH, ImageProcessing.IMG_HEIGHT);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        imageProcessing = new ImageProcessingOpti();
        new MyThread().start();



    }





    public void paint(Graphics g) {
        g = contentPane.getGraphics();
        //capture camera



        g.drawImage(imageProcessing.processing(), 0, 0, this);

    }

    class MyThread extends Thread {
        @Override
        public void run() {
            for (; ; ) {
                repaint();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
