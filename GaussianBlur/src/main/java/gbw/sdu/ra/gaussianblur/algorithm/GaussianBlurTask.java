package gbw.sdu.ra.gaussianblur.algorithm;

import java.util.concurrent.RecursiveTask;

public class GaussianBlurTask extends RecursiveTask<int[]> {
    private int[] pixels;
    private int width;
    private int height;
    private int kernelSize;

    public GaussianBlurTask(int[] pixels, int width, int height, int kernelSize) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.kernelSize = kernelSize;
    }

    @Override
    protected int[] compute() {
        int[] blurredPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] argb = sampleAndBlur(x, y);
                int alpha = argb[0];
                int red = argb[1];
                int green = argb[2];
                int blue = argb[3];
                int index = y * width + x;
                blurredPixels[index] = (alpha << 24) | (red << 16) | (green << 8) | blue;
            }
        }

        return blurredPixels;
    }

    private int[] sampleAndBlur(int x, int y) {
        int totalAlpha = 0;
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;

        for (int dy = -kernelSize; dy <= kernelSize; dy++) {
            for (int dx = -kernelSize; dx <= kernelSize; dx++) {
                int sampleX = x + dx;
                int sampleY = y + dy;

                if (sampleX >= 0 && sampleX < width && sampleY >= 0 && sampleY < height) {
                    int index = sampleY * width + sampleX;
                    int[] sampleChannels = extractChannelsAsARGB(pixels[index]);

                    // Calculate the distance from the current sample pixel to the center pixel.
                    int distance = (int) Math.sqrt(dx * dx + dy * dy);

                    // You can apply the Gaussian kernel weight based on the distance.
                    double weight = calculateGaussianWeight(distance);

                    totalAlpha += (int) (sampleChannels[0] * weight);
                    totalRed += (int) (sampleChannels[1] * weight);
                    totalGreen += (int) (sampleChannels[2] * weight);
                    totalBlue += (int) (sampleChannels[3] * weight);
                }
            }
        }

        // Calculate the average value for each channel (you can also apply the Gaussian kernel weight here)
        int numSamples = (2 * kernelSize + 1) * (2 * kernelSize + 1);
        int alpha = totalAlpha / numSamples;
        int red = totalRed / numSamples;
        int green = totalGreen / numSamples;
        int blue = totalBlue / numSamples;

        return new int[]{alpha, red, green, blue};
    }

    private int[] extractChannelsAsARGB(int pixel) {
        return new int[]{
                (pixel >> 24) & 0xFF, // Alpha
                (pixel >> 16) & 0xFF, // Red
                (pixel >> 8) & 0xFF,  // Green
                pixel & 0xFF          // Blue
        };
    }

    private double calculateGaussianWeight(int distance) {
        // You can use the Gaussian function to calculate the weight based on the distance.
        // The standard deviation (sigma) controls the spread of the kernel.
        double sigma = kernelSize / 2.0; // You can adjust this based on your requirements.
        return Math.exp(-distance * distance / (2 * sigma * sigma));
    }
}
