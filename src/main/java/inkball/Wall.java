package inkball;

import processing.core.PImage;
import processing.core.PVector;
import java.util.*;

public class Wall extends GameObject {
    protected PImage sprite;
    protected int wallType;
    protected Map<Ball, Integer> collisionBuffers;

    /**
     * Constructor for the Wall object.
     *
     * @param position The position of the wall in the game.
     * @param sprite The image representing the wall.
     * @param app Reference to the main application object.
     * @param wallType The type of wall (e.g., color-changing).
     */
    public Wall(PVector position, PImage sprite, App app, int wallType) {
        super(position, app);
        this.sprite = sprite;
        this.wallType = wallType;
        this.collisionBuffers = new HashMap<>();
    }

    /**
     * Gets the type of the wall.
     *
     * @return The type of wall as an integer.
     */
    public int getWallType() {
        return wallType;
    }

    /**
     * Displays the wall on the screen using the provided sprite.
     * Overrides the display method from the GameObject class.
     */
    @Override
    public void display() {
        if (sprite != null) {
            app.image(sprite, position.x, position.y, App.CELLSIZE, App.CELLSIZE);
        }
    }

    /**
     * Updates the state of the wall. Static walls do not require updates.
     * Overrides the update method from the GameObject class.
     */
    @Override
    public void update() {
        // No updates for static walls
    }

    /**
     * Checks for collisions with another GameObject.
     * Overrides the checkCollision method from GameObject.
     *
     * @param other Another GameObject to check for collision.
     * @return True if a collision is detected, false otherwise.
     */
    @Override
    public boolean checkCollision(GameObject other) {
        if (other instanceof Ball) {
            return checkCollision((Ball) other);
        }
        return false;
    }

    /**
     * Reflects the velocity of the ball upon collision with the wall.
     *
     * @param ball The ball that collides with the wall.
     */
    protected void reflectBallVelocity(Ball ball) {
        PVector normal = new PVector(0, 0);

        float overlapLeft = (ball.getPosition().x + ball.getRadius()) - position.x;
        float overlapRight = (position.x + App.CELLSIZE) - (ball.getPosition().x - ball.getRadius());
        float overlapTop = (ball.getPosition().y + ball.getRadius()) - position.y;
        float overlapBottom = (position.y + App.CELLSIZE) - (ball.getPosition().y - ball.getRadius());

        boolean ballFromLeft = overlapLeft < overlapRight;
        boolean ballFromTop = overlapTop < overlapBottom;

        float minOverlapX = ballFromLeft ? overlapLeft : overlapRight;
        float minOverlapY = ballFromTop ? overlapTop : overlapBottom;

        if (minOverlapX < minOverlapY) {
            normal.x = ballFromLeft ? -1 : 1;
        } else {
            normal.y = ballFromTop ? -1 : 1;
        }

        ball.setVelocity(ball.getVelocity().sub(normal.mult(2 * ball.getVelocity().dot(normal))));
    }

    /**
     * Detects if the wall is colliding with a ball based on position and velocity.
     *
     * @param ball A Ball object to check for collision.
     * @return True if a collision is detected, false otherwise.
     */
    protected boolean isCollidingWithBall(Ball ball) {
        float cx = ball.getPosition().x;
        float cy = ball.getPosition().y;
        float cr = ball.getRadius();

        float rx = position.x;
        float ry = position.y;
        float rw = App.CELLSIZE;
        float rh = App.CELLSIZE;

        float closestX = Math.max(rx, Math.min(cx, rx + rw));
        float closestY = Math.max(ry, Math.min(cy, ry + rh));

        float distX = cx - closestX;
        float distY = cy - closestY;

        return (distX * distX + distY * distY) < (cr * cr);
    }

    /**
     * Changes the ball's color when a collision with the wall occurs.
     *
     * @param ball The Ball object whose color is to be changed.
     */
    public void changeBallColor(Ball ball) {
        if (wallType >= 1 && wallType <= 4) {
            ball.setColor(wallType);
        }
    }

    /**
     * Checks for collisions with a Ball and applies collision response if needed.
     *
     * @param ball A Ball object to check for collision.
     * @return True if a collision is detected, false otherwise.
     */
    public boolean checkCollision(Ball ball) {
        if (isCollidingWithBall(ball)) {
            int buffer = collisionBuffers.getOrDefault(ball, 0);

            if (buffer <= 0) {
                correctBallPosition(ball);
                reflectBallVelocity(ball);
                changeBallColor(ball);
                collisionBuffers.put(ball, 5);
            } else {
                collisionBuffers.put(ball, buffer - 1);
                correctBallPosition(ball);
            }
            return true;
        } else {
            collisionBuffers.put(ball, 0);
            return false;
        }
    }

    /**
     * Cleans up cooldowns by removing entries for balls that are no longer active.
     *
     * @param activeBalls List of currently active balls.
     */
    public void cleanupCooldowns(List<Ball> activeBalls) {
        collisionBuffers.keySet().removeIf(ball -> !activeBalls.contains(ball));
    }

    /**
     * Corrects the position of the ball to prevent it from overlapping with the wall.
     *
     * @param ball The ball to correct position for.
     */
    protected void correctBallPosition(Ball ball) {
        float overlapLeft = (ball.getPosition().x + ball.getRadius()) - position.x;
        float overlapRight = (position.x + App.CELLSIZE) - (ball.getPosition().x - ball.getRadius());
        float overlapTop = (ball.getPosition().y + ball.getRadius()) - position.y;
        float overlapBottom = (position.y + App.CELLSIZE) - (ball.getPosition().y - ball.getRadius());

        boolean ballFromLeft = overlapLeft < overlapRight;
        boolean ballFromTop = overlapTop < overlapBottom;

        float minOverlapX = ballFromLeft ? overlapLeft : -overlapRight;
        float minOverlapY = ballFromTop ? overlapTop : -overlapBottom;

        if (Math.abs(minOverlapX) < Math.abs(minOverlapY)) {
            ball.getPosition().x -= minOverlapX;
        } else {
            ball.getPosition().y -= minOverlapY;
        }
    }
}