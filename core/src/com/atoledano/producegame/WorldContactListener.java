package com.atoledano.producegame;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import static com.atoledano.producegame.ProduceGame.GAME_OBJECT_BIT;
import static com.atoledano.producegame.ProduceGame.PLAYER_BIT;

public class WorldContactListener implements ContactListener {
    private final Array<PlayerCollisionListener> listeners;

    public WorldContactListener() {
        listeners = new Array<PlayerCollisionListener>();
    }

    public void addPlayerCollisionListener(final PlayerCollisionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void beginContact(Contact contact) {
        final Entity player;
        final Entity gameObject;
        final Body bodyA = contact.getFixtureA().getBody();
        final Body bodyB = contact.getFixtureB().getBody();
        final int fixtureACategory = contact.getFixtureA().getFilterData().categoryBits;
        final int fixtureBCategory = contact.getFixtureB().getFilterData().categoryBits;

        if ((int) (fixtureACategory & PLAYER_BIT) == PLAYER_BIT) {
            player = (Entity) bodyA.getUserData();
        } else if ((int) (fixtureBCategory & PLAYER_BIT) == PLAYER_BIT) {
            player = (Entity) bodyB.getUserData();
        } else {
            return;
        }
        if ((int) (fixtureACategory & GAME_OBJECT_BIT) == GAME_OBJECT_BIT) {
            gameObject = (Entity) bodyA.getUserData();
        } else if ((int) (fixtureBCategory & GAME_OBJECT_BIT) == GAME_OBJECT_BIT) {
            gameObject = (Entity) bodyB.getUserData();
        } else {
            return;
        }
        for (final PlayerCollisionListener listener : listeners) {
            listener.playerCollision(player, gameObject);
        }
//        Gdx.app.debug("COLLDEBUG", "Player collides with GameObject");
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public interface PlayerCollisionListener {
        void playerCollision(final Entity player, final Entity gameObject);
    }
}
