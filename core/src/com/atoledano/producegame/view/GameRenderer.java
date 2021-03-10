package com.atoledano.producegame.view;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.map.Map;
import com.atoledano.producegame.map.MapListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.atoledano.producegame.ProduceGame.UNIT_SCALE;

public class GameRenderer implements Disposable, MapListener {
    public static final String TAG = GameRenderer.class.getSimpleName();

    private final OrthographicCamera gameCamera;
    private final FitViewport fitViewport;
    private final SpriteBatch spriteBatch;
    private final AssetManager assetManager;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final GLProfiler glProfiler;
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final World world;

    public GameRenderer(final ProduceGame context) {
        assetManager = context.getAssetManager();
        fitViewport = context.getScreenViewport();
        gameCamera = context.getGameCamera();
        spriteBatch = context.getSpriteBatch();

        mapRenderer = new OrthogonalTiledMapRenderer(null, UNIT_SCALE, spriteBatch);
        context.getMapManager().addMapListener(this);

        glProfiler = new GLProfiler(Gdx.graphics);
        glProfiler.enable();
        if (glProfiler.isEnabled()) {
            box2DDebugRenderer = new Box2DDebugRenderer();
            world = context.getWorld();
        } else {
            box2DDebugRenderer = null;
            world = null;
        }
    }

    public void render(final float alpha) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fitViewport.apply(false);
        if (mapRenderer.getMap() != null) {
            mapRenderer.setView(gameCamera);
            mapRenderer.render();
        }
        if (glProfiler.isEnabled()) {
            Gdx.app.debug(TAG, "Bindings: " + glProfiler.getTextureBindings());
            Gdx.app.debug(TAG, "Drawcalls: " + glProfiler.getDrawCalls());
            glProfiler.reset();
            box2DDebugRenderer.render(world, gameCamera.combined);
        }
    }

    @Override
    public void dispose() {
        if (box2DDebugRenderer != null) {
            box2DDebugRenderer.dispose();
        }
        mapRenderer.dispose();
    }

    @Override
    public void mapChange(final Map map) {
        mapRenderer.setMap(map.getTiledMap());
    }
}
