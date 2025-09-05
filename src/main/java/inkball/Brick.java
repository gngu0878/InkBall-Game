package inkball;

import processing.core.PImage;
import processing.core.PVector;

public class Brick extends Wall {
    private int hitCount;

    /**
     * Constructor for the Brick object.
     *
     * @param position The position of the brick in the game.
     * @param sprite The image representing the brick.
     * @param app Reference to the main application object.
     * @param colorType The color type of the brick.
     */
    public Brick(PVector position, PImage sprite, App app, int colorType) {
        super(position, sprite, app, colorType);
        this.hitCount = 0;
    }

    /**
     * Gets the hit count for the brick.
     *
     * @return The number of times the brick has been hit.
     */
    public int getHitCount() {
        return hitCount;
    }

    /**
     * Increments the hit count of the brick.
     */
    public void incrementHitCount() {
        hitCount++;
    }

    /**
     * Updates the state of the brick. Bricks are static, so no updates are required.
     */
    @Override
    public void update() {
        // No update logic needed for static bricks
    }

    /**
     * Checks for collisions between the brick and a ball.
     *
     * @param ball The ball to check for collision.
     * @return True if a collision is detected, false otherwise.
     */
    @Override
    public boolean checkCollision(Ball ball) {
        if (isCollidingWithBall(ball)) {
            int buffer = collisionBuffers.getOrDefault(ball, 0);

            if (buffer <= 0) {
                correctBallPosition(ball);
                reflectBallVelocity(ball);
                collisionBuffers.put(ball, 5);

                if (wallType == 0 || wallType == ball.getColorType()) {
                    incrementHitCount();
                    if (hitCount >= 3) {
                        app.getCurrentLevel().removeBrick(this);
                    }
                }
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
}