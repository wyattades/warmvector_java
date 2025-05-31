package Entities.Player;

import GameState.GameStateManager;
import Main.Game;
import UI.Map;
import Util.MyMath;

import java.awt.image.BufferedImage;

/**
 * Directory: WarmVector_Client_Singleplayer/Entities/
 * Created by Wyatt on 1/22/2015.
 */
public class ThisPlayer extends Player {

    public int hitTime;
    public boolean left, right, up, down;

    private String[] walkSounds;
    private String reloadSound;

    public ThisPlayer(GameStateManager _gsm, Map _map) {
        super(_gsm, 0.0, _map);
        setTopSpeed(5.0);

        walkSounds = new String[6];
        for (int i = 0; i <= 5; i++) {
            walkSounds[i] = "concrete" + i + ".wav";
        }
        reloadSound = "reload.wav";

        shootSprite = (BufferedImage) gsm.assetManager.getAsset("player0g.png");
        defaultSprite = (BufferedImage) gsm.assetManager.getAsset("player0.png");
        setSpriteToDefault(weapon == null);
        setBounds(36.0, 36.0);

        left = right = up = down = false;

    }

    protected void updateSpecific() {
        // If leftKey is pressed and right isn't, player goes left
        if (left && !right)
            moveX(-Player.acceleration);

        // If rightKey is pressed and left isn't, player goes right
        else if (right && !left)
            moveX(Player.acceleration);

        // If upKey is pressed and down isn't, player goes up
        if (up && !down)
            moveY(-Player.acceleration);

        // If downKey is pressed and up isn't, player goes down
        else if (down && !up)
            moveY(Player.acceleration);

        regenHealth();

        if (moving && Game.currentTimeMillis() - stepTime > 300) {
            stepTime = Game.currentTimeMillis();
            gsm.audioManager.playSFX(walkSounds[(int) MyMath.random(0.0, 6.0)]);
        }
    }

    public void reloadWeapon() {
        if (weapon != null && weapon.ammo < weapon.clipSize && weapon.reserveAmmo > 0) {
            int addedAmmo = Math.min(weapon.clipSize - weapon.ammo, weapon.reserveAmmo);
            weapon.ammo += addedAmmo;
            weapon.reserveAmmo -= addedAmmo;
            gsm.audioManager.playSFX(reloadSound);
        }
    }

    private void regenHealth() {
        if (Game.currentTimeMillis() - hitTime > 2000) {
            life = Math.min(maxLife, life + 0.1);
        }
    }

}
