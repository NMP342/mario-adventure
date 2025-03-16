package levels;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Crabby;
import entities.Pinkstar;
import entities.Shark;
import main.Game;
import objects.BackgroundTree;
import objects.Candle;
import objects.CandleLight;
import objects.Cannon;
import objects.Coin;
import objects.GameContainer;
import objects.Grass;
import objects.Potion;
import objects.Spike;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.ObjectConstants.*;

public class Level {

    private BufferedImage img;
    private int[][] lvlData;

    private ArrayList<Crabby> crabs = new ArrayList<>();
    private ArrayList<Pinkstar> pinkstars = new ArrayList<>();
    private ArrayList<Shark> sharks = new ArrayList<>();
    private ArrayList<Potion> potions = new ArrayList<>();
    private ArrayList<Spike> spikes = new ArrayList<>();
    private ArrayList<GameContainer> containers = new ArrayList<>();
    private ArrayList<Cannon> cannons = new ArrayList<>();
    private ArrayList<BackgroundTree> trees = new ArrayList<>();
    private ArrayList<Grass> grass = new ArrayList<>();
    private ArrayList<Coin> coins = new ArrayList<>();
    private ArrayList<Candle> candles = new ArrayList<>();
    private ArrayList<CandleLight> candleLights = new ArrayList<>();

    private int lvlTilesWide, lvlTilesTall;
    private int maxTilesOffset;
    private int maxLvlOffsetX, maxLvlOffsetY;
    private Point playerSpawn;

    public Level(BufferedImage img) {
        this.img = img;
        lvlData = new int[img.getHeight()][img.getWidth()];
        loadLevel();
        calcLvlOffsetX();
        calcLvlOffsetY();
    }

    private void loadLevel() {

        // Looping through the image colors just once. Instead of one per
        // object/enemy/etc..
        // Removed many methods in HelpMethods class.
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y));
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();

                loadLevelData(red, x, y);
                loadEntities(green, x, y);
                loadObjects(blue, x, y);
            }
        }
    }

    private void loadLevelData(int redValue, int x, int y) {
        if (redValue >= 50) {
            lvlData[y][x] = 0;
        } else {
            lvlData[y][x] = redValue;
        }
        switch (redValue) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 30:
            case 31:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
                grass.add(new Grass((int) (x * Game.TILES_SIZE), (int) (y * Game.TILES_SIZE) - Game.TILES_SIZE, getRndGrassType(x)));
        }
    }

    private int getRndGrassType(int xPos) {
        return xPos % 2;
    }

    private void loadEntities(int greenValue, int x, int y) {
        switch (greenValue) {
            case CRABBY:
                crabs.add(new Crabby(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
                break;
            case PINKSTAR:
                pinkstars.add(new Pinkstar(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
                break;
            case SHARK:
                sharks.add(new Shark(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
                break;
            case 100:
                playerSpawn = new Point(x * Game.TILES_SIZE, y * Game.TILES_SIZE);
                break;
        }
    }

    private void loadObjects(int blueValue, int x, int y) {
        switch (blueValue) {
            case RED_POTION:
            case BLUE_POTION:
                potions.add(new Potion(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
                break;
            case BOX:
            case BARREL:
                containers.add(new GameContainer(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
                break;
            case SPIKE:
                spikes.add(new Spike(x * Game.TILES_SIZE, y * Game.TILES_SIZE, SPIKE));
                break;
            case CANNON_LEFT:
            case CANNON_RIGHT:
                cannons.add(new Cannon(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
                break;
            case SILVER_COIN:
                coins.add(new Coin(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
                break;
            case TREE_ONE:
            case TREE_TWO:
            case TREE_THREE:
                trees.add(new BackgroundTree(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
                break;
            case CANDLE:
                candles.add(new Candle(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
                candleLights.add(new CandleLight(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
                break;
        }
    }

    private void calcLvlOffsetX() {
        lvlTilesWide = img.getWidth();
        maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
        maxLvlOffsetX = Game.TILES_SIZE * maxTilesOffset;

    }

    public void calcLvlOffsetY() {
        lvlTilesTall = img.getHeight();
        maxTilesOffset = lvlTilesTall - Game.TILES_IN_HEIGHT;
        maxLvlOffsetY = Game.TILES_SIZE * maxTilesOffset;
    }

    public int getSpriteIndex(int x, int y) {
        return lvlData[y][x];
    }

    public int[][] getLevelData() {
        return lvlData;
    }

    public int getXLvlOffset() {
        return maxLvlOffsetX;
    }

    public int getYLvlOffset() {
        return maxLvlOffsetY;
    }

    public Point getPlayerSpawn() {
        return playerSpawn;
    }

    public ArrayList<Crabby> getCrabs() {
        return crabs;
    }

    public ArrayList<Shark> getSharks() {
        return sharks;
    }

    public ArrayList<Potion> getPotions() {
        return potions;
    }

    public ArrayList<GameContainer> getContainers() {
        return containers;
    }

    public ArrayList<Spike> getSpikes() {
        return spikes;
    }

    public ArrayList<Cannon> getCannons() {
        return cannons;
    }

    public ArrayList<Pinkstar> getPinkstars() {
        return pinkstars;
    }

    public ArrayList<BackgroundTree> getTrees() {
        return trees;
    }

    public ArrayList<Grass> getGrass() {
        return grass;
    }

    public ArrayList<Coin> getCoins() {
        return coins;
    }

    public ArrayList<Candle> getCandles() {
        return candles;
    }

    public ArrayList<CandleLight> getCandleLights() {
        return candleLights;
    }
}
