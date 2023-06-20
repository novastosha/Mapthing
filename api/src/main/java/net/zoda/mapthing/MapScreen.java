package net.zoda.mapthing;

import net.zoda.mapthing.canvas.CanvasModifier;
import net.zoda.mapthing.display.BakeableBaseMapDisplay;
import net.zoda.mapthing.display.BaseMapDisplay;
import org.bukkit.entity.Player;

import java.util.*;

public sealed abstract class MapScreen permits PersonalMapScreen, SharedMapScreen {

    public final int width,height;
    private final BaseMapDisplayBaker displayBaker;

    private final BaseMapDisplay baseMapDisplay;

    protected final List<Player> viewingPlayers = new ArrayList<>();
    protected final List<Player> inRangePlayers = new ArrayList<>(); // this will always contain players from viewingPlayers

    protected final Map<Integer,int[][]> cachedLayers = new HashMap<>(); // 0 - the base

    protected MapScreen(int blocksWide, int blocksHigh, BaseMapDisplay baseDisplay) {
        this.width = Math.max(1,blocksWide)*128;
        this.height = Math.max(1,blocksHigh)*128;
        this.displayBaker = new BaseMapDisplayBaker();
        this.baseMapDisplay = baseDisplay;

        if(baseDisplay instanceof BakeableBaseMapDisplay baking) baking.bakeObjects(displayBaker);
    }

    @SuppressWarnings("unchecked")
    public <E> E getBakedObject(String objKey, BakeableBaseMapDisplay baseMapDisplay) {
        if(!baseMapDisplay.equals(this.baseMapDisplay)) throw new IllegalCallerException("Only current display can grab cached objects!");
        return (E) displayBaker.bakedObjects.get(objKey);
    }

    /***
     * Destroys the map screen
     */
    public void destroy() {
        // clear cache, unhook from player channels, remove map entities

    }

    public class BaseMapDisplayBaker {
        private final Map<String,Object> bakedObjects = new HashMap<>();

        public void addObject(String objKey, Object object) {
            bakedObjects.put(objKey,object);
        }

        public MapScreen getMapScreen() {
            return MapScreen.this;
        }
    }

    public List<Player> getVisibleForPlayers() {
        return Collections.unmodifiableList(viewingPlayers);
    }

    public List<Player> getInRangePlayers() {
        return Collections.unmodifiableList(inRangePlayers);
    }

    protected final void receivedChanges(CanvasModifier modifier) {

    }

    protected final void sendChanges(Player player) {
    }

    protected void playerEnterRange(Player player) {
        inRangePlayers.add(player);
        sendChanges(player);
    }

    protected void playerExitRange(Player player) {
        inRangePlayers.remove(player);
    }

    protected void removePlayer(Player player) {
        viewingPlayers.remove(player);
        playerExitRange(player);
    }
}
