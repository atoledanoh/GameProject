package com.atoledano.producegame.view;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.ecs.ECSEngine;
import com.atoledano.producegame.ecs.component.AnimationComponent;
import com.atoledano.producegame.ecs.component.B2DComponent;
import com.atoledano.producegame.map.Map;
import com.atoledano.producegame.map.MapListener;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.atoledano.producegame.ProduceGame.UNIT_SCALE;

public class GameRenderer implements Disposable, MapListener {
    public static final String TAG = GameRenderer.class.getSimpleName();

    private final OrthographicCamera gameCamera;
    private final FitViewport fitViewport;
    private final SpriteBatch spriteBatch;
    private final AssetManager assetManager;
    private final ImmutableArray<Entity> animatedEntities;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Array<TiledMapTileLayer> tiledMapTileLayers;
    private final GLProfiler glProfiler;
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final World world;

    private Sprite dummySprite;

    public GameRenderer(final ProduceGame context) {
        assetManager = context.getAssetManager();
        fitViewport = context.getScreenViewport();
        gameCamera = context.getGameCamera();
        spriteBatch = context.getSpriteBatch();

        animatedEntities = context.getEcsEngine().getEntitiesFor(Family.all(AnimationComponent.class, B2DComponent.class).get());

        mapRenderer = new OrthogonalTiledMapRenderer(null, UNIT_SCALE, spriteBatch);
        context.getMapManager().addMapListener(this);
        tiledMapTileLayers = new Array<TiledMapTileLayer>();

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

        spriteBatch.begin();

        if (mapRenderer.getMap() != null) {
            mapRenderer.setView(gameCamera);
            for (final TiledMapTileLayer tiledMapTileLayer : tiledMapTileLayers) {
                mapRenderer.renderTileLayer(tiledMapTileLayer);
            }
        }
        for (final Entity entity : animatedEntities) {
            renderEntity(entity, alpha);
        }
        spriteBatch.end();

        if (glProfiler.isEnabled()) {
            Gdx.app.debug(TAG, "Bindings: " + glProfiler.getTextureBindings());
            Gdx.app.debug(TAG, "Drawcalls: " + glProfiler.getDrawCalls());
            glProfiler.reset();
            box2DDebugRenderer.render(world, gameCamera.combined);
        }
    }

    private void renderEntity(Entity entity, float alpha) {
        final B2DComponent b2DComponent = ECSEngine.b2DComponentMapper.get(entity);
        b2DComponent.renderPosition.lerp(b2DComponent.body.getPosition(), alpha);
        dummySprite.setBounds(b2DComponent.renderPosition.x - b2DComponent.width * 0.5f, b2DComponent.renderPosition.y - b2DComponent.height * 0.5f, b2DComponent.width, b2DComponent.height);
        dummySprite.draw(spriteBatch);
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
        map.getTiledMap().getLayers().getByType(TiledMapTileLayer.class, tiledMapTileLayers);

        if (dummySprite == null) {
            dummySprite = assetManager.get("characters/player.atlas", TextureAtlas.class).createSprite("104");
            dummySprite.setOriginCenter();
        }
    }
}
