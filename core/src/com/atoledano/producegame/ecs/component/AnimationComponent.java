package com.atoledano.producegame.ecs.component;

import com.atoledano.producegame.view.AnimationType;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class AnimationComponent implements Component, Pool.Poolable {
    public AnimationType animationType;
    public float animationTime;
    public float width;
    public float height;


    @Override
    public void reset() {
        animationType = null;
        animationTime = 0;
        width = height = 0;
    }
}
