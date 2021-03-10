package com.atoledano.producegame.ecs.component;

import com.atoledano.producegame.map.GameObjectType;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class GameObjectComponent implements Component, Pool.Poolable {
    public GameObjectType gameObjectType;
    public int animationIndex;

    @Override
    public void reset() {
        gameObjectType = null;
        animationIndex = -1;
    }
}
