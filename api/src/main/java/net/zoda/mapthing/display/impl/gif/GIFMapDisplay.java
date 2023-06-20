package net.zoda.mapthing.display.impl.gif;

import net.zoda.mapthing.MapScreen;
import net.zoda.mapthing.display.BakeableBaseMapDisplay;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class GIFMapDisplay implements BakeableBaseMapDisplay {
    private final InputStream gifStream;

    public GIFMapDisplay(InputStream gifStream) {
        this.gifStream = gifStream;
    }

    @Override
    public void draw(MapScreen screen) {
        GIFFrame[] frames = screen.getBakedObject("frames", this);

        for (final var frame : frames) {
            for (int x = 0; x < frame.width(); x++) {
                for (int y = 0; y < frame.height(); y++) {

                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bakeObjects(MapScreen.BaseMapDisplayBaker baker) {
        try {
            baker.addObject("frames", readGifAndDownscale(baker.getMapScreen()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GIFFrame[] readGifAndDownscale(MapScreen screen) throws IOException {
        var frames = new ArrayList<GIFFrame>(2);

        var reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(gifStream));

        int lastx = 0, lasty = 0, width = -1, height = -1;
        var metadata = reader.getStreamMetadata();

        Color backgroundColor = null;

        if (metadata != null) {
            var globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            var globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
            var globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreeDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }

            if (globalColorTable.getLength() > 0) {
                IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);

                if (colorTable != null) {
                    String bgIndex = colorTable.getAttribute("backgroundColorIndex");

                    IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
                    while (colorEntry != null) {
                        if (colorEntry.getAttribute("index").equals(bgIndex)) {
                            int red = Integer.parseInt(colorEntry.getAttribute("red"));
                            int green = Integer.parseInt(colorEntry.getAttribute("green"));
                            int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

                            backgroundColor = new Color(red, green, blue);
                            break;
                        }

                        colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
                    }
                }
            }
        }

        BufferedImage master = null;
        boolean hasBackground = false;

        for (int frameIndex = 0; ; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = screen.width;
                height = screen.height;
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            var children = root.getChildNodes();

            int delay = Integer.parseInt(gce.getAttribute("delayTime"));

            String disposal = gce.getAttribute("disposalMethod");

            var scaledImage = image.getScaledInstance(screen.width, screen.height, Image.SCALE_DEFAULT);
            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                master.createGraphics().setColor(backgroundColor);
                master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

                hasBackground = image.getWidth() == width && image.getHeight() == height;

                master.createGraphics().drawImage(scaledImage, 0, 0, null);
            } else {
                int x = 0;
                int y = 0;

                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    var nodeItem = children.item(nodeIndex);

                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        var map = nodeItem.getAttributes();

                        x = Integer.parseInt(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.parseInt(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }

                if (disposal.equals("restoreToPrevious")) {
                    BufferedImage from = null;
                    for (int i = frameIndex - 1; i >= 0; i--) {
                        if (!frames.get(i).disposal().equals("restoreToPrevious") || frameIndex == 0) {
                            from = frames.get(i).image();
                            break;
                        }
                    }

                    {
                        var model = Objects.requireNonNull(from).getColorModel();
                        boolean alpha = from.isAlphaPremultiplied();
                        var raster = from.copyData(null);
                        master = new BufferedImage(model, raster, alpha, null);
                    }
                } else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null) {
                    if (!hasBackground || frameIndex > 1) {
                        master.createGraphics().fillRect(lastx, lasty, frames.get(frameIndex - 1).width(), frames.get(frameIndex - 1).width());
                    }
                }
                master.createGraphics().drawImage(scaledImage, x, y, null);

                lastx = x;
                lasty = y;
            }

            {
                BufferedImage copy;

                {
                    var model = master.getColorModel();
                    boolean alpha = master.isAlphaPremultiplied();
                    var raster = master.copyData(null);
                    copy = new BufferedImage(model, raster, alpha, null);
                }
                frames.add(new GIFFrame(copy, delay, disposal, image.getWidth(), image.getHeight()));
            }

            master.flush();
        }
        reader.dispose();

        return frames.toArray(GIFFrame[]::new);
    }

}
