package com.atoledano.producegame.view;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.ecs.ECSEngine;
import com.atoledano.producegame.ecs.component.AnimationComponent;
import com.atoledano.producegame.ecs.component.B2DComponent;
import com.atoledano.producegame.ecs.component.GameObjectComponent;
import com.atoledano.producegame.map.Map;
import com.atoledano.producegame.map.MapListener;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.EnumMap;

import static com.atoledano.producegame.ProduceGame.UNIT_SCALE;

public class GameRenderer implements Disposable, MapListener {
    public static final String TAG = GameRenderer.class.getSimpleName();

    private final OrthographicCamera gameCamera;
    private final FitViewport fitViewport;
    private final SpriteBatch spriteBatch;
    private final AssetManager assetManager;
    private final EnumMap<AnimationType, Animation<Sprite>> animationCache;
    private final ObjectMap<String, TextureRegion[][]> regionCache;
    private final ImmutableArray<Entity> gameObjectEntities;
    private final ImmutableArray<Entity> animatedEntities;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Array<TiledMapTileLayer> tiledMapTileLayers;
    private final GLProfiler glProfiler;
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final World world;

    private IntMap<Animation<Sprite>> animationIntMap;

//    private Sprite dummySprite;

    public GameRenderer(final ProduceGame context) {
        assetManager = context.getAssetManager();
        fitViewport = context.getScreenViewport();
        gameCamera = context.getGameCamera();
        spriteBatch = context.getSpriteBatch();
        animationCache = new EnumMap<AnimationType, Animation<Sprite>>(AnimationType.class);
        regionCache = new ObjectMap<String, TextureRegion[][]>();

        gameObjectEntities = context.getEcsEngine().getEntitiesFor(Family.all(GameObjectComponent.class, B2DComponent.class, AnimationComponent.class).get());
        animatedEntities = context.getEcsEngine().getEntitiesFor(Family.all(AnimationComponent.class, B2DComponent.class).exclude(GameObjectComponent.class).get());

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
        mapRenderer.setView(gameCamera);
        spriteBatch.begin();

        if (mapRenderer.getMap() != null) {
            AnimatedTiledMapTile.updateAnimationBaseTime();
            for (final TiledMapTileLayer tiledMapTileLayer : tiledMapTileLayers) {
                mapRenderer.renderTileLayer(tiledMapTileLayer);
            }
        }
        for (final Entity entity : gameObjectEntities) {
            renderGameObject(entity, alpha);
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

    private void renderGameObject(final Entity entity, final float alpha) {
        final B2DComponent b2DComponent = ECSEngine.b2DComponentMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        final GameObjectComponent gameObjectComponent = ECSEngine.gameObjectComponentMapper.get(entity);

        if (gameObjectComponent.animationIndex != -1) {
            final Animation<Sprite> spriteAnimation = animationIntMap.get(gameObjectComponent.animationIndex);
            final Sprite keyFrame = spriteAnimation.getKeyFrame(animationComponent.animationTime);
            keyFrame.setBounds(b2DComponent.renderPosition.x, b2DComponent.renderPosition.y, animationComponent.width, animationComponent.height);
            keyFrame.setOriginCenter();
            keyFrame.setRotation(b2DComponent.body.getAngle() * MathUtils.radDeg);
            keyFrame.draw(spriteBatch);
        }
    }

    private void renderEntity(final Entity entity, final float alpha) {
        final B2DComponent b2DComponent = ECSEngine.b2DComponentMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);

        if (animationComponent.animationType != null) {
            final Animation<Sprite> animation = getAnimation(animationComponent.animationType);
            final Sprite keyFrame = animation.getKeyFrame(animationComponent.animationTime);
            b2DComponent.renderPosition.lerp(b2DComponent.body.getPosition(), alpha);
            keyFrame.setBounds(b2DComponent.renderPosition.x - animationComponent.width * 0.5f, b2DComponent.renderPosition.y - b2DComponent.height * 0.5f, animationComponent.width, animationComponent.height);
            keyFrame.draw(spriteBatch);
        }
        b2DComponent.renderPosition.lerp(b2DComponent.body.getPosition(), alpha);
    }

    private Animation<Sprite> getAnimation(final AnimationType animationType) {
        Animation<Sprite> spriteAnimation = animationCache.get(animationType);
        if (spriteAnimation == null) {
            //create animation
            Gdx.app.debug(TAG, "Creating new animation of type: " + animationType);
            TextureRegion[][] textureRegions = regionCache.get(animationType.getAtlasKey());
            if (textureRegions == null) {
                Gdx.app.debug(TAG, "Creating new texture regions for: " + animationType.getAtlasKey());
                final TextureAtlas.AtlasRegion atlasRegion = assetManager.get(animationType.getAtlasPath(), TextureAtlas.class).findRegion(animationType.getAtlasKey());
                textureRegions = atlasRegion.split(64, 64);
                regionCache.put(animationType.getAtlasKey(), textureRegions);
            }
            spriteAnimation = new Animation<Sprite>(animationType.getFrameTime(), getKeyFrames(textureRegions[animationType.getRowIndex()]));
            spriteAnimation.setPlayMode(Animation.PlayMode.LOOP);
            animationCache.put(animationType, spriteAnimation);
        }
        return spriteAnimation;
    }

    private Sprite[] getKeyFrames(final TextureRegion[] textureRegion) {
        final Sprite[] keyFrames = new Sprite[textureRegion.length];

        int i = 0;
        for (final TextureRegion region : textureRegion) {
            final Sprite sprite = new Sprite(region);
            sprite.setOriginCenter();
            keyFrames[i++] = sprite;
        }
        return keyFrames;
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
        animationIntMap = map.getMapAnimations();
    }
}
