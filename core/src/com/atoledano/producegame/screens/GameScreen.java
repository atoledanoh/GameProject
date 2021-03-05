package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.map.CollisionArea;
import com.atoledano.producegame.map.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import ui.GameUI;

import static com.atoledano.producegame.ProduceGame.*;

public class GameScreen extends AbstractScreen {

    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    private Body player;
//    private final Body cart;
//    private final Body sac;

    private final AssetManager assetManager;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final OrthographicCamera gameCamera;
    private final GLProfiler glProfiler;
    private Map map;

    public GameScreen(final ProduceGame context) {
        super(context);

        assetManager = context.getAssetManager();
        //initializing the map and telling the size of the units to box2d
        mapRenderer = new OrthogonalTiledMapRenderer(null, UNIT_SCALE, context.getSpriteBatch());
        this.gameCamera = context.getGameCamera();

        //initializing profiler to each for the number of texture bindings happening
        glProfiler = new GLProfiler(Gdx.graphics);
        glProfiler.enable();

        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();

        //getting maps ready
        final TiledMap tiledMap = assetManager.get("map/map.tmx", TiledMap.class);
        mapRenderer.setMap(tiledMap);
        map = new Map(tiledMap);

        spawnCollisionAreas();
        spawnPlayer();
    }

    @Override
    protected Table getScreenUI(final ProduceGame context) {
        return new GameUI(context);
    }

    private void spawnPlayer() {
        resetBodyAndFixtureDefinitions();

        //create player
        //todo double check size
        bodyDef.position.set(map.getStartLocation().x + 0.5f, map.getStartLocation().y + 0.5f);
        bodyDef.fixedRotation = true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        player = world.createBody(bodyDef);
        player.setUserData("PLAYER");

        fixtureDef.density = 1;
        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = PLAYER_BIT;
        fixtureDef.filter.maskBits = -1;
        final PolygonShape polygonShape = new PolygonShape();
        //todo double check size
        polygonShape.setAsBox(0.45f, 0.45f);
        fixtureDef.shape = polygonShape;
        player.createFixture(fixtureDef);
        polygonShape.dispose();
    }

    private void resetBodyAndFixtureDefinitions() {
        bodyDef.position.set(0, 0);
        bodyDef.gravityScale = 1;
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = false;

        fixtureDef.density = 1;
        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0.1f;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = 0x0001;
        fixtureDef.filter.maskBits = -1;
        fixtureDef.shape = null;
    }

    private void spawnCollisionAreas() {
        for (final CollisionArea collisionArea : map.getCollisionAreas()) {
            resetBodyAndFixtureDefinitions();

            //create room
            bodyDef.position.set(collisionArea.getX(), collisionArea.getY());
            bodyDef.fixedRotation = true;
            final Body room = world.createBody(bodyDef);
            room.setUserData("ROOM");

            fixtureDef.filter.categoryBits = ROOM_BIT;
            fixtureDef.filter.maskBits = -1; //collides with everything
            final ChainShape chainShape = new ChainShape();
            chainShape.createChain(collisionArea.getVertices());
            fixtureDef.shape = chainShape;
            room.createFixture(fixtureDef);
            chainShape.dispose();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

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

//        //cart properties
//        cart.setLinearVelocity(
//                (cart.getLinearVelocity().x - cart.getLinearVelocity().x * 0.05f),
//                (cart.getLinearVelocity().y - cart.getLinearVelocity().y * 0.05f));
//        cart.setAngularVelocity(0f);


        viewport.apply(true);
        mapRenderer.setView(gameCamera);
        mapRenderer.render();
        box2DDebugRenderer.render(world, viewport.getCamera().combined);

        //showing the number of bindings happening
//        Gdx.app.debug("RenderInfo", "No. of Bindings: " + glProfiler.getTextureBindings());
//        Gdx.app.debug("RenderInfo", "No. of DrawCalls: " + glProfiler.getDrawCalls());
        glProfiler.reset();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
    }
}
