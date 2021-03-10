package com.atoledano.producegame.ecs.system;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.input.GameKeyInputListener;
import com.atoledano.producegame.input.GameKeys;
import com.atoledano.producegame.input.InputManager;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.atoledano.producegame.ecs.ECSEngine;
import com.atoledano.producegame.ecs.component.B2DComponent;
import com.atoledano.producegame.ecs.component.PlayerComponent;

public class PlayerMovementSystem extends IteratingSystem implements GameKeyInputListener {
    private boolean directionChange;
    private int xFactor;
    private int yFactor;

    public PlayerMovementSystem(final ProduceGame context) {
        super(Family.all(PlayerComponent.class, B2DComponent.class).get());
        context.getInputManager().addInputListener(this);
        directionChange = false;
        xFactor = yFactor = 0;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

//        if (directionChange) {
        final PlayerComponent playerComponent = ECSEngine.playerComponentMapper.get(entity);
        final B2DComponent b2DComponent = ECSEngine.b2DComponentMapper.get(entity);

//            directionChange = false;
        b2DComponent.body.applyLinearImpulse(
                (xFactor * playerComponent.speed.x - b2DComponent.body.getLinearVelocity().x) * b2DComponent.body.getMass(),
                (yFactor * playerComponent.speed.y - b2DComponent.body.getLinearVelocity().y) * b2DComponent.body.getMass(),
                b2DComponent.body.getWorldCenter().x,
                b2DComponent.body.getWorldCenter().y,
                true
        );
//        }
    }

    @Override
    public void keyPressed(InputManager inputManager, GameKeys key) {
        switch (key) {
            case LEFT:
                directionChange = true;
                xFactor = -1;
                break;
            case RIGHT:
                directionChange = true;
                xFactor = 1;
                break;
            case UP:
                directionChange = true;
                yFactor = 1;
                break;
            case DOWN:
                directionChange = true;
                yFactor = -1;
                break;
            default:
        }
    }

    @Override
    public void keyUp(InputManager inputManager, GameKeys key) {
        switch (key) {
            case LEFT:
                directionChange = true;
                xFactor = inputManager.isKeyPressed(GameKeys.RIGHT) ? 1 : 0;
                break;
            case RIGHT:
                directionChange = true;
                xFactor = inputManager.isKeyPressed(GameKeys.LEFT) ? -1 : 0;
                break;
            case UP:
                directionChange = true;
                yFactor = inputManager.isKeyPressed(GameKeys.DOWN) ? -1 : 0;
                break;
            case DOWN:
                directionChange = true;
                yFactor = inputManager.isKeyPressed(GameKeys.UP) ? 1 : 0;
                break;
            default:
        }
    }

}
