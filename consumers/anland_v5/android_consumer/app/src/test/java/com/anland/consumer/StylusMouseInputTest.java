package com.anland.consumer;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class StylusMouseInputTest {
    private final List<String> events = new ArrayList<>();
    private final StylusMouseInput input = new StylusMouseInput(
            new StylusMouseInput.Sender() {
                @Override public void move(float x, float y) {
                    events.add("move:" + x + "," + y);
                }

                @Override public void button(int button, boolean pressed) {
                    events.add("button:" + button + ":" + pressed);
                }

                @Override public void scroll(int axis, float value) {
                    events.add("scroll:" + axis + ":" + value);
                }
            });

    private void update(float x, float y, boolean inProximity, boolean tipDown,
                        boolean sideButton, boolean rightClick, boolean scroll,
                        float sensitivity) {
        input.update(x, y, inProximity, tipDown, sideButton, rightClick,
                scroll, sensitivity);
    }

    @Test public void hoveringWithHeldSideButtonScrollsByMovementOnBothAxes() {
        update(10, 20, true, false, false, true, true, 1f);
        update(10, 20, true, false, true, true, true, 1f);
        update(14, 27, true, false, true, true, true, 2f);

        assertEquals("move:10.0,20.0", events.get(0));
        assertEquals("scroll:1:8.0", events.get(1));
        assertEquals("scroll:0:-14.0", events.get(2));
    }

    @Test public void tipClickUsesLeftAndSideButtonCanSelectRightClick() {
        update(1, 2, true, true, false, true, true, 1f);
        update(1, 2, true, false, false, true, true, 1f);
        update(1, 2, true, true, true, true, true, 1f);
        update(1, 2, true, false, true, true, true, 1f);

        assertEquals(7, events.size());
        assertEquals("button:272:true", events.get(1));
        assertEquals("button:272:false", events.get(3));
        assertEquals("button:273:true", events.get(5));
        assertEquals("button:273:false", events.get(6));
    }

    @Test public void disabledRightClickUsesLeftAndDisabledHoverScrollMovesPointer() {
        update(1, 2, true, false, false, false, false, 1f);
        update(4, 6, true, false, true, false, false, 1f);
        update(5, 7, true, true, true, false, false, 1f);

        assertEquals("move:4.0,6.0", events.get(1));
        assertEquals("button:272:true", events.get(3));
        assertFalse(events.stream().anyMatch(event -> event.startsWith("scroll:")));
    }

    @Test public void cancelReleasesAnActiveMouseButton() {
        update(1, 2, true, true, false, true, true, 1f);
        input.cancel();
        input.cancel();

        assertEquals(3, events.size());
        assertEquals("button:272:false", events.get(2));
    }
}
