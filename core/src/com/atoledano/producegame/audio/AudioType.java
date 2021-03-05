package com.atoledano.producegame.audio;

public enum AudioType {
    INTRO("audio/Caketown.mp3", true, 0.3f),
    BACKGROUND1("audio/floor.mp3", true, 0.3f),
    SELECT("audio/bellding.wav", false, 0.5f);

    private final String filePath;
    private final boolean isMusic;
    private final float volume;

    AudioType(String filePath, boolean isMusic, float volume) {
        this.filePath = filePath;
        this.isMusic = isMusic;
        this.volume = volume;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isMusic() {
        return isMusic;
    }

    public float getVolume() {
        return volume;
    }
}
