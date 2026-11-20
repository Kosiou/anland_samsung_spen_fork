package com.anland.consumer;

/** Converts hovering and contacting stylus samples into the existing mouse input API. */
final class StylusMouseInput {
    static final int BUTTON_LEFT = 0x110;
    static final int BUTTON_RIGHT = 0x111;
    interface Sender {
        void move(float x, float y);
        void button(int button, boolean pressed);
        void scroll(int axis, float value);
    }

    private final Sender sender;
    private int pressedButton;
    private float lastX;
    private float lastY;
    private boolean hasLastPosition;

    StylusMouseInput(Sender sender) {
        this.sender = sender;
    }

    void update(float x, float y, boolean inProximity, boolean tipDown,
                boolean sideButton, boolean rightClickEnabled,
                boolean hoverScrollEnabled, float scrollSensitivity) {
        if (!inProximity) {
            cancel();
            return;
        }
        if (!Float.isFinite(x) || !Float.isFinite(y))
            return;

        if (!hasLastPosition) {
            lastX = x;
            lastY = y;
            hasLastPosition = true;
        }
        float dx = x - lastX;
        float dy = y - lastY;
        lastX = x;
        lastY = y;

        boolean scrolling = !tipDown && sideButton && hoverScrollEnabled;
        if (scrolling) {
            if (dx != 0f)
                sender.scroll(1, dx * scrollSensitivity);
            if (dy != 0f)
                sender.scroll(0, -dy * scrollSensitivity);
        } else {
            sender.move(x, y);
        }

        int wantedButton = inProximity && tipDown
                ? (sideButton && rightClickEnabled ? BUTTON_RIGHT : BUTTON_LEFT) : 0;
        if (pressedButton != wantedButton) {
            if (pressedButton != 0)
                sender.button(pressedButton, false);
            pressedButton = wantedButton;
            if (pressedButton != 0)
                sender.button(pressedButton, true);
        }
    }

    void cancel() {
        if (pressedButton != 0)
            sender.button(pressedButton, false);
        pressedButton = 0;
        hasLastPosition = false;
    }
}
