package inkball;

import processing.core.PVector;
import processing.core.PImage;

public class Spawner extends GameObject {
    private PImage sprite;

    /**
     * Constructor for the Spawner object.
     *
     * @param position The position of the spawner in the game.
     * @param sprite The image representing the spawner.
     * @param app The main application object.
     */
    public Spawner(PVector position, PImage sprite, App app) {
        super(position, app);
        this.sprite = sprite;
    }

    /**
     * The Spawner object is static, so no updates are needed for position or velocity.
     */
    @Override
    public void update() {
        // No updates needed for static object
    }

    /**
     * Spawns a new ball at the spawner's current position.
     *
     * @param sprite The image representing the ball.
     * @param colorType The color type of the ball to be spawned.
     * @return A new Ball object at the spawner's position.
     */
    public Ball spawnBall(PImage sprite, int colorType) {
        PVector spawnPosition = PVector.add(position, new PVector(App.CELLSIZE / 2, App.CELLSIZE / 2));
        return new Ball(spawnPosition, sprite, app, colorType);
    }

    /**
     * Displays the spawner on the screen using the provided sprite.
     */
    @Override
    public void display() {
        if (sprite != null) {
            app.image(sprite, position.x, position.y, App.CELLSIZE, App.CELLSIZE);
        }
    }

    /**
     * Checks for collisions with another game object. Spawners do not collide with any objects.
     *
     * @param other Another game object.
     * @return Always returns false since Spawner does not collide.
     */
    @Override
    public boolean checkCollision(GameObject other) {
        return false;
    }
}