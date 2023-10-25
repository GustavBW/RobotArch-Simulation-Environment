package gbw.sdu.ra.gaussianblur.algorithm;

import gbw.sdu.ra.gaussianblur.ImageData;

import java.util.Arrays;

public class GuassianBlurManual {

    static class Task implements Runnable {
        private final int fromWidth, toWidth, height, kernelSize, width, destinationIndex;
        private final int[] imagePixels;
        private final int[][] destination;

        public Task(int fromWidth, int toWidth, int width, int height, int kernelSize, int[] imagePixels, int[][] destination, int destinationIndex){
            this.fromWidth = fromWidth;
            this.toWidth = toWidth;
            this.height = height;
            this.kernelSize = kernelSize;
            this.imagePixels = imagePixels;
            this.width = width;
            this.destination = destination;
            this.destinationIndex = destinationIndex;
        }
        public void run(){
            ImageData image = new ImageData(width, height, imagePixels);
            final int deltaWidth = toWidth - fromWidth;
            destination[destinationIndex] = new int[deltaWidth * height];
            for (int y = 0; y < height; y++) {
                for (int x = fromWidth; x < toWidth; x++) {
                    int[] argb = GaussianBlur.sampleAndBlur(x, y, kernelSize, image);
                    int alpha = argb[0];
                    int red = argb[1];
                    int green = argb[2];
                    int blue = argb[3];
                    int pixelIndex = y * deltaWidth + (x - fromWidth);
                    destination[destinationIndex][pixelIndex] = (alpha << 24) | (red << 16) | (green << 8) | blue;
                }
            }
        }
    }

    public static int[] run(ImageData image, int kernelSize, int numThreads) throws InterruptedException{
        Thread[] threads = new Thread[numThreads];
        Task[] tasks = new Task[numThreads];
        int[][] blurredSegments = new int[numThreads][];

        int remainder = image.width() % numThreads;
        int imageWidthPerThread = image.width() / numThreads;

        for(int i = 0; i < numThreads; i++){
            int fromWidth = imageWidthPerThread * i;
            int toWidth = imageWidthPerThread * (i + 1);
            if(i == numThreads - 1){ //if its the last one, add the last bit of width
                toWidth += remainder;
            }

            tasks[i] = new Task(
                        fromWidth,toWidth, image.width(), image.height(), kernelSize,
                        Arrays.copyOf(image.pixels(), image.pixels().length), //Extremely expensive
                        blurredSegments, i
            );
            threads[i] = new Thread(tasks[i]);
        }

        for(Thread t : threads){
            t.start();
        }
        for(Thread t : threads){
            t.join();
        }

        //combine segments
        int[] combinedImage = new int[image.width() * image.height()];
        for (int i = 0; i < numThreads; i++) {
            int fromWidth = tasks[i].fromWidth;
            int[] segment = blurredSegments[i];
            for (int y = 0; y < image.height(); y++) {
                int taskWidth = tasks[i].toWidth - tasks[i].fromWidth;
                System.arraycopy(segment, y * taskWidth, combinedImage, y * image.width() + fromWidth, taskWidth);
            }
        }

        return combinedImage;
    }

}
