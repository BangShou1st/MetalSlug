package cn.edu.scnu.controller;

/**
 * Swing 事件分派线程与游戏逻辑线程之间传递的不可变按键事件。
 */
public final class GameKeyEvent {
    private final int keyCode;
    private final boolean pressed;

    public GameKeyEvent(int keyCode, boolean pressed) {
        this.keyCode = keyCode;
        this.pressed = pressed;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isPressed() {
        return pressed;
    }
}
