import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.RecursiveAction;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import javax.imageio.ImageIO;
//Anele Danisa
//MedianFilterParallel

/**
 * Handles the input generates the input image pixels and gives the output
 */
public class MedianFilterParallel {

    /**
     * main method, handles the input and output process
     * @param args input arguments
     * @throws IOException file not found exception
     */
    public static void main(String[] args) throws IOException {
        String filename = args[0];
        String outputfile = args[1];
        int window = Integer.parseInt(args[2]);
        BufferedImage image = ImageIO.read(new File(filename));
        int width = image.getWidth();
        int height = image.getHeight();
        ArrayList<int[]> RGB_windows = new ArrayList();


        if (window >= 3 && window % 2 != 0) {
            //loop throughout the image with the step of a window to give the getRGB() method the correct start point
            for (int y = 0; y < height; y += window) {//looping through the height of the image
                for (int x = 0; x < width; x += window) { //looping through the width of the image
                    if ((x == 0 || x % window == 0) && window - (width - x) <= 0 && window - (height - y) <= 0) {//condition for the ignoring the pixels at the edge of the image
                        int[] window_list = image.getRGB(x, y, window, window, (int[]) null, 0, window);//returns the array of a window with pixels inside
                        RGB_windows.add(window_list);//adding all the windows to the arraylist
                    }
                }
            }
        }
        else {
            System.out.println("Window size not correct try an odd and greater than 2 window size");
        }


        //retrieving the destination pixels from the Filter class
        long start1 = System.currentTimeMillis();
       // long start = System.currentTimeMillis();
        ArrayList<int[]> dest_windows = new ArrayList(); //creating an array that with receive the destination/filtered pixels

        
        long start = System.currentTimeMillis();  //start timing the program
        for (int i = 0; i < RGB_windows.size(); ++i) {
            MedianFilterP parallel_median = new MedianFilterP(RGB_windows.get(i), 0, ((int[]) RGB_windows.get(i)).length);
            ForkJoinPool.commonPool().invoke(parallel_median);//invoking the forkjoinpool
            dest_windows.add(parallel_median.getDest());
        }
        //System.out.println(System.currentTimeMillis() - start);//end timing the program

        Image_Controller image_handler = new Image_Controller(outputfile, RGB_windows.size(), width, height, image, window);
        image_handler.Write_image(dest_windows); //writing the results to the output image
    }
}

/**
 * Generate the pool of threads that do the task recursively and return the filtered results
 */
 class MedianFilterP extends RecursiveAction {
    public int[] RGB;
    public int low;
    public int high;
    int cutoff = 5;
    int RGB_size;
    public int[] dest;
    public ArrayList<Integer> sub_array1;
    public ArrayList<Integer> sub_array2;
    public ArrayList<Integer> sub_array3;

     /**
      *Creates an object that filters an array
      * @param RGB stores the pixels of a window
      * @param low thread start point on the array
      * @param high thread end point on the array
      * @Title: MedianFilterP
      */
    public MedianFilterP(int[] RGB, int low, int high) {
        this.RGB = RGB;
        this.low = low;
        this.high = high;
        this.RGB_size = RGB.length;
        this.dest = new int[this.RGB_size];
        this.sub_array1 = new ArrayList();
        this.sub_array2 = new ArrayList();
        this.sub_array3 = new ArrayList();
    }

     /**
      *Generating the threads that will handle the filtering work
      */
    protected void compute() {

        if (this.high - low < cutoff && high != 0) {//once done cutting the work to chunks start doing the work
            //sorting out the chunk of pixels in a window
            for(int i = low; i < high; ++i) {
                this.sub_array1.add(RGB[i] >> 16 & 255);
                this.sub_array2.add(RGB[i] >> 8 & 255);
                this.sub_array3.add(RGB[i] & 255);
            }

            Collections.sort(sub_array1);
            Collections.sort(sub_array2);
            Collections.sort(sub_array3);
        } else {
            //cutting to chunk's of pixels
            MedianFilterP left = new MedianFilterP(RGB, low, (low + high) / 2);
            MedianFilterP right = new MedianFilterP(RGB, (low + high) / 2, high);
            //java forkjoin framework application
            left.fork();
            right.compute();
            left.join();
            //joining all the results together after calculations
            sub_array1 = addlist(left.sub_array1, right.sub_array1);
            sub_array2 = addlist(left.sub_array2, right.sub_array2);
            sub_array3 = addlist(left.sub_array3, right.sub_array3);
        }

    }
     /**
      * join two ArrayLists
      * @param sub1 sub array 1
      * @param sub2 sub array 2
      * @return joined array
      */
    public ArrayList<Integer> addlist(ArrayList<Integer> sub1, ArrayList<Integer> sub2) {
        sub1.addAll(sub2);
        Collections.sort(sub1);
        return sub1;
    }

     /**
      * Generates the filtered destination pixels
      * @return The Array with filtered pixels of a window
      */
    public int[] getDest() {
        //get the median for each
        int median_red = (Integer)sub_array1.get((sub_array1.size() + 1) / 2);
        int median_green = (Integer)sub_array2.get((sub_array2.size() + 1) / 2);
        int median_blue = (Integer)sub_array3.get((sub_array3.size() + 1) / 2);

        for(int i = 0; i < RGB.length; ++i) {
            int dpixel = -16777216 | median_red << 16 | median_green << 8 | median_blue;
            dest[i] = dpixel;
        }
        return dest;
    }
}

/**
 * Get the destination pixels and them to the image
 */
class Image_Controller {
    public ArrayList<Integer> pixels;
    String image_name;
    int rgbsize;
    int width;
    BufferedImage image;
    int height;
    int window;

    /**
     *Creates an Image controller object that will handle the process of writing the pixels to the new image
     * @param image input image
     * @param RGB_size size of the array
     * @param w width of the image
     * @param h height of the image
     * @param ima BufferedImage object
     * @param window window size
     */
    public Image_Controller(String image, int RGB_size, int w, int h, BufferedImage ima, int window) {
        this.image_name = image;
        this.rgbsize = RGB_size;
        this.pixels = new ArrayList();
        this.width = w;
        this.image = ima;
        this.height = h;
        this.window = window;
    }


    /**
     * Goes through the picture and allocate new filtered pixels
     * @param windows Array list of windows
     * @throws IOException exception error if file name is not found
     */
    public void Write_image(ArrayList<int[]> windows) throws IOException {
        File f = new File(image_name);
        int point = 0;
        for(int y = 0; y < height; y += window) {
            for(int x = 0; x < width; x += window) {
                if ((x == 1 || x % window == 0) && window - (width - x) <= 0 && window - (height - y) <= 0) {
                    image.setRGB(x, y, window, window, (int[])windows.get(point), 0, window);//setting back the windows to an image
                    ++point;
                }
            }
        }
        ImageIO.write(image, "jpeg", f);
    }
}