package main;

import gui.*;
import gui.Color;
import gui.Container;
import gui.Image;
import gui.Rectangle;
import gui.layoutManagers.CoordinatesLayout;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Kyle on 12/17/2016.
 */
public class Main {
    public static final boolean DEMO = true;
    public static boolean advancedDraw = false;
    private static boolean moreGranularDraw = true;
    public static final int GRANULAR_NUM_COLORS = 1 + 14 + 1 + 14 + 1 + 14;

    public static final int INITIAL_ITERATIONS = 511;
    public static final int fps = 30;
    public static int MANDELBROT_ITERATIONS = INITIAL_ITERATIONS;

    public static final int WIDTH = 1920, HEIGHT = 1080;
    public static final double ASPECT = WIDTH / ((double)HEIGHT);
    public static int currentPowerOfTwo = 2;
    public static double currentHorizontalScale = 4;
    public static double currentVerticalScale = currentHorizontalScale / ASPECT;
    public static ComplexNumber currentCoordinateOfTopLeftPixel = new ComplexNumber(
            -2, currentVerticalScale / 2);

    private static ArrayList<ComplexNumber> topLeftCoordinatesAtEachLevel = new ArrayList<>();

    public static Rectangle background;
    private static BufferedImage backgroundImage;

    public static MouseListener mouseListener;

    private static ArrayList<Screenshot> screenshots = new ArrayList<>();

    public static Container complexGridOverlayContainer;
    public static Rectangle realAxis;
    public static Rectangle imaginaryAxis;

    public static Rectangle oneLine;
    public static Rectangle negativeOneLine;
    public static Rectangle iLine;
    public static Rectangle negativeILine;

    public static Rectangle one;
    public static Rectangle negativeOne;
    public static Rectangle i;
    public static Rectangle negativeI;

    public static boolean displayingAxes = true;
    public static final boolean AXES_GET_SWITCHED_OFF = false;

    public static void init() {
        if (DEMO) {
            MANDELBROT_ITERATIONS = 1;
        }

        backgroundImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);

        for (int row = 0; row < 1080; row++) {
            for (int column = 0; column < 1920; column++) {
                ComplexNumber thisPixelsComplexCoordinate = new ComplexNumber(currentCoordinateOfTopLeftPixel.getRealComponent() +
                        (column / ((double)WIDTH)) * currentHorizontalScale,
                        currentCoordinateOfTopLeftPixel.getImaginaryCoefficient() -
                                (row / ((double)HEIGHT)) * currentVerticalScale);
                int mandelbrotValue = thisPixelsComplexCoordinate.getMandelbrotValue();
                int r, g, b;
                if (mandelbrotValue == MANDELBROT_ITERATIONS) {
                    r = 0; g = 0; b = 0;
                } else {
                    if (mandelbrotValue < 256) {
                        r = 0; g = mandelbrotValue; b = 255 - mandelbrotValue;
                    } else {
                        r = mandelbrotValue - 255; g = 510 - mandelbrotValue; b = 0;
                    }
                }
                r = r << 16;
                g = g << 8;
                int rgb = 0xFF000000 + r + g + b;
                backgroundImage.setRGB(column, row, rgb);
            }
        }

        background = new Rectangle(new Image(backgroundImage, null, null));
        GUIMain.WINDOW.addLayer(background);

        mouseListener = new MouseListener() {
            @Override
            public void mousePressed() {
                if (DEMO) {
                    if (MANDELBROT_ITERATIONS < INITIAL_ITERATIONS) {
                        MANDELBROT_ITERATIONS++;
                        scaleChanged();
                        return;
                    }
                }
                currentPowerOfTwo--;
                int column = Input.getMouseX();
                int row = Input.getMouseY();
                double realComponentAtMouseLocation = currentCoordinateOfTopLeftPixel.getRealComponent() +
                        (column / ((double)WIDTH)) * currentHorizontalScale;
                double imaginaryCoefficientAtMouseLocation = currentCoordinateOfTopLeftPixel.getImaginaryCoefficient() -
                        (row / ((double)HEIGHT)) * currentVerticalScale;
                currentVerticalScale = currentVerticalScale / 2.0;
                currentHorizontalScale = currentHorizontalScale / 2.0;
                currentCoordinateOfTopLeftPixel.setRealComponent(realComponentAtMouseLocation
                        - (currentHorizontalScale / 2.0));
                currentCoordinateOfTopLeftPixel.setImaginaryCoefficient(imaginaryCoefficientAtMouseLocation
                        + (currentVerticalScale / 2.0));
                topLeftCoordinatesAtEachLevel.add(null);
                topLeftCoordinatesAtEachLevel.set(2 - currentPowerOfTwo, currentCoordinateOfTopLeftPixel.clone());
                scaleChanged();
                if (AXES_GET_SWITCHED_OFF) {
                    if (displayingAxes) {
                        displayingAxes = false;
                        GUIMain.WINDOW.removeLayer(complexGridOverlayContainer);
                    }
                } else {
                    adjustComplexGridOverlay();
                }
            }

            @Override
            public void mouseReleased() {

            }

            @Override
            public void mouseMoved() {

            }
        };
        Input.addMouseListener(mouseListener);

        topLeftCoordinatesAtEachLevel.add(0, currentCoordinateOfTopLeftPixel.clone());

        complexGridOverlayContainer = new Container(null, new CoordinatesLayout());
        GUIMain.WINDOW.addLayer(complexGridOverlayContainer);

        realAxis = new Rectangle(Color.adjustAColorsAlpha(Color.WHITE, .67f));
        imaginaryAxis = new Rectangle(Color.adjustAColorsAlpha(Color.WHITE, .67f));

        complexGridOverlayContainer.addComponent(realAxis, new CoordinatesLayout.Constraints(0, HEIGHT / 2, WIDTH, 1));
        complexGridOverlayContainer.addComponent(imaginaryAxis, new CoordinatesLayout.Constraints(WIDTH / 2, 0, 1, HEIGHT));

        oneLine = new Rectangle(Color.adjustAColorsAlpha(Color.WHITE, .33f));
        negativeOneLine = new Rectangle(Color.adjustAColorsAlpha(Color.WHITE, .33f));
        iLine = new Rectangle(Color.adjustAColorsAlpha(Color.WHITE, .33f));
        negativeILine = new Rectangle(Color.adjustAColorsAlpha(Color.WHITE, .33f));

        complexGridOverlayContainer.addComponent(oneLine, new CoordinatesLayout.Constraints(WIDTH * 3 / 4, 0, 1, HEIGHT));
        complexGridOverlayContainer.addComponent(negativeOneLine, new CoordinatesLayout.Constraints(WIDTH / 4, 0, 1, HEIGHT));
        complexGridOverlayContainer.addComponent(iLine, new CoordinatesLayout.Constraints(0, (HEIGHT / 2) - (WIDTH / 4), WIDTH, 1));
        complexGridOverlayContainer.addComponent(negativeILine, new CoordinatesLayout.Constraints(0, (HEIGHT / 2) + (WIDTH / 4), WIDTH, 1));

        int fontSize = 25;
        int fontOffset = 10;

        Font iFont = new Font("Verdana", Font.ITALIC, fontSize);
        Font numberFont = new Font("Verdana", Font.PLAIN, fontSize);

        one = new Rectangle(new Text("1", numberFont, Color.WHITE));
        negativeOne = new Rectangle(new Text("-1", numberFont, Color.WHITE));
        i = new Rectangle(new Text("i", iFont, Color.WHITE));
        negativeI = new Rectangle(new Text("-i", iFont, Color.WHITE));

        complexGridOverlayContainer.addComponent(one, new CoordinatesLayout.Constraints((WIDTH * 3 / 4) + fontOffset, (HEIGHT / 2) - (fontOffset + fontSize), 20, 18));
        complexGridOverlayContainer.addComponent(negativeOne, new CoordinatesLayout.Constraints((WIDTH / 4) + fontOffset, (HEIGHT / 2) - (fontOffset + fontSize), 20, 18));
        complexGridOverlayContainer.addComponent(i, new CoordinatesLayout.Constraints((WIDTH / 2) + fontOffset, (HEIGHT / 2) - (WIDTH / 4) - (fontOffset + fontSize), 20, 18));
        complexGridOverlayContainer.addComponent(negativeI, new CoordinatesLayout.Constraints((WIDTH / 2) + fontOffset, (HEIGHT / 2) + (WIDTH / 4) - (fontOffset + fontSize), 20, 18));
    }

    public static void checkEvents() {
        Input.checkInputs();
        if (Input.isSKeyDown()) {
            if (!Input.wasSKeyDown()) {
                screenshots.add(new Screenshot(backgroundImage, currentCoordinateOfTopLeftPixel, currentPowerOfTwo, MANDELBROT_ITERATIONS));
            }
        }
        if (Input.isOKeyDown()) {
            if (!Input.wasOKeyDown()) {
                currentPowerOfTwo++;
                currentCoordinateOfTopLeftPixel = topLeftCoordinatesAtEachLevel.get(2 - currentPowerOfTwo).clone();
                currentHorizontalScale = Math.pow(2, currentPowerOfTwo);
                currentVerticalScale = currentHorizontalScale / ASPECT;
                adjustComplexGridOverlay();
                scaleChanged();
            }
        }
        if (Input.isCKeyDown()) {
            if (!Input.wasCKeyDown()) {
                if (moreGranularDraw) {
                    moreGranularDraw = false;
                    scaleChanged();
                } else {
                    moreGranularDraw = true;
                    scaleChanged();
                }
            }
        }
        if (Input.isRightMouseButtonDown()) {
            if (!Input.wasRightMouseButtonDown()) {
                if (MANDELBROT_ITERATIONS == INITIAL_ITERATIONS) {
                    MANDELBROT_ITERATIONS = 10000;
                    scaleChanged();
                } else if (MANDELBROT_ITERATIONS > INITIAL_ITERATIONS){
                    if (advancedDraw) {
                        advancedDraw = false;
                        MANDELBROT_ITERATIONS = INITIAL_ITERATIONS;
                        scaleChanged();
                    } else {
                        advancedDraw = true;
                        scaleChanged();
                    }
                } else if (MANDELBROT_ITERATIONS < 50) {
                    MANDELBROT_ITERATIONS = 50;
                    scaleChanged();
                } else if (MANDELBROT_ITERATIONS < INITIAL_ITERATIONS) {
                    MANDELBROT_ITERATIONS = INITIAL_ITERATIONS;
                    scaleChanged();
                }
            }
        }
    }

    public static void render() {
        GUIMain.WINDOW.draw();
    }

    public static void scaleChanged() {
        if (moreGranularDraw) {
            moreGranularScaleChanged();
            return;
        }
        if (advancedDraw) {
            advancedScaleChanged();
            return;
        }
        backgroundImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < 1080; row++) {
            for (int column = 0; column < 1920; column++) {
                ComplexNumber thisPixelsComplexCoordinate = new ComplexNumber(currentCoordinateOfTopLeftPixel.getRealComponent() +
                        (column / ((double)WIDTH)) * currentHorizontalScale,
                        currentCoordinateOfTopLeftPixel.getImaginaryCoefficient() -
                                (row / ((double)HEIGHT)) * currentVerticalScale);
                int mandelbrotValue = thisPixelsComplexCoordinate.getMandelbrotValue();
                int r, g, b;
                if (mandelbrotValue == MANDELBROT_ITERATIONS) {
                    r = 0; g = 0; b = 0;
                } else {
                    if (mandelbrotValue < 256) {
                        r = 0; g = mandelbrotValue; b = 255 - mandelbrotValue;
                    } else if (mandelbrotValue < 511) {
                        r = mandelbrotValue - 255; g = 510 - mandelbrotValue; b = 0;
                    } else {
                        r = 255; g = 0; b = 0;
                    }
                }
                r = r << 16;
                g = g << 8;
                int rgb = 0xFF000000 + r + g + b;
                backgroundImage.setRGB(column, row, rgb);
            }
        }
        ((Image)background.getStyle()).refreshTexture(backgroundImage);
    }

    public static void advancedScaleChanged() {
        for (int row = 0; row < 1080; row++) {
            for (int column = 0; column < 1920; column++) {
                ComplexNumber thisPixelsComplexCoordinate = new ComplexNumber(currentCoordinateOfTopLeftPixel.getRealComponent() +
                        (column / ((double)WIDTH)) * currentHorizontalScale,
                        currentCoordinateOfTopLeftPixel.getImaginaryCoefficient() -
                                (row / ((double)HEIGHT)) * currentVerticalScale);
                int mandelbrotValue = thisPixelsComplexCoordinate.getMandelbrotValue();
                int r, g, b;
                if (mandelbrotValue == MANDELBROT_ITERATIONS) {
                    r = 0; g = 0; b = 0;
                } else {
                    if (mandelbrotValue < 256) {
                        r = 0; g = mandelbrotValue; b = 255 - mandelbrotValue;
                    } else if (mandelbrotValue < 511) {
                        r = mandelbrotValue - 255; g = 510 - mandelbrotValue; b = 0;
                    } else {
                        mandelbrotValue = mandelbrotValue - 511;
                        mandelbrotValue = mandelbrotValue % 765;
                        if (mandelbrotValue <= 255) {
                            r = 255 - mandelbrotValue; g = 0; b = mandelbrotValue;
                        } else if (mandelbrotValue <= 510) {
                            r = 0; g = mandelbrotValue - 255; b = 510 - mandelbrotValue;
                        } else {
                            r = mandelbrotValue - 510; g = 765 - mandelbrotValue; b = 0;
                        }
                    }
                }
                r = r << 16;
                g = g << 8;
                int rgb = 0xFF000000 + r + g + b;
                backgroundImage.setRGB(column, row, rgb);
            }
        }
        ((Image)background.getStyle()).refreshTexture(backgroundImage);
    }

    public static void moreGranularScaleChanged() {
        for (int row = 0; row < 1080; row++) {
            for (int column = 0; column < 1920; column++) {
                ComplexNumber thisPixelsComplexCoordinate = new ComplexNumber(currentCoordinateOfTopLeftPixel.getRealComponent() +
                        (column / ((double)WIDTH)) * currentHorizontalScale,
                        currentCoordinateOfTopLeftPixel.getImaginaryCoefficient() -
                                (row / ((double)HEIGHT)) * currentVerticalScale);
                int mandelbrotValue = thisPixelsComplexCoordinate.getMandelbrotValue();
                int r, g, b;
                if (mandelbrotValue == MANDELBROT_ITERATIONS) {
                    r = 0; g = 0; b = 0;
                } else if (mandelbrotValue == 0) {
                    r = 0; g = 0; b = 255;
                } else {
                    mandelbrotValue = (mandelbrotValue + 3) % GRANULAR_NUM_COLORS;
                    if (mandelbrotValue < 15) {
                        r = 0;
                        g = mandelbrotValue * 17;
                        b = (15 - mandelbrotValue) * 17;
                    } else if (mandelbrotValue < 30) {
                        mandelbrotValue = mandelbrotValue - 15;
                        b = 0;
                        r = mandelbrotValue * 17;
                        g = (15 - mandelbrotValue) * 17;
                    } else {
                        mandelbrotValue = mandelbrotValue - 30;
                        g = 0;
                        b = mandelbrotValue * 17;
                        r = (15 - mandelbrotValue) * 17;
                    }
                }
                r = r << 16;
                g = g << 8;
                int rgb = 0xFF000000 + r + g + b;
                backgroundImage.setRGB(column, row, rgb);
            }
        }
        ((Image)background.getStyle()).refreshTexture(backgroundImage);
    }

    public static void exiting() {
        long time = System.currentTimeMillis();
        int numShots = screenshots.size();
        for (int i = 0; i < numShots; i++) {
            screenshots.get(i).drawToFile(i, time);
        }
    }

    public static void adjustComplexGridOverlay() {
        // TODO
        complexGridOverlayContainer.empty();
        double leftX = currentCoordinateOfTopLeftPixel.getRealComponent();
        double topY = currentCoordinateOfTopLeftPixel.getImaginaryCoefficient();
        double rightX = leftX + currentHorizontalScale;
        double bottomY = topY - currentVerticalScale;
        if (leftX < 0 && rightX > 0) {
            double newX = ((-1 * leftX) / (currentHorizontalScale)) * WIDTH;
            complexGridOverlayContainer.addComponent(imaginaryAxis, new CoordinatesLayout.Constraints((int) newX, 0, 1, HEIGHT));
        }
        if (leftX < -1 && rightX > -1) {
            double newX = ((-1 - leftX) / (currentHorizontalScale)) * WIDTH;
            complexGridOverlayContainer.addComponent(negativeOneLine, new CoordinatesLayout.Constraints((int) newX, 0, 1, HEIGHT));
        }
        if (leftX < 1 && rightX > 1) {
            double newX = ((-1 * (leftX - 1)) / (currentHorizontalScale)) * WIDTH;
            complexGridOverlayContainer.addComponent(oneLine, new CoordinatesLayout.Constraints((int) newX, 0, 1, HEIGHT));
        }
        if (topY > 0 && bottomY < 0) {
            double newY = ((topY) / (currentVerticalScale)) * HEIGHT;
            complexGridOverlayContainer.addComponent(realAxis, new CoordinatesLayout.Constraints(0, (int) newY, WIDTH, 1));
        }
        if (topY > 1 && bottomY < 1) {
            double newY = ((topY - 1) / (currentVerticalScale)) * HEIGHT;
            complexGridOverlayContainer.addComponent(iLine, new CoordinatesLayout.Constraints(0, (int) newY, WIDTH, 1));
        }
        if (topY > -1 && bottomY < -1) {
            double newY = ((topY + 1) / (currentVerticalScale)) * HEIGHT;
            complexGridOverlayContainer.addComponent(negativeILine, new CoordinatesLayout.Constraints(0, (int) newY, WIDTH, 1));
        }
    }

    public static void setCurrentPowerOfTwo(int currentPowerOfTwo) {
        Main.currentPowerOfTwo = currentPowerOfTwo;
    }

    public static int getCurrentPowerOfTwo() {
        return currentPowerOfTwo;
    }
}
