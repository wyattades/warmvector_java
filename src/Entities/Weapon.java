package Entities;

import GameState.GameStateManager;
import Util.MyMath;
import Util.StringUtil;

import java.awt.image.BufferedImage;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 1/22/2015.
 */
public abstract class Weapon extends Entity implements Cloneable {

    public static final double ORIGIN_RADIUS = 20.0;
    public static final int TYPE_AMOUNT = 6;

    public int ammo, reserveAmmo;

    private String shootSound;
    public String hitSound;

    public final int clipSize, amountPerShot, damage, rate;
    public final String name, bulletImage;
    public final double i_speed, spread, accel, explodeRadius;

    public static class RPG extends Weapon {
        public RPG(GameStateManager _gsm) {
            super(_gsm, "RPG", 3, 16, 1, 1500, 70, 0.02, 2.0, 0.5, 100.0, "impact_missile.wav", "missile.png");
        }
    }

    public static class LMG extends Weapon {
        public LMG(GameStateManager _gsm) {
            super(_gsm, "LMG", 84, 168, 1, 50, 9, 0.12, 15.0, 0.0, 15.0, StringUtil.randomAsset("ric%d.wav", 2),
                    "bullet.png");
        }
    }

    public static class Pistol extends Weapon {
        public Pistol(GameStateManager _gsm) {
            super(_gsm, "Pistol", 12, 32, 1, 280, 10, 0.05, 15.0, 0.0, 15.0, StringUtil.randomAsset("ric%d.wav", 2),
                    "bullet.png");
        }
    }

    public static class Rifle extends Weapon {
        public Rifle(GameStateManager _gsm) {
            super(_gsm, "Rifle", 32, 64, 1, 100, 15, 0.05, 19.0, 0.0, 22.0, StringUtil.randomAsset("ric%d.wav", 2),
                    "bullet.png");
        }
    }

    public static class Shotgun extends Weapon {
        public Shotgun(GameStateManager _gsm) {
            super(_gsm, "Shotgun", 6, 18, 6, 1100, 13, 0.35, 16.0, 0.0, 18.0, StringUtil.randomAsset("ric%d.wav", 2),
                    "bullet.png");
        }
    }

    public static class Sniper extends Weapon {
        public Sniper(GameStateManager _gsm) {
            super(_gsm, "Sniper", 10, 20, 1, 1000, 50, 0.01, 22.0, 0.0, 30.0, StringUtil.randomAsset("ric%d.wav", 2),
                    "bullet.png");
        }
    }

    protected Weapon(GameStateManager _gsm, String _name, int _clipSize, int _reserveAmmo, int _amountPerShot,
            int _rate,
            int _damage, double _spread, double _i_speed, double _accel, double _explodeRadius,
            String _hitSound, String _bulletImage) {

        super(_gsm, 0, 0, MyMath.random(0.0, MyMath.TWO_PI));

        name = _name;
        ammo = clipSize = _clipSize;
        reserveAmmo = _reserveAmmo;
        amountPerShot = _amountPerShot;
        rate = _rate;
        damage = _damage;
        spread = _spread;
        i_speed = _i_speed;
        accel = _accel;
        explodeRadius = _explodeRadius;

        bulletImage = _bulletImage;

        hitSound = _hitSound;
        shootSound = "shoot_" + name + ".wav";

        sprite = (BufferedImage) gsm.assetManager.getAsset("gun_" + name + ".png");
        setBounds(sprite.getWidth(), sprite.getWidth());

    }

    @Override
    public Weapon clone() {
        try {
            final Weapon result = (Weapon) super.clone();
            result.state = true;
            return result;
        } catch (final CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public static Weapon getType(GameStateManager gsm, int type) {
        switch (type) {
            case (0):
                return new Weapon.Pistol(gsm);
            case (1):
                return new Weapon.Shotgun(gsm);
            case (2):
                return new Weapon.Rifle(gsm);
            case (3):
                return new Weapon.Sniper(gsm);
            case (4):
                return new Weapon.LMG(gsm);
            case (5):
                return new Weapon.RPG(gsm);
            default:
                return null;
        }
    }

    public void shoot() {
        gsm.audioManager.playSFX(shootSound);
        changeAmmo(-1);
    }

    private void changeAmmo(int amount) {
        ammo += amount;
        if (ammo < 0)
            ammo = 0;
    }

}
