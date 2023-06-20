package net.zoda.mapthing.display.impl;

import net.zoda.mapthing.MapScreen;
import net.zoda.mapthing.canvas.CanvasModifier;
import net.zoda.mapthing.display.BaseMapDisplay;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class BasicCanvasMapDisplay implements BaseMapDisplay {

    private final List<BiConsumer<MapScreen,CanvasModifier>> canvasActions = new ArrayList<>();

    public BasicCanvasMapDisplay fillCanvas(Color color) {
        canvasActions.add((screen, modifier) -> {
            for (int x = 0; x < screen.width; x++) {
                for (int y = 0; y < screen.height; y++) {
                    modifier.drawPixel(x,y,color);
                }
            }
        });
        return this;
    }

    @Override
    public void draw(MapScreen screen, CanvasModifier modifier) {
        canvasActions.forEach(action -> action.accept(screen,modifier));
        //TODO: Handle out of bounds for shapes and text
    }
}
