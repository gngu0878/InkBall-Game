package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONObject;

public class Hole extends GameObject {
    private PImage sprite;
    private int colorType;

    /**
     * Constructor for the Hole object.
     *
     * @param position The position of the hole in the game.
     * @param sprite The image representing the hole.
     * @param app Reference to the main application object.
     * @param colorType The color type of the hole.
     * @param scoreIncreaseModifier Modifier for score increase on ball capture.
     * @param scoreDecreaseModifier Modifier for score decrease on wrong capture.
     */
    public Hole(PVector position, PImage sprite, PApplet app, int colorType, float scoreIncreaseModifier, float scoreDecreaseModifier) {
        super(position, (App) app);
        this.sprite = sprite;
        this.colorType = colorType;
    }

    /**
     * Displays the hole on the screen.
     * This method is overridden from the GameObject class.
     */
    @Override
    public void display() {
        if (sprite != null) {
            app.image(sprite, position.x, position.y, App.CELLSIZE * 2, App.CELLSIZE * 2);
        }
    }

    /**
     * Attracts a ball toward the hole if it is within range and handles ball capture.
     *
     * @param ball The ball to attract.
     * @param app Reference to the main application object.
     * @param config Configuration for ball capture.
     */
    public void attractBall(Ball ball, App app, JSONObject config) {
        PVector holeCenter = PVector.add(position, new PVector(App.CELLSIZE, App.CELLSIZE));
        PVector ballCenter = ball.getPosition();
        PVector direction = PVector.sub(holeCenter, ballCenter);
        float distance = direction.mag();

        if (distance < 32) {
            direction.normalize();
            ball.applyForce(direction.mult(0.005f * distance));
            float originalSize = ball.getOriginalSize();
            float newSize = Math.max(originalSize * (distance / 32), 8);
            ball.setSize(newSize);
        } else {
            ball.setSize(ball.getOriginalSize());
        }

        if (distance < ball.getRadius()) {
            handleCapture(ball, app, config);
        }
    }

    /**
     * Handles the capture logic when a ball reaches the hole.
     *
     * @param ball The ball being captured.
     * @param app Reference to the main application object.
     * @param config Configuration for scoring and penalties.
     */
    public void handleCapture(Ball ball, App app, JSONObject config) {
        String ballColor = ball.getColorName();
        String holeColor = getColorName();

        if (config.hasKey("score_increase_from_hole_capture") && config.hasKey("score_decrease_from_wrong_hole")) {
            JSONObject scoreIncreaseMap = config.getJSONObject("score_increase_from_hole_capture");
            JSONObject scoreDecreaseMap = config.getJSONObject("score_decrease_from_wrong_hole");

            if (this.colorType == ball.getColorType() || this.colorType == 0 || ball.getColorType() == 0) {
                int baseScore = getCapturePoints(scoreIncreaseMap, ballColor, holeColor);
                float scoreIncreaseModifier = app.getCurrentLevel().getScoreIncreaseMultiplier();
                int finalScore = (int) (baseScore * scoreIncreaseModifier);
                app.increaseScore(finalScore);
                removeBallFromGame(ball, app);
            } else {
                int basePenalty = getWrongCapturePenalty(scoreDecreaseMap, ballColor, holeColor);
                float scoreDecreaseModifier = app.getCurrentLevel().getScoreDecreaseMultiplier();
                int finalPenalty = (int) (basePenalty * scoreDecreaseModifier);
                app.decreaseScore(finalPenalty);

                if (this.colorType != 0 && ball.getColorType() != 0) {
                    app.getCurrentLevel().addBallToRespawnQueue(ball);
                }
                removeBallFromGame(ball, app);
            }
        }
    }

    /**
     * Removes the ball from the game after being captured or penalized.
     *
     * @param ball The ball to remove.
     * @param app Reference to the main application object.
     */
    public void removeBallFromGame(Ball ball, App app) {
        app.getCurrentLevel().removeBall(ball);
    }

    /**
     * Gets the color name of the hole based on its color type.
     *
     * @return The name of the hole's color.
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
     * Gets the capture points for successfully matching the ball and hole colors.
     *
     * @param scoreIncreaseMap A map containing score values for each color.
     * @param ballColor The color of the ball.
     * @param holeColor The color of the hole.
     * @return The score for a successful capture.
     */
    public int getCapturePoints(JSONObject scoreIncreaseMap, String ballColor, String holeColor) {
        return scoreIncreaseMap.hasKey(ballColor) ? scoreIncreaseMap.getInt(ballColor) : 0;
    }

    /**
     * Gets the penalty for capturing a ball with the wrong color in the hole.
     *
     * @param scoreDecreaseMap A map containing penalty values for each color.
     * @param ballColor The color of the ball.
     * @param holeColor The color of the hole.
     * @return The penalty for an incorrect capture.
     */
    public int getWrongCapturePenalty(JSONObject scoreDecreaseMap, String ballColor, String holeColor) {
        return scoreDecreaseMap.hasKey(ballColor) ? scoreDecreaseMap.getInt(ballColor) : 0;
    }

    /**
     * Gets the color type of the hole.
     *
     * @return The color type of the hole.
     */
    public int getColorType() {
        return colorType;
    }

    /**
     * Updates the state of the hole.
     * This method is overridden from the GameObject class.
     * No continuous updates are required for holes.
     */
    @Override
    public void update() {
        // No update logic needed for holes
    }

    /**
     * Checks for collisions between the hole and another GameObject.
     * This method is overridden from the GameObject class.
     * Holes do not collide directly with other objects.
     *
     * @param other Another GameObject to check for collision.
     * @return Always returns false as holes do not collide directly.
     */
    @Override
    public boolean checkCollision(GameObject other) {
        return false;
    }

    /**
     * Gets the position of the hole.
     *
     * @return The position of the hole.
     */
    public PVector getPosition() {
        return position;
    }

    /**
     * Sets the position of the hole.
     *
     * @param position The position to set for the hole.
     */
    public void setPosition(PVector position) {
        this.position = position;
    }

    /**
     * Gets the sprite of the hole.
     *
     * @return The sprite of the hole.
     */
    public PImage getSprite() {
        return sprite;
    }

    /**
     * Sets the sprite of the hole.
     *
     * @param sprite The sprite image to set for the hole.
     */
    public void setSprite(PImage sprite) {
        this.sprite = sprite;
    }

    /**
     * Sets the color type of the hole.
     *
     * @param colorType The color type to set for the hole.
     */
    public void setColorType(int colorType) {
        this.colorType = colorType;
    }
}