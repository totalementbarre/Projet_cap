import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.opencv.core.CvType.*;

public class ImageProcessing {

    public static final int IMG_HEIGHT = 720;
    public static final int IMG_WIDTH = 1280;

    private VideoCap videoCap;
    private Mat img_prewittH;
    private Mat img_prewittV;
    private Mat normeGr;
    private Mat imgMat;
    private Mat imgMatGREY;
    private Mat normeGr8UC1;


    private BufferedImage sourceImg;
    private BufferedImage imgMatBuff;


    public ImageProcessing() {
        videoCap = new VideoCap();
        //img_prewittH = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_32F);
        //img_prewittV = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_32F);
        normeGr = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_32F);
        normeGr8UC1 = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_8UC1);
        imgMat = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_8UC3);
        imgMatGREY = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_8UC1);

        imgMatBuff = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        Thread t1 = new Thread(videoCap);
        t1.start();
        //imgMatBuff = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
    }

    public BufferedImage processing() {
        Date date = new Date();
        long startingTime = date.getTime();
//        sourceImg = videoCap.getOneFrame();
        if (videoCap.isReady()) {
            sourceImg = videoCap.getCurrentImageCopy();

            BufferedImageToMat(sourceImg, imgMat);
            MatRGBToGrey(imgMat, imgMatGREY);


            Gradient(imgMatGREY, normeGr);

            normeGr.convertTo(normeGr8UC1, CV_8UC1);
            MatToBufferedImage(normeGr8UC1, imgMatBuff);

        /*
        Mat cameraMat = ImageProcessing.BufferedImageToMat(sourceImg);
        Mat cameraMat_GREY = ImageProcessing.MatRGBToGrey(cameraMat);
        long convToGrayTime = (new Date()).getTime();
        System.out.println("Converting to gray : " + (convToGrayTime - startingTime));
        ////Mat cameraMat = ImageProcessing.DownsizeResolution(cameraMat_full,4);

        // Template
        //BufferedImage template = ImageProcessing.PatternImage();
        //Mat template_mat_RGB = ImageProcessing.BufferedImageToMat(template);
        //Mat template_mat_GREY = ImageProcessing.MatRGBToGrey(template_mat_RGB);


        ImageProcessing.applyPrewittH(cameraMat_GREY, img_prewittH);
        ImageProcessing.applyPrewittV(cameraMat_GREY, img_prewittV);
        long prewittTime = (new Date()).getTime();
        System.out.println("Converting to prewitt : " + (prewittTime - convToGrayTime));

        ImageProcessing.NormeGradient(img_prewittH, img_prewittV, normeGr);
        long gradientTime = (new Date()).getTime();
        System.out.println("Gradient : " + (gradientTime - prewittTime));

        Mat test = ImageProcessing.MatToMatCV_8C1(normeGr);  //TODO super long , a optimiser
        long matToMatTime = (new Date()).getTime();
        System.out.println("MatToMat : " + (matToMatTime - gradientTime));
        //Mat prewittH_converted = ImageProcessing.MatToMatCV_8C1(test);

        BufferedImage test2 = ImageProcessing.MatToBufferedImage(test);
        long matToBufTime = (new Date()).getTime();
        System.out.println("MatToBuffer : " + (matToBufTime - matToMatTime));

        System.out.println("Total processing duration time : " + ((new Date()).getTime() - startingTime) + "\n");
        return test2;

         */
        }
        return imgMatBuff;
    }

    public void Gradient(Mat in, Mat norm) {
        int nb_lignes = in.rows();
        int nb_cols = in.cols();
        ;
        for (int i = 1; i < nb_lignes - 1; i++) {
            for (int j = 1; j < nb_cols - 1; j++) {
                double temp[] = in.get(i, j - 1);
                double h = temp[0] * -1;
                double temp2[] = in.get(i, j);
                h += temp2[0] * 1;

                double temp3[] = in.get(i - 1, j);
                double v = temp3[0] * -1;
                double temp4[] = in.get(i, j);
                v += temp4[0] * 1;


                double n = Math.sqrt(Math.pow(h, 2) + Math.pow(v, 2));

                norm.put(i, j, n);
            }

        }

    }

    public void MatRGBToGrey(Mat m, Mat mat1) {
        Imgproc.cvtColor(m, mat1, Imgproc.COLOR_RGB2GRAY);

    }

    public void BufferedImageToMat(BufferedImage sourceImg, Mat imgMat) {


        DataBuffer dataBuffer = sourceImg.getRaster().getDataBuffer();
        byte[] imgPixels = null;


        int width = sourceImg.getWidth();
        int height = sourceImg.getHeight();

        if (dataBuffer instanceof DataBufferByte) {
            imgPixels = ((DataBufferByte) dataBuffer).getData();
        }

        if (dataBuffer instanceof DataBufferInt) {

            int byteSize = width * height;
            imgPixels = new byte[byteSize * 3];

            int[] imgIntegerPixels = ((DataBufferInt) dataBuffer).getData();

            for (int p = 0; p < byteSize; p++) {
                imgPixels[p * 3 + 0] = (byte) ((imgIntegerPixels[p] & 0x00FF0000) >> 16);
                imgPixels[p * 3 + 1] = (byte) ((imgIntegerPixels[p] & 0x0000FF00) >> 8);
                imgPixels[p * 3 + 2] = (byte) (imgIntegerPixels[p] & 0x000000FF);
                /*imgPixels[p*3 + 0] = (byte)255;
                imgPixels[p*3 + 1] = (byte)255;
                imgPixels[p*3 + 2] = (byte)255;*/
            }
        }

        if (imgPixels != null) {

            imgMat.put(0, 0, imgPixels);
        }


    }

    public void MatToBufferedImage(Mat m, BufferedImage image) {
        if (!m.empty()) {
            int type = BufferedImage.TYPE_BYTE_GRAY;
            if (m.channels() > 1) {
                type = BufferedImage.TYPE_3BYTE_BGR;
            }
            int bufferSize = m.channels() * m.cols() * m.rows();
            byte[] b = new byte[bufferSize];
            m.get(0, 0, b); // get all the pixels
            final byte[] targetPixels = ((DataBufferByte) imgMatBuff.getRaster().getDataBuffer()).getData();
            System.arraycopy(b, 0, targetPixels, 0, b.length);
        }

    }


    ////////////////////////////old

    static BufferedImage PatternImage() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("/DATA/FAC/M2/carte_a_puce/ellipse.png"));
        } catch (IOException e) {

        }
        final BufferedImage img1 = img;
        return img;
    }

    static Mat BufferedImageToMat(BufferedImage sourceImg) {


        DataBuffer dataBuffer = sourceImg.getRaster().getDataBuffer();
        byte[] imgPixels = null;
        Mat imgMat = null;

        int width = sourceImg.getWidth();
        int height = sourceImg.getHeight();

        if (dataBuffer instanceof DataBufferByte) {
            imgPixels = ((DataBufferByte) dataBuffer).getData();
        }

        if (dataBuffer instanceof DataBufferInt) {

            int byteSize = width * height;
            imgPixels = new byte[byteSize * 3];

            int[] imgIntegerPixels = ((DataBufferInt) dataBuffer).getData();

            for (int p = 0; p < byteSize; p++) {
                imgPixels[p * 3 + 0] = (byte) ((imgIntegerPixels[p] & 0x00FF0000) >> 16);
                imgPixels[p * 3 + 1] = (byte) ((imgIntegerPixels[p] & 0x0000FF00) >> 8);
                imgPixels[p * 3 + 2] = (byte) (imgIntegerPixels[p] & 0x000000FF);
                /*imgPixels[p*3 + 0] = (byte)255;
                imgPixels[p*3 + 1] = (byte)255;
                imgPixels[p*3 + 2] = (byte)255;*/
            }
        }

        if (imgPixels != null) {
            imgMat = new Mat(height, width, CV_8UC3);
            imgMat.put(0, 0, imgPixels);
        }


        return imgMat;
    }


    static BufferedImage MatToBufferedImage(Mat m) {
        if (!m.empty()) {
            int type = BufferedImage.TYPE_BYTE_GRAY;
            if (m.channels() > 1) {
                type = BufferedImage.TYPE_3BYTE_BGR;
            }
            int bufferSize = m.channels() * m.cols() * m.rows();
            byte[] b = new byte[bufferSize];
            m.get(0, 0, b); // get all the pixels
            BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
            final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(b, 0, targetPixels, 0, b.length);
            return image;
        }

        return null;
    }

    static Mat MatRGBToGrey(Mat m) {
        Mat mat1 = new Mat(m.rows(), m.cols(), CV_8UC1);
        Imgproc.cvtColor(m, mat1, Imgproc.COLOR_RGB2GRAY);
        return mat1;
    }

    static int[][] Mat2IntArray(Mat m) {

        int numChannels = m.channels();//is 3 for 8UC3 (e.g. RGB)
        int frameSize = m.rows() * m.cols();
        System.out.println("colonnes :" + m.cols());
        byte[] byteBuffer = new byte[frameSize * numChannels];
        if (numChannels == 3) {
            System.out.println("Mat RGB");


            m.get(0, 0, byteBuffer);

            //write to separate R,G,B arrays
            int[][] out = new int[m.rows()][m.cols()];
            int i = 0;
            for (int r = 0; r < m.rows(); r++) {
                for (int c = 0; c < m.cols(); c++, c++) {
                    out[r][c] = (byteBuffer[i] + byteBuffer[i + 1] + byteBuffer[i + 2]) / 3;
                    System.out.println(out[r][c]);
                    i = i + 3;
                }
            }
            return out;

        } else if (numChannels == 1) {
            System.out.println("Mat GREY");

            m.get(0, 0, byteBuffer);

            //write to separate R,G,B arrays
            int[][] out = new int[m.rows()][m.cols()];
            int i = 0;
            for (int r = 0; r < m.rows(); r++) {
                for (int c = 0; c < m.cols(); c++, c++) {
                    out[r][c] = byteBuffer[i];
                    //System.out.println(out[r][c]);
                    i = i + 1;
                }
            }
            return out;
        } else {
            System.out.println("Erreur nb de dim de l'image pas bon!");
            return null;
        }
    }

    static Mat MatToMatCV_8C1(Mat m) {
        Mat out = new Mat(m.rows(), m.cols(), CV_8UC1);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(m);
        double min = mmr.minVal;
        double max = mmr.maxVal;

        int nb_lignes = m.rows();
        int nb_cols = m.cols();

        for (int i = 0; i < nb_lignes; i++) {
            for (int j = 0; j < nb_cols; j++) {
                double temp[] = m.get(i, j);
                double p = ((temp[0] - min) / (max - min)) * 255;
                m.put(i, j, p);
            }

        }

        m.convertTo(out, CV_8UC1);
        return out;
    }


    static void applyPrewittH(Mat image, Mat img_prewittH) {

        int nb_lignes = image.rows();
        int nb_cols = image.cols();
        ;
        for (int i = 1; i < nb_lignes - 1; i++) {
            for (int j = 1; j < nb_cols - 1; j++) {
                double temp[] = image.get(i, j - 1);
                double p = temp[0] * -1;
                double temp2[] = image.get(i, j);
                p += temp2[0] * 1;
                img_prewittH.put(i, j, p);
            }

        }


    }

    static void applyPrewittV(Mat image, Mat img_prewittH) {

        //Mat img_prewittH = new Mat(image.rows(), image.cols(), CV_32F);
        int nb_lignes = image.rows();
        int nb_cols = image.cols();
        ;
        for (int i = 1; i < nb_lignes - 1; i++) {
            for (int j = 1; j < nb_cols - 1; j++) {
                double temp[] = image.get(i - 1, j);
                double p = temp[0] * -1;
                double temp2[] = image.get(i, j);
                p += temp2[0] * 1;
                img_prewittH.put(i, j, p);
            }

        }


    }


    static void NormeGradient(Mat a, Mat b, Mat normeGr) {


        int nb_lignes = a.rows();
        int nb_cols = b.cols();
        for (int i = 0; i < nb_lignes; i++) {
            for (int j = 0; j < nb_cols; j++) {
                double temp[] = a.get(i, j);
                double temp2[] = b.get(i, j);
                double p = Math.sqrt(Math.pow(temp[0], 2) + Math.pow(temp2[0], 2));
                normeGr.put(i, j, p);
            }

        }
    }

    static Mat DownsizeResolution(Mat m, int factor) {
        int nb_lignes = m.rows() / factor;
        int nb_cols = m.cols() / factor;

        Mat out = new Mat(nb_lignes, nb_cols, CV_8UC1);


        for (int i = 0; i < nb_lignes; i++) {
            for (int j = 0; j < nb_cols; j++) {
                double temp[] = m.get(i * factor, j * factor);
                out.put(i, j, temp[0]);
            }

        }
        return out;
    }

    static Mat Convolution(Mat m, Mat mask) {
        return m;
    }
}
