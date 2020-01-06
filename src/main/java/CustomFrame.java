import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

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
        Mat cameraMat = ImageProcessing.BufferedImageToMat(img);
        BufferedImage template = ImageProcessing.PatternImage();
        Mat img_mat_RGB = ImageProcessing.BufferedImageToMat(template);
        Mat img_mat_GREY = ImageProcessing.MatRGBToGrey(img_mat_RGB);

        Mat prewittH = ImageProcessing.applyPrewittV(cameraMat);
        Mat prewittV = ImageProcessing.applyPrewittV(cameraMat);
        Mat normeGr = ImageProcessing.NormeGradient(prewittH,prewittV);

        Mat test = ImageProcessing.MatToMatCV_8C1(normeGr);
        Mat prewittH_converted = ImageProcessing.MatToMatCV_8C1(test);
        BufferedImage test2 = ImageProcessing.MatToBufferedImage(prewittH_converted);


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
