package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;

import static com.atoledano.producegame.ProduceGame.*;

public class GameScreen extends AbstractScreen {

    private final Body player;
    private final Body cart;
    private final Body sac;


    public GameScreen(final ProduceGame context) {
        super(context);

        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();

        //create player
        bodyDef.position.set(4.5f, 3);
        bodyDef.gravityScale = 1;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        player = world.createBody(bodyDef);
        player.setUserData("Player");

        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = PLAYER_BIT;
        fixtureDef.filter.maskBits = -1;
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(0.5f, 0.5f);
        fixtureDef.shape = polygonShape;
        player.createFixture(fixtureDef);
        polygonShape.dispose();

        //create cart
        bodyDef.position.set(4.5f, 6);
        bodyDef.gravityScale = 1;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        cart = world.createBody(bodyDef);
        cart.setUserData("Cart");

        fixtureDef.density = 1;
        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0.1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = CART_BIT;
        fixtureDef.filter.maskBits = -1;
        polygonShape = new PolygonShape();
        polygonShape.setAsBox(0.5f, 0.5f);
        fixtureDef.shape = polygonShape;
        cart.createFixture(fixtureDef);
        polygonShape.dispose();

        //create sacs
        bodyDef.position.set(2.5f, 6);
        bodyDef.gravityScale = 0.1f;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        sac = world.createBody(bodyDef);
        sac.setUserData("Sac");

        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0;
        fixtureDef.filter.categoryBits = SAC_BIT;
        fixtureDef.filter.maskBits = -1;
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(0.5f);
        fixtureDef.shape = circleShape;
        sac.createFixture(fixtureDef);
        circleShape.dispose();

        //create room
        bodyDef.position.set(0, 0);
        bodyDef.gravityScale = 1;
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body room = world.createBody(bodyDef);
        cart.setUserData("Room");

        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = ROOM_BIT;
        fixtureDef.filter.maskBits = -1;
        final ChainShape chainShape = new ChainShape();
        chainShape.createLoop(new float[]{1, 1, 1, 15, 8, 15, 8, 1});
        fixtureDef.shape = chainShape;
        room.createFixture(fixtureDef);
        chainShape.dispose();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        //pause/loading screen
        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            context.setScreen(ScreenType.LOADING);
        }

        //temporal player movement
        final float speedX;
        final float speedY;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            speedX = -4;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            speedX = 4;
        } else {
            speedX = 0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            speedY = -4;
        } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            speedY = 4;
        } else {
            speedY = 0;
        }
        //appy the force to move
        player.applyLinearImpulse(
                (speedX - player.getLinearVelocity().x) * player.getMass(),
                (speedY - player.getLinearVelocity().y) * player.getMass(),
                player.getWorldCenter().x,
                player.getWorldCenter().y,
                true
        );

        //cart properties
        cart.setLinearVelocity(
                (cart.getLinearVelocity().x - cart.getLinearVelocity().x*0.05f),
                (cart.getLinearVelocity().y - cart.getLinearVelocity().y*0.05f));
        cart.setAngularVelocity(0f);

        //sac properties




        viewport.apply(true);
        box2DDebugRenderer.render(world, viewport.getCamera().combined);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
