import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.opencv.core.CvType.*;

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
    Mat img_prewittH = new Mat(720, 1280, CV_32F);
    Mat img_prewittV = new Mat(720, 1280, CV_32F);
    Mat normeGr = new Mat(720, 1280, CV_32F);
    public void paint(Graphics g) {
        g = contentPane.getGraphics();
        //capture camera
        BufferedImage img =videoCap.getOneFrame();
        Mat cameraMat = ImageProcessing.BufferedImageToMat(img);
        Mat cameraMat_GREY = ImageProcessing.MatRGBToGrey(cameraMat);
        ////Mat cameraMat = ImageProcessing.DownsizeResolution(cameraMat_full,4);

        // Template
        BufferedImage template = ImageProcessing.PatternImage();
        Mat template_mat_RGB = ImageProcessing.BufferedImageToMat(template);
        Mat template_mat_GREY = ImageProcessing.MatRGBToGrey(template_mat_RGB);



        ImageProcessing.applyPrewittH(cameraMat_GREY,img_prewittH);
        ImageProcessing.applyPrewittV(cameraMat_GREY,img_prewittV);
        ImageProcessing.NormeGradient(img_prewittH,img_prewittV,normeGr);

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
