import java.util.ArrayList;
import java.util.List;

public class HoughOpti {
    private double col_centre;
    private double ligne_centre;
    final private double seuil;
    final private double pas_angle;

    public HoughOpti(double seuil, double pas_angle) {
        col_centre = 0;
        ligne_centre = 0;
        this.seuil = seuil;
        this.pas_angle = pas_angle;

    }

    public void Barycentre(float[][] in) {
        float count = 0;

        for (int c = 0; c < ImageProcessingOpti.IMG_WIDTH_REDUCED; c++) {
            for (int l = 0; l < ImageProcessingOpti.IMG_HEIGHT_REDUCED; l++) {
                //System.out.println(pixel_at_x_y);
                if (in[c][l] == 0) {
                    col_centre += c;
                    ligne_centre += l;
                    count++;
                }

            }

        }
        col_centre = col_centre / count;
        ligne_centre = ligne_centre / count;

    }


    public void PatternCreation(float[][] norm, float[][] angle,ArrayList<HoughOpti.Beta>[] beta) {
        //float beta[][];
        //beta = new float[(int)(360/pas_angle)][2];


        int count =0;
        for (int c = 0; c < ImageProcessingOpti.IMG_WIDTH_REDUCED; c++) {
            for (int l = 0; l < ImageProcessingOpti.IMG_HEIGHT_REDUCED; l++) {
                if (norm[c][l] > 10) {
                    int indice = (int) (angle[c][l]) + 180;
                    if (indice >= 0 && indice < 360 / pas_angle) {
                        float dc = (float) (c - col_centre);
                        float dl = (float) (l - ligne_centre);
                        float alpha = (float) (Math.atan2(dl, dc) * 180 / 3.14 + 180);
                        ///beta[indice][0] = dc;
                        //beta[indice][1] = dl;
                        //System.out.println(indice);

                        beta[indice].add(new Beta(alpha, dc, dl));
                        count++;

                    }
                }

            }

        }
    System.out.println("Pattern creation nb elements :"+count);

    }

    public int getSizeArrayList() {
        return (int) (360 / pas_angle);
    }


    public double getCol_centre() {
        return col_centre;
    }

    public double getLigne_centre() {
        return ligne_centre;
    }

    public void houghVote(float[][] norm, float[][] angle,ArrayList<HoughOpti.Beta>[] beta, float[][] out){
        for (int c = 0; c < ImageProcessingOpti.IMG_WIDTH_REDUCED; c++) {
            for (int l = 0; l < ImageProcessingOpti.IMG_HEIGHT_REDUCED; l++) {
                out[c][l] =0;
            }
        }


        for (int c = 0; c < ImageProcessingOpti.IMG_WIDTH_REDUCED; c++) {
            for (int l = 0; l < ImageProcessingOpti.IMG_HEIGHT_REDUCED; l++) {
                if (norm[c][l] > 100) {
                    int indice = (int) (angle[c][l]) + 180;
                    if (indice >= 0 && indice < 360 / pas_angle) {
                        for (HoughOpti.Beta b : beta[indice]){
                            int dc = (int) (c - b.dist_c);
                            int dl = (int) (l - b.dist_l);
                            if(dc>=0 && dc<ImageProcessingOpti.IMG_WIDTH_REDUCED && dl>=0 &&dl<ImageProcessingOpti.IMG_HEIGHT_REDUCED){
                                out[dc][dl]++;

                            }



                        }





                    }
                }

            }

        }

    }

    public class Beta {
        public double alpha = 0;
        public double dist_c = 0;
        public double dist_l = 0;

        public Beta(double alpha, double dist_c, double dist_l) {
            this.alpha = alpha;
            this.dist_c = dist_c;
            this.dist_l = dist_l;
        }

        public void affichage() {
            System.out.println("a:" + alpha + "\tdc:" + dist_c + "\tdl:" + dist_l);
        }
    }
}
