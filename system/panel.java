package system;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class panel {

	private static final int ROWS = 3;
	private static final int COLS = 3;

	private final BufferedImage[][] slices;
	private final int leftWidth;
	private final int rightWidth;
	private final int topHeight;
	private final int bottomHeight;

	public panel() {
		this("/asset/Panel");
	}

	public panel(String panelDirectory) {
		slices = new BufferedImage[ROWS][COLS];
		loadSlices(panelDirectory);

		leftWidth = slices[0][0].getWidth();
		rightWidth = slices[0][2].getWidth();
		topHeight = slices[0][0].getHeight();
		bottomHeight = slices[2][0].getHeight();
	}

	public int getMinWidth() {
		return leftWidth + rightWidth;
	}

	public int getMinHeight() {
		return topHeight + bottomHeight;
	}

	public void draw(Graphics2D g2, int x, int y, int width, int height) {
		if (g2 == null || width <= 0 || height <= 0) {
			return;
		}

		int drawWidth = Math.max(width, getMinWidth());
		int drawHeight = Math.max(height, getMinHeight());

		int centerWidth = Math.max(0, drawWidth - leftWidth - rightWidth);
		int centerHeight = Math.max(0, drawHeight - topHeight - bottomHeight);

		int[] dstX = { x, x + leftWidth, x + leftWidth + centerWidth };
		int[] dstY = { y, y + topHeight, y + topHeight + centerHeight };

		Object oldInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		try {
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					BufferedImage part = slices[row][col];
					int partDrawWidth = getPartWidth(col, centerWidth);
					int partDrawHeight = getPartHeight(row, centerHeight);

					if (partDrawWidth <= 0 || partDrawHeight <= 0) {
						continue;
					}

					g2.drawImage(
						part,
						dstX[col],
						dstY[row],
						dstX[col] + partDrawWidth,
						dstY[row] + partDrawHeight,
						0,
						0,
						part.getWidth(),
						part.getHeight(),
						null
					);
				}
			}
		} finally {
			if (oldInterpolation != null) {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
			}
		}
	}

	private int getPartWidth(int col, int centerWidth) {
		if (col == 0) {
			return leftWidth;
		}
		if (col == 1) {
			return centerWidth;
		}
		return rightWidth;
	}

	private int getPartHeight(int row, int centerHeight) {
		if (row == 0) {
			return topHeight;
		}
		if (row == 1) {
			return centerHeight;
		}
		return bottomHeight;
	}

	private void loadSlices(String panelDirectory) {
		for (int row = 1; row <= ROWS; row++) {
			for (int col = 1; col <= COLS; col++) {
				String name = "row-" + row + "-column-" + col + ".png";
				String resourcePath = panelDirectory + "/" + name;
				slices[row - 1][col - 1] = loadImage(resourcePath);
			}
		}
	}

	private BufferedImage loadImage(String resourcePath) {
		try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
			if (stream != null) {
				BufferedImage loaded = ImageIO.read(stream);
				if (loaded != null) {
					return loaded;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load panel resource: " + resourcePath, e);
		}

		String filePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
		File file = new File(filePath);
		if (!file.exists()) {
			throw new IllegalStateException(
				"Panel slice not found: " + resourcePath + " (classpath) and " + file.getPath() + " (filesystem)"
			);
		}

		try {
			BufferedImage loaded = ImageIO.read(file);
			if (loaded == null) {
				throw new IllegalStateException("Panel slice could not be decoded: " + file.getPath());
			}
			return loaded;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load panel file: " + file.getPath(), e);
		}
	}
}
