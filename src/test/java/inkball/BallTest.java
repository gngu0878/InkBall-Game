package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PVector;
import processing.core.PImage;
import processing.data.JSONObject;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class BallTest {

    private Ball ball;
    private App app;
    private PImage sprite;
    private PVector initialPosition;

    @BeforeEach
    public void setup() {
        app = new App();  // Use the real App instance
        sprite = new PImage();  // Use a real PImage object
        initialPosition = new PVector(100, 100);

        // Create the ball with manually controlled velocity by setting it after instantiation
        ball = new Ball(initialPosition, sprite, app, 1);
        ball.setVelocity(new PVector(2, 2));  // Set velocity manually for deterministic tests
    }

    @Test
    public void testBallInitialization() {
        assertEquals(initialPosition, ball.getPosition(), "Ball should be initialized at the correct position");
        assertEquals(12, ball.getSize(), "Ball should have the correct initial size");
        assertEquals(1, ball.getColorType(), "Ball should have the correct initial color type");
        assertNotNull(ball.getSprite(), "Sprite should be set for the ball");
    }

    @Test
    public void testSetSize() {
        ball.setSize(20);
        assertEquals(20, ball.getSize(), "Ball's size should be updated correctly");

        ball.setSize(-5);
        assertEquals(0, ball.getSize(), "Ball's size should not go below 0");
    }

    @Test
    public void testGetOriginalSize() {
        assertEquals(12, ball.getOriginalSize(), "Original size should be initialized correctly");
    }

    @Test
    public void testSetOriginalSize() {
        ball.setOriginalSize(15);
        assertEquals(15, ball.getOriginalSize(), "Original size should be updated correctly");
    }

    @Test
    public void testUpdatePosition() {
        PVector initialPosition = ball.getPosition().copy();
        ball.update();
        assertNotEquals(initialPosition, ball.getPosition(), "Ball's position should change based on velocity");
    }

    @Test
    public void testApplyForce() {
        PVector initialVelocity = ball.getVelocity().copy();
        ball.applyForce(new PVector(2, 3));
        assertEquals(new PVector(initialVelocity.x + 2, initialVelocity.y + 3), ball.getVelocity(), "Force should be applied correctly to velocity");
    }

    @Test
    public void testCaptureWrongHole() {
        // Use a different color type for the hole
        PImage holeSprite = new PImage();
        Hole hole = new Hole(new PVector(100, 100), holeSprite, app, 2, 1.0f, 0.5f);
        JSONObject config = new JSONObject();

        ball.capture(hole, app, config);
        assertEquals(ball.getOriginalSize(), ball.getSize(), "Ball should respawn with original size after being captured by the wrong hole");
    }

    @Test
    public void testReduceSize() {
        ball.reduceSize(10);
        assertTrue(ball.getSize() < ball.getOriginalSize(), "Ball's size should reduce based on distance to the hole");

        ball.reduceSize(50);
        assertEquals(0, ball.getSize(), "Ball's size should not go below 0 after significant reduction");
    }

    @Test
    public void testRespawn() {
        ball.respawn();
        assertEquals(ball.getOriginalSize(), ball.getSize(), "Ball should respawn with original size");
        assertEquals(-1, ball.getColorType(), "Ball should respawn with a default color type (-1)");
    }

    @Test
    public void testGetColorName() {
        ball.setColorType(0);
        assertEquals("grey", ball.getColorName(), "Color name should be grey");

        ball.setColorType(1);
        assertEquals("orange", ball.getColorName(), "Color name should be orange");

        ball.setColorType(2);
        assertEquals("blue", ball.getColorName(), "Color name should be blue");

        ball.setColorType(3);
        assertEquals("green", ball.getColorName(), "Color name should be green");

        ball.setColorType(4);
        assertEquals("yellow", ball.getColorName(), "Color name should be yellow");
    }

    @Test
    public void testCheckLineCollisions() {
        Line line = new Line(new ArrayList<>(), app) {
            @Override
            public boolean checkCollision(Ball ball) {
                return true;  // Force collision to return true
            }

            @Override
            public PVector calculateNewVelocity(Ball ball) {
                return new PVector(1, 1);  // Return constant velocity
            }
        };

        List<Line> lines = new ArrayList<>();
        List<Line> playerLines = new ArrayList<>();
        lines.add(line);
        playerLines.add(line);

        ball.checkLineCollisions(lines, playerLines);
        assertEquals(new PVector(1, 1), ball.getVelocity(), "Ball's velocity should update after colliding with a line");
        assertFalse(playerLines.contains(line), "Line should be removed from player lines after collision");
    }

    @Test
    public void testCheckCollisionWithBall() {
        Ball otherBall = new Ball(new PVector(120, 100), sprite, app, 2);
        assertTrue(ball.checkCollision(otherBall), "Ball should detect a collision with another ball");
    }

    @Test
    public void testSetAndGetColor() {
        ball.setColor(2);
        assertEquals(2, ball.getColorType(), "Ball's color type should be updated correctly");
        assertEquals(app.getBallSprites()[2], ball.getSprite(), "Ball's sprite should update according to its color");
    }

    @Test
    public void testSetAndGetSprite() {
        PImage newSprite = new PImage();
        ball.setSprite(newSprite);
        assertEquals(newSprite, ball.getSprite(), "Ball's sprite should be updated correctly");
    }

    @Test
    public void testSetAndGetPosition() {
        PVector newPosition = new PVector(200, 300);
        ball.setPosition(newPosition);
        assertEquals(newPosition, ball.getPosition(), "Ball's position should be updated correctly");
    }

    @Test
    public void testSetAndGetVelocity() {
        PVector newVelocity = new PVector(5, 5);
        ball.setVelocity(newVelocity);
        assertEquals(newVelocity, ball.getVelocity(), "Ball's velocity should be updated correctly");
    }

    @Test
    public void testCheckCollisionWithNonBallObject() {
        // Create the first ball (this object)
        Ball ball1 = new Ball(new PVector(100, 100), new PImage(), app, 1);
        ball1.setRadius(10); // Set radius for the first ball
    
        // Create a non-ball object (e.g., Line, Hole, Spawner)
        Line line = new Line(new ArrayList<>(), app);  // Example of another game object
    
        // Check if collision is detected between the ball and a non-ball object
        boolean collisionResult = ball1.checkCollision(line);
    
        // Assert that no collision is detected since the object is not a ball
        assertFalse(collisionResult, "No collision should be detected with a non-ball object.");
    }

    @Test
    public void testCheckCollisionNoCollisionWithAnotherBall() {
        // Create the first ball (this object)
        Ball ball1 = new Ball(new PVector(100, 100), new PImage(), app, 1);
        ball1.setRadius(10); // Set radius for the first ball
    
        // Create another ball to check collision
        Ball ball2 = new Ball(new PVector(120, 100), new PImage(), app, 1); // Position close but balls should not collide
        ball2.setRadius(10); // Set radius for the second ball
    
        // Check if collision is detected (even though they shouldn't collide in this game)
        boolean collisionResult = ball1.checkCollision(ball2);
    
        // Assert that no collision is detected since balls do not collide
        assertFalse(collisionResult, "No collision should be detected between two balls as per the game's rules.");
    }

    @Test
    public void testCaptureConditionTrueWithHoleConstructor() {
        App app = new App(); 
        PImage sprite = new PImage(); 
        PVector holePosition = new PVector(150, 150);
        float scoreIncreaseModifier = 1.0f; 
        float scoreDecreaseModifier = 1.0f;
        int colorType = 1; 
    
        // Create a real instance of Hole using its constructor
        Hole hole = new Hole(holePosition, sprite, app, colorType, scoreIncreaseModifier, scoreDecreaseModifier);
        JSONObject config = new JSONObject();  
    
        ball.setColorType(1);
        ball.capture(hole, app, config);
    
        // Assertions to verify behavior
        assertFalse(app.getScore() > 0);
        assertTrue(ball.getRadius() > 0);
    }    
}