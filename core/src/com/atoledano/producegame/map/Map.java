package com.atoledano.producegame.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import static com.atoledano.producegame.ProduceGame.UNIT_SCALE;

public class Map {
    public static final String TAG = Map.class.getSimpleName();
    private final TiledMap tiledMap;
    private final Array<CollisionArea> collisionAreas;
    private final Array<GameObject> gameObjects;
    private final IntMap<Animation<Sprite>> mapAnimations;

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    private final Vector2 startLocation;

    public Map(final TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        collisionAreas = new Array<CollisionArea>();

        parseCollisionLayer();
        startLocation = new Vector2();
        parsePlayerStartLocation();

        gameObjects = new Array<GameObject>();
        mapAnimations = new IntMap<Animation<Sprite>>();
        parseGameObjectLayer();
    }

    private void parseGameObjectLayer() {
        final MapLayer gameObjectsLayer = tiledMap.getLayers().get("gameObjects");
        if (gameObjectsLayer == null) {
            Gdx.app.debug(TAG, "There is no gameObjects layer");
            return;
        }
        final MapObjects mapObjects = gameObjectsLayer.getObjects();
        for (final MapObject mapObject : mapObjects) {
            if (!(mapObject instanceof TiledMapTileMapObject)) {
                Gdx.app.debug(TAG, "GameObject of type " + mapObject + " is not supported");
                continue;
            }
            final TiledMapTileMapObject tiledMapTileMapObject = (TiledMapTileMapObject) mapObject;
            final MapProperties tileMapObjectProperties = tiledMapTileMapObject.getProperties();
            final MapProperties tileProperties = tiledMapTileMapObject.getTile().getProperties();
            final GameObjectType gameObjectType;
            if (tileMapObjectProperties.containsKey("type")) {
                gameObjectType = GameObjectType.valueOf(tileMapObjectProperties.get("type", String.class));
            } else if (tileProperties.containsKey("type")) {
                gameObjectType = GameObjectType.valueOf(tileProperties.get("type", String.class));
            } else {
                Gdx.app.debug(TAG, "There is no gameobjecttype defined for tile " + tileMapObjectProperties.get("id", Integer.class));
                continue;
            }
            final int animationIndex = tiledMapTileMapObject.getTile().getId();
            if (!createAnimation(animationIndex, tiledMapTileMapObject.getTile())) {
                Gdx.app.debug(TAG, "Could not get animation for tile " + tileMapObjectProperties.get("id", Integer.class));
                continue;
            }

            final float width = tileMapObjectProperties.get("width", Float.class) * UNIT_SCALE;
            final float height = tileMapObjectProperties.get("height", Float.class) * UNIT_SCALE;
            gameObjects.add(new GameObject(gameObjectType, new Vector2(tiledMapTileMapObject.getX() * UNIT_SCALE, tiledMapTileMapObject.getY() * UNIT_SCALE), width, height, tiledMapTileMapObject.getRotation(), animationIndex));
        }
    }

    private boolean createAnimation(int animationIndex, TiledMapTile tile) {
        Animation<Sprite> spriteAnimation = mapAnimations.get(animationIndex);
        if (spriteAnimation == null) {
            Gdx.app.debug(TAG, "Creating new map animation for tile " + tile.getId());
            if (tile instanceof AnimatedTiledMapTile) {
                final AnimatedTiledMapTile animatedTiledMapTile = (AnimatedTiledMapTile) tile;
                final Sprite[] keyFrames = new Sprite[animatedTiledMapTile.getFrameTiles().length];
                int i = 0;
                for (final StaticTiledMapTile staticTiledMapTile : animatedTiledMapTile.getFrameTiles()) {
                    keyFrames[i++] = new Sprite(staticTiledMapTile.getTextureRegion());
                }
                spriteAnimation = new Animation<Sprite>(animatedTiledMapTile.getAnimationIntervals()[0] * 0.001f, keyFrames);
                spriteAnimation.setPlayMode(Animation.PlayMode.LOOP);
                mapAnimations.put(animationIndex, spriteAnimation);
            } else if (tile instanceof StaticTiledMapTile) {
                spriteAnimation = new Animation<Sprite>(0, new Sprite(tile.getTextureRegion()));
                mapAnimations.put(animationIndex, spriteAnimation);
            } else {
                Gdx.app.debug(TAG, "Tile of type " + tile + " is not supported for map animations");
                return false;
            }
        }
        return true;
    }

    private void parsePlayerStartLocation() {
        final MapLayer playerStartLocation = tiledMap.getLayers().get("playerStartLocation");
        if (playerStartLocation == null) {
            //debug line
            Gdx.app.debug(TAG, "No playerStartLocation found.");
            return;
        }
        final MapObjects mapObjects = playerStartLocation.getObjects();
        for (final MapObject mapObject : mapObjects) {
            if (mapObject instanceof RectangleMapObject) {
                final RectangleMapObject rectangleMapObject = (RectangleMapObject) mapObject;
                final Rectangle rectangle = rectangleMapObject.getRectangle();
                startLocation.set(rectangle.x * UNIT_SCALE, rectangle.y * UNIT_SCALE);
            } else {
                Gdx.app.debug(TAG, "MapObject of type " + mapObject + " not supported for playerStartLocation.");
            }
        }
    }

    private void parseCollisionLayer() {
        final MapLayer collisionLayer = tiledMap.getLayers().get("collision");
        if (collisionLayer == null) {
            //debug line
            Gdx.app.debug(TAG, "No collision layer found.");
            return;
        }

        //dealing with the different types of objects/lines
        for (final MapObject mapObject : collisionLayer.getObjects()) {
            if (mapObject instanceof RectangleMapObject) {
                final RectangleMapObject rectangleMapObject = (RectangleMapObject) mapObject;
                final Rectangle rectangle = rectangleMapObject.getRectangle();
                final float[] rectangleVertices = new float[10];

                //left bottom
                rectangleVertices[0] = 0;
                rectangleVertices[1] = 0;

                //left top
                rectangleVertices[2] = 0;
                rectangleVertices[3] = rectangle.height;

                //right top
                rectangleVertices[4] = rectangle.width;
                rectangleVertices[5] = rectangle.height;

                //right bottom
                rectangleVertices[6] = rectangle.width;
                rectangleVertices[7] = 0;

                //left bottom
                rectangleVertices[8] = 0;
                rectangleVertices[9] = 0;

                collisionAreas.add(new CollisionArea(rectangle.x, rectangle.y, rectangleVertices));

            } else if (mapObject instanceof PolylineMapObject) {
                final PolylineMapObject polylineMapObject = (PolylineMapObject) mapObject;
                final Polyline polyline = polylineMapObject.getPolyline();
                collisionAreas.add(new CollisionArea(polyline.getX(), polyline.getY(), polyline.getVertices()));
            } else {
                Gdx.app.debug(TAG, "MapObject of type " + mapObject + " not supported for collision layer.");
            }
        }
    }

    public Array<CollisionArea> getCollisionAreas() {
        return collisionAreas;
    }

    public Vector2 getStartLocation() {
        return startLocation;
    }

    public Array<GameObject> getGameObjects() {
        return gameObjects;
    }

    public IntMap<Animation<Sprite>> getMapAnimations() {
        return mapAnimations;
    }
}
