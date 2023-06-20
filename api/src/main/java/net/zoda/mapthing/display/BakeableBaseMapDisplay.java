package net.zoda.mapthing.display;

import net.zoda.mapthing.MapScreen;

public interface BakeableBaseMapDisplay extends BaseMapDisplay {

    /**
     *
     * - Do baking operations here... -
     * <p>
     * Only use this for taxing operations that cannot be called every rendering operation.
     * <p>
     * Add the objects to be cached in {@link net.zoda.mapthing.MapScreen} to then be retrieved in the rendering
     */
    void bakeObjects(MapScreen.BaseMapDisplayBaker baker);
}
