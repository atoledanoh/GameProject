package com.atoledano.producegame.entityComponentSystem;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.atoledano.producegame.entityComponentSystem.component.B2DComponent;
import com.atoledano.producegame.entityComponentSystem.component.PlayerComponent;
import com.atoledano.producegame.entityComponentSystem.system.PlayerCameraSystem;
import com.atoledano.producegame.entityComponentSystem.system.PlayerMovementSystem;

import static com.atoledano.producegame.ProduceGame.PLAYER_BIT;

public class ECSEngine extends PooledEngine {

    public static final ComponentMapper<PlayerComponent> playerComponentMapper = ComponentMapper.getFor(PlayerComponent.class);
    public static final ComponentMapper<B2DComponent> b2DComponentMapper = ComponentMapper.getFor(B2DComponent.class);

    private final World world;
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;

    public ECSEngine(final ProduceGame context) {
        super();

        world = context.getWorld();
        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();

        this.addSystem(new PlayerMovementSystem(context));
        this.addSystem(new PlayerCameraSystem(context));
    }

    private void resetBodyAndFixtureDefinitions() {
        bodyDef.position.set(0, 0);
        bodyDef.gravityScale = 1;
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = false;

        fixtureDef.density = 0;
        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = 0x0001;
        fixtureDef.filter.maskBits = -1;
        fixtureDef.shape = null;
    }

    public void createPlayer(final Vector2 playerSpawnLocation, final float width, final float height) {
        final Entity player = this.createEntity();

        //player component
        final PlayerComponent playerComponent = this.createComponent(PlayerComponent.class);
        playerComponent.speed.set(3, 3);
        player.add(playerComponent);

        //box2d component
        resetBodyAndFixtureDefinitions();
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);
        bodyDef.position.set(playerSpawnLocation.x + 0.5f, playerSpawnLocation.y + height * 0.5f);
        bodyDef.fixedRotation = true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2DComponent.body = world.createBody(bodyDef);
        b2DComponent.body.setUserData("PLAYER");
        b2DComponent.width = width;
        b2DComponent.height = height;
        fixtureDef.filter.categoryBits = PLAYER_BIT;
        fixtureDef.filter.maskBits = -1;
        final PolygonShape polygonShape = new PolygonShape();
        //todo double check size
        polygonShape.setAsBox(0.45f, 0.45f);
        fixtureDef.shape = polygonShape;
        b2DComponent.body.createFixture(fixtureDef);
        polygonShape.dispose();

        player.add(b2DComponent);
        this.addEntity(player);
    }
}