package com.atoledano.producegame;

import com.atoledano.producegame.audio.AudioManager;
import com.atoledano.producegame.input.InputManager;
import com.atoledano.producegame.map.MapManager;
import com.atoledano.producegame.screens.AbstractScreen;
import com.atoledano.producegame.screens.ScreenType;
import com.atoledano.producegame.view.GameRenderer;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.atoledano.producegame.ecs.ECSEngine;

import java.util.EnumMap;

public class ProduceGame extends Game {

    private static final String TAG = ProduceGame.class.getSimpleName();
    public static final BodyDef BODY_DEF = new BodyDef();
    public static final FixtureDef FIXTURE_DEF = new FixtureDef();
    public static final float UNIT_SCALE = 1 / 32f;
    public static final Short PLAYER_BIT = 1 << 0;
    public static final Short CART_BIT = 1 << 1;
    public static final Short SAC_BIT = 1 << 2;
    public static final Short GAME_OBJECT_BIT = 1 << 3;
    public static final Short ROOM_BIT = 1 << 4;

    //fixing time step to make it more consistent in the simulations
    private static final float FIXED_TIME_STEP = 1 / 60f;
    private float accumulator;

    private SpriteBatch spriteBatch;
    private EnumMap<ScreenType, AbstractScreen> screenCache;
    private OrthographicCamera gameCamera;
    private FitViewport screenViewport;
    private WorldContactListener worldContactListener;
    private World world;
    private AssetManager assetManager;
    private AudioManager audioManager;
    private Stage stage;
    private Skin skin;
    private I18NBundle i18NBundle;
    private InputManager inputManager;
    private ECSEngine ecsEngine;
    private MapManager mapManager;
    private GameRenderer gameRenderer;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        spriteBatch = new SpriteBatch();

        //initializing box2d
        accumulator = 0;
        Box2D.init();
        //vector2 contains gravity values
        world = new World(Vector2.Zero, true);
        worldContactListener = new WorldContactListener();
        world.setContactListener(worldContactListener);

        //asset manager section
        assetManager = new AssetManager();
        //telling the asset manager how to load the tiled map
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(assetManager.getFileHandleResolver()));

        //skin section
        initializeSkin();

        //stage section
        stage = new Stage(new FitViewport(1024, 768), spriteBatch);

        //audio section
        audioManager = new AudioManager(this);

        //input section
        inputManager = new InputManager();
        //setting up for later input methods
        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager, stage));

        //viewport setup - com.atoledano.producegame.view size in tiles
        gameCamera = new OrthographicCamera();
        screenViewport = new FitViewport(12, 9, gameCamera);


        //ecs engine section
        ecsEngine = new ECSEngine(this);

        //map manager section
        mapManager = new MapManager(this);

        //game renderer section
        gameRenderer = new GameRenderer(this);

        //set first screen
        screenCache = new EnumMap<ScreenType, AbstractScreen>(ScreenType.class);
        setScreen(ScreenType.LOADING);
    }

    public static void resetBodyAndFixtureDefinition() {
        BODY_DEF.position.set(0, 0);
        BODY_DEF.gravityScale = 1;
        BODY_DEF.type = BodyDef.BodyType.StaticBody;
        BODY_DEF.fixedRotation = false;
        BODY_DEF.angle = 0;
        BODY_DEF.angularVelocity = 0;

        FIXTURE_DEF.density = 0;
        FIXTURE_DEF.isSensor = false;
        FIXTURE_DEF.restitution = 0;
        FIXTURE_DEF.friction = 0.2f;
        FIXTURE_DEF.filter.categoryBits = 0x0001;
        FIXTURE_DEF.filter.maskBits = -1;
        FIXTURE_DEF.shape = null;
    }

    private void initializeSkin() {
        //setup markup colors
        Colors.put("Red", Color.RED);
        Colors.put("Blue", Color.BLUE);

        //generate ttf bitmap
        final ObjectMap<String, Object> resources = new ObjectMap<String, Object>();
        final FreeTypeFontGenerator freeTypeFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("com/atoledano/producegame/view/pixelFont.ttf"));
        final FreeTypeFontGenerator.FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        //texture filter
        freeTypeFontParameter.minFilter = Texture.TextureFilter.Linear;
        freeTypeFontParameter.magFilter = Texture.TextureFilter.Linear;
        final int[] sizesToCreate = {16, 20, 26, 32};
        for (int size : sizesToCreate) {
            freeTypeFontParameter.size = size;
            final BitmapFont bitmapFont = freeTypeFontGenerator.generateFont(freeTypeFontParameter);
            bitmapFont.getData().markupEnabled = true;
            resources.put("font_" + size, bitmapFont);
        }
        freeTypeFontGenerator.dispose();

        //load skin
        final SkinLoader.SkinParameter skinParameter = new SkinLoader.SkinParameter("com/atoledano/producegame/view/hud.atlas", resources);
        assetManager.load("com/atoledano/producegame/view/hud.json", Skin.class, skinParameter);
        assetManager.load("com/atoledano/producegame/view/strings", I18NBundle.class);
        assetManager.finishLoading();
        skin = assetManager.get("com/atoledano/producegame/view/hud.json", Skin.class);
        i18NBundle = assetManager.get("com/atoledano/producegame/view/strings", I18NBundle.class);
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public ECSEngine getEcsEngine() {
        return ecsEngine;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public I18NBundle getI18NBundle() {
        return i18NBundle;
    }

    public Stage getStage() {
        return stage;
    }

    public Skin getSkin() {
        return skin;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public FitViewport getScreenViewport() {
        return screenViewport;
    }

    public WorldContactListener getWorldContactListener() {
        return worldContactListener;
    }

    public World getWorld() {
        return world;
    }

    public void setScreen(final ScreenType screenType) {
        final Screen screen = screenCache.get(screenType);
        if (screen == null) {
            //screen has to be created
            try {
                Gdx.app.debug(TAG, "Creating new screen: " + screenType);
                final AbstractScreen newScreen = (AbstractScreen) ClassReflection.getConstructor(screenType.getScreenClass(), ProduceGame.class).newInstance(this);
                screenCache.put(screenType, newScreen);
                setScreen(newScreen);
            } catch (ReflectionException e) {
                throw new GdxRuntimeException("Screen type " + screenType + " could not be created.", e);
            }
        } else {
            Gdx.app.debug(TAG, "Switching to screen: " + screenType);
            setScreen(screen);
        }
    }

    @Override
    public void render() {
        super.render();

        final float deltaTime = Math.min(0.25f, Gdx.graphics.getDeltaTime());
        ecsEngine.update(deltaTime);
        accumulator += deltaTime;
        while (accumulator >= FIXED_TIME_STEP) {
            world.step(FIXED_TIME_STEP, 6, 2);
            accumulator -= FIXED_TIME_STEP;
        }

        //interpolation setup
        gameRenderer.render(accumulator / FIXED_TIME_STEP);

        //setting up the stage
        stage.getViewport().apply();
        stage.act(deltaTime);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        gameRenderer.dispose();
        world.dispose();
        assetManager.dispose();
        spriteBatch.dispose();
        stage.dispose();
    }
}
