package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;
import static utilz.Constants.Directions.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import audio.AudioPlayer;
import gamestates.Playing;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import main.Game;
import objects.Coin;
import static utilz.Constants.ObjectConstants.RED_POTION;
import static utilz.Constants.ObjectConstants.SILVER_COIN;
import utilz.LoadSave;

public class Player extends Entity {

    private BufferedImage[][] animations;
    private boolean moving = false, attacking = false;
    private boolean left, right, jump;
    private int[][] lvlData;
    private float xDrawOffset = 21 * Game.SCALE;
    private float yDrawOffset = 4 * Game.SCALE;

    // Jumping / Gravity
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    // StatusBarUI
    private BufferedImage statusBarImg, redPotionImg, greenPotionImg, silverCoinImg;

    private int statusBarWidth = (int) (192 * Game.SCALE);
    private int statusBarHeight = (int) (58 * Game.SCALE);
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);

    private int healthBarWidth = (int) (150 * Game.SCALE);
    private int healthBarHeight = (int) (4 * Game.SCALE);
    private int healthBarXStart = (int) (34 * Game.SCALE);
    private int healthBarYStart = (int) (14 * Game.SCALE);
    private int healthWidth = healthBarWidth;

    private int powerBarWidth = (int) (104 * Game.SCALE);
    private int powerBarHeight = (int) (2 * Game.SCALE);
    private int powerBarXStart = (int) (44 * Game.SCALE);
    private int powerBarYStart = (int) (34 * Game.SCALE);
    private int powerWidth = powerBarWidth;
    private int powerMaxValue;
    private int powerValue = powerMaxValue;

    private int redPotionX = (int) (10 * Game.SCALE);
    private int greenPotionX = (int) (80 * Game.SCALE);
    private int silverCoinX = (int) (154 * Game.SCALE);
    private int yPos = (int) (68 * Game.SCALE);
    private int potionHeight = (int) (17 * Game.SCALE);
    private int coinHeight = (int) (16 * Game.SCALE);

    private int coin;
    private int redPotionNumber;
    private int greenPotionNumber;

    private boolean doubleHealth;
    private boolean buyGreenPotion;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackChecked;
    private Playing playing;

    private int tileY = 0;

    private boolean powerAttackActive;
    private int powerAttackTick;
    private int powerGrowSpeed = 15;
    private int powerGrowTick;

    private int yPosBeforeDie, xPosBeforeDie;

    File file = new File("ObjectsInfo.in");

    public Player(float x, float y, int width, int height, Playing playing) throws IOException {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.powerMaxValue = 100;
        this.powerValue = powerMaxValue;
        readFile();
        this.walkSpeed = Game.SCALE * 1.0f;
        loadAnimations();
        initHitbox(20, 27);
        initAttackBox();
    }

    private void readFile() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        Scanner sc = new Scanner(file);
        while (sc.hasNext()) {
            redPotionNumber = sc.nextInt();
            greenPotionNumber = sc.nextInt();
            coin = sc.nextInt();
        }
    }

    public void wirteFile() throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(redPotionNumber + " " + greenPotionNumber + " " + coin);
        fw.close();
    }

    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    public void newSpawnOfRelive() {
        this.x = xPosBeforeDie;
        this.y = yPosBeforeDie;
    }

    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int) (35 * Game.SCALE), (int) (20 * Game.SCALE));
        resetAttackBox();
    }

    public void update() {
        updateHealthBar();
        updatePowerBar();

        if (currentHealth <= 0) {
            if (state != DEAD) {
                state = DEAD;
                aniTick = 0;
                aniIndex = 0;
                playing.setPlayerDying(true);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

                // Check if player died in air
                if (!IsEntityOnFloor(hitbox, lvlData)) {
                    inAir = true;
                    airSpeed = 0;
                }
            } else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
//                playing.setGameOver(true);
                playing.setBeforeGameOver(true);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();
                // Fall if in air
                if (inAir) {
                    if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else {
                        inAir = false;
                    }
                }
            }
            return;
        }

        updateAttackBox();

        if (state == HIT) {
            if (aniIndex <= GetSpriteAmount(state) - 3) {
                pushBack(pushBackDir, lvlData, 1.25f);
            }
            updatePushBackDrawOffset();
        } else {
            updatePos();
        }

        if (moving) {
            checkPotionTouched();
            checkCoinTouched();
            checkSpikesTouched();
            checkInsideWater();
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
            if (powerAttackActive) {
                powerAttackTick++;
                if (powerAttackTick >= 35) {
                    powerAttackTick = 0;
                    powerAttackActive = false;
                }
            }
        }

        if (attacking || powerAttackActive) {
            checkAttack();
        }

        updateAnimationTick();
        setAnimation();
    }

    private void checkInsideWater() {
        if (IsEntityInWater(hitbox, playing.getLevelManager().getCurrentLevel().getLevelData())) {
            currentHealth = 0;
        }
    }

    private void checkSpikesTouched() {
        playing.checkSpikesTouched(this);
    }

    private void checkPotionTouched() {
        playing.checkPotionTouched(hitbox);
    }

    private void checkCoinTouched() {
        playing.checkCoinTouched(hitbox);
    }

    private void checkAttack() {
        if (attackChecked || aniIndex != 1) {
            return;
        }
        attackChecked = true;

        if (powerAttackActive) {
            attackChecked = false;
        } else {
            changePower(-10);
        }
        playing.checkEnemyHit(attackBox);
        playing.checkObjectHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    private void setAttackBoxOnRightSide() {
        attackBox.x = hitbox.x + hitbox.width - (int) (Game.SCALE * 5);
    }

    private void setAttackBoxOnLeftSide() {
        attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
    }

    private void updateAttackBox() {
        if (right && left) {
            if (flipW == 1) {
                setAttackBoxOnRightSide();
            } else {
                setAttackBoxOnLeftSide();
            }

        } else if (right || (powerAttackActive && flipW == 1)) {
            setAttackBoxOnRightSide();
        } else if (left || (powerAttackActive && flipW == -1)) {
            setAttackBoxOnLeftSide();
        }

        attackBox.y = hitbox.y + (Game.SCALE * 10);
    }

    private void updateHealthBar() {
        healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
    }

    private void updatePowerBar() {
        powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

        powerGrowTick++;
        if (powerGrowTick >= powerGrowSpeed) {
            powerGrowTick = 0;
            changePower(1);
        }
    }

    public void usePotion(int number, int kindPotion) {
        if (number > 0) {
            number -= 1;
            if (currentHealth > 0) {
                if (kindPotion == RED_POTION) {
                    changeHealth(50);
                    redPotionNumber = number;
//                } else {
//                    changePower(50);
//                    greenPotionNumber = number;
                }
            }
        }
    }

    public void render(Graphics g, int xlvlOffset, int yLvlOffset) {
        g.drawImage(animations[state][aniIndex], (int) (hitbox.x - xDrawOffset) - xlvlOffset + flipX, (int) (hitbox.y - yDrawOffset - yLvlOffset + (int) (pushDrawOffset)), width * flipW, height, null);
//        drawHitbox(g, xlvlOffset, yLvlOffset);
//		drawAttackBox(g, lvlOffset);
//        drawUI(g);
    }

    public void drawUI(Graphics g) {
        // Background ui
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

        // Health bar
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

        if (isDoubleHealth()) {
            g.setColor(Color.pink);
            g.fillRect(healthBarXStart + statusBarX + healthWidth / 2, healthBarYStart + statusBarY, (int) (2.5 * Game.SCALE), healthBarHeight - 1);
        }

        // Power Bar
        g.setColor(Color.yellow);
        g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);

        g.drawImage(redPotionImg, redPotionX, yPos, (int) (35 * Game.SCALE), (int) (potionHeight / 2 * Game.SCALE), null);
        g.drawImage(greenPotionImg, greenPotionX, yPos, (int) (35 * Game.SCALE), (int) (potionHeight / 2 * Game.SCALE), null);
        g.drawImage(silverCoinImg, silverCoinX, yPos + 3, (int) (35 * Game.SCALE), (int) (coinHeight / 2 * Game.SCALE), null);

        g.setColor(Color.white);
        Font myFont = new Font("Algerian", Font.PLAIN, (int) (15 * Game.SCALE));
        g.setFont(myFont);
        g.drawString(Integer.toString(redPotionNumber), (int) (42 * Game.SCALE), (int) (83.5 * Game.SCALE));
        g.drawString(Integer.toString(greenPotionNumber), (int) (119 * Game.SCALE), (int) (83.5 * Game.SCALE));
        g.drawString(Integer.toString(coin), (int) (188 * Game.SCALE), (int) (83.5 * Game.SCALE));

        if (currentHealth < 0.2 * maxHealth) {
            g.setColor(Color.red);
        }
        g.drawString(Integer.toString(currentHealth) + "/" + Integer.toString(maxHealth), (int) (210 * Game.SCALE), (int) (31 * Game.SCALE));

        g.setColor(Color.white);
        myFont = new Font("Algerian", Font.PLAIN, (int) (12.5 * Game.SCALE));
        g.setFont(myFont);
        g.drawString(Integer.toString(powerValue) + "/" + Integer.toString(powerMaxValue), (int) (170 * Game.SCALE), (int) (51 * Game.SCALE));
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetSpriteAmount(state)) {
                aniIndex = 0;
                attacking = false;
                attackChecked = false;
                if (state == HIT) {
                    newState(IDLE);
                    airSpeed = 0f;
                    if (!IsFloor(hitbox, 0, lvlData)) {
                        inAir = true;
                    }
                }
            }
        }
    }

    private void setAnimation() {
        int startAni = state;

        if (state == HIT) {
            return;
        }

        if (moving) {
            state = RUNNING;
        } else {
            state = IDLE;
        }

        if (inAir) {
            if (airSpeed < 0) {
                state = JUMP;
            } else {
                state = FALLING;
            }
        }

        if (powerAttackActive) {
            state = ATTACK;
            aniIndex = 1;
            aniTick = 0;
            return;
        }

        if (attacking) {
            state = ATTACK;
            if (startAni != ATTACK) {
                aniIndex = 1;
                aniTick = 0;
                return;
            }
        }
        if (startAni != state) {
            resetAniTick();
        }
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    private void updatePos() {
        moving = false;

        if (jump) {
            jump();
        }

        if (!inAir) {
            if (!powerAttackActive) {
                if ((!left && !right) || (right && left)) {
                    return;
                }
            }
        }

        float xSpeed = 0;

        if (left && !right) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if (right && !left) {
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }

        if (powerAttackActive) {
            if ((!left && !right) || (left && right)) {
                if (flipW == -1) {
                    xSpeed = -walkSpeed;
                } else {
                    xSpeed = walkSpeed;
                }
            }

            xSpeed *= 3;
        }

        if (!inAir) {
            if (!IsEntityOnFloor(hitbox, lvlData)) {
                inAir = true;
            }
        }

        if (inAir && !powerAttackActive) {
            if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed);
            } else {
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0) {
                    resetInAir();
                } else {
                    airSpeed = fallSpeedAfterCollision;
                }
                updateXPos(xSpeed);
            }

        } else {
            xPosBeforeDie = (int) (hitbox.x - 21 * Game.SCALE * flipW);
            yPosBeforeDie = (int) hitbox.y;
            updateXPos(xSpeed);
        }
        moving = true;
    }

    private void jump() {
        if (inAir) {
            return;
        }
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
        inAir = true;
        airSpeed = jumpSpeed;
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void updateXPos(float xSpeed) {
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += xSpeed;
        } else {
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
            if (powerAttackActive) {
                powerAttackActive = false;
                powerAttackTick = 0;
            }
        }
    }

    public void changeHealth(int value) {
        if (value < 0) {
            if (state == HIT) {
                return;
            } else {
                newState(HIT);
            }
        }

        currentHealth += value;
        currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
    }

    public void changeHealth(int value, Enemy e) {
        if (state == HIT) {
            return;
        }
        changeHealth(value);
        pushBackOffsetDir = UP;
        pushDrawOffset = 0;

        if (e.getHitbox().x < hitbox.x) {
            pushBackDir = RIGHT;
        } else {
            pushBackDir = LEFT;
        }
    }

    public void changeRedPotionNumber(int number) {
        this.redPotionNumber += number;
    }

    public void changeGreenPotionNumber(int number) {
        this.greenPotionNumber += number;
    }

    public void doubleHealth() {
        maxHealth *= 2;
        currentHealth *= 2;
        doubleHealth = true;
    }

    public void kill() {
        currentHealth = 0;
    }

    public void changePower(int value) {
        powerValue += value;
        powerValue = Math.max(Math.min(powerValue, powerMaxValue), 0);
    }

    public void changeCoinNumber(int value) {
        coin += value;
    }

    private void loadAnimations() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
        animations = new BufferedImage[7][8];
        for (int j = 0; j < animations.length; j++) {
            for (int i = 0; i < animations[j].length; i++) {
                animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);
            }
        }

        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
        redPotionImg = LoadSave.GetSpriteAtlas(LoadSave.RED_POTION);
        greenPotionImg = LoadSave.GetSpriteAtlas(LoadSave.GREEN_POTION);
        silverCoinImg = LoadSave.GetSpriteAtlas(LoadSave.SILVER_COIN);

    }

    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
        if (!IsEntityOnFloor(hitbox, lvlData)) {
            inAir = true;
        }
    }

    public void coinAppear() {
        if (coin > 0) {
            playing.getObjectManager().getCoins().add(new Coin((int) hitbox.x, (int) hitbox.y, SILVER_COIN));
            changeCoinNumber(-1);
        }
    }

    public void resetDirBooleans() {
        left = false;
        right = false;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void resetAll() {
        resetDirBooleans();
        inAir = false;
        attacking = false;
        moving = false;
//        airSpeed = 0f;
        jump = false;
        state = IDLE;
        maxHealth = 100;
        currentHealth = maxHealth;
        doubleHealth = false;
        buyGreenPotion = false;
        powerAttackActive = false;
        powerAttackTick = 0;
        powerValue = powerMaxValue;

        if (playing.isBeforeGameOver()) {
            newSpawnOfRelive();
        }

        hitbox.x = x;
        hitbox.y = y;
        resetAttackBox();

        if (!IsEntityOnFloor(hitbox, lvlData)) {
            inAir = true;
        }

    }

    private void resetAttackBox() {
        if (flipW == 1) {
            setAttackBoxOnRightSide();
        } else {
            setAttackBoxOnLeftSide();
        }
    }

    public int getTileY() {
        return tileY;
    }

    public void powerAttack() {
        if (powerAttackActive) {
            return;
        }
        if (powerValue >= 60) {
            powerAttackActive = true;
            changePower(-60);
        }
    }

    public int getPowerValue() {
        return powerValue;
    }

    public int getRedPotionNumber() {
        return redPotionNumber;
    }

    public int getGreenPotionNumber() {
        return greenPotionNumber;
    }

    public int getCoinNumber() {
        return coin;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isDoubleHealth() {
        return doubleHealth;
    }

    public void setIsDoubleHealth(boolean doubleHealth) {
        this.doubleHealth = doubleHealth;
    }

    public boolean isBuyGreenPotion() {
        return buyGreenPotion;
    }

    public void setBuyGreenPotion(boolean buyGreenPotion) {
        this.buyGreenPotion = buyGreenPotion;
    }

}
