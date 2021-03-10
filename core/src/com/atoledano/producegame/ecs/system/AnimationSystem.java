package com.atoledano.producegame.ecs.system;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.ecs.ECSEngine;
import com.atoledano.producegame.ecs.component.AnimationComponent;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

public class AnimationSystem extends IteratingSystem {
    public AnimationSystem(final ProduceGame context) {
        super(Family.all(AnimationComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        if (animationComponent.animationType != null) {
            animationComponent.animationTime += deltaTime;
        }
    }
}
