package inkball;

import processing.core.PVector;

public abstract class GameObject {
    protected PVector position;
    protected PVector velocity;
    protected App app;

    /**
     * Constructor for the GameObject class.
     *
     * @param position The initial position of the object.
     * @param app Reference to the main application.
     */
    public GameObject(PVector position, App app) {
        this.position = position;
        this.velocity = new PVector(0, 0);  // Default velocity
        this.app = app;
    }

    /**
     * Gets the current position of the object.
     *
     * @return The position of the object as a PVector.
     */
    public PVector getPosition() {
        return position;
    }

    /**
     * Sets a new position for the object.
     *
     * @param position The new position to set.
     */
    public void setPosition(PVector position) {
        this.position = position;
    }

    /**
     * Gets the current velocity of the object.
     *
     * @return The velocity of the object as a PVector.
     */
    public PVector getVelocity() {
        return velocity;
    }

    /**
     * Sets a new velocity for the object.
     *
     * @param velocity The new velocity to set.
     */
    public void setVelocity(PVector velocity) {
        this.velocity = velocity;
    }

    /**
     * Updates the state of the object. To be implemented by subclasses.
     */
    public abstract void update();

    /**
     * Displays the object on the screen. To be implemented by subclasses.
     */
    public abstract void display();

    /**
     * Checks for collisions with another GameObject.
     *
     * @param other Another game object to check collision with.
     * @return True if a collision is detected, false otherwise.
     */
    public abstract boolean checkCollision(GameObject other);

    /**
     * Checks for collisions with a Ball object.
     *
     * @param ball A Ball object to check collision with.
     * @return True if a collision is detected, false otherwise.
     */
}