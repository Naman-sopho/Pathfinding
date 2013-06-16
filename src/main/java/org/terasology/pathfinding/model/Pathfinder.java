package org.terasology.pathfinding.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.rendering.InfoGrid;
import org.terasology.world.WorldProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author synopia
 */
public class Pathfinder {
    private static final Logger logger = LoggerFactory.getLogger(Pathfinder.class);

    private WorldProvider world;
    private Map<Vector3i, HeightMap> heightMaps = new HashMap<Vector3i, HeightMap>();
    private HAStar haStar;
    private PathCache cache;

    public Pathfinder(WorldProvider world) {
        this.world = world;
        haStar = new HAStar();
        cache = new PathCache();
    }

    public Path findPath(final WalkableBlock start, final WalkableBlock end) {
        return cache.findPath(start, end, new PathCache.Callback() {
            @Override
            public Path run(WalkableBlock from, WalkableBlock to) {
                haStar.reset();
                Path path = null;
                long time = System.nanoTime();
                if( haStar.run(start, end) ) {
                    path = haStar.getPath();
                    path.add(start);
                    Collections.reverse(path);
                    logger.info("Found path of "+path.size()+" steps, "+ haStar);
                } else {
                    logger.info("No path found "+ haStar);
                    path = Path.INVALID;
                }
                logger.info("Searching path "+start.getBlockPosition()+"->"+end.getBlockPosition()+" took "+((System.nanoTime()-time)/1000/1000f)+" ms");
                return path;
            }
        });
    }

    public HeightMap init( Vector3i chunkPos ) {
        HeightMap heightMap = heightMaps.get(chunkPos);
        if( heightMap==null ) {
            cache.clear();
            long time = System.nanoTime();
            heightMap = new HeightMap(world, chunkPos);
            heightMap.update();
            logger.info("Update chunk " + chunkPos + " took " + ((System.nanoTime() - time) / 1000 / 1000f) + " ms");
            logger.info("Found " + heightMap);
            heightMaps.put(chunkPos, heightMap);
            heightMap.connectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1), getNeighbor(chunkPos, 1,0), getNeighbor(chunkPos, 0,1));
        }
        return heightMap;
    }

    private HeightMap getNeighbor(Vector3i chunkPos, int x, int z) {
        Vector3i neighborPos = new Vector3i(chunkPos);
        neighborPos.add(x, 0, z);
        return heightMaps.get(neighborPos);
    }

    public HeightMap update( Vector3i chunkPos ) {

        HeightMap heightMap = heightMaps.remove(chunkPos);
        if( heightMap!=null ) {
            heightMap.disconnectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1), getNeighbor(chunkPos, 1,0), getNeighbor(chunkPos, 0,1));
        }
        return init(chunkPos);
    }

    public WalkableBlock getBlock(Vector3i pos) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        HeightMap heightMap = heightMaps.get(chunkPos);
        if( heightMap!=null ) {
            return heightMap.getBlock(pos.x, pos.y, pos.z);
        } else {
            return null;
        }
    }
}
