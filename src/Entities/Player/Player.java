package Entities.Player;

import Entities.Entity;
import Entities.Hittable;
import Entities.Projectile;
import Entities.Weapon;
import GameState.GameStateManager;
import Main.Game;
import UI.Map;
import Util.ImageUtils;
import Util.MyMath;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Directory: WarmVector_Client_Singleplayer/Entities/
 * Created by Wyatt on 1/22/2015.
 */
public abstract class Player extends Entity implements Hittable {

    public Weapon weapon;
    public double life;
    public double maxLife;
    protected double topSpeed, diagSpeed;
    public static final double acceleration = 1.0;
    public int shootTime;
    public boolean shooting, moving;
    protected BufferedImage shootSprite, defaultSprite, deadSprite;
    protected Map map;

    private String hitSound = "ric0.wav";
    private final BufferedImage[] HIT_ANIMATION;

    protected Player(GameStateManager _gsm, double _orient, Map _map) {
        super(_gsm, 0, 0, _orient);

        // TODO: recolor this somewhere else (so it only runs once in entire program)
        HIT_ANIMATION = ImageUtils.recolorAnimation((BufferedImage[]) gsm.assetManager.getAsset("hit_"), Color.RED);
        map = _map;
        shooting = moving = false;
        life = maxLife = 100.0;
        shootTime = Game.currentTimeMillis();
        setVelocity(0);
    }

    protected void setTopSpeed(double value) {
        topSpeed = value;
        diagSpeed = topSpeed / Math.sqrt(2.0);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        if (weapon != null) {
            AffineTransform oldTForm = g.getTransform();
            g.rotate(orient, x, y);
            g.drawImage(weapon.sprite, (int) (x - weapon.sprite_w2 + 26), (int) (y - weapon.sprite_h2 + 2), null);
            g.setTransform(oldTForm);
        }
    }

    public void setWeapon(Weapon w) {
        weapon = w;
        if (w == null) {
            setSpriteToDefault(true);
        } else {
            setSpriteToDefault(false);
        }
    }

    protected void setSpriteToDefault(boolean bool) {
        if (bool)
            sprite = defaultSprite;
        else
            sprite = shootSprite;
    }

    private boolean moveX, moveY;

    public void moveX(double deltaV) {
        // vx += deltaV;
        if (deltaV > 0.0)
            vx = topSpeed;
        else
            vx = -topSpeed;
        moveX = true;

        // if (vx > topSpeed) vx = topSpeed;
        // else if (vx < -topSpeed) vx = -topSpeed;
    }

    public void moveY(double deltaV) {
        // vy += deltaV;
        if (deltaV > 0.0)
            vy = topSpeed;
        else
            vy = -topSpeed;
        moveY = true;

        // if (vy > topSpeed) vy = topSpeed;
        // else if (vy < -topSpeed) vy = -topSpeed;
    }

    public void updatePosition(double deltaTime) {

        // Limit total speed to diagSpeed
        if (Math.abs(vx) > diagSpeed && Math.abs(vy) > diagSpeed) {
            if (vx > 0.0)
                vx = diagSpeed;
            else
                vx = -diagSpeed;
            if (vy > 0.0)
                vy = diagSpeed;
            else
                vy = -diagSpeed;
        }

        // If there is no obstacle horizontally
        if (vx != 0.0 && !obstacleX(vx)) {
            x += vx * deltaTime;
        } else {
            // Restrict horizontal movement
            vx = 0;
        }

        // If there is no obstacle vertically
        if (vy != 0.0 && !obstacleY(vy)) {
            y += vy * deltaTime;
        } else {
            // Restrict vertical movement
            vy = 0.0;
        }

        // // Apply friction on y velocity if not accelerating
        // if (slowY) {
        // if (vy > 0.0) {
        // vy = Math.max(vy - acceleration, 0.0);
        // } else if (vy < 0.0) {
        // vy = Math.min(vy + acceleration, 0.0);
        // }
        // }
        //
        // // Apply friction on x velocity if not accelerating
        // if (slowX) {
        // if (vx > 0.0) {
        // vx = Math.max(vx - acceleration, 0.0);
        // } else if (vx < 0.0) {
        // vx = Math.min(vx + acceleration, 0.0);
        // }
        // }

        if (moveX) {
            moveX = false;
        } else {
            vx = 0.0;
        }
        if (moveY) {
            moveY = false;
        } else {
            vy = 0.0;
        }

        moving = vy != 0.0 || vx != 0.0;

        // double deccel = topSpeed * 0.1;
        //
        // if (moving) {
        // if (vx > 0) {
        // vx = Math.max(0.0, vx - deccel);
        // } else if (vx < 0) {
        // vx = Math.min(0.0, vx + deccel);
        // }
        // if (vy > 0) {
        // vy = Math.max(0.0, vy - deccel);
        // } else if (vy < 0) {
        // vy = Math.min(0.0, vy + deccel);
        // }
        // }
    }

    public void handleDamage(double damage) {
        life -= damage;
        if (life < 0.0) {
            life = 0.0;
            state = false;
        }
    }

    public boolean handleIndirectHit(Projectile p) {
        double distSq = (y - p.y) * (y - p.y) + (x - p.x) * (x - p.x);
        double radiusSq = p.explodeRadius * p.explodeRadius;
        if (distSq < radiusSq) {
            handleDamage(p.damage * distSq / radiusSq);
            return true;
        }
        return false;
    }

    public boolean handleDirectHit(Projectile p) {
        if (collideBox.intersectsLine(p.collideLine)) {
            handleDamage(p.damage);
            gsm.audioManager.playSFX(hitSound);
            return true;
        } else {
            return false;
        }
    }

    public BufferedImage[] getHitAnimation() {
        return HIT_ANIMATION;
    }

    public int stepTime = Game.currentTimeMillis();

    public void update(double deltaTime) {
        updateSpecific();
        updatePosition(deltaTime);
        updateCollideBox();
    }

    abstract protected void updateSpecific();

    public Weapon getWeaponForDrop() {
        double spawnDist = MyMath.random(10.0, 24.0);
        Weapon w = weapon.clone();
        w.x = x + spawnDist * Math.cos(MyMath.random(0.0, MyMath.TWO_PI));
        w.y = y + spawnDist * Math.sin(MyMath.random(0.0, MyMath.TWO_PI));
        w.orient = MyMath.random(0.0, MyMath.TWO_PI);
        return w;
    }

    private boolean obstacleX(double vx) {
        Rectangle2D futureX = new Rectangle2D.Double(x - w * 0.5 + vx, y - h * 0.5, w, h);
        return map.inverseRegion.intersects(futureX);
    }

    private boolean obstacleY(double vy) {
        Rectangle2D futureY = new Rectangle2D.Double(x - w * 0.5, y - h * 0.5 + vy, w, h);
        return map.inverseRegion.intersects(futureY);
    }

    public void deathSequence() {
        weapon = null;
        setVelocity(0);
        sprite = deadSprite;
        // animation???
    }

}
