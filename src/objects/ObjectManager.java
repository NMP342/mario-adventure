package objects;

import audio.AudioPlayer;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Enemy;
import entities.Player;
import gamestates.Playing;
import levels.Level;
import main.Game;
import utilz.LoadSave;
import static utilz.Constants.ObjectConstants.*;
import static utilz.HelpMethods.CanCannonSeePlayer;
import static utilz.HelpMethods.IsProjectileHittingLevel;
import static utilz.Constants.Projectiles.*;

public class ObjectManager {

    private Playing playing;
    private BufferedImage[][] potionImgs, containerImgs;
    private BufferedImage[] cannonImgs, grassImgs;
    private BufferedImage[][] treeImgs;
    private BufferedImage[] coinImgs;
    private BufferedImage[] candleImgs, candleLightImgs;
    private BufferedImage spikeImg, cannonBallImg;
    private ArrayList<Potion> potions;
    private ArrayList<GameContainer> containers;
    private ArrayList<Coin> coins;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Candle> candles;
    private ArrayList<CandleLight> candleLights;

    private Level currentLevel;

    public ObjectManager(Playing playing) {
        this.playing = playing;
        currentLevel = playing.getLevelManager().getCurrentLevel();
        loadImgs();
    }

    public void checkSpikesTouched(Player p) {
        for (Spike s : currentLevel.getSpikes()) {
            if (s.getHitbox().intersects(p.getHitbox())) {
                p.kill();
            }
        }
    }

    public void checkSpikesTouched(Enemy e) {
        for (Spike s : currentLevel.getSpikes()) {
            if (s.getHitbox().intersects(e.getHitbox())) {
                e.hurt(200);
            }
        }
    }

    public void checkPotionTouched(Rectangle2D.Float hitbox) {
        for (Potion p : potions) {
            if (p.isActive()) {
                if (hitbox.intersects(p.getHitbox())) {
                    p.setActive(false);
                    applyEffectToPlayer(p);
                    playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GET_POTION);
                }
            }
        }
    }

    public void applyEffectToPlayer(Potion p) {
        if (p.getObjType() == RED_POTION) {
            playing.getPlayer().changeHealth(RED_POTION_VALUE);
        } else {
            playing.getPlayer().changePower(BLUE_POTION_VALUE);
        }
    }

    public void checkCoinTouched(Rectangle2D.Float hitbox) {
        for (Coin c : coins) {
            if (c.isActive()) {
                if (hitbox.intersects(c.getHitbox())) {
                    c.setActive(false);
//                    applyEffectToPlayer(p);
                    playing.getPlayer().changeCoinNumber(1);
                    playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GET_COIN);
                }
            }
        }
    }

    public void checkObjectHit(Rectangle2D.Float attackbox) {
        for (GameContainer gc : containers) {
            if (gc.isActive() && !gc.doAnimation) {
                if (gc.getHitbox().intersects(attackbox)) {
                    gc.setAnimation(true);
                    int type = 0;
                    if (gc.getObjType() == BARREL) {
                        type = 1;
                    }
                    potions.add(new Potion((int) (gc.getHitbox().x + gc.getHitbox().width / 2), (int) (gc.getHitbox().y), type));
                    return;
                }
            }
        }
    }

    public void loadObjects(Level newLevel) {
        currentLevel = newLevel;
        potions = new ArrayList<>(newLevel.getPotions());
        containers = new ArrayList<>(newLevel.getContainers());
        coins = new ArrayList<>(newLevel.getCoins());
        candles = new ArrayList<>(newLevel.getCandles());
        candleLights = new ArrayList<>(newLevel.getCandleLights());
        projectiles = new ArrayList<>();
//        projectiles.clear();
    }

    private void loadImgs() {
        BufferedImage potionSprite = LoadSave.GetSpriteAtlas(LoadSave.POTION_ATLAS);
        potionImgs = new BufferedImage[2][7];

        for (int j = 0; j < potionImgs.length; j++) {
            for (int i = 0; i < potionImgs[j].length; i++) {
                potionImgs[j][i] = potionSprite.getSubimage(12 * i, 16 * j, 12, 16);
            }
        }

        BufferedImage containerSprite = LoadSave.GetSpriteAtlas(LoadSave.CONTAINER_ATLAS);
        containerImgs = new BufferedImage[2][8];

        for (int j = 0; j < containerImgs.length; j++) {
            for (int i = 0; i < containerImgs[j].length; i++) {
                containerImgs[j][i] = containerSprite.getSubimage(40 * i, 30 * j, 40, 30);
            }
        }

        spikeImg = LoadSave.GetSpriteAtlas(LoadSave.TRAP_ATLAS);

        cannonImgs = new BufferedImage[7];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.CANNON_ATLAS);

        for (int i = 0; i < cannonImgs.length; i++) {
            cannonImgs[i] = temp.getSubimage(i * 40, 0, 40, 26);
        }

        cannonBallImg = LoadSave.GetSpriteAtlas(LoadSave.CANNON_BALL);

        treeImgs = new BufferedImage[2][4];
        BufferedImage treeOneImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_ONE_ATLAS);
        for (int i = 0; i < 4; i++) {
            treeImgs[0][i] = treeOneImg.getSubimage(i * 39, 0, 39, 92);
        }

        BufferedImage treeTwoImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_TWO_ATLAS);
        for (int i = 0; i < 4; i++) {
            treeImgs[1][i] = treeTwoImg.getSubimage(i * 62, 0, 62, 54);
        }

        BufferedImage grassTemp = LoadSave.GetSpriteAtlas(LoadSave.GRASS_ATLAS);
        grassImgs = new BufferedImage[2];
        for (int i = 0; i < grassImgs.length; i++) {
            grassImgs[i] = grassTemp.getSubimage(32 * i, 0, 32, 32);
        }

        BufferedImage coinTemp = LoadSave.GetSpriteAtlas(LoadSave.SILVER_COIN_ATLAS);
        coinImgs = new BufferedImage[4];
        for (int i = 0; i < coinImgs.length; ++i) {
            coinImgs[i] = coinTemp.getSubimage(i * 16, 0, 16, 16);
        }

        BufferedImage candleTemp = LoadSave.GetSpriteAtlas(LoadSave.CANDLE_ATLAS);
        candleImgs = new BufferedImage[6];
        for (int i = 0; i < candleImgs.length; ++i) {
            candleImgs[i] = candleTemp.getSubimage(i * 32, 0, 32, 32);
        }

        BufferedImage candleLightTemp = LoadSave.GetSpriteAtlas(LoadSave.CANDLE_LIGHT_ATLAS);
        candleLightImgs = new BufferedImage[4];
        for (int i = 0; i < candleLightImgs.length; ++i) {
            candleLightImgs[i] = candleLightTemp.getSubimage(i * 32, 0, 32, 32);
        }
    }

    public void update(int[][] lvlData, Player player) {
        updateBackgroundTrees();
        updatePotions();
        updateGameContainers();
        updateCoins();
        updateCannons(lvlData, player);
        updateProjectiles(lvlData, player);
        updateCandle();
        updateCandleLights();
    }

    private void updateCandle() {
        for (Candle c : candles) {
            c.update();
        }
    }

    private void updateCandleLights() {
        for (CandleLight cl : candleLights) {
            cl.update();
        }
    }

    private void updateCoins() {
        for (Coin c : coins) {
            if (c.isActive()) {
                c.update();
            }
        }
    }

    private void updatePotions() {
        for (Potion p : potions) {
            if (p.isActive()) {
                p.update();
            }
        }
    }

    private void updateGameContainers() {
        for (GameContainer gc : containers) {
            if (gc.isActive()) {
                gc.update();
            }
        }
    }

    private void updateBackgroundTrees() {
        for (BackgroundTree bt : currentLevel.getTrees()) {
            bt.update();
        }
    }

    private void updateProjectiles(int[][] lvlData, Player player) {
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.updatePos();
                if (p.getHitbox().intersects(player.getHitbox())) {
                    player.changeHealth(-25);
                    p.setActive(false);
                } else if (IsProjectileHittingLevel(p, lvlData)) {
                    p.setActive(false);
                }
            }
        }
    }

    private boolean isPlayerInRange(Cannon c, Player player) {
        int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
        return absValue <= Game.TILES_SIZE * 5;
    }

    private boolean isPlayerInfrontOfCannon(Cannon c, Player player) {
        if (c.getObjType() == CANNON_LEFT) {
            if (c.getHitbox().x > player.getHitbox().x) {
                return true;
            }

        } else if (c.getHitbox().x < player.getHitbox().x) {
            return true;
        }
        return false;
    }

    private void updateCannons(int[][] lvlData, Player player) {
        for (Cannon c : currentLevel.getCannons()) {
            if (!c.doAnimation) {
                if (c.getTileY() == player.getTileY()) {
                    if (isPlayerInRange(c, player)) {
                        if (isPlayerInfrontOfCannon(c, player)) {
                            if (CanCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY())) {
                                c.setAnimation(true);
                            }
                        }
                    }
                }
            }

            c.update();
            if (c.getAniIndex() == 4 && c.getAniTick() == 0) {
                shootCannon(c);
            }
        }
    }

    private void shootCannon(Cannon c) {
        int dir = 1;
        if (c.getObjType() == CANNON_LEFT) {
            dir = -1;
        }

        projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));
    }

    public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
        drawPotions(g, xLvlOffset, yLvlOffset);
        drawContainers(g, xLvlOffset, yLvlOffset);
        drawTraps(g, xLvlOffset, yLvlOffset);
        drawCannons(g, xLvlOffset, yLvlOffset);
        drawProjectiles(g, xLvlOffset, yLvlOffset);
        drawGrass(g, xLvlOffset, yLvlOffset);
        drawCoins(g, xLvlOffset, yLvlOffset);
        drawCandles(g, xLvlOffset, yLvlOffset);
        drawCandleLights(g, xLvlOffset, yLvlOffset);
    }

    private void drawGrass(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Grass grass : currentLevel.getGrass()) {
            g.drawImage(grassImgs[grass.getType()], grass.getX() - xLvlOffset, grass.getY() - yLvlOffset, (int) (32 * Game.SCALE), (int) (32 * Game.SCALE), null);
        }
    }

    public void drawBackgroundTrees(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (BackgroundTree bt : currentLevel.getTrees()) {

            int type = bt.getType();
            if (type == 9) {
                type = 8;
            }
            g.drawImage(treeImgs[type - 7][bt.getAniIndex()], bt.getX() - xLvlOffset + GetTreeOffsetX(bt.getType()), (int) (bt.getY() + GetTreeOffsetY(bt.getType())) - yLvlOffset, GetTreeWidth(bt.getType()),
                    GetTreeHeight(bt.getType()), null);
        }
    }

    private void drawProjectiles(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                g.drawImage(cannonBallImg, (int) (p.getHitbox().x - xLvlOffset), (int) (p.getHitbox().y - yLvlOffset), CANNON_BALL_WIDTH, CANNON_BALL_HEIGHT, null);
            }
        }
    }

    private void drawCannons(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Cannon c : currentLevel.getCannons()) {
            int x = (int) (c.getHitbox().x - xLvlOffset);
            int width = CANNON_WIDTH;

            if (c.getObjType() == CANNON_RIGHT) {
                x += width;
                width *= -1;
            }
            g.drawImage(cannonImgs[c.getAniIndex()], x, (int) (c.getHitbox().y - yLvlOffset), width, CANNON_HEIGHT, null);
        }
    }

    private void drawTraps(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Spike s : currentLevel.getSpikes()) {
            g.drawImage(spikeImg, (int) (s.getHitbox().x - xLvlOffset), (int) (s.getHitbox().y - s.getyDrawOffset() - yLvlOffset), SPIKE_WIDTH, SPIKE_HEIGHT, null);
        }

    }

    private void drawContainers(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (GameContainer gc : containers) {
            if (gc.isActive()) {
                int type = 0;
                if (gc.getObjType() == BARREL) {
                    type = 1;
                }
                g.drawImage(containerImgs[type][gc.getAniIndex()], (int) (gc.getHitbox().x - gc.getxDrawOffset() - xLvlOffset), (int) (gc.getHitbox().y - gc.getyDrawOffset() - yLvlOffset), CONTAINER_WIDTH,
                        CONTAINER_HEIGHT, null);
            }
        }
    }

    private void drawPotions(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Potion p : potions) {
            if (p.isActive()) {
                int type = 0;
                if (p.getObjType() == RED_POTION) {
                    type = 1;
                }
                g.drawImage(potionImgs[type][p.getAniIndex()], (int) (p.getHitbox().x - p.getxDrawOffset() - xLvlOffset), (int) (p.getHitbox().y - p.getyDrawOffset() - yLvlOffset),
                        POTION_WIDTH, POTION_HEIGHT, null);
            }
        }
    }

    private void drawCoins(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Coin c : coins) {
            if (c.isActive()) {
                g.drawImage(coinImgs[c.getAniIndex()], (int) (c.getHitbox().x - c.getxDrawOffset() - xLvlOffset), (int) (c.getHitbox().y - c.getyDrawOffset() - yLvlOffset),
                        COIN_WIDTH, COIN_HEIGHT, null);
            }
        }
    }

    private void drawCandles(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Candle c : currentLevel.getCandles()) {
            g.drawImage(candleImgs[c.getAniIndex()], c.getX() - xLvlOffset, c.getY() - yLvlOffset + (int) (7 * Game.SCALE), (int) (32 * Game.SCALE), (int) (32 * Game.SCALE), null);
        }
    }

    private void drawCandleLights(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (CandleLight cl : currentLevel.getCandleLights()) {
            g.drawImage(candleLightImgs[cl.getAniIndex()], cl.getX() - xLvlOffset, cl.getY() - yLvlOffset + (int) (7 * Game.SCALE), (int) (32 * Game.SCALE), (int) (32 * Game.SCALE), null);
        }
    }

    public void resetAllObjects() {
        loadObjects(playing.getLevelManager().getCurrentLevel());
        for (Potion p : potions) {
            p.reset();
        }
        for (GameContainer gc : containers) {
            gc.reset();
        }
        for (Cannon c : currentLevel.getCannons()) {
            c.reset();
        }
    }

    public void resetCoins() {
        for (Coin c : currentLevel.getCoins()) {
            c.reset();
        }
    }

    public ArrayList<Coin> getCoins() {
        return coins;
    }

}
