import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC1;

public class ImageProcessing {


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
            imgMat = new Mat(height, width, CvType.CV_8UC3);
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
        System.out.println("colonnes :"+m.cols());
        byte[] byteBuffer = new byte[frameSize * numChannels];
        if (numChannels == 3) {
            System.out.println("Mat RGB");


            m.get(0, 0, byteBuffer);

            //write to separate R,G,B arrays
            int[][] out = new int[m.rows()][m.cols()];
            int i = 0;
            for (int r = 0; r < m.rows(); r++) {
                for (int c = 0; c < m.cols(); c++, c++) {
                    out[r][c] = (byteBuffer[i] + byteBuffer[i + 1] + byteBuffer[i + 2])/3;
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

    static Mat MatToMatCV_8C1(Mat m){
        Mat out = new Mat(m.rows(), m.cols(), CV_8UC1);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(m);
        double min = mmr.minVal;
        double max = mmr.maxVal;

        int nb_lignes = m.rows();
        int nb_cols   = m.cols();
        ;
        for (int i = 0; i < nb_lignes; i++) {
            for (int j = 0; j < nb_cols; j++) {
                double temp[] = m.get(i,j);
                double p = ((temp[0] - min)/(max-min))*255;
                m.put(i,j,p);
            }

        }

        m.convertTo(out,CV_8UC1);
        return out;
    }


    static Mat applyPrewittH(Mat image) {
        Mat image_converted = new Mat();
        image.convertTo(image_converted,CV_32F);
        Mat img_prewittH = new Mat(image.rows(), image.cols(), CV_32F);
        int nb_lignes = image.rows();
        int nb_cols   = image.cols();
        ;
        for (int i = 1; i < nb_lignes-1; i++) {
            for (int j = 1; j < nb_cols-1; j++) {
                double temp[] = image_converted.get(i,j-1);
                double p = temp[0] * -1;
                double temp2[] = image_converted.get(i,j);
                p += temp2[0]*1;
                img_prewittH.put(i,j,p);
            }

        }



        return img_prewittH;
    }

    static Mat applyPrewittV(Mat image) {
        Mat image_converted = new Mat();
        image.convertTo(image_converted,CV_32F);
        Mat img_prewittH = new Mat(image.rows(), image.cols(), CV_32F);
        int nb_lignes = image.rows();
        int nb_cols   = image.cols();
        ;
        for (int i = 1; i < nb_lignes-1; i++) {
            for (int j = 1; j < nb_cols-1; j++) {
                double temp[] = image_converted.get(i-1,j);
                double p = temp[0] * -1;
                double temp2[] = image_converted.get(i,j);
                p += temp2[0]*1;
                img_prewittH.put(i,j,p);
            }

        }



        return img_prewittH;
    }


    static Mat NormeGradient(Mat a, Mat b){
        Mat normeGr = new Mat(a.rows(), b.cols(), CV_32F);

        int nb_lignes = a.rows();
        int nb_cols   = b.cols();
        for (int i = 0; i < nb_lignes; i++) {
            for (int j = 0; j < nb_cols; j++) {
                double temp[] = a.get(i,j);
                double temp2[] = b.get(i,j);
                double p = Math.sqrt(    Math.pow(temp[0],2) + Math.pow(temp2[0],2)   );
                normeGr.put(i,j,p);
            }

        }
    return normeGr;
    }

    static Mat Convolution(Mat m, Mat mask) {
        return m;
    }
}
