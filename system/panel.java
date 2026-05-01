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

	public int getTopHeight()    { return topHeight; }
	public int getBottomHeight() { return bottomHeight; }
	public int getLeftWidth()    { return leftWidth; }
	public int getRightWidth()   { return rightWidth; }

	public void draw(Graphics2D g2, int x, int y, int width, int height) {
		if (g2 == null || width <= 0 || height <= 0) {
			return;
		}

		// Scale borders proportionally if the requested size is smaller than the natural minimum
		float scaleX = (width  < getMinWidth())  ? (float) width  / getMinWidth()  : 1f;
		float scaleY = (height < getMinHeight()) ? (float) height / getMinHeight() : 1f;
		float scale  = Math.min(scaleX, scaleY);

		int scaledLeft   = Math.max(1, Math.round(leftWidth   * scale));
		int scaledRight  = Math.max(1, Math.round(rightWidth  * scale));
		int scaledTop    = Math.max(1, Math.round(topHeight   * scale));
		int scaledBottom = Math.max(1, Math.round(bottomHeight * scale));

		int centerWidth  = Math.max(0, width  - scaledLeft - scaledRight);
		int centerHeight = Math.max(0, height - scaledTop  - scaledBottom);

		int[] dstX = { x, x + scaledLeft, x + scaledLeft + centerWidth };
		int[] dstY = { y, y + scaledTop,  y + scaledTop  + centerHeight };

		int[] partW = { scaledLeft, centerWidth, scaledRight };
		int[] partH = { scaledTop, centerHeight, scaledBottom };

		Object oldInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		try {
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					BufferedImage part = slices[row][col];
					int pw = partW[col];
					int ph = partH[row];

					if (pw <= 0 || ph <= 0) {
						continue;
					}

					g2.drawImage(
						part,
						dstX[col],
						dstY[row],
						dstX[col] + pw,
						dstY[row] + ph,
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
