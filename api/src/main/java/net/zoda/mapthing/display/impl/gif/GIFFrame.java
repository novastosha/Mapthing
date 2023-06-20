package net.zoda.mapthing.display.impl.gif;

import java.awt.image.BufferedImage;

record GIFFrame(BufferedImage image, int delay, String disposal, int width, int height) {
    public GIFFrame(BufferedImage image) {
        this(image,-1,null,-1,-1);
    }
}
