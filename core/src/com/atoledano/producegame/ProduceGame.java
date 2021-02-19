package com.atoledano.producegame;

import com.atoledano.producegame.screens.GameScreen;
import com.atoledano.producegame.screens.LoadingScreen;
import com.atoledano.producegame.screens.ScreenType;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.EnumMap;

public class ProduceGame extends Game {

    private static final String TAG = ProduceGame.class.getSimpleName();
    private EnumMap<ScreenType, Screen> screenCache;
    private FitViewport screenViewport;
    public static final Short PLAYER_BIT = 1 << 0;
    public static final Short CART_BIT = 1 << 1;
    public static final Short SAC_BIT = 1 << 3;
    public static final Short ROOM_BIT = 1 << 4;

    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;
    //fixing time step to make it more consistent in the simulations
    private static final float FIXED_TIME_STEP = 1 / 60f;
    private float accumulator;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        accumulator = 0;

        Box2D.init();
        //vector2 contains gravity values
        world = new World(new Vector2(0, 0), true);
        box2DDebugRenderer = new Box2DDebugRenderer();

        screenViewport = new FitViewport(9, 16);
        screenCache = new EnumMap<ScreenType, Screen>(ScreenType.class);
        setScreen(ScreenType.GAME);
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
    }

    @Override
    public void dispose() {
        super.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
    }
}
