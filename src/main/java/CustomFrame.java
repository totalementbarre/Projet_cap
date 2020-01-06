import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CustomFrame extends JFrame {
    private JPanel contentPane;

    public CustomFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, 1280, 720);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        new MyThread().start();
    }

    VideoCap videoCap = new VideoCap();

    public void paint(Graphics g) {
        g = contentPane.getGraphics();
        BufferedImage img =videoCap.getOneFrame();
        //img = ImageProcessing.applyPrewittH(img);
        BufferedImage template = ImageProcessing.PatternImage();
        Mat test = ImageProcessing.BufferedImageToMat(img);
        BufferedImage test2 = ImageProcessing.MatToBufferedImage(test);


        g.drawImage(test2, 0, 0, this);
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            for (; ; ) {
                repaint();
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
