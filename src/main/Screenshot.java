package main;

import gui.Color;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;

/**
 * Created by Kyle on 6/4/2017.
 */
public class Screenshot {
    private ComplexNumber coordinateOfTopLeftPixel;
    private int zoomFactor;
    private int iterations;
    private BufferedImage theImage;

    public Screenshot(BufferedImage theImage, ComplexNumber coordinateOfTopLeftPixel, int zoomFactor, int iterations) {
        this.theImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < 1080; row++) {
            for (int column = 0; column < 1920; column++) {
                this.theImage.setRGB(column, row, theImage.getRGB(column, row));
            }
        }
        this.coordinateOfTopLeftPixel = new ComplexNumber(coordinateOfTopLeftPixel.getRealComponent(), coordinateOfTopLeftPixel.getImaginaryCoefficient());
        this.zoomFactor = zoomFactor;
        this.iterations = iterations;
    }

    public void drawToFile(int name, long time) {
        System.out.println();
        System.out.println("Screenshot " + time + "__" + name + ":");
        System.out.println("Top Left Coordinates: " + coordinateOfTopLeftPixel.getRealComponent() + " + " + coordinateOfTopLeftPixel.getImaginaryCoefficient() + "i");
        System.out.println("Zoom (width): 2^" + zoomFactor);
        System.out.println("# of iterations: " + iterations);


        try {
            File file = new File("screenshots/" + time + "__" + name + ".png");
            file.createNewFile();
            boolean b = ImageIO.write(theImage, "png", file);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
