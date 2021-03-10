package com.atoledano.producegame.map;

import com.badlogic.gdx.math.Vector2;

public class GameObject {
    private final GameObjectType gameObjectType;
    private final Vector2 position;
    private final float width;
    private final float height;
    private final float rotationDegree;
    private final int animationIndex;

    public GameObject(final GameObjectType gameObjectType, final Vector2 position, final float width, final float height, final float rotationDegree, final int animationIndex) {
        this.gameObjectType = gameObjectType;
        this.position = position;
        this.width = width;
        this.height = height;
        this.rotationDegree = rotationDegree;
        this.animationIndex = animationIndex;
    }

    public GameObjectType getGameObjectType() {
        return gameObjectType;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRotationDegree() {
        return rotationDegree;
    }

    public int getAnimationIndex() {
        return animationIndex;
    }
}
