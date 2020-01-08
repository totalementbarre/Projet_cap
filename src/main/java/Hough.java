import org.opencv.core.Mat;

public class Hough {

    private double x_pos;
    private double y_pos;
    private double seuil;
    private double pas_angle;
    Mat imageSrc;
    public Hough(double seuil, double pas_angle){
        x_pos =0;
        y_pos =0;
        this.seuil = seuil;
        this.pas_angle = pas_angle;

    }

    public double getX_pos() {
        return x_pos;
    }

    public double getY_pos() {
        return y_pos;
    }
    public void Barycentre(Mat in){
        int nb_lignes = in.rows();
        int nb_cols = in.cols();
        int size = nb_cols*nb_lignes;
        byte [] bytes = new byte[size];
        in.get(0,0, bytes); // all of them.
        double count =0;
        for (int x = 1; x < nb_cols; x++) {
            for (int y = 1; y < nb_lignes; y++) {
                byte pixel_at_x_y = bytes[ y * nb_cols + x ];
                //System.out.println(pixel_at_x_y);
                if( pixel_at_x_y ==0){
                    x_pos += x;
                    y_pos += y;
                    count++;
                }

            }

        }

        //System.out.println(count);
        x_pos = x_pos/count;
        y_pos = y_pos/count;
    }

    void Transformation(Mat n,Mat a, Mat out){
        int size =ImageProcessing.IMG_HEIGHT* ImageProcessing.IMG_WIDTH;
        int nb_lignes = n.rows();
        int nb_cols = n.cols();


        float [] norm_array = new float[size];
        float [] angles_array = new float[size];




        n.get(0,0, norm_array);
        a.get(0,0,angles_array);
        int x;
        int y;
        /*
        for (int x = 1; x < nb_cols; x++) {
            for (int y = 1; y < nb_lignes; y++) {
                byte pixel_at_x_y = n[ y * nb_cols + x ];
                if(pixel_at_x_y>seuil)
                    count++;

            }

        }
*/




    }
}
