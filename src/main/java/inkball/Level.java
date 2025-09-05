package inkball;

import processing.core.PVector;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private List<Ball> balls;
    private List<Wall> walls;
    private List<Hole> holes;
    private List<Spawner> spawners;
    private List<Line> lines;
    private App app;
    private PImage[] wallSprites;
    private PImage[] holeSprites;
    private PImage[] ballSprites;
    private PImage spawnerSprite;
    private float spawnInterval;
    private int spawnCounter;
    private List<String> configBalls;
    private float scoreIncreaseMultiplier;
    private float scoreDecreaseMultiplier;
    private final float MIN_SPAWN_INTERVAL = 1.0f;
    private PImage[] brickSprites;
    private List<Brick> bricks;

    /**
     * Constructor for the Level object.
     *
     * @param app Reference to the main application object.
     */
    public Level(App app) {
        this.app = app;
        this.balls = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.spawners = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.configBalls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.spawnCounter = 0;
        wallSprites = new PImage[5];
        holeSprites = new PImage[5];
        ballSprites = new PImage[5];
        brickSprites = new PImage[5];
        loadSprites();
    }


    public void setBalls(List<Ball> balls) {
        this.balls = balls;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }

    public List<Hole> getHoles() {
        return holes;
    }

    public void setHoles(List<Hole> holes) {
        this.holes = holes;
    }

    public List<Spawner> getSpawners() {
        return spawners;
    }

    public void setSpawners(List<Spawner> spawners) {
        this.spawners = spawners;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public PImage[] getWallSprites() {
        return wallSprites;
    }

    public void setWallSprites(PImage[] wallSprites) {
        this.wallSprites = wallSprites;
    }

    public PImage[] getHoleSprites() {
        return holeSprites;
    }

    public void setHoleSprites(PImage[] holeSprites) {
        this.holeSprites = holeSprites;
    }

    public PImage[] getBallSprites() {
        return ballSprites;
    }

    public void setBallSprites(PImage[] ballSprites) {
        this.ballSprites = ballSprites;
    }

    public PImage getSpawnerSprite() {
        return spawnerSprite;
    }

    public void setSpawnerSprite(PImage spawnerSprite) {
        this.spawnerSprite = spawnerSprite;
    }

    public float getSpawnInterval() {
        return spawnInterval;
    }

    public void setSpawnInterval(float spawnInterval) {
        this.spawnInterval = spawnInterval;
    }

    public int getSpawnCounter() {
        return spawnCounter;
    }

    public void setSpawnCounter(int spawnCounter) {
        this.spawnCounter = spawnCounter;
    }

    public List<String> getConfigBalls() {
        return configBalls;
    }

    public void setConfigBalls(List<String> configBalls) {
        this.configBalls = configBalls;
    }

    public float getScoreIncreaseMultiplier() {
        return scoreIncreaseMultiplier;
    }

    public void setScoreIncreaseMultiplier(float scoreIncreaseMultiplier) {
        this.scoreIncreaseMultiplier = scoreIncreaseMultiplier;
    }

    public void setScoreDecreaseMultiplier(float scoreDecreaseMultiplier) {
        this.scoreDecreaseMultiplier = scoreDecreaseMultiplier;
    }

    public PImage[] getBrickSprites() {
        return brickSprites;
    }

    public void setBrickSprites(PImage[] brickSprites) {
        this.brickSprites = brickSprites;
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public void setBricks(List<Brick> bricks) {
        this.bricks = bricks;
    }

    public float getMinSpawnInterval() {
        return MIN_SPAWN_INTERVAL;
    }

    /**
     * Adds a ball to the respawn queue.
     *
     * @param ball The Ball object to add to the respawn queue.
     */
    public void addBallToRespawnQueue(Ball ball) {
        String ballColor = ball.getColorName();
        configBalls.add(ballColor);
    }

    /**
     * Gets the score decrease multiplier.
     *
     * @return The score decrease multiplier.
     */
    public float getScoreDecreaseMultiplier() {
        return this.scoreDecreaseMultiplier;
    }

    /**
     * Loads sprites for walls, holes, balls, spawners, and bricks.
     */
    public void loadSprites() {
        for (int i = 0; i < 5; i++) {
            PImage wallSprite = loadImageWithDecode("/inkball/wall" + i + ".png");
            if (wallSprite != null) {
                wallSprites[i] = wallSprite;
            }

            PImage holeSprite = loadImageWithDecode("/inkball/hole" + i + ".png");
            if (holeSprite != null) {
                holeSprites[i] = holeSprite;
            }

            PImage ballSprite = loadImageWithDecode("/inkball/ball" + i + ".png");
            if (ballSprite != null) {
                ballSprites[i] = ballSprite;
            }

            PImage brickSprite = loadImageWithDecode("/inkball/brick" + i + ".png");
            if (brickSprite != null) {
                brickSprites[i] = brickSprite;
            }
        }
        spawnerSprite = loadImageWithDecode("/inkball/entrypoint.png");
    }

    /**
     * Loads an image from the provided path and decodes it.
     *
     * @param imagePath The path to the image.
     * @return The loaded PImage object.
     */
    private PImage loadImageWithDecode(String imagePath) {
        URL resourceUrl = getClass().getResource(imagePath);
        String decodedPath = App.decodePath(resourceUrl.getPath());
        return app.loadImage(decodedPath);
    }

    /**
     * Loads the level configuration from a JSON object.
     *
     * @param levelConfig The JSON object containing level configuration.
     */
    public void loadLevel(JSONObject levelConfig) {
        String layoutFile = levelConfig.getString("layout");
        InputStream layoutStream = getClass().getResourceAsStream("/inkball/" + layoutFile);

        String[] layout = App.loadStrings(layoutStream);
        parseLayout(layout);

        this.spawnInterval = levelConfig.getFloat("spawn_interval", 10.0f);
        this.scoreIncreaseMultiplier = levelConfig.getFloat("score_increase_from_hole_capture_modifier", 1.0f);
        this.scoreDecreaseMultiplier = levelConfig.getFloat("score_decrease_from_wrong_hole_modifier", 1.0f);
        JSONArray ballsArray = levelConfig.getJSONArray("balls");
        for (int i = 0; i < ballsArray.size(); i++) {
            configBalls.add(ballsArray.getString(i));
        }
    }

    /**
     * Adds a ball to the level.
     *
     * @param ball The Ball object to add.
     */
    public void addBall(Ball ball) {
        this.balls.add(ball);
    }

    /**
     * Parses the layout of the level from the provided string array.
     *
     * @param layout The layout of the level as a string array.
     */
    public void parseLayout(String[] layout) {
        for (int row = 0; row < layout.length; row++) {
            String line = layout[row];
            for (int col = 0; col < line.length(); col++) {
                char tile = line.charAt(col);
                int x = col * App.CELLSIZE;
                int y = row * App.CELLSIZE;

                switch (tile) {
                    case 'X':
                        walls.add(new Wall(new PVector(x, y), wallSprites[0], app, 0));
                        break;
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                        int wallType = Character.getNumericValue(tile);
                        walls.add(new Wall(new PVector(x, y), wallSprites[wallType], app, wallType));
                        break;
                    case 'H':
                        if (col + 1 < line.length()) {
                            int holeType = Character.getNumericValue(line.charAt(col + 1));
                            holes.add(new Hole(new PVector(x, y), holeSprites[holeType], app, holeType, 1.0f, 1.0f));
                            col++;
                        }
                        break;
                    case 'S':
                        spawners.add(new Spawner(new PVector(x, y), spawnerSprite, app));
                        break;
                    case 'B':
                        if (col + 1 < line.length()) {
                            int ballColor = Character.getNumericValue(line.charAt(col + 1));
                            balls.add(new Ball(new PVector(x, y), ballSprites[ballColor], app, ballColor));
                            col++;
                        }
                        break;
                    case 'E':
                        if (col + 1 < line.length()) {
                            int brickColor = Character.getNumericValue(line.charAt(col + 1));
                            bricks.add(new Brick(new PVector(x, y), brickSprites[brickColor], app, brickColor));
                            col++;
                        }
                        break;
                }
            }
        }
    }

    /**
     * Updates the level, including ball movement and collision checks.
     *
     * @param config The JSON configuration for the level.
     */
    public void update(JSONObject config) {
        spawnCounter++;
        int framesToNextSpawn = (int) (spawnInterval * App.FPS);

        if (spawnCounter >= framesToNextSpawn && !configBalls.isEmpty()) {
            spawnBallFromSpawner();
            spawnCounter = 0;
            if (spawnInterval > MIN_SPAWN_INTERVAL) {
                spawnInterval = Math.max(MIN_SPAWN_INTERVAL, spawnInterval - 0.1f);
            }
        }

        for (Ball ball : new ArrayList<>(balls)) {
            ball.update();
            for (Wall wall : walls) {
                wall.checkCollision(ball);
            }
            for (Brick brick : new ArrayList<>(bricks)) {
                brick.checkCollision(ball);
            }
            for (Hole hole : holes) {
                hole.attractBall(ball, app, config);
            }
        }
        
        for (Wall wall : walls) {
            wall.cleanupCooldowns(balls);
        }
        // Clean up collision cooldowns for bricks
        for (Brick brick : bricks) {
            brick.cleanupCooldowns(balls);
        }
    }

    /**
     * Returns the sprites of the next five balls to spawn.
     *
     * @return A list of PImage objects representing the upcoming ball sprites.
     */
    public List<PImage> getUpcomingBallSprites() {
        List<PImage> ballSpritesToReturn = new ArrayList<>();
        for (int i = 0; i < Math.min(5, configBalls.size()); i++) {
            String ballColor = configBalls.get(i);
            int ballIndex = getColorIndex(ballColor);
            if (ballIndex >= 0) {
                ballSpritesToReturn.add(ballSprites[ballIndex]);
            }
        }
        return ballSpritesToReturn;
    }

    /**
     * Gets the countdown until the next ball spawns.
     *
     * @return The countdown time in seconds.
     */
    public float getSpawnCountdown() {
        if (configBalls.isEmpty()) {
            return 0;
        }
        return Math.max(0, (spawnInterval * App.FPS - spawnCounter) / (float) App.FPS);
    }

    /**
     * Spawns a ball from a random spawner.
     */
    public void spawnBallFromSpawner() {
        if (!spawners.isEmpty() && !configBalls.isEmpty()) {
            Spawner spawner = spawners.get((int) app.random(spawners.size()));
            String nextBallColor = configBalls.remove(0);
            int ballIndex = getColorIndex(nextBallColor);
            if (ballIndex >= 0) {
                balls.add(spawner.spawnBall(ballSprites[ballIndex], ballIndex));
                app.startMovingUpcomingBalls();
            }
        }
    }

    /**
     * Converts a color name to its corresponding index.
     *
     * @param color The name of the color.
     * @return The index of the color in the sprites array.
     */
    private int getColorIndex(String color) {
        switch (color.toLowerCase()) {
            case "grey":
                return 0;
            case "orange":
                return 1;
            case "blue":
                return 2;
            case "green":
                return 3;
            case "yellow":
                return 4;
            default:
                return -1;
        }
    }

    /**
     * Displays all elements of the level.
     */
    public void display() {
        for (Wall wall : walls) {
            wall.display();
        }
        for (Hole hole : holes) {
            hole.display();
        }
        for (Spawner spawner : spawners) {
            spawner.display();
        }
        for (Brick brick : bricks) {
            brick.display();
        }
        for (Ball ball : balls) {
            ball.display();
        }
        for (Line line : lines) {
            line.display();
        }
    }

    /**
     * Restarts the level by clearing balls and resetting spawn interval.
     */
    public void restart() {
        balls.clear();
        spawnCounter = 0;
        spawnInterval = 10.0f;
    }

    /**
     * Retrieves the list of balls in the level.
     *
     * @return A list of Ball objects.
     */
    public List<Ball> getBalls() {
        return balls;
    }

    /**
     * Adds a line to the level.
     *
     * @param line The Line object to add.
     */
    public void addLine(Line line) {
        lines.add(line);
    }

    /**
     * Removes a line from the level.
     *
     * @param line The Line object to remove.
     */
    public void removeLine(Line line) {
        lines.remove(line);
    }

    /**
     * Removes a ball from the level.
     *
     * @param ball The Ball object to remove.
     */
    public void removeBall(Ball ball) {
        balls.remove(ball);
    }

    /**
     * Checks if the level is completed.
     *
     * @return True if the level is completed, false otherwise.
     */
    public boolean isLevelCompleted() {
        return configBalls.isEmpty() && balls.isEmpty();
    }

    /**
     * Displays static elements (walls, holes, spawners) of the level.
     */
    public void displayStaticElements() {
        for (Wall wall : walls) {
            wall.display();
        }
        for (Hole hole : holes) {
            hole.display();
        }
        for (Spawner spawner : spawners) {
            spawner.display();
        }
    }

    /**
     * Removes a brick from the level.
     *
     * @param brick The Brick object to remove.
     */
    public void removeBrick(Brick brick) {
        bricks.remove(brick);
    }
}