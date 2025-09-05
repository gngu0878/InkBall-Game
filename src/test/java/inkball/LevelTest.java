package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LevelTest {

    private App app;
    private Level level;
    private JSONObject config;

    @BeforeEach
    public void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();

        // Initialize the level and create a valid configuration object
        level = new Level(app);
        
        config = new JSONObject();
        config.setString("layout", "level1.txt");  // Use the actual level file from resources
        config.setFloat("spawn_interval", 5.0f);
        config.setFloat("score_increase_from_hole_capture_modifier", 1.5f);
        config.setFloat("score_decrease_from_wrong_hole_modifier", 0.5f);
        JSONArray ballsArray = new JSONArray();
        ballsArray.append("orange");
        ballsArray.append("blue");
        config.setJSONArray("balls", ballsArray);

        level.loadLevel(config);  // Load the level using the actual configuration
    }

    @Test
    public void testLevelInitialization() {
        assertNotNull(level.getBalls(), "Balls list should be initialized");
        assertNotNull(level.getWalls(), "Walls list should be initialized");
        assertNotNull(level.getHoles(), "Holes list should be initialized");
        assertNotNull(level.getSpawners(), "Spawners list should be initialized");
        assertNotNull(level.getLines(), "Lines list should be initialized");
        assertNotNull(level.getBricks(), "Bricks list should be initialized");
        assertEquals(0, level.getSpawnCounter(), "Spawn counter should be initialized to 0");
    }

    @Test
    public void testAddBall() {
        Ball ball = new Ball(new PVector(100, 100), app.getBallSprites()[1], app, 1);
        level.addBall(ball);
        assertTrue(level.getBalls().contains(ball), "Ball should be added to the level");
    }

    @Test
    public void testRemoveBall() {
        Ball ball = new Ball(new PVector(100, 100), app.getBallSprites()[1], app, 1);
        level.addBall(ball);
        level.removeBall(ball);
        assertFalse(level.getBalls().contains(ball), "Ball should be removed from the level");
    }

    @Test
    public void testAddLine() {
        Line line = new Line(new ArrayList<>(), app);
        level.addLine(line);
        assertTrue(level.getLines().contains(line), "Line should be added to the level");
    }

    @Test
    public void testRemoveLine() {
        Line line = new Line(new ArrayList<>(), app);
        level.addLine(line);
        level.removeLine(line);
        assertFalse(level.getLines().contains(line), "Line should be removed from the level");
    }

    @Test
    public void testIsLevelCompletedFalse() {
        level.loadLevel(config);
        level.addBall(new Ball(new PVector(100, 100), app.getBallSprites()[1], app, 1));
        assertFalse(level.isLevelCompleted(), "Level should not be completed when balls are present");
    }

    @Test
    public void testIsLevelCompletedTrue() {
        level.loadLevel(config);
        level.getConfigBalls().clear();  // Ensure no balls are configured
        level.getBalls().clear();        // Clear all balls from the level
        assertTrue(level.isLevelCompleted(), "Level should be completed when no balls are left");
    }

    @Test
    public void testGetBalls() {
        List<Ball> balls = new ArrayList<>();
        level.setBalls(balls);
        assertEquals(balls, level.getBalls(), "Should return the correct balls list");
    }

    @Test
    public void testGetWalls() {
        List<Wall> walls = new ArrayList<>();
        level.setWalls(walls);
        assertEquals(walls, level.getWalls(), "Should return the correct walls list");
    }

    @Test
    public void testGetHoles() {
        List<Hole> holes = new ArrayList<>();
        level.setHoles(holes);
        assertEquals(holes, level.getHoles(), "Should return the correct holes list");
    }

    @Test
    public void testGetSpawners() {
        List<Spawner> spawners = new ArrayList<>();
        level.setSpawners(spawners);
        assertEquals(spawners, level.getSpawners(), "Should return the correct spawners list");
    }

    @Test
    public void testGetLines() {
        List<Line> lines = new ArrayList<>();
        level.setLines(lines);
        assertEquals(lines, level.getLines(), "Should return the correct lines list");
    }

    @Test
    public void testGetBricks() {
        List<Brick> bricks = new ArrayList<>();
        level.setBricks(bricks);
        assertEquals(bricks, level.getBricks(), "Should return the correct bricks list");
    }

    @Test
    public void testRestartClearsBallsAndResetsSpawnInterval() {
        level.loadLevel(config);
        level.addBall(new Ball(new PVector(100, 100), app.getBallSprites()[1], app, 1));
        level.setSpawnInterval(3.0f);
        level.restart();

        assertTrue(level.getBalls().isEmpty(), "Balls list should be cleared after restart");
        assertEquals(0, level.getSpawnCounter(), "Spawn counter should be reset to 0");
        assertEquals(10.0f, level.getSpawnInterval(), "Spawn interval should reset to 10.0f after restart");
    }

    @Test
    public void testDisplayCallsDisplayOnAllElements() {
        level.loadLevel(config);
        try {
            level.display();  // Should not throw exceptions
        } catch (Exception e) {
            fail("Display method should run without throwing exceptions.");
        }
    }

    @Test
    public void testDisplayStaticElementsRunsWithoutError() {
        level.loadLevel(config);
        try {
            level.displayStaticElements();  // Should not throw exceptions
        } catch (Exception e) {
            fail("Display static elements should run without throwing exceptions.");
        }
    }

    @Test
    public void testLoadSpritesLoadsAllSprites() {
        level.loadSprites();
        for (PImage sprite : level.getWallSprites()) {
            assertNotNull(sprite, "Wall sprites should be loaded");
        }
        for (PImage sprite : level.getHoleSprites()) {
            assertNotNull(sprite, "Hole sprites should be loaded");
        }
        for (PImage sprite : level.getBallSprites()) {
            assertNotNull(sprite, "Ball sprites should be loaded");
        }
        for (PImage sprite : level.getBrickSprites()) {
            assertNotNull(sprite, "Brick sprites should be loaded");
        }
        assertNotNull(level.getSpawnerSprite(), "Spawner sprite should be loaded");
    }

    @Test
    public void testAddBallToRespawnQueue() {
        level.loadLevel(config);
        Ball ball = new Ball(new PVector(100, 100), app.getBallSprites()[1], app, 1);
        
        // Call addBallToRespawnQueue to add the ball color
        level.addBallToRespawnQueue(ball);
        assertTrue(level.getConfigBalls().contains("orange"), "Ball color should be added to configBalls for respawn");
    }

    @Test
    public void testGetScoreIncreaseMultiplier() {
        level.setScoreIncreaseMultiplier(2.0f);
        assertEquals(2.0f, level.getScoreIncreaseMultiplier(), "Score increase multiplier should be set correctly");
    }

    @Test
    public void testSetScoreIncreaseMultiplier() {
        level.setScoreIncreaseMultiplier(2.5f);
        assertEquals(2.5f, level.getScoreIncreaseMultiplier(), "Score increase multiplier should be updated correctly");
    }

    @Test
    public void testGetScoreDecreaseMultiplier() {
        level.setScoreDecreaseMultiplier(0.8f);
        assertEquals(0.8f, level.getScoreDecreaseMultiplier(), "Score decrease multiplier should be set correctly");
    }

    @Test
    public void testSetScoreDecreaseMultiplier() {
        level.setScoreDecreaseMultiplier(0.6f);
        assertEquals(0.6f, level.getScoreDecreaseMultiplier(), "Score decrease multiplier should be updated correctly");
    }

    @Test
    public void testUpdateHandlesEmptyBallsList() {
        level.loadLevel(config);
        level.getBalls().clear();
        level.update(config);
        // If no exceptions occur, the test passes
    }

    @Test
    public void testSpawnBallFromSpawnerWhenNoSpawners() {
        level.loadLevel(config);
        level.getSpawners().clear();  // Ensure no spawners
        level.setSpawnCounter((int) (level.getSpawnInterval() * App.FPS));
        int initialBallCount = level.getBalls().size();
        level.update(config);
        assertEquals(initialBallCount, level.getBalls().size(), "No balls should be spawned when there are no spawners");
    }

    @Test
    public void testSpawnBallFromSpawnerWhenNoConfigBalls() {
        level.loadLevel(config);
        level.getConfigBalls().clear();  // Ensure no config balls
        level.setSpawnCounter((int) (level.getSpawnInterval() * App.FPS));
        int initialBallCount = level.getBalls().size();
        level.update(config);
        assertEquals(initialBallCount, level.getBalls().size(), "No balls should be spawned when configBalls is empty");
    }

    @Test
    public void testGetSpawnCountdownWhenNoConfigBalls() {
        level.loadLevel(config);
        level.getConfigBalls().clear();  // Ensure no config balls
        float countdown = level.getSpawnCountdown();
        assertEquals(0, countdown, "Spawn countdown should be zero when no configBalls");
    }

    @Test
    public void testAddLineAndRetrieveLines() {
        Line line = new Line(new ArrayList<>(), app);
        level.addLine(line);
        assertTrue(level.getLines().contains(line), "Line should be added to the level");
        assertEquals(1, level.getLines().size(), "Lines list should contain one line");
    }

    @Test
    public void testUpdateHandlesNullConfig() {
        level.loadLevel(config);
        try {
            level.update(null); 
            // Test passes if no exceptions occur
        } catch (Exception e) {
            fail("Level should handle null config without exceptions.");
        }
    }

    @Test
    public void testIsLevelCompletedWhenOnlyBallsLeft() {
        level.loadLevel(config);
        level.getConfigBalls().clear();
        level.addBall(new Ball(new PVector(100, 100), app.getBallSprites()[1], app, 1));
        assertFalse(level.isLevelCompleted(), "Level should not be completed when balls are still in play");
    }

    @Test
    public void testUpdateHandlesNoWalls() {
        level.loadLevel(config);
        level.getWalls().clear();  // Ensure no walls
        try {
            level.update(config);  // Should run without exceptions
        } catch (Exception e) {
            fail("Level should handle no walls without throwing exceptions.");
        }
    }

    @Test
    public void testUpdateHandlesNoHoles() {
        level.loadLevel(config);
        level.getHoles().clear();  // Ensure no holes
        try {
            level.update(config);  // Should run without exceptions
        } catch (Exception e) {
            fail("Level should handle no holes without throwing exceptions.");
        }
    }

    @Test
    public void testSpawnBallFromSpawner() {
        // Ensure that a ball is spawned from the spawners when configBalls contains valid entries
        int initialBallCount = level.getBalls().size();
        level.spawnBallFromSpawner();  // Attempt to spawn a ball
        
        assertEquals(initialBallCount + 1, level.getBalls().size(), "A ball should be spawned when valid spawners and configBalls exist.");
    }

    @Test
    public void testSpawnBallFromSpawnerNoSpawners() {
        // Remove all spawners to ensure no ball is spawned
        level.getSpawners().clear();
        int initialBallCount = level.getBalls().size();
        
        level.spawnBallFromSpawner();  // Attempt to spawn a ball
        
        assertEquals(initialBallCount, level.getBalls().size(), "No ball should be spawned when there are no spawners.");
    }

    @Test
    public void testSpawnBallFromSpawnerEmptyConfigBalls() {
        // Clear configBalls to prevent ball spawning
        level.getConfigBalls().clear();
        int initialBallCount = level.getBalls().size();
        
        level.spawnBallFromSpawner();  // Attempt to spawn a ball
        
        assertEquals(initialBallCount, level.getBalls().size(), "No ball should be spawned when configBalls is empty.");
    }

    @Test
    public void testSpawnBallFromSpawnerInvalidBallColor() {
        // Add an invalid color to configBalls and verify no ball is spawned
        level.getConfigBalls().clear();
        level.getConfigBalls().add("invalidColor");
        
        int initialBallCount = level.getBalls().size();
        level.spawnBallFromSpawner();  // Attempt to spawn a ball
        
        assertEquals(initialBallCount, level.getBalls().size(), "No ball should be spawned for an invalid color in configBalls.");
    }
}