import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;

import static org.opencv.core.CvType.CV_8UC1;

public class ImageProcessingOpti {
    public static final int IMG_WIDTH = 1280;
    public static final int IMG_HEIGHT = 720;
    public static final int SEUIL_HOUGH = 10;

    private VideoCap videoCap;
    private float[][] image;
    private float[][] gradientNorm;
    private float[][] gradientAngles;


    private float[][] image_out;

    private BufferedImage sourceImg;
    private BufferedImage imgToPlot;

    public ImageProcessingOpti() {
        videoCap = new VideoCap();
        imgToPlot = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);


        gradientNorm = new float[IMG_WIDTH][IMG_HEIGHT];
        gradientAngles = new float[IMG_WIDTH][IMG_HEIGHT];
        image = new float[IMG_WIDTH][IMG_HEIGHT];
        image_out = new float[IMG_WIDTH][IMG_HEIGHT];

        Thread t1 = new Thread(videoCap);
        t1.start();
    }


    public BufferedImage processing() {
        Date date = new Date();
        long startingTime = date.getTime();
//        sourceImg = videoCap.getOneFrame();
        if (videoCap.isReady()) {
            sourceImg = videoCap.getCurrentImageCopy();

            BufferedImageToArray(sourceImg, image);

            GradientFast(image,gradientNorm,gradientAngles);

            //ArrayToBufferedImage(gradientAngles, imgToPlot);
            ArrayToBufferedImage(gradientNorm, imgToPlot);

        }
        long loopTime = (new Date()).getTime();
        System.out.println("Loop duration : " + (loopTime - startingTime));

        return imgToPlot;
    }

    private void BufferedImageToArray(BufferedImage in, float[][] out) {
        int r, g, b;
        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 0; l < IMG_HEIGHT; l++) {

                Color pix = new Color(in.getRGB(c, l));
                r = pix.getRed();
                g = pix.getGreen();
                b = pix.getBlue();
                out[c][l] = (r + g + b) / 3;
                //System.out.println(out[c][l]);

            }

        }
    }

    private void ArrayToBufferedImage(float[][] in, BufferedImage out) {
        double pix;
        int r, g, b, a, p;
        //AUTOSCALLING
        double min=100,max=0;
        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 0; l < IMG_HEIGHT; l++) {
                pix = in[c][l];
                if(pix>max) max = pix;
                if(pix<min) min = pix;
            }
        }

        //System.out.println("min:"+min +"\tmax:"+max);

        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 0; l < IMG_HEIGHT; l++) {
                pix = in[c][l];

                pix = (((pix - min)/(max-min))-0.5)*128+128;
                //System.out.println(pix);


                a = 255;
                r = (int) pix;
                g = (int) pix;
                b = (int) pix;

                //set the pixel value
                p = (a << 24) | (r << 16) | (g << 8) | b;

                out.setRGB(c, l, p);
            }
        }
    }

    public void GradientFast(float[][] in, float[][] norm,float[][] angles) {


        float gradH,gradV;
        for (int c = 1; c < IMG_WIDTH-1; c++) {
            for (int l = 1; l < IMG_HEIGHT-1; l++) {

                gradH = -in[c-1][l] + in[c][l];
                gradV = -in[c][l-1] + in[c][l];

                norm[c][l]= (float)Math.sqrt(Math.pow(gradH,2)+Math.pow(gradV,2));
                angles [c][l] = (float) (Math.atan2(gradH,gradV)*180/3.14);

            }
        }
    }
}
