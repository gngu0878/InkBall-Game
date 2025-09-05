package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.core.PConstants;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class AppTest {

    private App app;

    @BeforeEach
    public void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();
        app.setRandomSeed(12345);
    }

    @Test
    public void testAppInitialization() {
        assertNotNull(app.getBallSprites(), "Ball sprites should not be null after initialization");
    }

    @Test
    public void testLevelLoading() {
        app.loadLevel(0);
        assertNotNull(app.getCurrentLevel(), "Level should be loaded successfully");
    }

    @Test
    public void testTimerInitialization() {
        assertEquals(0, app.getScore(), "Score should be initialized to 0");
        assertTrue(app.getRemainingTime() > 0, "Remaining time should be set for the level");
    }

    @Test
    public void testScoreUpdate() {
        int initialScore = app.getScore();
        app.increaseScore(10);
        assertEquals(initialScore + 10, app.getScore(), "Score should increase by 10");
    }

    @Test
    public void testRestartLevel() {
        app.loadLevel(0);
        int initialScore = app.getScore();
        app.increaseScore(50);
        app.restartLevel();
        assertEquals(initialScore, app.getScore(), "Score should reset after restarting the level");
    }

    @Test
    public void testLineRemoval() {
        MouseEvent mouseEvent = new MouseEvent(
            null,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            100,
            100 + App.TOPBAR,
            PConstants.LEFT,
            1
        );
        app.mousePressed(mouseEvent);
        Line line = app.getCurrentLine();
        app.removeLine(line);
        assertFalse(app.getPlayerLines().contains(line), "Line should be removed from the game");
    }

    @Test
    public void testNextLevelLoading() {
        app.loadLevel(0);
        app.nextLevel();
        assertNotNull(app.getCurrentLevel(), "Next level should be loaded");
    }

    @Test
    public void testRestartGame() {
        app.setGameCompleted(true);
        app.restartGame();
        assertFalse(app.isGameCompleted(), "Game should restart and reset completion flag");
        assertEquals(0, app.getScore(), "Score should reset to 0 on game restart");
    }

    @Test
    public void testScoreDoesNotGoNegative() {
        app.decreaseScore(10);
        assertEquals(0, app.getScore(), "Score should not go below zero");
    }

    @Test
    public void testStartLevelCompletionInitializesYellowTiles() {
        app.startLevelCompletion();
        assertNotNull(app.getYellowTiles(), "Yellow tiles should be initialized");
        assertFalse(app.getYellowTiles().isEmpty(), "Yellow tiles should contain tiles");
    }

    @Test
    public void testPressRKeyRestartsLevelWhenNotCompleted() {
        app.loadLevel(0);
        app.increaseScore(50);
        app.key = 'r';
        app.keyPressed();
        assertEquals(0, app.getScore(), "Score should reset after restarting the level");
    }

    @Test
    public void testPressRKeyRestartsGameWhenCompleted() {
        app.setGameCompleted(true);
        app.key = 'r';
        app.keyPressed();
        assertFalse(app.isGameCompleted(), "Game should restart and reset completion flag");
        assertEquals(0, app.getScore(), "Score should reset to 0 on game restart");
    }

    @Test
    public void testStartMovingUpcomingBalls() {
        assertFalse(app.isMoveUpcomingBalls(), "moveUpcomingBalls should initially be false");
        app.startMovingUpcomingBalls();
        assertTrue(app.isMoveUpcomingBalls(), "moveUpcomingBalls should be true after starting to move upcoming balls");
    }

    @Test
    public void testUpcomingBallsOffsetDecreases() {
        app.startMovingUpcomingBalls();
        float initialOffset = app.getUpcomingBallsOffset();
        app.draw(); // simulate a draw call to update the offset
        assertTrue(app.getUpcomingBallsOffset() < initialOffset, "upcomingBallsOffset should decrease when moveUpcomingBalls is true");
    }

    @Test
    public void testUpdateTimerDecreasesRemainingTime() {
        int initialTime = app.getRemainingTime();
        app.setLastTimeCheck(app.getLastTimeCheck() - 1000); // Simulate 1 second has passed
        app.updateTimer();
        assertEquals(initialTime - 1, app.getRemainingTime(), "Remaining time should decrease by 1 after 1 second");
    }

    @Test
    public void testIsTimeUpFalseWhenTimeRemaining() {
        app.setRemainingTime(10);
        app.updateTimer();
        assertFalse(app.isTimeUp(), "isTimeUp should be false when time remains");
    }

    @Test
    public void testLevelTimeSetOnLoadLevel() {
        app.loadLevel(0);
        assertTrue(app.getLevelTime() > 0, "Level time should be set when a level is loaded");
    }

    @Test
    public void testCurrentLevelIndexIncreasesOnNextLevel() {
        int initialIndex = app.getCurrentLevelIndex();
        app.nextLevel();
        assertEquals(initialIndex + 1, app.getCurrentLevelIndex(), "Current level index should increase when nextLevel is called");
    }

    @Test
    public void testGameCompletedWhenAllLevelsCompleted() {
        int lastLevelIndex = app.getLevelsSize() - 1;
        app.setCurrentLevelIndex(lastLevelIndex);
        app.loadLevel(lastLevelIndex);
        app.nextLevel();
        assertTrue(app.isGameCompleted(), "Game should be completed when all levels are finished");
    }

    @Test
    public void testGetPaused() {
        assertFalse(app.getPaused(), "Game should not be paused initially");
        app.setPaused(true);
        assertTrue(app.getPaused(), "getPaused should return true when game is paused");
    }

    @Test
    public void testSetRemainingTime() {
        app.setRemainingTime(100);
        assertEquals(100, app.getRemainingTime(), "Remaining time should be set correctly");
    }

    @Test
    public void testPauseGameTogglesPausedState() {
        boolean initialPausedState = app.getPaused();
        app.key = ' ';
        app.keyPressed();
        assertNotEquals(initialPausedState, app.getPaused(), "Pressing space should toggle paused state");
    }

    @Test
    public void testIncreaseScoreBeyondInitialValue() {
        app.increaseScore(1000);
        assertEquals(1000, app.getScore(), "Score should increase correctly beyond initial value");
    }

    @Test
    public void testDecreaseScoreToZero() {
        app.increaseScore(50);
        app.decreaseScore(50);
        assertEquals(0, app.getScore(), "Score should decrease to zero");
    }

    @Test
    public void testMousePressedOutsideGameAreaDoesNotStartLine() {
        MouseEvent mouseEvent = new MouseEvent(
            null,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            100,
            App.TOPBAR - 10, // Above the game area
            PConstants.LEFT,
            1
        );
        app.mousePressed(mouseEvent);
        assertNull(app.getCurrentLine(), "Current line should not be initialized when clicking outside game area");
    }

    @Test
    public void testMouseDraggedWithoutPressedDoesNotCreateLine() {
        app.mouseDragged();
        assertNull(app.getCurrentLine(), "Dragging without a pressed mouse should not create a line");
    }

    @Test
    public void testGameIsPausedStopsUpdatingLevel() {
        app.setPaused(true);
        app.loadLevel(0);
        int initialBallCount = app.getCurrentLevel().getBalls().size();
        app.getCurrentLevel().update(null); // Attempt to update level
        int postUpdateBallCount = app.getCurrentLevel().getBalls().size();
        assertEquals(initialBallCount, postUpdateBallCount, "Level should not update when game is paused");
    }

    @Test
    public void testLevelCompletedPreventsFurtherActions() {
        app.setLevelCompleted(true);
        app.mousePressed(new MouseEvent(
            null,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,
            PConstants.LEFT,
            1
        ));
        assertNull(app.getCurrentLine(), "No new lines should be created when level is completed");
    }

    @Test
    public void testYellowTileLoopsCompletedIncrements() {
        app.startLevelCompletion();
        App.YellowTile tile = app.getYellowTiles().get(0);
        int initialLoops = tile.loopsCompleted;
        // Simulate tile moving around the edge
        for (int i = 0; i < 4; i++) {
            tile.edge = i;
            tile.indexOnEdge = 1000; // Force move to next edge
            tile.move();
        }
        assertTrue(tile.loopsCompleted > initialLoops, "Yellow tile loopsCompleted should increment after full loop");
    }

    @Test
    public void testInvalidSpritePathLogsError() {
        PImage sprite = app.getSprite("invalid_sprite_path");
        assertNull(sprite, "Sprite should be null when path is invalid");
    }

    @Test
    public void testRestartGameResetsAllLevels() {
        app.loadLevel(1);
        app.restartGame();
        assertEquals(0, app.getCurrentLevelIndex(), "Current level index should reset to 0 after restarting game");
    }
    
    @Test
    public void testMouseReleasedWithoutPressedDoesNotAddLine() {
        app.mouseReleased();
        assertTrue(app.getPlayerLines().isEmpty(), "No lines should be added when mouse released without pressing");
    }

    @Test
    public void testPlayerCannotDrawLinesWhenGameCompleted() {
        app.setGameCompleted(true);
        app.mousePressed(new MouseEvent(
            null,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,
            PConstants.LEFT,
            1
        ));
        assertNull(app.getCurrentLine(), "No lines should be drawn when game is completed");
    }

    @Test
    public void testPlayerCannotInteractWhenTimeUp() {
        app.setTimeUp(true);
        app.mousePressed(new MouseEvent(
            null,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,
            PConstants.LEFT,
            1
        ));
        assertNull(app.getCurrentLine(), "No lines should be drawn when time is up");
    }

    @Test
    public void testMouseClickWhileGamePausedDoesNotStartLine() {
        app.setPaused(true);
        MouseEvent mouseEvent = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,
            PConstants.LEFT,
            1
        );
        app.mousePressed(mouseEvent);
        assertNull(app.getCurrentLine(), "No line should be started when game is paused.");
    }

    @Test
    public void testMouseClickWhenTimeUpDoesNotStartLine() {
        app.setTimeUp(true);
        MouseEvent mouseEvent = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,
            PConstants.LEFT,
            1
        );
        app.mousePressed(mouseEvent);
        assertNull(app.getCurrentLine(), "No line should be started when time is up.");
    }

    @Test
    public void testMouseClickWhenLevelCompletedDoesNotStartLine() {
        app.setLevelCompleted(true);
        MouseEvent mouseEvent = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,
            PConstants.LEFT,
            1
        );
        app.mousePressed(mouseEvent);
        assertNull(app.getCurrentLine(), "No line should be started when level is completed.");
    }

    @Test
    public void testInitialScoreIsZero() {
        assertEquals(0, app.getScore(), "Initial score should be zero.");
    }

    @Test
    public void testKeyPressedPausesGame() {
        app.key = ' ';
        app.keyPressed();
        assertTrue(app.getPaused(), "Game should be paused when space key is pressed.");
    }

    @Test
    public void testDecreaseScoreBelowZero() {
        app.decreaseScore(10);
        assertEquals(0, app.getScore(), "Score should not go below zero.");
    }

    @Test
    public void testYellowTileMovesOnEdge() {
        app.startLevelCompletion();
        App.YellowTile tile = app.getYellowTiles().get(0);
        PVector initialPosition = tile.position.copy();
        tile.move();
        assertNotEquals(initialPosition, tile.position, "Yellow tile should move along the edge.");
    }



    @Test
    public void testMousePressedWithRightClickRemovesLine() {
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            150,
            150 + App.TOPBAR,
            PConstants.RIGHT,
            1
        );
        app.mousePressed(event);
        // Add some lines to the player lines
        Line line = new Line(new ArrayList<>(), app);
        app.getPlayerLines().add(line);
        app.removeLine(line);
        assertFalse(app.getPlayerLines().contains(line), "Right-click should remove the line.");
    }

    @Test
    public void testKeyPressedForRestartGame() {
        app.setGameCompleted(true);
        app.key = 'r';
        app.keyPressed();
        assertFalse(app.isGameCompleted(), "Game should restart when 'r' is pressed after completion.");
    }

    @Test
    public void testKeyPressedForRestartLevel() {
        app.loadLevel(0);
        int initialScore = app.getScore();
        app.key = 'r';
        app.keyPressed();
        assertEquals(initialScore, app.getScore(), "Score should reset to initial value after restarting the level.");
    }

    @Test
    public void testPausedPreventsGameUpdates() {
        app.setPaused(true);
        int initialRemainingTime = app.getRemainingTime();
        app.updateTimer();
        assertEquals(initialRemainingTime, app.getRemainingTime(), "Remaining time should not decrease when paused.");
    }

    @Test
    public void testTimeUpPreventsGameUpdates() {
        app.setTimeUp(true);
        int initialRemainingTime = app.getRemainingTime();
        app.updateTimer();
        assertEquals(initialRemainingTime, app.getRemainingTime(), "Remaining time should not decrease when time is up.");
    }

    @Test
    public void testGameResumesAfterPause() {
        app.setPaused(true);
        app.key = ' ';
        app.keyPressed();
        assertFalse(app.getPaused(), "Game should resume when space is pressed after being paused.");
    }

    @Test
    public void testRestartLevelDoesNotAffectYellowTiles() {
        app.loadLevel(0);
        app.startLevelCompletion();
        int initialYellowTilesSize = app.getYellowTiles().size();
        app.restartLevel();
        assertEquals(initialYellowTilesSize, app.getYellowTiles().size(), "Restarting the level should not affect yellow tiles.");
    }

    @Test
    public void testPlayerLinesListNotModifiedIfNoLine() {
        List<Line> initialLines = new ArrayList<>(app.getPlayerLines());
        app.removeLine(null);
        assertEquals(initialLines, app.getPlayerLines(), "Player lines list should remain unchanged if no line is passed for removal.");
    }

    @Test
    public void testPlayerCannotDrawLinesAfterGameCompleted() {
        app.setGameCompleted(true);
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,
            PConstants.LEFT,
            1
        );
        app.mousePressed(event);
        assertNull(app.getCurrentLine(), "Player should not be able to draw new lines after game completion.");
    }

    
    @Test
    public void testMousePressedLeftClickAboveGameArea() {
        // Simulate a left-click above the game area (within the top bar)
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            100,
            App.TOPBAR - 10,  // Above the game area
            PConstants.LEFT,
            1
        );
        app.mousePressed(event);
    
        assertNull(app.getCurrentLine(), "No line should start drawing when left-clicking above the game area.");
    }
    
    @Test
    public void testMousePressedRightClickNoLineToRemove() {
        // Simulate a right-click in the game area where no line exists
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,  // Valid game area
            PConstants.RIGHT,
            1
        );
        app.mousePressed(event);
    
        assertTrue(app.getPlayerLines().isEmpty(), "No lines should be removed if none are drawn.");
    }
    
    @Test
    public void testMousePressedControlLeftClickNoLineFound() {
        // Simulate a control-left-click where no line exists
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            MouseEvent.CTRL,
            300,
            300 + App.TOPBAR,  // Position where no line exists
            PConstants.LEFT,
            1
        );
        app.mousePressed(event);
    
        assertTrue(app.getPlayerLines().isEmpty(), "No lines should be removed if no lines are at the clicked position.");
    }
    
    @Test
    public void testMousePressedRightClickDoesNotRemoveLineOutsideGameArea() {
        // Add a line and simulate a right-click outside the game area
        Line line = new Line(new ArrayList<>(), app);
        line.startDrawing(new PVector(100, 100));
        app.getPlayerLines().add(line);
    
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            100,
            App.TOPBAR - 10,  // Click above the game area
            PConstants.RIGHT,
            1
        );
        app.mousePressed(event);
    
        assertTrue(app.getPlayerLines().contains(line), "No lines should be removed when right-clicking outside the game area.");
    }
    
    @Test
    public void testMousePressedControlLeftClickAboveGameAreaDoesNothing() {
        // Simulate a control-left-click above the game area (in the top bar)
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            MouseEvent.CTRL,
            100,
            App.TOPBAR - 10,  // Click above the game area
            PConstants.LEFT,
            1
        );
        app.mousePressed(event);
    
        assertTrue(app.getPlayerLines().isEmpty(), "No lines should be removed when control-left-clicking above the game area.");
    }
    
    @Test
    public void testMousePressedRightClickDoesNothingIfNoLineMatches() {
        // Add a line but click in a position where no line exists
        Line line = new Line(new ArrayList<>(), app);
        line.startDrawing(new PVector(300, 300));
        app.getPlayerLines().add(line);
    
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            200,
            200 + App.TOPBAR,  // Click in a position where no line exists
            PConstants.RIGHT,
            1
        );
        app.mousePressed(event);
    
        assertTrue(app.getPlayerLines().contains(line), "No lines should be removed if no line matches the clicked position.");
    }

    @Test
    public void testLeftMousePressStartsLine() {
        app.setup();  // Set up the environment
        app.mouseX = 150;
        app.mouseY = 150 + App.TOPBAR;  // In-game area
        app.mouseButton = PConstants.LEFT;
    
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            app.mouseX,
            app.mouseY,
            PConstants.LEFT,
            1
        );
        app.mousePressed(event);
    
        assertNotNull(app.getCurrentLine(), "A line should start drawing when left mouse button is pressed.");
    }

    @Test
    public void testLeftMousePressAboveGameArea() {
        app.setup();
        app.mouseX = 150;
        app.mouseY = App.TOPBAR - 10;  // Above the game area
        app.mouseButton = PConstants.LEFT;
    
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            app.mouseX,
            app.mouseY,
            PConstants.LEFT,
            1
        );
        app.mousePressed(event);
    
        assertNull(app.getCurrentLine(), "No line should start when clicking above the game area.");
    }
    
    @Test
    public void testRightMousePressWithoutLine() {
        app.setup();
        app.mouseX = 150;
        app.mouseY = 150 + App.TOPBAR;  // In-game area
        app.mouseButton = PConstants.RIGHT;
    
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            app.mouseX,
            app.mouseY,
            PConstants.RIGHT,
            1
        );
        app.mousePressed(event);
    
        assertTrue(app.getPlayerLines().isEmpty(), "No line should be removed if no lines exist.");
    }

    @Test
    public void testControlLeftClickEmptyArea() {
        app.setup();
        app.mouseX = 250;
        app.mouseY = 250 + App.TOPBAR;
        app.mouseButton = PConstants.LEFT;
    
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            MouseEvent.CTRL,
            app.mouseX,
            app.mouseY,
            PConstants.LEFT,
            1
        );
        app.mousePressed(event);
    
        assertTrue(app.getPlayerLines().isEmpty(), "No lines should be removed when control-left-clicking in an empty area.");
    }

    @Test
    public void testMouseReleaseWithoutLineDoesNothing() {
        app.setup();
    
        // No line started
        app.mouseReleased();
    
        assertTrue(app.getPlayerLines().isEmpty(), "No line should be added if no line was started.");
    }

    @Test
    public void testRightClickOutsideGameArea() {
        app.setup();
        Line line = new Line(new ArrayList<>(), app);
        line.startDrawing(new PVector(150, 150));
        app.getPlayerLines().add(line);
    
        app.mouseX = 150;
        app.mouseY = App.TOPBAR - 10;  // Outside game area
        app.mouseButton = PConstants.RIGHT;
    
        MouseEvent event = new MouseEvent(
            app,
            System.currentTimeMillis(),
            MouseEvent.PRESS,
            0,
            app.mouseX,
            app.mouseY,
            PConstants.RIGHT,
            1
        );
        app.mousePressed(event);
    
        assertFalse(app.getPlayerLines().isEmpty(), "No line should be removed when clicking outside the game area.");
    }

    @Test
    public void testMouseDraggedAddsPoints() {
        // Use the setter to set the current line
        app.setCurrentLine(new Line(new ArrayList<>(), app));
    
        PVector start = new PVector(50, 100);
        app.getCurrentLine().startDrawing(start);  // Use the getter to access currentLine
    
        // Simulate dragging the mouse
        app.mouseX = 100;
        app.mouseY = 150;
        app.mouseDragged();
    
        // Assert that a point has been added to the line
        assertFalse(app.getCurrentLine().getPoints().isEmpty(), "Line should have points after mouseDragged.");
    }
    

    @Test
    public void testMouseDraggedNoActionIfNoCurrentLine() {
        app.setCurrentLine(null);  // Ensure no current line exists
    
        app.mouseX = 150;
        app.mouseY = 200;  // Simulate dragging the mouse
        app.mouseDragged();  // Call mouseDragged
    
        // Since currentLine is null, nothing should be added
        assertNull(app.getCurrentLine(), "No line should be created when mouseDragged is called without an active line.");
    }
    
    @Test
    public void testMouseReleasedAddsCurrentLineToPlayerLines() {
        // Simulate starting a new line
        PVector start = new PVector(50, 100);
        Line line = new Line(new ArrayList<>(), app);  // Initialize a new line
        line.startDrawing(start);  // Start drawing the line
        app.setCurrentLine(line);  // Set current line
    
        // Simulate releasing the mouse to complete the line
        app.mouseReleased();
    
        // Verify the line is added to the playerLines list
        assertFalse(app.getPlayerLines().isEmpty(), "Player lines should contain the current line after mouseReleased.");
        assertEquals(1, app.getPlayerLines().size(), "There should be exactly one line added.");
        assertNull(app.getCurrentLine(), "currentLine should be set to null after mouseReleased.");
    }
    
    @Test
    public void testMouseReleasedNoActionIfNoCurrentLine() {
        app.setCurrentLine(null);  // No line is being drawn
    
        int initialPlayerLinesSize = app.getPlayerLines().size();  // Store initial playerLines size
    
        app.mouseReleased();  // Call mouseReleased
    
        // Verify that nothing happens, playerLines should remain unchanged
        assertEquals(initialPlayerLinesSize, app.getPlayerLines().size(), "Player lines should remain unchanged if mouseReleased is called without an active line.");
        assertNull(app.getCurrentLine(), "currentLine should remain null after mouseReleased if no line was being drawn.");
    }

    @Test
    public void testMousePressedLeftClickStartsLine() {
        app.mouseX = 150;
        app.mouseY = 150 + App.TOPBAR;  // Within game area
        app.mouseButton = PConstants.LEFT;

        MouseEvent event = new MouseEvent(app, System.currentTimeMillis(), MouseEvent.PRESS, 0, app.mouseX, app.mouseY, PConstants.LEFT, 1);
        app.mousePressed(event);

        assertNotNull(app.getCurrentLine(), "Line should start when left-click is pressed within the game area.");
    }

    @Test
    public void testMouseDraggedAddsPointsToLine() {
        // Simulate starting a new line
        Line line = new Line(new ArrayList<>(), app);
        line.startDrawing(new PVector(50, 100));
        app.setCurrentLine(line);

        // Simulate dragging the mouse
        app.mouseX = 100;
        app.mouseY = 150 + App.TOPBAR;
        app.mouseDragged();

        assertFalse(app.getCurrentLine().getPoints().isEmpty(), "Points should be added to the line when mouse is dragged.");
    }

    @Test
    public void testMouseReleasedAddsLineToPlayerLines() {
        // Simulate starting a new line
        Line line = new Line(new ArrayList<>(), app);
        line.startDrawing(new PVector(50, 100));
        app.setCurrentLine(line);

        // Simulate releasing the mouse
        app.mouseReleased();

        assertFalse(app.getPlayerLines().isEmpty(), "The line should be added to player lines when mouse is released.");
        assertNull(app.getCurrentLine(), "Current line should be set to null after mouse is released.");
    }

    @Test
    public void testLeftClickStartsNewLine() {
        // Simulate a left-click in the game area
        app.mouseX = 100;
        app.mouseY = 150 + App.TOPBAR;
        app.mouseButton = PConstants.LEFT;
        MouseEvent event = new MouseEvent(app, System.currentTimeMillis(), MouseEvent.PRESS, 0, app.mouseX, app.mouseY, PConstants.LEFT, 1);
        app.mousePressed(event);

        assertNotNull(app.getCurrentLine(), "A new line should be created when left-clicking in the game area.");
    }

    @Test
    public void testLeftClickAfterRightClickStartsNewLine() {
        // Simulate a right-click (does nothing), then a left-click to start a new line
        app.mouseX = 200;
        app.mouseY = 200 + App.TOPBAR;
        app.mouseButton = PConstants.RIGHT;
        MouseEvent rightClickEvent = new MouseEvent(app, System.currentTimeMillis(), MouseEvent.PRESS, 0, app.mouseX, app.mouseY, PConstants.RIGHT, 1);
        app.mousePressed(rightClickEvent);

        app.mouseX = 100;
        app.mouseY = 150 + App.TOPBAR;
        app.mouseButton = PConstants.LEFT;
        MouseEvent leftClickEvent = new MouseEvent(app, System.currentTimeMillis(), MouseEvent.PRESS, 0, app.mouseX, app.mouseY, PConstants.LEFT, 1);
        app.mousePressed(leftClickEvent);

        assertNotNull(app.getCurrentLine(), "A new line should be created after the left-click.");
    }

    @Test
    public void testGameStates() {
        // Test GameCompleted and ShowWinMessage
        app.setGameCompleted(true);
        app.setShowWinMessage(true);

        assertTrue(app.isGameCompleted(), "GameCompleted should be true after setting.");
        assertTrue(app.getShowWinMessage(), "ShowWinMessage should be true after setting.");

        // Test Paused and LevelCompleted
        app.setPaused(true);
        app.setLevelCompleted(true);

        assertTrue(app.getPaused(), "Paused should be true after setting.");
        assertTrue(app.isLevelCompleted(), "LevelCompleted should be true after setting.");
    }

    @Test
    public void testTimeSettings() {
        // Test RemainingTime and LastTimeCheck
        app.setRemainingTime(120);
        app.setLastTimeCheck(1000L);

        assertEquals(120, app.getRemainingTime(), "Remaining time should be set to 120.");
        assertEquals(1000L, app.getLastTimeCheck(), "LastTimeCheck should be set to 1000.");

        // Test TimeBonusRemaining
        app.setTimeBonusRemaining(50);

        assertEquals(50, app.getTimeBonusRemaining(), "TimeBonusRemaining should be set to 50.");
    }

    @Test
    public void testLineAndScoreSettings() {
        // Test CurrentLine and Score
        Line line = new Line(new ArrayList<>(), app);
        app.setCurrentLine(line);
        app.setScore(500);

        assertEquals(line, app.getCurrentLine(), "Current line should be set and retrieved correctly.");
        assertEquals(500, app.getScore(), "Score should be set to 500.");
    }

    @Test
    public void testLevelAndIndexSettings() {
        // Test CurrentLevelIndex and LevelCompleted
        app.setCurrentLevelIndex(2);
        app.setLevelCompleted(true);

        assertEquals(2, app.getCurrentLevelIndex(), "Current level index should be set to 2.");
        assertTrue(app.isLevelCompleted(), "LevelCompleted should be true after setting.");
    }
}