package org.jhotdraw.draw;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jhotdraw.draw.figure.Figure;

import java.awt.geom.Rectangle2D;

@RunWith(MockitoJUnitRunner.class)
public class BringForwardSendBackwardTest {

    private QuadTreeDrawing drawing;

    @Mock
    private Figure figureA;
    @Mock
    private Figure figureB;
    @Mock
    private Figure figureC;

    @Before
    public void setUp() {
        drawing = new QuadTreeDrawing();

        // Stub getDrawingArea() so fireAreaInvalidated() doesn't NPE
        when(figureA.getDrawingArea()).thenReturn(new Rectangle2D.Double());
        when(figureB.getDrawingArea()).thenReturn(new Rectangle2D.Double());
        when(figureC.getDrawingArea()).thenReturn(new Rectangle2D.Double());

        drawing.add(figureA); // index 0 (back)
        drawing.add(figureB); // index 1
        drawing.add(figureC); // index 2 (front)
    }

    @Test
    public void bringForward_movesTargetUpByOne() {
        drawing.bringForward(figureA);
        assertEquals(1, drawing.indexOf(figureA));
    }

    @Test
    public void sendBackward_movesTargetDownByOne() {
        drawing.sendBackward(figureC);
        assertEquals(1, drawing.indexOf(figureC));
    }

    @Test
    public void bringForward_doesNotAffectOtherFigures() {
        drawing.bringForward(figureA);
        assertEquals(0, drawing.indexOf(figureB)); // B shifts down
        assertEquals(2, drawing.indexOf(figureC)); // C unchanged
    }

    // Figure already at front — should not move
    @Test
    public void bringForward_figureAlreadyAtFront_noChange() {
        drawing.bringForward(figureC);
        assertEquals(2, drawing.indexOf(figureC));
    }

    // Figure already at back — should not move
    @Test
    public void sendBackward_figureAlreadyAtBack_noChange() {
        drawing.sendBackward(figureA);
        assertEquals(0, drawing.indexOf(figureA));
    }

    // Figure not in drawing — should do nothing, no exception
    @Test
    public void bringForward_figureNotInDrawing_noException() {
        Figure stranger = mock(Figure.class);
        drawing.bringForward(stranger); // must not throw
    }

    // Single figure in drawing — both operations are no-ops
    @Test
    public void sendBackward_singleFigure_noChange() {
        QuadTreeDrawing solo = new QuadTreeDrawing();
        solo.add(figureA);
        // Remove the when() line that was here - figureA is already stubbed in @Before
        solo.sendBackward(figureA);
        assertEquals(0, solo.indexOf(figureA));
    }
}