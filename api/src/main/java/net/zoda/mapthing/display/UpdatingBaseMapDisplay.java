package net.zoda.mapthing.display;

import net.zoda.mapthing.MapScreen;
import net.zoda.mapthing.canvas.CanvasModifier;

/**
 * A map display that only draws to the canvas if there are players in-range.
 */
public interface UpdatingBaseMapDisplay extends BaseMapDisplay {

    void draw(int frame, MapScreen screen);

    @Override
    default void draw(MapScreen screen, CanvasModifier modifier) {
        if(screen.getInRangePlayers().size() == 0) return;
        draw(0,screen);
    }
}
