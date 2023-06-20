package net.zoda.mapthing;

import net.zoda.mapthing.display.BaseMapDisplay;
import org.bukkit.entity.Player;

public non-sealed class SharedMapScreen extends MapScreen {
    private final Player[] limitedTo;

    public SharedMapScreen(int blocksWide, int blocksHigh, BaseMapDisplay baseDisplay, Player[] limitTo) {
        super(blocksWide, blocksHigh, baseDisplay);
        this.limitedTo = limitTo;
    }

    public SharedMapScreen(int blocksWide, int blocksHigh, BaseMapDisplay baseDisplay) {
        this(blocksWide,blocksHigh,baseDisplay,null);
    }
}
