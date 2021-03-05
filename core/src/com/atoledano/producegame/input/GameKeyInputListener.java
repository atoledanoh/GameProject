package com.atoledano.producegame.input;

public interface InputListener {
    void keyPressed(final InputManager inputManager, final GameKeys key);
    void keyUp(final InputManager inputManager, final GameKeys key);}
