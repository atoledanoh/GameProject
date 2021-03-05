package com.atoledano.producegame.audio;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    private AudioType currentAudioType;
    private Music currentMusic;
    private final AssetManager assetManager;

    public AudioManager(final ProduceGame context) {
        this.assetManager = context.getAssetManager();
        currentMusic = null;
        currentAudioType = null;
    }

    public void playAudio(final AudioType audioType) {
        if (audioType.isMusic()) {
            //play music
            if (currentAudioType == audioType) {
                return;
            } else if (currentMusic != null) {
                currentMusic.stop();
            }
            currentAudioType = audioType;
            currentMusic = assetManager.get(audioType.getFilePath(), Music.class);
            currentMusic.setLooping(true);
            currentMusic.setVolume(audioType.getVolume());
            currentMusic.play();
        } else {
            //play sound
            assetManager.get(audioType.getFilePath(), Sound.class).play(audioType.getVolume());
        }
    }

    public void stopAudio() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
            currentAudioType = null;
        }
    }
}