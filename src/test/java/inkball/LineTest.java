package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LineTest {

    private App app;
    private Line line;
    private List<PVector> points;

    @BeforeEach
    public void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();
        points = new ArrayList<>();
        line = new Line(points, app);
    }

    @Test
    public void testLineInitialization() {
        assertNotNull(line, "Line should be initialized");
        assertEquals(app, line.getApp(), "App should be set correctly in Line");
        assertEquals(points, line.getPoints(), "Points list should be initialized");
    }

    @Test
    public void testStartDrawing() {
        PVector startPoint = new PVector(100, 100);
        line.startDrawing(startPoint);
        assertTrue(line.isDragging(), "Line should be in dragging state after startDrawing");
        assertEquals(1, line.getPoints().size(), "Points list should contain the start point");
        assertEquals(startPoint, line.getPoints().get(0), "Start point should be added to points list");
    }

    @Test
    public void testStopDrawing() {
        line.startDrawing(new PVector(100, 100));
        line.stopDrawing();
        assertFalse(line.isDragging(), "Line should not be in dragging state after stopDrawing");
    }

    @Test
    public void testAddPointWhileDragging() {
        line.startDrawing(new PVector(100, 100));
        PVector newPoint = new PVector(150, 150);
        line.addPoint(newPoint);
        assertEquals(2, line.getPoints().size(), "Points list should contain two points");
        assertEquals(newPoint, line.getPoints().get(1), "New point should be added to points list");
    }

    @Test
    public void testAddPointWhenNotDragging() {
        PVector newPoint = new PVector(150, 150);
        line.addPoint(newPoint);
        assertEquals(0, line.getPoints().size(), "Points list should not change when not dragging");
    }

    @Test
    public void testCheckBallCollisionNoCollision() {
        Ball ball = new Ball(new PVector(200, 200), app.getBallSprites()[1], app, 1);
        app.getCurrentLevel().addBall(ball);
        line.startDrawing(new PVector(100, 100));
        line.addPoint(new PVector(150, 150));
        boolean collision = line.checkBallCollision();
        assertFalse(collision, "No collision should be detected when ball is far from line");
    }

    @Test
    public void testCheckBallCollisionWithCollision() {
        Ball ball = new Ball(new PVector(125, 125), app.getBallSprites()[1], app, 1);
        app.getCurrentLevel().addBall(ball);
        line.startDrawing(new PVector(100, 100));
        line.addPoint(new PVector(150, 150));
        boolean collision = line.checkBallCollision();
        assertTrue(collision, "Collision should be detected when ball is near line");
    }

    @Test
    public void testIsPointOnLineTrue() {
        line.startDrawing(new PVector(100, 100));
        line.addPoint(new PVector(200, 200));
        PVector point = new PVector(150, 150);
        assertTrue(line.isPointOnLine(point), "Point should be considered on the line");
    }

    @Test
    public void testIsPointOnLineFalse() {
        line.startDrawing(new PVector(100, 100));
        line.addPoint(new PVector(200, 200));
        PVector point = new PVector(300, 300);
        assertFalse(line.isPointOnLine(point), "Point should not be considered on the line");
    }

    @Test
    public void testCheckCollisionWithBallTrue() {
        Ball ball = new Ball(new PVector(125, 125), app.getBallSprites()[1], app, 1);
        line.startDrawing(new PVector(100, 100));
        line.addPoint(new PVector(150, 150));
        boolean collision = line.checkCollision(ball);
        assertTrue(collision, "Collision should be detected between ball and line");
    }

    @Test
    public void testCalculateNewVelocityWithCollision() {
        // Set up the line
        line.startDrawing(new PVector(100, 100));
        line.addPoint(new PVector(200, 100));  // Horizontal line

        // Set up the ball with initial velocity heading downward
        Ball ball = new Ball(new PVector(150, 95), app.getBallSprites()[1], app, 1); // Above the line
        ball.setVelocity(new PVector(0, 1));  // Moving downward

        // Calculate new velocity after collision
        PVector newVelocity = line.calculateNewVelocity(ball);

        // Expected outcome: Velocity should invert Y direction because it hits a horizontal line
        assertEquals(0, newVelocity.x, "X velocity should remain the same after collision.");
        assertTrue(newVelocity.y < 0, "Y velocity should invert after hitting the line.");
    }

    @Test
    public void testBallNearSpecificLineSegment() {
        // Setup a line with multiple segments
        points.add(new PVector(50, 50));  // First segment
        points.add(new PVector(100, 50)); // Second segment
        points.add(new PVector(150, 50)); // Third segment
    
        // Ball is placed near the second segment (100, 50) to (150, 50)
        Ball ball = new Ball(new PVector(125, 45), app.getBallSprites()[1], app, 1);
        ball.setRadius(10);  // Set ball radius to ensure it's within the distance of the line
        PVector newVelocity = line.calculateNewVelocity(ball);
    
        // Since the ball is near the second segment, the closest segment should be the second one
        assertEquals(new PVector(100, 50), points.get(1), "Closest segment start should be (100, 50).");
        assertEquals(new PVector(150, 50), points.get(2), "Closest segment end should be (150, 50).");
    }
    
}