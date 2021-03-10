package com.atoledano.producegame.ecs.system;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.WorldContactListener;
import com.atoledano.producegame.ecs.ECSEngine;
import com.atoledano.producegame.ecs.component.GameObjectComponent;
import com.atoledano.producegame.ecs.component.RemoveComponent;
import com.atoledano.producegame.map.GameObjectType;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

public class PlayerCollisionSystem extends IteratingSystem implements WorldContactListener.PlayerCollisionListener {
    public PlayerCollisionSystem(final ProduceGame context) {
        super(Family.all(RemoveComponent.class).get());

        context.getWorldContactListener().addPlayerCollisionListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        getEngine().removeEntity(entity);
    }

    @Override
    public void playerCollision(Entity player, Entity gameObject) {
        final GameObjectComponent gameObjectComponent = ECSEngine.gameObjectComponentMapper.get(gameObject);

        switch (gameObjectComponent.gameObjectType) {
            case CRYSTAL:
                gameObject.add(getEngine().createComponent(RemoveComponent.class));
                break;
            case AXE:
                ECSEngine.playerComponentMapper.get(player).hasAxe = true;
                gameObject.add(getEngine().createComponent(RemoveComponent.class));
                break;
            case TREE:
                if (ECSEngine.playerComponentMapper.get(player).hasAxe) {
                    gameObject.add(getEngine().createComponent(RemoveComponent.class));
                }
                break;
        }
    }
}
