package Entities;

import Entities.Player.Player;
import GameState.GameStateManager;
import Util.MyMath;

import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/**
 * Directory: WarmVector_Client_Singleplayer/Entities/
 * Created by Wyatt on 9/12/2015.
 */
public class Projectile extends Entity {

    public Line2D collideLine;
    private double lastX, lastY;
    private double accel;
    public double explodeRadius, damage;
    public String hitSound;

    public Player shooter;

    protected Projectile(GameStateManager _gsm, Player _shooter) {

        super(_gsm, _shooter.x, _shooter.y,
                _shooter.orient + MyMath.random(-_shooter.weapon.spread * 0.5, _shooter.weapon.spread * 0.5));

        shooter = _shooter;

        sprite = (BufferedImage) gsm.assetManager.getAsset(_shooter.weapon.bulletImage);
        setBounds(sprite.getWidth(), sprite.getHeight());

        setVelocity(_shooter.weapon.i_speed);

        x += Weapon.ORIGIN_RADIUS * dirX;
        y += Weapon.ORIGIN_RADIUS * dirY;
        lastX = x;
        lastY = y;

        hitSound = _shooter.weapon.hitSound;
        accel = _shooter.weapon.accel;
        explodeRadius = _shooter.weapon.explodeRadius;
        damage = _shooter.weapon.damage;

        collideLine = new Line2D.Double(x, y, x, y);

    }

    public void move(double deltaTime) {
        if (accel != 0) {
            vx += dirX * accel;
            vy += dirY * accel;
        }

        lastX = x;
        lastY = y;

        x += vx * deltaTime;
        y += vy * deltaTime;

        updateCollideBox();
    }

    @Override
    public void updateCollideBox() {
        collideLine.setLine(x, y, lastX, lastY);
    }
}
