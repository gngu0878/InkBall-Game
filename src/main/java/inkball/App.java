package inkball;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import processing.core.PImage;
import processing.event.MouseEvent;

public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int TOPBAR = 64;
    public static final int WIDTH = 576;
    public static final int HEIGHT = 640;
    public static final int FPS = 30;

    private String configPath;
    private JSONObject config;
    private Level currentLevel;
    private boolean paused = false;
    private boolean timeUp = false;

    private PImage[] ballSprites = new PImage[5];

    private int levelTime;
    private int remainingTime;
    private long lastTimeCheck;
    private int score;
    private int initialScore;

    private int currentLevelIndex = 0;
    private JSONArray levels;
    private HashMap<String, PImage> sprites;

    private float upcomingBallsOffset = 0;
    private boolean moveUpcomingBalls = false;

    private List<Line> playerLines;
    private Line currentLine;

    private boolean levelCompleted = false;
    private int timeBonusRemaining;
    private long lastBonusTime;
    private List<YellowTile> yellowTiles;
    private long lastTileMoveTime;
    private PImage yellowTileSprite;

    private boolean gameCompleted = false;
    private boolean showWinMessage = false;

    /**
     * Constructor for the App class. Initializes the config path.
     */
    public App() {
        this.configPath = "/inkball/config.json";
    }

    /**
     * Sets a seed for the random number generator. 
     * This helps ensure consistent random values during testing.
     *
     * @param seed The seed value to set for random number generation.
     */
    public void setRandomSeed(long seed) {
        randomSeed(seed);  // PApplet's method to set the seed for random
    }

    /**
     * Class representing a yellow tile that moves along the edges of the game window.
     */
    public class YellowTile {
        public PVector position;
        int edge;
        int indexOnEdge;
        int loopsCompleted;

        /**
         * Constructor for the YellowTile object.
         *
         * @param startPosition The starting position of the tile.
         * @param startEdge The edge where the tile starts moving.
         */
        public YellowTile(PVector startPosition, int startEdge) {
            this.position = startPosition;
            this.edge = startEdge;
            this.indexOnEdge = 0;
            this.loopsCompleted = 0;
        }

        /**
         * Moves the yellow tile along the edges of the game window.
         */
        public void move() {
            int maxEdgeIndex;
            switch (edge) {
                case 0:
                    maxEdgeIndex = (width / CELLSIZE) - 1;
                    if (indexOnEdge < maxEdgeIndex) {
                        position.x += CELLSIZE;
                        indexOnEdge++;
                    } else {
                        edge = 1;
                        indexOnEdge = 0;
                        move();
                    }
                    break;
                case 1:
                    maxEdgeIndex = ((height - TOPBAR) / CELLSIZE) - 1;
                    if (indexOnEdge < maxEdgeIndex) {
                        position.y += CELLSIZE;
                        indexOnEdge++;
                    } else {
                        edge = 2;
                        indexOnEdge = 0;
                        move();
                    }
                    break;
                case 2:
                    maxEdgeIndex = (width / CELLSIZE) - 1;
                    if (indexOnEdge < maxEdgeIndex) {
                        position.x -= CELLSIZE;
                        indexOnEdge++;
                    } else {
                        edge = 3;
                        indexOnEdge = 0;
                        move();
                    }
                    break;
                case 3:
                    maxEdgeIndex = ((height - TOPBAR) / CELLSIZE) - 1;
                    if (indexOnEdge < maxEdgeIndex) {
                        position.y -= CELLSIZE;
                        indexOnEdge++;
                    } else {
                        edge = 0;
                        indexOnEdge = 0;
                        loopsCompleted++;
                    }
                    break;
            }
        }
    }

    /**
     * Starts the sequence when the level is completed.
     * Initializes the time bonus and yellow tile sequence.
     */
    public void startLevelCompletion() {
        timeBonusRemaining = remainingTime;
        lastBonusTime = millis();
        remainingTime = 0;

        yellowTiles = new ArrayList<>();
        yellowTiles.add(new YellowTile(new PVector(0, 0), 0));
        yellowTiles.add(new YellowTile(new PVector(width - CELLSIZE, height - TOPBAR - CELLSIZE), 2));
        lastTileMoveTime = millis();
    }

    /**
     * Updates the level completion sequence, including time bonus and yellow tiles.
     */
    public void updateLevelCompletion() {
        if (timeBonusRemaining > 0) {
            if (millis() - lastBonusTime >= 67) {
                score += 1;
                timeBonusRemaining -= 1;
                lastBonusTime = millis();
            }
        }
        if (timeBonusRemaining < 0) {
            timeBonusRemaining = 0;
        }

        if (millis() - lastTileMoveTime >= 67) {
            moveYellowTiles();
            lastTileMoveTime = millis();
        }

        drawYellowTiles();

        if (timeBonusRemaining <= 0 && yellowTilesCompleted()) {
            if (gameCompleted) {
                showWinMessage = true;
                levelCompleted = false;
            } else {
                nextLevel();
            }
        }
    }

    /**
     * Moves all yellow tiles along their respective edges.
     */
    private void moveYellowTiles() {
        for (YellowTile tile : yellowTiles) {
            tile.move();
        }
    }

    /**
     * Draws all yellow tiles on the screen.
     */
    private void drawYellowTiles() {
        pushMatrix();
        for (YellowTile tile : yellowTiles) {
            image(yellowTileSprite, tile.position.x, tile.position.y, CELLSIZE, CELLSIZE);
        }
        popMatrix();
    }

    /**
     * Checks if the yellow tiles have completed one full loop around the edges.
     *
     * @return True if all yellow tiles have completed one loop, false otherwise.
     */
    private boolean yellowTilesCompleted() {
        for (YellowTile tile : yellowTiles) {
            if (tile.loopsCompleted < 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Override function that sets the window size for the game.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }


    /**
     * Override function that initializes the game environment, including loading sprites, config, and setting frame rate.
     */
    @Override
    public void setup() {
        frameRate(FPS);
        sprites = new HashMap<>();
        playerLines = new ArrayList<>();
        loadBallSprites();
        loadConfig();
        score = 0;
        initialScore = score;
        yellowTileSprite = loadImageFromResources("inkball/wall4.png");
    }

    /**
     * Loads the ball sprites from the resource files.
     */
    private void loadBallSprites() {
        ballSprites[0] = loadImageFromResources("inkball/ball0.png");
        ballSprites[1] = loadImageFromResources("inkball/ball1.png");
        ballSprites[2] = loadImageFromResources("inkball/ball2.png");
        ballSprites[3] = loadImageFromResources("inkball/ball3.png");
        ballSprites[4] = loadImageFromResources("inkball/ball4.png");
    }

    /**
     * Loads an image from the resources folder based on a relative path.
     *
     * @param relativePath The relative path to the image file.
     * @return The loaded PImage object.
     */
    private PImage loadImageFromResources(String relativePath) {
        URL resource = getClass().getClassLoader().getResource(relativePath);
        return loadImage(resource.getPath().replace("%20", " "));
    }

    /**
     * Retrieves the ball sprites array.
     *
     * @return An array of PImage objects representing the ball sprites.
     */
    public PImage[] getBallSprites() {
        return ballSprites;
    }

    /**
     * Retrieves a sprite by name from the cache or loads it if not present.
     *
     * @param s The name of the sprite to retrieve.
     * @return The PImage object for the sprite.
     */
    public PImage getSprite(String s) {
        PImage result = sprites.get(s);
        if (result == null) {
            URL resourceUrl = getClass().getResource(s + ".png");
            if (resourceUrl != null) {
                String decodedPath = resourceUrl.getPath().replace("%20", " ");
                result = loadImage(decodedPath);
                sprites.put(s, result);
            } else {
                System.out.println("Sprite not found: " + s);
            }
        }
        return result;
    }

    /**
     * Loads the game configuration from the JSON file.
     */
    private void loadConfig() {
        try {
            URL configUrl = getClass().getResource(this.configPath);
            String decodedPath = URLDecoder.decode(configUrl.getPath(), StandardCharsets.UTF_8.name());
            config = loadJSONObject(decodedPath);
            levels = config.getJSONArray("levels");

            loadLevel(currentLevelIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the specified level from the config file.
     *
     * @param index The index of the level to load.
     */
    public void loadLevel(int index) {
        if (index >= levels.size()) {
            return;
        }

        initialScore = score;

        JSONObject levelConfig = levels.getJSONObject(index);
        currentLevel = new Level(this);
        currentLevel.loadLevel(levelConfig);

        levelTime = levelConfig.getInt("time", 120);
        remainingTime = levelTime;
        lastTimeCheck = millis();
        timeUp = false;
    }

    /**
     * Restarts the current level, resetting the score and clearing any player-drawn lines.
     */
    public void restartLevel() {
        score = initialScore;
        playerLines.clear();
        loadLevel(currentLevelIndex);
        paused = false;
        timeUp = false;
    }

    /**
     * Proceeds to the next level in the game. If all levels are completed, starts the level completion sequence.
     */
    public void nextLevel() {
        currentLevelIndex++;
        if (currentLevelIndex < levels.size()) {
            levelCompleted = false;
            playerLines.clear();
            loadLevel(currentLevelIndex);
        } else {
            gameCompleted = true;
            levelCompleted = true;
            startLevelCompletion();
        }
    }


    /**
     * Override function that handles keyboard input.
     * The 'r' key restarts the game or the current level.
     * The spacebar pauses or unpauses the game.
     */
    @Override
    public void keyPressed() {
        if (key == 'r') {
            if (gameCompleted) {
                restartGame();
            } else {
                restartLevel();
            }
        } else if (key == ' ') {
            paused = !paused;
        }
    }

    /**
     * Restarts the entire game from the first level, resetting score and game state.
     */
    public void restartGame() {
        currentLevelIndex = 0;
        score = 0;
        initialScore = score;
        levelCompleted = false;
        gameCompleted = false;
        showWinMessage = false;
        playerLines.clear();
        loadLevel(currentLevelIndex);
    }


    /**
     * Override function that handles game logic, drawing the current state of the game each frame.
     * This function is automatically called by the Processing engine every frame.
     */
    @Override
    public void draw() {
        background(255);
        PImage tileImage = getSprite("tile");

        pushMatrix();
        translate(0, TOPBAR);

        for (int y = 0; y < height - TOPBAR; y += CELLSIZE) {
            for (int x = 0; x < width; x += CELLSIZE) {
                image(tileImage, x, y, CELLSIZE, CELLSIZE);
            }
        }

        if (!levelCompleted) {
            if (!paused && !timeUp) {
                currentLevel.update(config);
                updateTimer();
            }

            currentLevel.display();

            for (Line line : playerLines) {
                line.display();
            }

            if (currentLine != null) {
                currentLine.display();
            }

            checkBallLineCollisions();

            if (currentLevel.isLevelCompleted()) {
                levelCompleted = true;
                startLevelCompletion();
            }
        } else {
            currentLevel.displayStaticElements();
            updateLevelCompletion();
        }

        popMatrix();
        strokeWeight(1);
        fill(200);
        rect(0, 0, width, TOPBAR);

        fill(0);
        rect(10, 10, CELLSIZE * 5 + 20, TOPBAR - 20);

        displayTimerAndScore();
        displaySpawnCountdown();
        displayUpcomingBalls();

        if (showWinMessage) {
            textSize(25);
            fill(0);
            text("=== ENDED ===", width / 2 - 50, TOPBAR / 2 - 5);
        } else if (timeUp && !levelCompleted) {
            textSize(25);
            fill(0);
            text("=== TIME'S UP ===", width / 2 - 50, TOPBAR / 2 - 5);
        } else if (paused && !levelCompleted) {
            textSize(25);
            fill(0);
            text("*** PAUSED ***", width / 2 - 50, TOPBAR / 2 - 5);
        }
    }

    /**
     * Removes a player-drawn line from the game.
     *
     * @param line The line to remove.
     */
    public void removeLine(Line line) {
        playerLines.remove(line);
    }

    /**
     * Displays the timer and the current score on the screen.
     */
    public void displayTimerAndScore() {
        textSize(20);
        fill(0);
        textAlign(RIGHT, TOP);

        if (!levelCompleted) {
            text(String.format("Time: %d", remainingTime), WIDTH - 15, 10);
        } else {
            text(String.format("Time: %d", timeBonusRemaining), WIDTH - 15, 10);
        }

        text("Score: " + score, WIDTH - 15, 40);
    }

    /**
     * Starts moving the upcoming balls in the UI.
     */
    public void startMovingUpcomingBalls() {
        moveUpcomingBalls = true;
    }


    /**
     * Override function that handles mouse press events.
     * Left-click starts drawing a line or removes a line if Control is held.
     * Right-click removes a player-drawn line if clicked on it.
     *
     * @param event The MouseEvent containing information about the mouse press.
     */
    @Override
    public void mousePressed(MouseEvent event) {
        if (mouseButton == LEFT) {
            if (event.isControlDown()) {
                Line lineToRemove = null;
                for (Line line : playerLines) {
                    if (line.isPointOnLine(new PVector(mouseX, mouseY - TOPBAR))) {
                        lineToRemove = line;
                        break;
                    }
                }
                if (lineToRemove != null) {
                    playerLines.remove(lineToRemove);
                }
            } else if (mouseY > TOPBAR) {
                PVector start = new PVector(mouseX, mouseY - TOPBAR);
                currentLine = new Line(new ArrayList<>(), this);
                currentLine.startDrawing(start);
            }
        } else if (mouseButton == RIGHT) {
            Line lineToRemove = null;
            for (Line line : playerLines) {
                if (line.isPointOnLine(new PVector(mouseX, mouseY - TOPBAR))) {
                    lineToRemove = line;
                    break;
                }
            }
            if (lineToRemove != null) {
                playerLines.remove(lineToRemove);
            }
        }
    }

    /**
     * Override function that handles mouse drag events.
     * Adds points to the current line being drawn as the mouse is dragged.
     */
    @Override
    public void mouseDragged() {
        if (currentLine != null && mouseY > TOPBAR) {
            currentLine.addPoint(new PVector(mouseX, mouseY - TOPBAR));
        }
    }

    /**
     * Override function that handles mouse release events.
     * Adds the current line to the player's list of lines and stops drawing it.
     */
    @Override
    public void mouseReleased() {
        if (currentLine != null) {
            playerLines.add(currentLine);
            currentLine.stopDrawing();
            currentLine = null;
        }
    }

    /**
     * Checks for collisions between balls and player-drawn lines.
     */
    public void checkBallLineCollisions() {
        for (Ball ball : currentLevel.getBalls()) {
            ball.checkLineCollisions(playerLines, playerLines);
        }
    }

    /**
     * Removes the current line being drawn.
     */
    public void removeCurrentLine() {
        currentLine = null;
    }

    /**
     * Gets the current level of the game.
     *
     * @return The current Level object.
     */
    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Displays the countdown timer for the next ball spawn.
     */
    private void displaySpawnCountdown() {
        textSize(20);
        fill(0);
        textAlign(LEFT, TOP);

        float secondsUntilSpawn = currentLevel.getSpawnCountdown();
        text(String.format("%.1f", secondsUntilSpawn), 10 + (CELLSIZE * 5) + 30, 25);
    }

    /**
     * Displays the upcoming balls in the UI, showing their sprites in a queue.
     */
    private void displayUpcomingBalls() {
        List<PImage> upcomingBallSprites = currentLevel.getUpcomingBallSprites();
        int padding = 10;
        int availableHeight = TOPBAR - 20;

        int maxBalls = 5;

        if (moveUpcomingBalls) {
            upcomingBallsOffset -= 1;

            if (upcomingBallsOffset <= -CELLSIZE) {
                upcomingBallsOffset = 0;
                moveUpcomingBalls = false;
            }
        }

        int ballsToDisplay = maxBalls;
        if (moveUpcomingBalls) {
            ballsToDisplay = Math.min(maxBalls + 1, upcomingBallSprites.size());
        } else {
            ballsToDisplay = Math.min(maxBalls, upcomingBallSprites.size());
        }

        for (int i = 0; i < ballsToDisplay; i++) {
            float ballX = padding + (i * CELLSIZE) + upcomingBallsOffset;
            int ballY = 10 + (availableHeight - (CELLSIZE - 10)) / 2;

            if (ballX + CELLSIZE - 10 > padding && ballX < padding + CELLSIZE * maxBalls + 20) {
                image(upcomingBallSprites.get(i), ballX, ballY, CELLSIZE - 10, CELLSIZE - 10);
            }
        }
    }

    /**
     * Updates the countdown timer for the current level, decrementing the remaining time.
     */
    public void updateTimer() {
        int currentTime = millis();
        if (currentTime - lastTimeCheck >= 1000) {
            remainingTime--;
            lastTimeCheck = currentTime;

            if (remainingTime <= 0) {
                timeUp = true;
                paused = true;
            }
        }
    }

    /**
     * Increases the player's score by a specified amount.
     *
     * @param amount The amount to increase the score by.
     */
    public void increaseScore(int amount) {
        score += amount;
    }

    /**
     * Decreases the player's score by a specified amount.
     * If the score becomes negative, it is set to 0.
     *
     * @param amount The amount to decrease the score by.
     */
    public void decreaseScore(int amount) {
        score -= amount;
        if (score < 0) {
            score = 0;
        }
    }

    /**
     * Decodes a URL-encoded path.
     *
     * @param path The URL-encoded path to decode.
     * @return The decoded path as a string.
     */
    public static String decodePath(String path) {
        try {
            return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Getter and setter methods for various class variables

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setGameCompleted(boolean gameCompleted) {
        this.gameCompleted = gameCompleted;
    }

    public void setShowWinMessage(boolean showWinMessage) {
        this.showWinMessage = showWinMessage;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public boolean getShowWinMessage() {
        return showWinMessage;
    }

    public boolean isTimeUp() {
        return timeUp;
    }

    public boolean getPaused() {
        return paused;
    }

    public Line getCurrentLine() {
        return currentLine;
    }

    public List<Line> getPlayerLines() {
        return playerLines;
    }
    
    public int getScore() {
        return score;
    }

    public List<YellowTile> getYellowTiles() {
        return yellowTiles;
    }

    public boolean isMoveUpcomingBalls() {
        return moveUpcomingBalls;
    }
    
    public float getUpcomingBallsOffset() {
        return upcomingBallsOffset;
    }
    
    public void setLastTimeCheck(long lastTimeCheck) {
        this.lastTimeCheck = lastTimeCheck;
    }
    
    public long getLastTimeCheck() {
        return lastTimeCheck;
    }
    
    public int getLevelTime() {
        return levelTime;
    }
    
    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }
    
    public void setCurrentLevelIndex(int index) {
        this.currentLevelIndex = index;
    }
    
    public int getLevelsSize() {
        return levels.size();
    }
    
    public void setLevelCompleted(boolean levelCompleted) {
        this.levelCompleted = levelCompleted;
    }
    
    public boolean isLevelCompleted() {
        return levelCompleted;
    }
    
    public void setTimeBonusRemaining(int timeBonusRemaining) {
        this.timeBonusRemaining = timeBonusRemaining;
    }

    public void setTimeUp(boolean timeUp) {
        this.timeUp = timeUp;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setLastBonusTime(long lastBonusTime) {
        this.lastBonusTime = lastBonusTime;
    }

    public long getLastBonusTime() {
        return lastBonusTime;
    }

    public int getTimeBonusRemaining() {
        return timeBonusRemaining;
    }

    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setconfigPath(String path) {
        this.configPath = path;
    }

    public void setCurrentLine(Line currentLine) {
        this.currentLine = currentLine;
    }

    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}