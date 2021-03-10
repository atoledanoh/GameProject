package com.atoledano.producegame.ecs.system;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.ecs.ECSEngine;
import com.atoledano.producegame.ecs.component.AnimationComponent;
import com.atoledano.producegame.ecs.component.B2DComponent;
import com.atoledano.producegame.ecs.component.PlayerComponent;
import com.atoledano.producegame.view.AnimationType;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;

public class PlayerAnimationSystem extends IteratingSystem {
    public PlayerAnimationSystem(final ProduceGame context) {
        super(Family.all(AnimationComponent.class, PlayerComponent.class, B2DComponent.class).get());
    }

    @Override
    protected void processEntity(final Entity entity, float deltaTime) {
        final B2DComponent b2DComponent = ECSEngine.b2DComponentMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);

        //player does not move
        if (b2DComponent.body.getLinearVelocity().equals(Vector2.Zero)) {
            animationComponent.animationTime = 0;
            //player moves up
        } else if (b2DComponent.body.getLinearVelocity().y > 0) {
            animationComponent.animationType = AnimationType.PLAYER_MOVE_UP;
            //player moves left
        } else if (b2DComponent.body.getLinearVelocity().x < 0) {
            animationComponent.animationType = AnimationType.PLAYER_MOVE_LEFT;
            //player moves right
        } else if (b2DComponent.body.getLinearVelocity().x > 0) {
            animationComponent.animationType = AnimationType.PLAYER_MOVE_RIGHT;
            //player moves down
        } else if (b2DComponent.body.getLinearVelocity().y < 0) {
            animationComponent.animationType = AnimationType.PLAYER_MOVE_DOWN;
        }
    }
}
