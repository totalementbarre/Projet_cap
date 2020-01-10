import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.StrictMath.abs;
import static org.opencv.imgproc.Imgproc.*;

public class HoughCircles {
    public List<Eyes> listEyes = new ArrayList<>();
    public List<Eyes> listEyesWinners = new ArrayList<>();
    public Eyes w;


    public List<Eyes> getListEyes() {
        return listEyesWinners;
    }

    public Eyes getW() {
        return w;
    }

    public HoughCircles(Mat image){
        // Load the native library.
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat gray = image.clone();
        cvtColor(image, gray, COLOR_BGR2GRAY);
        medianBlur(gray, gray, 3);
        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1,
                50, // change this value to detect circles with different distances to each other
                100.0, 22, 5, 25); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles


        float[][] centers = new float[circles.cols()][3];


        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            centers[x][0] = (float)c[0];
            centers[x][1] = (float)c[1];
            centers[x][2] = (float)c[2];
        }


        //List<Eyes> listEyes = new ArrayList<>();


        double radius_tolerance = 2;
        for (int i = 0; i < circles.cols(); i++) {
            for (int j = 0; j <circles.cols() ; j++) {
                if(Math.abs(centers[i][2]-centers[j][2])<radius_tolerance)
                {
                    float dx = centers[i][0]-centers[j][0];
                    float dy = centers[i][1]-centers[j][1];
                    double distance  = Math.sqrt(  Math.pow(dx,2)+Math.pow(dy,2));
                    double ratio = distance/((centers[i][2]+centers[j][2])/2);
                    listEyes.add(new Eyes(centers[i][2],centers[j][2],centers[i][0],centers[i][1],centers[j][0],centers[j][1],ratio));


                }


            }

        }
        listEyes.sort(new EyesComparator());

        double ideal_ratio = 10.3;


        for (Eyes eyes : listEyes){
            if(abs(eyes.ratio-ideal_ratio)<5){
                if(abs(eyes.y1-eyes.y2)<70 && eyes.x1<eyes.x2){

                    //System.out.println(eyes.ratio);
                    Point center1 = new Point(Math.round(eyes.x1), Math.round(eyes.y1));
                    int radius1 = (int)Math.round(eyes.rayon1);
                    Point center2 = new Point(Math.round(eyes.x2), Math.round(eyes.y2));
                    int radius2 = (int)Math.round(eyes.rayon2);

                    Imgproc.circle(image, center1, radius1, new Scalar(255,0,255), 3, 8, 0 );
                    Imgproc.circle(image, center2, radius2, new Scalar(255,0,255), 3, 8, 0 );

                    listEyesWinners.add(eyes);

                }

            }

        }

    }

    class Eyes {
        public double rayon1,rayon2,x1,y1,x2,y2;
        public double ratio;
        public Eyes(double rayon1,double rayon2,double x1,double y1, double x2, double y2, double ratio){
            this.ratio = ratio;
            this.rayon1 = rayon1;
            this.rayon2 = rayon2;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;


        }




    }
    public class EyesComparator implements Comparator<Eyes>{

        @Override
        public int compare(Eyes eyes, Eyes t1) {
            return (int) (eyes.ratio - t1.ratio);
        }
    }




}
