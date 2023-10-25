package gbw.sdu.ra.gaussianblur.algorithm;

import gbw.sdu.ra.gaussianblur.ImageData;

public class GaussianBlur {


    /**
     *
     * @param x
     * @param y
     * @param kernelSize
     * @return
     */
    public static int[] sampleAndBlur(int x, int y, int kernelSize, ImageData image) {
        int totalAlpha = 0;
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;

        for (int dy = -kernelSize; dy <= kernelSize; dy++) {
            for (int dx = -kernelSize; dx <= kernelSize; dx++) {
                int sampleX = x + dx;
                int sampleY = y + dy;

                if (sampleX >= 0 && sampleX < image.width() && sampleY >= 0 && sampleY < image.height()) {
                    int index = sampleY * image.width() + sampleX;
                    int[] sampleChannels = extractChannelsAsARGB(image.pixels()[index]);

                    // Calculate the distance from the current sample pixel to the center pixel.
                    int distance = (int) Math.sqrt(dx * dx + dy * dy);

                    // You can apply the Gaussian kernel weight based on the distance.
                    double weight = calculateGaussianWeight(distance, kernelSize);

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

    public static int[] extractChannelsAsARGB(int pixel) {
        return new int[]{
                (pixel >> 24) & 0xFF, // Alpha
                (pixel >> 16) & 0xFF, // Red
                (pixel >> 8) & 0xFF,  // Green
                pixel & 0xFF          // Blue
        };
    }

    public static double calculateGaussianWeight(int distance, int kernelSize) {
        // You can use the Gaussian function to calculate the weight based on the distance.
        // The standard deviation (sigma) controls the spread of the kernel.
        double sigma = kernelSize / 2.0; // You can adjust this based on your requirements.
        return Math.exp(-distance * distance / (2 * sigma * sigma));
    }
}
