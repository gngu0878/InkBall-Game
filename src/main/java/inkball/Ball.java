package inkball;

import processing.core.PVector;
import processing.core.PImage;
import processing.data.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Ball extends GameObject {
    private PImage sprite;
    private float radius;
    private float originalSize;
    private int colorType;

    /**
     * Constructor to initialize the ball.
     *
     * @param spawnPosition The position where the ball will spawn.
     * @param sprite The image representing the ball.
     * @param app Reference to the main application object.
     * @param colorType The color type of the ball.
     */
    public Ball(PVector spawnPosition, PImage sprite, App app, int colorType) {
        super(spawnPosition, app);
        this.sprite = sprite;
        this.velocity = new PVector(randomVelocity(), randomVelocity());
        this.originalSize = 12;
        this.radius = this.originalSize;
        this.colorType = colorType;
    }

    /**
     * Sets the size of the ball.
     *
     * @param newSize The new size of the ball.
     */
    public void setSize(float newSize) {
        this.radius = Math.max(0, newSize);
    }

    /**
     * Gets the current size (radius) of the ball.
     *
     * @return The current size (radius) of the ball.
     */
    public float getSize() {
        return this.radius;
    }

    /**
     * Gets the original size of the ball.
     *
     * @return The original size of the ball.
     */
    public float getOriginalSize() {
        return this.originalSize;
    }

    /**
     * Sets the original size of the ball.
     *
     * @param originalSize The original size to set.
     */
    public void setOriginalSize(float originalSize) {
        this.originalSize = originalSize;
    }

    /**
     * Generates a random velocity for the ball.
     *
     * @return A float representing the random velocity (-2 or 2).
     */
    private float randomVelocity() {
        float randomValue = app.random(1);
        return randomValue < 0.5 ? -2 : 2;
    }

    /**
     * Updates the ball's position based on its velocity.
     * This method is overridden from the GameObject class.
     */
    @Override
    public void update() {
        position.add(velocity);
    }

    /**
     * Displays the ball on the screen with the correct size.
     * This method is overridden from the GameObject class.
     */
    @Override
    public void display() {
        if (sprite != null) {
            app.image(sprite, position.x - radius, position.y - radius, radius * 2, radius * 2);
        }
    }

    /**
     * Applies a force to the ball, affecting its velocity.
     *
     * @param force The force to apply to the ball.
     */
    public void applyForce(PVector force) {
        velocity.add(force);
    }

    /**
     * Handles the ball being captured by a hole.
     *
     * @param hole The hole that captured the ball.
     * @param app Reference to the main application object.
     * @param config Configuration for scoring and capture behavior.
     */
    public void capture(Hole hole, App app, JSONObject config) {
        if (this.colorType == hole.getColorType() || this.colorType == -1 || hole.getColorType() == -1) {
            app.increaseScore(hole.getCapturePoints(config, getColorName(), hole.getColorName()));
            float distance = PVector.dist(position, new PVector(hole.getPosition().x + App.CELLSIZE, hole.getPosition().y + App.CELLSIZE));
            reduceSize(distance);

            if (radius <= 0) {
                respawn();
            }
        } else {
            app.decreaseScore(hole.getWrongCapturePenalty(config, getColorName(), hole.getColorName()));
            respawn();
        }
    }

    /**
     * Reduces the size of the ball based on its distance to the hole.
     *
     * @param distance The distance between the ball and the hole.
     */
    public void reduceSize(float distance) {
        radius = Math.max(0, originalSize * (1 - (distance / 32)));
    }

    /**
     * Respawns the ball at a random position with original size and velocity.
     */
    public void respawn() {
        this.position = new PVector(app.random(app.width), app.random(app.height));
        this.velocity = new PVector(randomVelocity(), randomVelocity());
        this.radius = originalSize;
        this.colorType = -1;
    }

    /**
     * Gets the name of the ball's color based on its color type.
     *
     * @return The name of the color as a string.
     */
    public String getColorName() {
        switch (this.colorType) {
            case 0: return "grey";
            case 1: return "orange";
            case 2: return "blue";
            case 3: return "green";
            case 4: return "yellow";
            default: return "unknown";
        }
    }

    /**
     * Checks for collisions between the ball and lines.
     *
     * @param lines A list of Line objects to check for collisions.
     * @param playerLines A list of lines drawn by the player.
     */
    public void checkLineCollisions(List<Line> lines, List<Line> playerLines) {
        List<Line> linesToRemove = new ArrayList<>();
        for (Line line : lines) {
            if (line.checkCollision(this)) {
                PVector newVelocity = line.calculateNewVelocity(this);
                this.velocity.set(newVelocity);
                linesToRemove.add(line);
            }
        }
        playerLines.removeAll(linesToRemove);
    }

    /**
     * Sets the velocity of the ball.
     *
     * @param newVelocity The new velocity of the ball.
     */
    public void setVelocity(PVector newVelocity) {
        this.velocity.set(newVelocity);
    }

    /**
     * Gets the velocity of the ball.
     *
     * @return The velocity of the ball.
     */
    public PVector getVelocity() {
        return this.velocity;
    }

    /**
     * Checks for collisions with another GameObject.
     * This method is overridden from the GameObject class.
     *
     * @param other The other GameObject to check for collision.
     * @return True if a collision is detected, false otherwise.
     */
    @Override
    public boolean checkCollision(GameObject other) {
        if (other instanceof Ball) {
            Ball otherBall = (Ball) other;
            return PVector.dist(this.position, otherBall.getPosition()) < this.radius + otherBall.getRadius();
        }
        return false;
    }

    /**
     * Gets the radius of the ball.
     *
     * @return The radius of the ball.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the ball.
     *
     * @param radius The radius to set.
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Sets the color of the ball based on a wall type.
     *
     * @param wallType The type of the wall that changes the ball's color.
     */
    public void setColor(int wallType) {
        this.colorType = wallType;
        this.sprite = app.getBallSprites()[this.colorType];
    }

    /**
     * Gets the color type of the ball.
     *
     * @return The color type of the ball.
     */
    public int getColorType() {
        return colorType;
    }

    /**
     * Sets the color type of the ball.
     *
     * @param colorType The color type to set.
     */
    public void setColorType(int colorType) {
        this.colorType = colorType;
        this.sprite = app.getBallSprites()[this.colorType];
    }

    /**
     * Gets the sprite of the ball.
     *
     * @return The sprite image of the ball.
     */
    public PImage getSprite() {
        return sprite;
    }

    /**
     * Sets the sprite of the ball.
     *
     * @param sprite The sprite image to set.
     */
    public void setSprite(PImage sprite) {
        this.sprite = sprite;
    }

    /**
     * Gets the position of the ball.
     *
     * @return The position of the ball.
     */
    public PVector getPosition() {
        return this.position;
    }

    /**
     * Sets the position of the ball.
     *
     * @param position The position to set.
     */
    public void setPosition(PVector position) {
        this.position = position;
    }
}