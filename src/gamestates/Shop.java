package gamestates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import main.Game;
import ui.ShopButton;
import static utilz.Constants.ObjectConstants.*;
import utilz.LoadSave;

public class Shop extends State implements Statemethods {

    private ShopButton[] buttons = new ShopButton[3];
    private BufferedImage backgroundImg, backgroundImgPink;
    private int shopX, shopY, shopWidth, shopHeight;
//    private Game game;

    public Shop(Game game) {
        super(game);
        loadButtons();
        loadBackground();
        backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
    }

    private void loadBackground() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.SHOP_BACKGROUND);
        shopWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
        shopHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
        shopX = Game.GAME_WIDTH / 2 - shopWidth / 2;
        shopY = (int) (25 * Game.SCALE);
    }

    private void loadButtons() {

        buttons[0] = new ShopButton(Game.GAME_WIDTH / 2, (int) (193 * Game.SCALE), 0, RED_POTION_COST);
        buttons[1] = new ShopButton(Game.GAME_WIDTH / 2, (int) (263 * Game.SCALE), 1, GREEN_POTION_COST);
        buttons[2] = new ShopButton(Game.GAME_WIDTH / 2, (int) (333 * Game.SCALE), 2, DOUBLE_HEALTH_COST);
    }

    public void update() {
        for (int i = 0; i < buttons.length; ++i) {
            if (i == 2) {
                if (game.getPlaying().isDoubleHealth()) {
                    continue;
                }
            } else if (i == 1) {
                if (game.getPlaying().isBuyGreenPotion()) {
                    continue;
                }
            }
            buttons[i].update();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImgPink, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(backgroundImg, shopX, shopY, shopWidth, shopHeight, null);

        for (ShopButton sb : buttons) {
            sb.draw(g);
        }

        g.setColor(Color.white);
        Font myFont = new Font("Algerian", Font.PLAIN, 40);
        g.setFont(myFont);
        g.drawString(Integer.toString(game.getPlaying().getRedPotionNumber()), 702, 296);
        g.drawString(Integer.toString(game.getPlaying().getGreenPotionNumber()), 837, 296);
        g.drawString(Integer.toString(game.getPlaying().getPlayer().getCoinNumber()), 983, 296);

    }

    public void mousePressed(MouseEvent e) {
        for (ShopButton sb : buttons) {
            if (isIn(e, sb)) {
                sb.setMousePressed(true);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        for (ShopButton sb : buttons) {
            if (isIn(e, sb)) {
                if (sb.isMousePressed()) {
//                    sb.applyGamestate();
                    if (sb.getRowIndex() == 0) {
                        if (game.getPlaying().getPlayer().getCoinNumber() >= sb.getCost()) {
                            game.getPlaying().changeRedPotionNumber(1);
                            game.getPlaying().getPlayer().changeCoinNumber(-sb.getCost());
                        }
                    } else if (sb.getRowIndex() == 1) {
                        if (game.getPlaying().isBuyGreenPotion() == false) {
                            if (game.getPlaying().getPlayer().getCoinNumber() >= sb.getCost()) {
                                game.getPlaying().getPlayer().changeGreenPotionNumber(1);
                                game.getPlaying().getPlayer().changeCoinNumber(-sb.getCost());
                                game.getPlaying().getPlayer().setBuyGreenPotion(true);
                            }
                        }
                    } else {
                        if (game.getPlaying().isDoubleHealth() == false) {
                            if (game.getPlaying().getPlayer().getCoinNumber() >= sb.getCost()) {
                                game.getPlaying().doubleHealth();
                                game.getPlaying().getPlayer().changeCoinNumber(-sb.getCost());
                            }
                        }
                    }
                }
                break;
            }
        }
        resetButtons();
    }

    private void resetButtons() {
        for (ShopButton sb : buttons) {
            sb.resetBools();
        }
    }

    private boolean isIn(MouseEvent e, ShopButton sb) {
        return sb.getBounds().contains(e.getX(), e.getY());
    }

    public void mouseMoved(MouseEvent e) {
        for (ShopButton sb : buttons) {
            sb.setMouseOver(false);
        }

        for (ShopButton sb : buttons) {
            if (isIn(e, sb)) {
                sb.setMouseOver(true);
                break;
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
        }
    }

    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

}
