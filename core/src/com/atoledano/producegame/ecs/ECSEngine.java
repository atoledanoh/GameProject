package com.atoledano.producegame.ecs;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.ecs.component.AnimationComponent;
import com.atoledano.producegame.ecs.component.B2DComponent;
import com.atoledano.producegame.ecs.component.GameObjectComponent;
import com.atoledano.producegame.ecs.component.PlayerComponent;
import com.atoledano.producegame.ecs.system.*;
import com.atoledano.producegame.map.GameObject;
import com.atoledano.producegame.view.AnimationType;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.atoledano.producegame.ProduceGame.GAME_OBJECT_BIT;
import static com.atoledano.producegame.ProduceGame.PLAYER_BIT;

public class ECSEngine extends PooledEngine {

    public static final ComponentMapper<PlayerComponent> playerComponentMapper = ComponentMapper.getFor(PlayerComponent.class);
    public static final ComponentMapper<B2DComponent> b2DComponentMapper = ComponentMapper.getFor(B2DComponent.class);
    public static final ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
    public static final ComponentMapper<GameObjectComponent> gameObjectComponentMapper = ComponentMapper.getFor(GameObjectComponent.class);

    private final World world;
    private final Vector2 localPosition;
    private final Vector2 positionBeforeRotation;
    private final Vector2 positionAfterRotation;

    public ECSEngine(final ProduceGame context) {
        super();

        world = context.getWorld();
        localPosition = new Vector2();
        positionBeforeRotation = new Vector2();
        positionAfterRotation = new Vector2();

        this.addSystem(new PlayerMovementSystem(context));
        this.addSystem(new PlayerCameraSystem(context));
        this.addSystem(new AnimationSystem(context));
        this.addSystem(new PlayerAnimationSystem(context));
        this.addSystem(new PlayerCollisionSystem(context));
    }

    public void createPlayer(final Vector2 playerSpawnLocation, final float width, final float height) {
        final Entity player = this.createEntity();

        //player component
        final PlayerComponent playerComponent = this.createComponent(PlayerComponent.class);
        playerComponent.speed.set(3, 3);
        player.add(playerComponent);

        //box2d component
        ProduceGame.resetBodyAndFixtureDefinition();
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);
        ProduceGame.BODY_DEF.position.set(playerSpawnLocation.x, playerSpawnLocation.y + height * 0.5f);
        ProduceGame.BODY_DEF.fixedRotation = true;
        ProduceGame.BODY_DEF.type = BodyDef.BodyType.DynamicBody;
        b2DComponent.body = world.createBody(ProduceGame.BODY_DEF);
        b2DComponent.body.setUserData(player);
        b2DComponent.width = width;
        b2DComponent.height = height;
        b2DComponent.renderPosition.set(b2DComponent.body.getPosition());

        ProduceGame.FIXTURE_DEF.filter.categoryBits = PLAYER_BIT;
        ProduceGame.FIXTURE_DEF.filter.maskBits = -1;
        final PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(width * 0.45f, height * 0.45f);
        ProduceGame.FIXTURE_DEF.shape = polygonShape;
        b2DComponent.body.createFixture(ProduceGame.FIXTURE_DEF);
        polygonShape.dispose();
        player.add(b2DComponent);

        //animation component
        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = AnimationType.PLAYER_MOVE_DOWN;
        animationComponent.width = 64 * ProduceGame.UNIT_SCALE * 0.75f;
        animationComponent.height = 64 * ProduceGame.UNIT_SCALE * 0.75f;
        player.add(animationComponent);

        this.addEntity(player);
    }

    public void createGameObject(final GameObject gameObject) {
        final Entity gameObjectEntity = this.createEntity();

        //GameObject component
        final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);
        gameObjectComponent.animationIndex = gameObject.getAnimationIndex();
        gameObjectComponent.gameObjectType = gameObject.getGameObjectType();
        gameObjectEntity.add(gameObjectComponent);

        //box2d component
        ProduceGame.resetBodyAndFixtureDefinition();
        final float halfWidth = gameObject.getWidth() * 0.5f;
        final float halfHeight = gameObject.getHeight() * 0.5f;
        final float angleRad = -gameObject.getRotationDegree() * MathUtils.degreesToRadians;
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);
        ProduceGame.BODY_DEF.type = BodyDef.BodyType.StaticBody;
        ProduceGame.BODY_DEF.position.set(gameObject.getPosition().x + halfWidth, gameObject.getPosition().y + halfHeight);
        b2DComponent.body = world.createBody(ProduceGame.BODY_DEF);
        b2DComponent.body.setUserData(gameObjectEntity);
        b2DComponent.width = gameObject.getWidth();
        b2DComponent.height = gameObject.getHeight();

        //animation component
        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = null;
        animationComponent.width = gameObject.getWidth();
        animationComponent.height = gameObject.getHeight();
        gameObjectEntity.add(animationComponent);

        //save position before rotation (rotates on lower left corner)
        localPosition.set(-halfWidth, -halfHeight);
        positionBeforeRotation.set(b2DComponent.body.getWorldPoint(localPosition));
        b2DComponent.body.setTransform(b2DComponent.body.getPosition(), angleRad);
        positionAfterRotation.set(b2DComponent.body.getWorldPoint(localPosition));
        b2DComponent.body.setTransform(b2DComponent.body.getPosition().add(positionBeforeRotation).sub(positionAfterRotation), angleRad);
        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - animationComponent.width * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);

        ProduceGame.FIXTURE_DEF.filter.categoryBits = GAME_OBJECT_BIT;
        ProduceGame.FIXTURE_DEF.filter.maskBits = PLAYER_BIT;
        final PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(halfWidth, halfHeight);
        ProduceGame.FIXTURE_DEF.shape = polygonShape;
        b2DComponent.body.createFixture(ProduceGame.FIXTURE_DEF);
        polygonShape.dispose();
        gameObjectEntity.add(b2DComponent);

        this.addEntity(gameObjectEntity);
    }
}