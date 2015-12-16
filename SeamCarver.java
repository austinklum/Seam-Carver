/**
 * SeamCarver.java
 * Frederik Roenn Stensaeth
 * Date: 12.11.15
 *
 * Java program to perform content-aware image resizing using seam carving.
 **/

import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

/**
 * SeamCarver() xx
 *
 * @param xx
 * @return xx
 */
public class SeamCarver {
	public static void main(String[] args) {
		boolean showImages = false;
		String imageFilePath = null;
		String outputImageFilePath = null;
		Integer num  = null;
		String direction = null;
		String outputFormatName = ""; // e.g. "png" or "jpg"
		for (String arg: args) {
			if (arg.equals("--show")) {
				showImages = true;
			} else if (imageFilePath == null) {
				imageFilePath = arg;
			} else if (outputImageFilePath == null) {
				outputImageFilePath = arg;
				int index = outputImageFilePath.lastIndexOf('.');
                if (index >= 0) {
                    outputFormatName = outputImageFilePath.substring(index + 1);
                }
			} else if (num == null) {
				num = Integer.parseInt(arg);
			} else if (direction == null) {
				direction = arg;
			}
		}

		if (imageFilePath == null && outputImageFilePath == null) {
            System.err.println("Usage: java SeamCarver inputImage outputImage numOfSeams direction [--show]");
            return;
        } else if (!direction.equals("vertical") && !direction.equals("horizontal")) {
        	System.err.println("Usage: java SeamCarver inputImage outputImage numOfSeams direction [--show]");
            System.err.println("Direction needs to be either 'horizontal' or 'vertical'.");
            return;
        } else if (num <= 0) {
        	System.err.println("Usage: java SeamCarver inputImage outputImage numOfSeams direction [--show]");
        	System.err.println("numOfSeams needs to be a positive integer.");
        	return;
        }

        // Open the input image
        BufferedImage image;
        try {
            image = ImageIO.read(new File(imageFilePath));
        } catch(IOException e) {
            System.err.println("Can't open " + imageFilePath);
            return;
        }

        // Get the new image w/o one seam.
        BufferedImage newImage = image;
        while (num > 0) {
        	System.out.println(num);

        	newImage = carveSeam(newImage, direction);

        	num = num - 1;
        }

        // Create the new image file.
        try {
            File outputfile = new File(outputImageFilePath);
            ImageIO.write(newImage, outputFormatName, outputfile);
        } catch (IOException e) {
            System.err.println("Trouble saving " + outputImageFilePath);
            return;
        }

        // Show the before and after images
        if (showImages) {
            showImage(image);
            showImage(newImage);
        }
	}

	private static BufferedImage carveSeam(BufferedImage image, String direction) {
		// We need to:
		// computeEnergy
		// findSeam
		// removeSeam aka construct new image w/o the seam.

		BufferedImage newImage = null;
		double[][] energyTable = computeEnergy(image);
		// System.out.println("ENERGY TABLE:");
		// System.out.println(energyTable);
		// for(double[] rowData: energyTable) {
		// 	System.out.println(rowData);
		// }
		int[][] seam = findSeam(energyTable, direction);
		// // System.out.println("SEAM:");
		// // System.out.println(seam);
		// for(int[] rowData: seam) {
		// 	System.out.println(rowData);
		// }
		newImage = removeSeam(image, seam, direction);

		return newImage;
	}

	private static double[][] computeEnergy(BufferedImage image) {
		int width = image.getWidth();
        int height = image.getHeight();
		double[][] energyTable = new double[width][height];

		// Loop over every pixel in the image and compute its energy.
		for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = image.getRGB(x, y);
                int alpha = (color & 0xff000000) >> 24;
                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = (color & 0x000000ff);
                int gray = (red + green + blue) / 3;

                // compute the energy and store it in the table.
                // make into fraction.
                energyTable[x][y] = gray / 255; // redo this later with the actual method.
            }
        }

		return energyTable;
	}

	private static int[][] findSeam(double[][] energyTable, String direction) {
		// Come back and complete this, after removeSeam --> more interesting...
		int[][] seam;
		int width = energyTable.length; // might have these two opposite
		int height = energyTable[0].length; // might have these two opposite.
		// seamDynamic is the table we will use for dynamic programming.
		double[][] seamDynamic = new double[width][height];
		int[][] backtracker = new int[width][height];
		double minimum;
		if (direction.equals("vertical")) {
			// vertical seam.
			seam = new int[energyTable[0].length][2];

			// Loops over the energy table and finds the lowest energy path.
			for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	            	if (y == 0) {
	            		seamDynamic[x][y] = energyTable[x][y];
	            		backtracker[x][y] = -1;
	            	} else {
	            		// every other row.
	            		// need to special case the sides.
	            		if (x == 0) {
	            			minimum = Math.min(seamDynamic[x][y - 1], seamDynamic[x + 1][y - 1]);
	            			// seamDynamic[x][y] = energyTable[x][y] + minimum;

	            			if (minimum == seamDynamic[x][y - 1]) {
	            				// add backtracker.
	            				backtracker[x][y] = 1;
	            			} else { // x + 1
	            				// add backtracker.
	            				backtracker[x][y] = 2;
	            			}
	            		} else if (x == (width - 1)) {
	       					minimum = Math.min(seamDynamic[x][y - 1], seamDynamic[x - 1][y - 1]);
	            			// seamDynamic[x][y] = energyTable[x][y] + minimum;

	            			if (minimum == seamDynamic[x][y - 1]) {
	            				// add backtracker.
	            				backtracker[x][y] = 1;
	            			} else { // x - 1
	            				// add backtracker.
	            				backtracker[x][y] = 0;
	            			}
	            		} else {
	            			minimum = Math.min(seamDynamic[x - 1][y - 1], Math.min(seamDynamic[x][y - 1], seamDynamic[x + 1][y - 1]));
	            			// seamDynamic[x][y] = energyTable[x][y] + minimum;

	            			if (minimum == seamDynamic[x - 1][y - 1]) {
	            				// add backtracker.
	            				backtracker[x][y] = 0;
	            			} else if (minimum == seamDynamic[x][y - 1]) {
	            				// add backtracker.
	            				backtracker[x][y] = 1;
	            			} else { // x + 1
	            				// add backtracker.
	            				backtracker[x][y] = 2;
	            			}
	            		}
	            		seamDynamic[x][y] = energyTable[x][y] + minimum;
	            	}
	            }
	        }

	        // now that we have computed the paths, we need to backtrace the minimum one.
	        // 0 --> x - 1.
	        // 1 --> x.
	        // 2 --> x + 1.
	        // code: loop until null.
	        // first we need to find the min at the end.
	        double min_num = seamDynamic[0][height - 1];
	        int min_index = 0;
            for (int x = 0; x < width; x++) {
            	if (min_index > seamDynamic[x][height - 1]) {
            		min_index = x;
            		min_num = seamDynamic[x][height - 1];
            	}
            }

            // now that we have the min we need to backtrace it.
            int y_index = height - 1;
            int x_index = min_index;
            seam[x_index][0] = x_index;
            seam[x_index][1] = y_index;
            int backtrack;
            while (y_index > 0) {
            	backtrack = backtracker[x_index][y_index];

            	if (backtrack == 0) {
            		x_index = x_index - 1;
            	} else if (backtrack == 1) {
            		x_index = x_index;
            	} else { // = 2
            		x_index = x_index + 1;
            	}
            	y_index = y_index - 1;

            	seam[x_index][0] = x_index;
	            seam[x_index][1] = y_index;
            }
		} else {
			// horizontal seam.
			seam = new int[energyTable.length][2];

			// Loops over the energy table and finds the lowest energy path.
			for (int x = 0; x < width; x++) {
	            for (int y = 0; y < height; y++) {
	            	if (x == 0) {
	            		seamDynamic[x][y] = energyTable[x][y];
	            		backtracker[x][y] = -1;
	            	} else {
	            		// every other column.
	            		// need to special case the top/bottom.
	            		if (y == 0) {
	            			minimum = Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y + 1]);
	            			// seamDynamic[x][y] = energyTable[x][y] + minimum;

	            			if (minimum == seamDynamic[x - 1][y]) {
	            				// add backtracker.
	            				backtracker[x][y] = 1;
	            			} else { // y + 1
	            				// add backtracker.
	            				backtracker[x][y] = 2;
	            			}
	            		} else if (y == (height - 1)) {
	            			minimum = Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y - 1]);
	            			// seamDynamic[x][y] = energyTable[x][y] + minimum;

	            			if (minimum == seamDynamic[x - 1][y]) {
	            				// add backtracker.
	            				backtracker[x][y] = 1;
	            			} else { // y - 1
	            				// add backtracker.
	            				backtracker[x][y] = 0;
	            			}
	            		} else {
	            			minimum = Math.min(seamDynamic[x - 1][y - 1], Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y + 1]));
	            			// seamDynamic[x][y] = energyTable[x][y] + minimum;

	            			if (minimum == seamDynamic[x - 1][y - 1]) {
	            				// add backtracker.
	            				backtracker[x][y] = 0;
	            			} else if (minimum == seamDynamic[x - 1][y]) {
	            				// add backtracker.
	            				backtracker[x][y] = 1;
	            			} else { // y + 1
	            				// add backtracker.
	            				backtracker[x][y] = 2;
	            			}
	            		}
	            		seamDynamic[x][y] = energyTable[x][y] + minimum;
	            	}
	            }
	        }

	        // now that we have computed the paths, we need to backtrace the minimum one.
	        // 0 --> y - 1.
	        // 1 --> y.
	        // 2 --> y + 1.
	        // code: loop until null.
	        // first we need to find the min at the end.
	        double min_num = seamDynamic[width - 1][0];
	        int min_index = 0;
            for (int y = 0; y < height; y++) {
            	if (min_index > seamDynamic[width - 1][y]) {
            		min_index = y;
            		min_num = seamDynamic[width - 1][y];
            	}
            }

            // now that we have the min we need to backtrace it.
            int y_index = height - 1;
            int x_index = min_index;
            seam[x_index][0] = x_index;
            seam[x_index][1] = y_index;
            int backtrack;
            while (y_index > 0) {
            	backtrack = backtracker[x_index][y_index];

            	if (backtrack == 0) {
            		y_index = y_index - 1;
            	} else if (backtrack == 1) {
            		y_index = y_index;
            	} else { // = 2
            		y_index = y_index + 1;
            	}
            	x_index = x_index - 1;

            	seam[x_index][0] = x_index;
	            seam[x_index][1] = y_index;
            }
		}

		return seam;
	}

	private static BufferedImage removeSeam(BufferedImage image, int[][] seam, String direction) {
		BufferedImage newImage;
		int width = image.getWidth();
        int height = image.getHeight();
		if (direction.equals("vertical")) {
			// vertical seam.
			newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_ARGB);
		} else {
			// horizontal seam.
			newImage = new BufferedImage(width, height - 1, BufferedImage.TYPE_INT_ARGB);
		}

		// Loops over ever pixel in the original image and copies them over.
		// Do not copy over the pixels in the seam.
		if (direction.equals("vertical")) {
			// vertical seam.
			for (int y = 0; y < height; y++) {
				boolean shift = false;
	            for (int x = 0; x < width - 1; x++) {
	            	// Simple loop to check if the pixel is part of the seam or not.
	            	boolean inSeam = false;
	            	for (int z = 0; z < seam.length; z++) {
	            		// System.out.println(y);
	            		// System.out.println(x);
	            		// System.out.println(seam[z][1]);
	            		// System.out.println(seam[z][0]);
	            		if ((seam[z][0] == x) && (seam[z][1] == y)) {
	            			inSeam = true;
	            			shift = true;
	            		}
	            	}
	            	// if (shift) {
		            // 	System.out.println("entered!");
		            // }
	            	// System.out.println(x);
	            	// System.out.println(y);
	            	// System.out.println(newImage.getWidth());
	            	// System.out.println(newImage.getHeight());
	            	// this does not work, as we might need to put it at either x-1 or y-1.
	            	if (!inSeam) {
	            		// pixel not part of the seam, so we add it.
	            		int color = image.getRGB(x, y);
	            		if (shift) {
	            			newImage.setRGB(x - 1, y, color);
	            		} else {
		            		newImage.setRGB(x, y, color);
		            	}
	            	}
	            }
	        }
	    } else {
	    	// horizontal seam.
	    	for (int x = 0; x < width; x++) {
				boolean shift = false;
	            for (int y = 0; y < height - 1; y++) {
	            	// Simple loop to check if the pixel is part of the seam or not.
	            	boolean inSeam = false;
	            	for (int z = 0; z < seam.length; z++) {
	            		if ((seam[z][0] == x) && (seam[z][1] == y)) {
	            			inSeam = true;
	            			shift = true;
	            		}
	            	}

	            	// this does not work, as we might need to put it at either x-1 or y-1.
	            	if (!inSeam) {
	            		// pixel not part of the seam, so we add it.
	            		if (shift) {
	            			newImage.setRGB(x, y - 1, image.getRGB(x, y));
	            		} else {
		            		newImage.setRGB(x, y, image.getRGB(x, y));
		            	}
	            	}
	            }
	        }
	    }

		return newImage;
	}

	private static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }
}


