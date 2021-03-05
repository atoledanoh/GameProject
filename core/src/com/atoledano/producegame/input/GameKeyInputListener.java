package com.atoledano.producegame.input;

public interface GameKeyInputListener {
    void keyPressed(final InputManager inputManager, final GameKeys key);
    void keyUp(final InputManager inputManager, final GameKeys key);}
