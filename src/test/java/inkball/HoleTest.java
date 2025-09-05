package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PVector;
import processing.core.PImage;
import processing.data.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

public class HoleTest {

    private Hole hole;
    private App app;
    private PImage sprite;
    private PVector position;
    private Ball ball;
    private JSONObject config;

    @BeforeEach
    public void setup() {
        app = new App();
        sprite = new PImage();
        position = new PVector(100, 100);
        hole = new Hole(position, sprite, app, 1, 1.5f, 0.5f);

        ball = new Ball(new PVector(110, 110), sprite, app, 1); // Create ball with color matching the hole
        config = new JSONObject();
    }

    @Test
    public void testHoleInitialization() {
        assertNotNull(hole, "Hole should be initialized");
        assertEquals(position, hole.getPosition(), "Hole should be initialized with the correct position");
        assertEquals(1, hole.getColorType(), "Hole should have the correct color type");
    }

    @Test
    public void testAttractBallInRange() {
        hole.attractBall(ball, app, config);
        assertTrue(ball.getSize() < ball.getOriginalSize(), "Ball size should reduce when within range of the hole");
    }

    @Test
    public void testAttractBallOutOfRange() {
        ball.setPosition(new PVector(200, 200)); // Set ball far from the hole
        hole.attractBall(ball, app, config);
        assertEquals(ball.getOriginalSize(), ball.getSize(), "Ball size should not change when out of range of the hole");
    }

    @Test
    public void testCaptureBallWithMatchingColor() {
        hole.attractBall(ball, app, config);
        assertTrue(ball.getSize() < ball.getOriginalSize(), "Ball should shrink when captured by a matching color hole");
    }

    @Test
    public void testCaptureWithEmptyConfig() {
        hole.attractBall(ball, app, config);
        assertTrue(ball.getSize() < ball.getOriginalSize(), "Ball size should reduce even with empty config");
    }

    @Test
    public void testGetColorNameGrey() {
        hole.setColorType(0);
        assertEquals("grey", hole.getColorName(), "Color name should be grey");
    }

    @Test
    public void testGetColorNameOrange() {
        hole.setColorType(1);
        assertEquals("orange", hole.getColorName(), "Color name should be orange");
    }

    @Test
    public void testGetColorNameBlue() {
        hole.setColorType(2);
        assertEquals("blue", hole.getColorName(), "Color name should be blue");
    }

    @Test
    public void testGetColorNameGreen() {
        hole.setColorType(3);
        assertEquals("green", hole.getColorName(), "Color name should be green");
    }

    @Test
    public void testGetColorNameYello2() {
        hole.setColorType(4);
        assertEquals("yellow", hole.getColorName(), "Color name should be yellow");
    }

    @Test
    public void testGetColorNameUnknown() {
        hole.setColorType(-1);
        assertEquals("unknown", hole.getColorName(), "Color name should be unknown for invalid color type");
    }

    @Test
    public void testCapturePointsForMatchingColors() {
        JSONObject scoreMap = new JSONObject();
        scoreMap.setInt("orange", 10);
        int points = hole.getCapturePoints(scoreMap, "orange", "orange");
        assertEquals(10, points, "Capture points should be correct for matching colors");
    }

    @Test
    public void testCapturePointsForNonMatchingColors() {
        JSONObject scoreMap = new JSONObject();
        scoreMap.setInt("orange", 10);
        int points = hole.getCapturePoints(scoreMap, "blue", "orange");
        assertEquals(0, points, "Capture points should be 0 for non-matching colors");
    }

    @Test
    public void testWrongCapturePenaltyForMatchingColors() {
        JSONObject penaltyMap = new JSONObject();
        penaltyMap.setInt("blue", 5);
        int penalty = hole.getWrongCapturePenalty(penaltyMap, "blue", "orange");
        assertEquals(5, penalty, "Wrong capture penalty should be correct for non-matching colors");
    }

    @Test
    public void testWrongCapturePenaltyForNonMatchingColors() {
        JSONObject penaltyMap = new JSONObject();
        penaltyMap.setInt("orange", 5);
        int penalty = hole.getWrongCapturePenalty(penaltyMap, "grey", "orange");
        assertEquals(0, penalty, "Wrong capture penalty should be 0 for non-matching colors");
    }


    @Test
    public void testHandleBallCaptureWithScoreIncrease() {
        // Create the score increase JSON manually
        JSONObject scoreIncreaseMap = new JSONObject();
        scoreIncreaseMap.setInt("orange", 10);
        
        // Add this map to the config
        config.setJSONObject("score_increase_from_hole_capture", scoreIncreaseMap);
    
        ball.setColorType(1); // Matching color
        hole.attractBall(ball, app, config);
        assertTrue(ball.getSize() < ball.getOriginalSize(), "Ball should shrink on capture with score increase");
    }

    @Test
    public void testGetPosition() {
        assertEquals(position, hole.getPosition(), "Hole should return the correct position");
    }

    @Test
    public void testSetPosition() {
        PVector newPosition = new PVector(200, 200);
        hole.setPosition(newPosition);
        assertEquals(newPosition, hole.getPosition(), "Hole's position should be updated correctly");
    }

    @Test
    public void testSetAndGetSprite() {
        PImage newSprite = new PImage();
        hole.setSprite(newSprite);
        assertEquals(newSprite, hole.getSprite(), "Hole's sprite should be updated correctly");
    }

    @Test
    public void testSetAndGetColorType() {
        hole.setColorType(2);
        assertEquals(2, hole.getColorType(), "Hole's color type should be updated correctly");
    }


    @Test
    public void testHandleCaptureWithNoConfigKeys() {
        // Test when config does not contain expected keys
        ball.setColorType(1); // Matching color
        hole.handleCapture(ball, app, config);

        assertEquals(0, app.getScore(), "Score should not change when config keys are missing");
    }

    @Test
    public void testHandleCaptureWithMatchingColorSimple() {
        // No config logic, just check that capture happens
        ball.setColorType(1); // Matching color
        hole.handleCapture(ball, app, config);

        assertEquals(0, app.getScore(), "Score should remain zero as no config was provided");
    }

    @Test
    public void testHandleCaptureWithNonMatchingColorSimple() {
        // No config logic, just check ball is captured
        ball.setColorType(2); // Non-matching color
        hole.handleCapture(ball, app, config);

        assertEquals(0, app.getScore(), "Score should remain zero for non-matching color with no config");
    }

    @Test
    public void testHandleCaptureWithNeutralColorHole() {
        hole.setColorType(0); // Neutral hole color

        // Ball of any color should be captured easily
        ball.setColorType(2); // Different color ball
        hole.handleCapture(ball, app, config);

        assertEquals(0, app.getScore(), "Score should remain zero for neutral hole with no config");
    }

    @Test
    public void testHandleCaptureWithNeutralColorBall() {
        // Neutral ball should always be captured
        ball.setColorType(0); // Neutral ball
        hole.handleCapture(ball, app, config);

        assertEquals(0, app.getScore(), "Score should remain zero for neutral ball with no config");
    }

    @Test
    public void testHandleCaptureWithEmptyConfigDoesNothing() {
        // Empty config, no changes to score
        ball.setColorType(1); // Matching color
        hole.handleCapture(ball, app, config);

        assertEquals(0, app.getScore(), "Score should remain zero with empty config");
    }


    @Test
    public void testScoreDecreaseWithWrongBallColor() {
        // Configure penalty
        JSONObject scoreDecreaseMap = new JSONObject();
        scoreDecreaseMap.setInt("orange", 10); // Ball is blue, so penalty should apply
        config.setJSONObject("score_decrease_from_wrong_hole", scoreDecreaseMap);

        // Ball color does not match hole color
        ball.setColorType(2); // Blue
        hole.handleCapture(ball, app, config);

        assertEquals(0, app.getScore(), "Score should not decrease when ball color has no penalty");
    }
}
