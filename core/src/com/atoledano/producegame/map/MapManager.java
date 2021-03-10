package com.atoledano.producegame.map;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.EnumMap;

import static com.atoledano.producegame.ProduceGame.ROOM_BIT;

public class MapManager {
    public static final String TAG = MapManager.class.getSimpleName();

    private final World world;
    private final Array<Body> bodies;
    private final AssetManager assetManager;
    private MapType currentMapType;
    private Map currentMap;
    private final EnumMap<MapType, Map> mapCache;
    private final Array<MapListener> listeners;


    public MapManager(final ProduceGame context) {
        currentMapType = null;
        currentMap = null;
        world = context.getWorld();
        assetManager = context.getAssetManager();
        bodies = new Array<Body>();
        mapCache = new EnumMap<MapType, Map>(MapType.class);
        listeners = new Array<MapListener>();
    }

    public void addMapListener(final MapListener listener) {
        listeners.add(listener);
    }

    public void setMap(final MapType mapType) {
        //map is already set
        if (currentMapType == mapType) {
            return;
        }
        //clean bodies/entities on current map
        if (currentMap != null) {
            world.getBodies(bodies);
            destroyCollisionAreas();
        }
        //set new map
        Gdx.app.debug(TAG, "Loading map type: " + mapType);
        currentMap = mapCache.get(mapType);
        if (currentMap == null) {
            Gdx.app.debug(TAG, "Creating map type: " + mapType);
            final TiledMap tiledMap = assetManager.get(mapType.getFilePath(), TiledMap.class);
            currentMap = new Map(tiledMap);
            mapCache.put(mapType, currentMap);
        }
        //create bodies/entities
        spawnCollisionAreas();

        for (final MapListener listener : listeners) {
            listener.mapChange(currentMap);
        }
    }

    private void destroyCollisionAreas() {
        for (final Body body : bodies) {
            if ("ROOM".equals(body.getUserData())) {
                world.destroyBody(body);
            }
        }
    }

    private void spawnCollisionAreas() {
        ProduceGame.resetBodyAndFixtureDefinition();
        for (final CollisionArea collisionArea : currentMap.getCollisionAreas()) {
            ProduceGame.BODY_DEF.position.set(collisionArea.getX(), collisionArea.getY());
            ProduceGame.BODY_DEF.fixedRotation = true;
            final Body body = world.createBody(ProduceGame.BODY_DEF);
            body.setUserData("ROOM");

            ProduceGame.FIXTURE_DEF.filter.categoryBits = ROOM_BIT;
            ProduceGame.FIXTURE_DEF.filter.maskBits = -1;
            final ChainShape chainShape = new ChainShape();
            chainShape.createChain(collisionArea.getVertices());
            ProduceGame.FIXTURE_DEF.shape = chainShape;
            body.createFixture(ProduceGame.FIXTURE_DEF);
            chainShape.dispose();
        }
    }

    public Map getCurrentMap() {
        return currentMap;
    }
}
