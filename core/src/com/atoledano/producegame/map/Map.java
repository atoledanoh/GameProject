package com.atoledano.producegame.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static com.atoledano.producegame.ProduceGame.UNIT_SCALE;

public class Map {
    public static final String TAG = Map.class.getSimpleName();
    private final TiledMap tiledMap;
    private final Array<CollisionArea> collisionAreas;
    private final Vector2 startLocation;

    public Map(final TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        collisionAreas = new Array<CollisionArea>();

        parseCollisionLayer();
        startLocation = new Vector2();
        parsePlayerStartLocation();
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
}
