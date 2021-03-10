package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.audio.AudioType;
import com.atoledano.producegame.input.GameKeys;
import com.atoledano.producegame.input.InputManager;
import com.atoledano.producegame.map.Map;
import com.atoledano.producegame.map.MapListener;
import com.atoledano.producegame.map.MapManager;
import com.atoledano.producegame.map.MapType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.atoledano.producegame.view.GameUI;

public class GameScreen extends AbstractScreen<GameUI> implements MapListener {
    private final MapManager mapManager;
    private boolean isMusicLoaded;

    public GameScreen(final ProduceGame context) {
        super(context);

        mapManager = context.getMapManager();
        mapManager.addMapListener(this);
        mapManager.setMap(MapType.MAP_1);

        //create player
        context.getEcsEngine().createPlayer(mapManager.getCurrentMap().getStartLocation(), 1, 1);

        //loading audio
        isMusicLoaded = false;
        for (final AudioType audioType : AudioType.values()) {
            context.getAssetManager().load(audioType.getFilePath(), audioType.isMusic() ? Music.class : Sound.class);
        }
    }

    @Override
    protected GameUI getScreenUI(final ProduceGame context) {
        return new GameUI(context);
    }

    @Override
    public void render(final float delta) {

//        //cart properties
//        cart.setLinearVelocity(
//                (cart.getLinearVelocity().x - cart.getLinearVelocity().x * 0.05f),
//                (cart.getLinearVelocity().y - cart.getLinearVelocity().y * 0.05f));
//        cart.setAngularVelocity(0f);

        //todo remove map change test stuff
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            mapManager.setMap(MapType.MAP_1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            mapManager.setMap(MapType.MAP_2);
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
    }

    @Override
    public void keyPressed(InputManager inputManager, GameKeys key) {
    }

    @Override
    public void keyUp(InputManager inputManager, GameKeys key) {
    }

    @Override
    public void mapChange(Map map) {
    }
}
