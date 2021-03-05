package com.atoledano.producegame;

import com.atoledano.producegame.audio.AudioManager;
import com.atoledano.producegame.input.InputManager;
import com.atoledano.producegame.screens.ScreenType;
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
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.EnumMap;

public class ProduceGame extends Game {

    private static final String TAG = ProduceGame.class.getSimpleName();

    private SpriteBatch spriteBatch;
    private EnumMap<ScreenType, Screen> screenCache;
    private OrthographicCamera gameCamera;
    private FitViewport screenViewport;
    public static final float UNIT_SCALE = 1 / 32f;
    public static final Short PLAYER_BIT = 1 << 0;
    public static final Short CART_BIT = 1 << 1;
    public static final Short SAC_BIT = 1 << 2;
    public static final Short ROOM_BIT = 1 << 3;

    private WorldContactListener worldContactListener;
    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;
    //fixing time step to make it more consistent in the simulations
    private static final float FIXED_TIME_STEP = 1 / 60f;
    private float accumulator;

    private AssetManager assetManager;
    private AudioManager audioManager;

    private Stage stage;
    private Skin skin;
    private I18NBundle i18NBundle;

    private InputManager inputManager;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        spriteBatch = new SpriteBatch();

        //initializing box2d
        accumulator = 0;
        Box2D.init();
        //vector2 contains gravity values
        world = new World(new Vector2(0, 0), true);
        worldContactListener = new WorldContactListener();
        world.setContactListener(worldContactListener);
        box2DDebugRenderer = new Box2DDebugRenderer();

        //asset manager
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

        //set initial screen
        gameCamera = new OrthographicCamera();
        screenViewport = new FitViewport(42, 28, gameCamera);
        screenCache = new EnumMap<ScreenType, Screen>(ScreenType.class);
        setScreen(ScreenType.LOADING);
    }

    private void initializeSkin() {
        //setup markup colors
        Colors.put("Red", Color.RED);
        Colors.put("Blue", Color.BLUE);

        //generate ttf bitmap
        final ObjectMap<String, Object> resources = new ObjectMap<String, Object>();
        final FreeTypeFontGenerator freeTypeFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("ui/pixelFont.ttf"));
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
        final SkinLoader.SkinParameter skinParameter = new SkinLoader.SkinParameter("ui/hud.atlas", resources);
        assetManager.load("ui/hud.json", Skin.class, skinParameter);
        assetManager.load("ui/strings", I18NBundle.class);
        assetManager.finishLoading();
        skin = assetManager.get("ui/hud.json", Skin.class);
        i18NBundle = assetManager.get("ui/strings", I18NBundle.class);
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

    public World getWorld() {
        return world;
    }

    public Box2DDebugRenderer getBox2DDebugRenderer() {
        return box2DDebugRenderer;
    }

    public void setScreen(final ScreenType screenType) {
        final Screen screen = screenCache.get(screenType);
        if (screen == null) {
            //screen has to be created
            try {
                Gdx.app.debug(TAG, "Creating new screen: " + screenType);
                final Object newScreen = ClassReflection.getConstructor(screenType.getScreenClass(), ProduceGame.class).newInstance(this);
                screenCache.put(screenType, (Screen) newScreen);
                setScreen((Screen) newScreen);
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

        //fixing time step to make it more consistent in the simulations
        accumulator += Math.min(0.25f, Gdx.graphics.getDeltaTime());
        while (accumulator >= FIXED_TIME_STEP) {
            world.step(FIXED_TIME_STEP, 6, 2);
            accumulator -= FIXED_TIME_STEP;
        }

        //preparing interpolation
        //final float alpha = accumulator / FIXED_TIME_STEP;

        //setting up the stage
        stage.getViewport().apply();
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
        assetManager.dispose();
        spriteBatch.dispose();
        stage.dispose();
    }
}
