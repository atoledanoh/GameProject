package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.input.GameKeys;
import com.atoledano.producegame.input.InputManager;
import com.atoledano.producegame.map.CollisionArea;
import com.atoledano.producegame.map.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.ScreenUtils;
import ui.GameUI;

import static com.atoledano.producegame.ProduceGame.ROOM_BIT;
import static com.atoledano.producegame.ProduceGame.UNIT_SCALE;

public class GameScreen extends AbstractScreen {
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
        glProfiler.disable();

        //getting maps ready
        final TiledMap tiledMap = assetManager.get("map/map.tmx", TiledMap.class);
        mapRenderer.setMap(tiledMap);
        map = new Map(tiledMap);

        spawnCollisionAreas();

        //create player
        context.getEcsEngine().createPlayer(map.getStartLocation(), 1, 1);
    }

    @Override
    protected GameUI getScreenUI(final ProduceGame context) {
        return new GameUI(context);
    }

    private void spawnCollisionAreas() {
        final BodyDef bodyDef = new BodyDef();
        final FixtureDef fixtureDef = new FixtureDef();
        for (final CollisionArea collisionArea : map.getCollisionAreas()) {

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
        if (glProfiler.isEnabled()) {
            Gdx.app.debug("RenderInfo", "No. of Bindings: " + glProfiler.getTextureBindings());
            Gdx.app.debug("RenderInfo", "No. of DrawCalls: " + glProfiler.getDrawCalls());
            glProfiler.reset();
        }
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

    @Override
    public void keyPressed(InputManager inputManager, GameKeys key) {

    }

    @Override
    public void keyUp(InputManager inputManager, GameKeys key) {

    }
}
