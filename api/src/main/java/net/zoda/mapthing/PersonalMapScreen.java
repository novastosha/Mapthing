package net.zoda.mapthing;

import net.zoda.mapthing.display.BaseMapDisplay;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public non-sealed class PersonalMapScreen extends MapScreen {
    private final Player player;

    public PersonalMapScreen(int blocksWide, int blocksHigh, BaseMapDisplay baseDisplay, Player player) {
        super(blocksWide, blocksHigh, baseDisplay);
        viewingPlayers.add(player);

        this.player = player;
    }

    /**
     * @return in the case of a personal screen, this will return a singleton list.
     */
    @Override
    public List<Player> getVisibleForPlayers() {
        return Collections.singletonList(player);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    protected void removePlayer(Player player) {
        super.removePlayer(player);
        super.destroy();
    }
}
