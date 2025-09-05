package inkball;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class Line extends GameObject {
    private List<PVector> points;
    private boolean isDragging;

    /**
     * Constructor for the Line object.
     *
     * @param points A list of PVector points defining the line.
     * @param app Reference to the main application object.
     */
    public Line(List<PVector> points, App app) {
        super(new PVector(0, 0), app);  // Position is not relevant for Line objects
        this.points = points;
        this.isDragging = false;
    }

    /**
     * Starts drawing the line when the mouse is pressed.
     *
     * @param startPoint The starting point of the line.
     */
    public void startDrawing(PVector startPoint) {
        this.isDragging = true;
        points.clear();  // Start a new line
        addPoint(startPoint);  // Add the first point
    }

    /**
     * Stops drawing the line when the mouse is released.
     */
    public void stopDrawing() {
        this.isDragging = false;
    }

    /**
     * Adds a point to the line if dragging is active.
     *
     * @param point The point to add to the line.
     */
    public void addPoint(PVector point) {
        if (isDragging) {
            points.add(point);
            if (checkBallCollision()) {
                app.removeCurrentLine();
            }
        }
    }

    /**
     * Checks for collisions between the ball and the line.
     *
     * @return True if a ball collides with the line, false otherwise.
     */
    public boolean checkBallCollision() {
        for (Ball ball : app.getCurrentLevel().getBalls()) {
            if (checkCollision(ball)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Displays the line on the screen.
     * Overrides the display method of the GameObject.
     */
    @Override
    public void display() {
        app.stroke(0);  // Set stroke color to black
        app.strokeWeight(10);  // Set line thickness
        app.noFill();
        app.beginShape();
        for (PVector point : points) {
            app.vertex(point.x, point.y);
        }
        app.endShape();
    }

    /**
     * Checks if a given point is on or near the line.
     *
     * @param point The point to check.
     * @return True if the point is near the line, false otherwise.
     */
    public boolean isPointOnLine(PVector point) {
        float threshold = 10;
        for (int i = 0; i < points.size() - 1; i++) {
            PVector start = points.get(i);
            PVector end = points.get(i + 1);
            if (distanceToLine(point, start, end) < threshold) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for a collision between the ball and the line.
     *
     * @param ball The ball to check for collision.
     * @return True if the ball collides with the line, false otherwise.
     */
    public boolean checkCollision(Ball ball) {
        for (int i = 0; i < points.size() - 1; i++) {
            PVector start = points.get(i);
            PVector end = points.get(i + 1);
            if (distanceToLine(ball.getPosition(), start, end) < ball.getRadius()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for collision with another GameObject.
     * Not used for lines but required as part of the GameObject class.
     *
     * @param other Another GameObject to check for collision.
     * @return Always returns false as lines do not collide directly with other game objects.
     */
    @Override
    public boolean checkCollision(GameObject other) {
        return false;
    }

    /**
     * Calculates the new velocity of the ball after colliding with the line.
     *
     * @param ball The ball involved in the collision.
     * @return The new velocity vector after the collision.
     */
    public PVector calculateNewVelocity(Ball ball) {
        PVector v = ball.getVelocity();
        PVector closestSegmentStart = points.get(0);
        PVector closestSegmentEnd = points.get(1);
        for (int i = 1; i < points.size() - 1; i++) {
            PVector start = points.get(i);
            PVector end = points.get(i + 1);
            if (distanceToLine(ball.getPosition(), start, end) < ball.getRadius()) {
                closestSegmentStart = start;
                closestSegmentEnd = end;
            }
        }

        PVector segment = PVector.sub(closestSegmentEnd, closestSegmentStart);
        PVector normal1 = new PVector(-segment.y, segment.x).normalize();
        PVector normal2 = new PVector(segment.y, -segment.x).normalize();

        PVector midpoint = PVector.add(closestSegmentStart, closestSegmentEnd).mult(0.5f);
        PVector candidate1 = PVector.add(midpoint, normal1);
        PVector candidate2 = PVector.add(midpoint, normal2);

        PVector chosenNormal = (PVector.dist(candidate1, ball.getPosition()) < PVector.dist(candidate2, ball.getPosition())) ? normal1 : normal2;

        float dotProduct = PVector.dot(v, chosenNormal);
        PVector newVelocity = PVector.sub(v, PVector.mult(chosenNormal, 2 * dotProduct));

        return newVelocity;
    }

    /**
     * Calculates the distance from a point to a line segment.
     *
     * @param point The point to check.
     * @param start The start of the line segment.
     * @param end The end of the line segment.
     * @return The distance from the point to the line segment.
     */
    public float distanceToLine(PVector point, PVector start, PVector end) {
        PVector lineDir = PVector.sub(end, start);
        float t = PVector.sub(point, start).dot(lineDir) / lineDir.magSq();
        t = PApplet.constrain(t, 0, 1);
        PVector closestPoint = PVector.add(start, PVector.mult(lineDir, t));
        return PVector.dist(point, closestPoint);
    }

    /**
     * Updates the line by checking for ball collisions.
     * Overrides the update method of GameObject.
     */
    @Override
    public void update() {
        if (checkBallCollision()) {
            app.removeLine(this);
        }
    }

    /**
     * Sets the velocity of the line.
     * Although not typically used for lines, it's available for compatibility with GameObject.
     *
     * @param newVelocity The new velocity of the line.
     */
    public void setVelocity(PVector newVelocity) {
        this.velocity = newVelocity;
    }

    /**
     * Returns the points that make up the line.
     *
     * @return A copy of the list of points.
     */
    public List<PVector> getPoints() {
        return new ArrayList<>(points); // Return a copy to prevent external modification
    }

    /**
     * Checks whether the line is currently being dragged (drawn).
     *
     * @return True if the line is being dragged, false otherwise.
     */
    public boolean isDragging() {
        return isDragging;
    }

    /**
     * Gets the current velocity of the line (not typically used for lines).
     *
     * @return The velocity of the line.
     */
    public PVector getVelocity() {
        return velocity;
    }

    /**
     * Gets the reference to the main application object.
     *
     * @return The reference to the App object.
     */
    public App getApp() {
        return app;
    }
}