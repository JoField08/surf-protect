package dev.slne.protect.bukkit.region.visual.visualizer.visualizers;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.math.Mth;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PolygonalProtectionVisualizer extends ProtectionVisualizer<ProtectedRegion> {

    /**
     * Create a new visualizer for a region.
     *
     * @param world  The world that the region is in.
     * @param region The region to visualize.
     * @param player The player to visualize for.
     */
    public PolygonalProtectionVisualizer(World world, ProtectedRegion region, Player player) {
        super(world, region, player);

        setScaleUp(true);
    }

    @Override
    public List<Location> visualize() {
        List<Location> locations = new ArrayList<>();
        List<BlockVector2> points = getRegion().getPoints();

        for (BlockVector2 point : points) {
            locations.addAll(this.formVisualizerPillar(point));
        }

        locations.addAll(this.formVisualizerLine(points));
        return locations;
    }

    /**
     * Visualize a pillar.
     *
     * @param point The point that the pillar is at.
     * @return The locations of the pillar.
     */
    private List<Location> formVisualizerPillar(BlockVector2 point) {
        List<Location> locations = new ArrayList<>();

        addHighestBlockIfLoaded(locations, point);

        return locations;
    }

    /**
     * Visualize a line.
     *
     * @param linePoints The points that the line is at.
     * @return The locations of the line.
     */
    private List<Location> formVisualizerLine(List<BlockVector2> linePoints) {
        List<Location> locations = new ArrayList<>();
        List<BlockVector2> addedLinePoints = new ArrayList<>(linePoints);

        for (int i = 0; i < linePoints.size(); i++) {
            BlockVector2 currentPoint = linePoints.get(i);
            BlockVector2 nextPoint = linePoints.get(i + 1 == linePoints.size() ? 0 : i + 1);

            List<BlockVector2> points = walkPointAToB(currentPoint, nextPoint);
            for (BlockVector2 point : points) {
                addedLinePoints.add(point);
                locations.add(new Location(getWorld(), point.getBlockX(), 0, point.getBlockZ()));
            }
        }

        List<Location> finalLocations = new ArrayList<>();
        for (Location location : locations) {
            BlockVector2 point = BlockVector2.at(location.getBlockX(), location.getBlockZ());

            int tries = 0;
            int maxTries = 4;

            while (tries < maxTries) {
                if (getRegion().contains(point)) {
                    break;
                }

                BlockVector2[] vectors = new BlockVector2[]{
                        point.add(1, 0),
                        point.add(-1, 0),
                        point.add(0, 1),
                        point.add(0, -1)
                };

                for (BlockVector2 usableVector : vectors) {
                    if (!addedLinePoints.contains(usableVector) && getRegion().contains(usableVector)) {
                        point = usableVector;
                    }
                }

                tries++;
            }

            if (point != null) {
                addHighestBlockIfLoaded(finalLocations, point);
            }
        }

        return finalLocations;
    }

    private void addHighestBlockIfLoaded(List<Location> finalLocations, BlockVector2 point) {
        if (getWorld().isChunkLoaded(point.getBlockX() >> 4, point.getBlockZ() >> 4)) {
            Block highestYBlock = getWorld().getHighestBlockAt(point.getBlockX(), point.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES);
            int highestY = highestYBlock.getY() + 1;
            finalLocations.add(new Location(getWorld(), point.getBlockX(), highestY, point.getBlockZ()));
        }
    }

    /**
     * Walk from point A to point B.
     *
     * @param pointStart The starting point.
     * @param pointEnd   The ending point.
     * @return The points in between.
     */
    private List<BlockVector2> walkPointAToB(BlockVector2 pointStart, BlockVector2 pointEnd) {
        List<BlockVector2> points = new ArrayList<>();

        Mth.walkCoordinatesAToB(pointStart.getBlockX(), pointStart.getBlockZ(), pointEnd.getBlockX(), pointEnd.getBlockZ(),
                (x, z) -> points.add(BlockVector2.at(x, z)));

        return points;
    }

}
