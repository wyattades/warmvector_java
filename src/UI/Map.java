package UI;

import Entities.Hittable;
import Entities.Projectile;
import GameState.GameStateManager;
import Util.ImageUtils;
import Util.MyMath;
import Util.Rect;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by wyatt on 8/27/2015.
 */

// TODO: only render local region of map to reduce performance, but how???

public class Map implements Hittable {

    // This class creates a randomly generated map, consisting of rooms with
    // corridors connecting them.
    // I use the Binary Space Partitioning (look it up) to initially split the space
    // into partitions.
    // Then, I randomly reduce the size of these partitions, and I add corridors
    // that branch out to all the rooms
    // The room and corridor rectangles are stored separately, as well as the list
    // of the surround walls
    // Then I add random obstacles to the edges of the rooms (without blocking the
    // corridors)
    // Then I use my smoothing algorithm to smooth the edges and make it "cave-like"
    // and more natural

    // Public variables
    public int width, height;
    public List<Rect> cells, // Stores all types of rooms
            rooms, // Stores rooms (not corridors)
            corridors; // Stores only corridors
    private List<Rectangle2D> openSpaces; // Adjacent regions to corridors (no obstacles can spawn here)
    public Area region, // Accessible area of map
            inverseRegion, // Destructible wall
            borderRegion, // Indestructible wall
            innerRegion;
    public List<Line2D> walls; // Stores line segments of map (these are used for shadowing, collision
                               // detection, etc.)

    // Private variables
    private int iterations;

    // Private constants
    private static final Color FILL_COLOR = new Color(50, 50, 50);
    private static final Stroke STROKE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color STROKE_COLOR = new Color(90, 90, 90);
    private static final double roomReductionFactor = 0.42,
            smoothingFactor = 0.5,
            splitSizeFactor = 0.22;
    private static final int minSpacing = 4,
            pathThickness = 6,
            smoothingIterations = 3,
            minRoomSize = 50,
            minObstacleSize = 6,
            scale = 12; // The value in which the generated map is scaled by
    private static final boolean randomObstaclesOnEdges = true;

    public BufferedImage background;

    private Random randomGenerate;

    private final Color fillColor;

    // Public constants
    private final BufferedImage[] HIT_ANIMATION;

    private GameStateManager gsm;

    private String hitSound = "ric1.wav";

    public Map(GameStateManager _gsm, int i_width, int i_height, double scaleFactor, boolean smooth,
            Random _randomGenerate) {

        gsm = _gsm;

        // TODO: put this somewhere else so it only runs once at beginning of program
        // make a put(Asset) function in AssetManager?
        HIT_ANIMATION = ImageUtils.recolorAnimation((BufferedImage[]) gsm.assetManager.getAsset("hit_"), Color.black);

        randomGenerate = _randomGenerate;

        float randHue = (float) MyMath.random(0.0, 1.0, randomGenerate);
        fillColor = Color.getHSBColor(randHue, 1.0f, 0.06f);
        background = ImageUtils.colorizeImage((BufferedImage) gsm.assetManager.getAsset("background.png"), randHue);

        // Make sure width and height are always even numbers
        width = MyMath.round(i_width, 2);
        height = MyMath.round(i_height, 2);

        // Modify iterations so that the rooms sizes change based on scaleFactor
        // (default is 4 cause it looks the best)
        iterations = 4 + (int) (Math.log(scaleFactor) / Math.log(2.0));

        Area totalArea = new Area(new Rectangle2D.Double(
                -0.5 * width * scale, -0.5 * height * scale, 2.0 * width * scale, 2.0 * height * scale));

        innerRegion = new Area(new Rectangle2D.Double(
                // 0.1f * width * scale, 0.1f * height * scale, 0.8f * width * scale, 0.8f *
                // height * scale
                0.0, 0.0, width * scale, height * scale));

        borderRegion = new Area(totalArea);
        borderRegion.subtract(innerRegion);

        // Keep looping until there are no null rooms (a null room
        // has about a 1 in 50 chance of spawning)
        boolean creationFailed = true;
        while (creationFailed) {

            creationFailed = false;

            // create empty lists of rectangles
            cells = new ArrayList<>();
            rooms = new ArrayList<>();
            corridors = new ArrayList<>();
            openSpaces = new ArrayList<>();

            // add randomized subsection rects to the array
            new Cell(new Rect(0, 0, width, height), 0);

            // Randomly shrink the rectangles,
            // this produces the actual room dimensions
            for (Rect r : cells) {
                int oldRight = r.x + r.w,
                        oldBottom = r.y + r.h;
                r.w = MyMath.round(MyMath.random(r.w * roomReductionFactor, r.w - minSpacing, randomGenerate), 2);
                r.x = MyMath.round(
                        MyMath.random(r.x + minSpacing * 0.5, oldRight - r.w - minSpacing * 0.5, randomGenerate), 2);
                r.h = MyMath.round(MyMath.random(r.h * roomReductionFactor, r.h - minSpacing, randomGenerate), 2);
                r.y = MyMath.round(
                        MyMath.random(r.y + minSpacing * 0.5, oldBottom - r.h - minSpacing * 0.5, randomGenerate), 2);
            }

            // Copy all rooms into rooms list
            rooms.addAll(cells);

            // Keep a copy of cells.size() because cells is modified within the loop (below)
            int roomsAmount = cells.size();

            // The code below creates two lists of rectangles (sisters), where there will be
            // one corridor
            // (it also needs revising: the Cell class is poorly written, it should be tree
            // data instead)
            List<Rect> sister1, sister2;
            for (int i = 0; i < iterations; i++) {
                int iterateAmount = (int) Math.pow(2, i);
                for (int j = 0; j < roomsAmount; j += 2 * iterateAmount) {

                    sister1 = new ArrayList<>();
                    sister2 = new ArrayList<>();

                    for (int k = 0; k < iterateAmount; k++) {
                        sister1.add(cells.get(j + k));
                        sister2.add(cells.get(j + k + iterateAmount));
                    }

                    // Creates one corridor in between a random rectangle from both sister lists
                    Rect corridor = corridorBetween(cells, minSpacing, pathThickness, sister1, sister2);

                    // If the corridor was created successfully, add it to the list of cells
                    if (corridor != null) {
                        corridors.add(corridor);
                        cells.add(corridor);
                    } else {// If not, restart the map generation
                        creationFailed = true;
                    }

                }
            }
        }

        // Create a single region from all of the cells
        region = new Area();

        // Add the rooms and corridors to the region
        for (Rect r : cells) {
            region.add(new Area(new Rectangle2D.Double(r.x, r.y, r.w, r.h)));
        }

        if (randomObstaclesOnEdges) {

            // Add randomized "obstacles" to the rooms
            for (Rect r : rooms) {
                List<Rectangle2D> objects = objectsInRoom(r);
                for (Rectangle2D object : objects) {
                    region.subtract(new Area(object));
                }
            }

        }

        // Create list of points that describe the region
        List<double[]> areaPoints = areaPoints(region);

        for (double[] p : areaPoints) {
            p[1] *= scale;
            p[2] *= scale;
        }

        // smooth edges algorithm
        if (smooth) {
            for (int i = 0; i < smoothingIterations; i++) {
                areaPoints = smoothEdges(areaPoints, smoothingFactor);
            }
        }

        // Redefine region based on new points
        Path2D newPoints = new Path2D.Double();
        newPoints.moveTo(areaPoints.get(0)[1], areaPoints.get(0)[2]);
        for (int i = 1; i < areaPoints.size(); i++) {
            double[] p = areaPoints.get(i);
            newPoints.lineTo(p[1], p[2]);
        }
        newPoints.closePath();
        region = new Area(newPoints);

        // Define an inverse of the region
        inverseRegion = new Area(totalArea);
        inverseRegion.subtract(region);

        // Define the walls (used for shadowing) based on the new region
        walls = calculateWalls(areaPoints);

        width *= scale;
        height *= scale;

        for (Rect r : cells) {
            r.x *= scale;
            r.y *= scale;
            r.w *= scale;
            r.h *= scale;
        }

    }

    private ArrayList<Line2D> calculateWalls(List<double[]> areaPoints) {

        double[] start = new double[3]; // To record where each polygon starts

        // Create an empty list of lines
        ArrayList<Line2D> walls = new ArrayList<>();

        // Based on the region, add to the list of walls
        for (int i = 0; i < areaPoints.size(); i++) {
            // if not on the last point, return a line from this point to the next
            double[] currentElement = areaPoints.get(i);

            // default value in case it reached the end of the ArrayList
            double[] nextElement = { -1, -1, -1 };
            if (i < areaPoints.size() - 1) {
                nextElement = areaPoints.get(i + 1);
            }

            // make the lines
            if (currentElement[0] == PathIterator.SEG_MOVETO) {
                start = currentElement; // Record where the polygon started to close it later
            }

            if (nextElement[0] == PathIterator.SEG_LINETO) {
                walls.add(
                        new Line2D.Double(
                                currentElement[1], currentElement[2],
                                nextElement[1], nextElement[2]));
            } else if (nextElement[0] == PathIterator.SEG_CLOSE) {
                walls.add(
                        new Line2D.Double(
                                currentElement[1], currentElement[2],
                                start[1], start[2]));
            }
        }

        return walls;
    }

    // Randomly placed rectangular obstacles inside a room
    private List<Rectangle2D> objectsInRoom(Rect r) {

        List<Rectangle2D> objects = new ArrayList<>();

        int maxWidth = (int) ((r.w - pathThickness) * 0.5),
                maxHeight = (int) ((r.h - pathThickness) * 0.5);

        if (maxWidth > minObstacleSize && maxHeight > minObstacleSize) {

            int amount = MyMath.round(MyMath.map(Math.min(3500, r.w * r.h), 100, 3500, 1, 5));

            for (int i = 0; i < amount; i++) {
                Rectangle2D object;
                for (int j = 0; j < 10; j++) {

                    int w = (int) (MyMath.random(minObstacleSize, maxWidth, randomGenerate));
                    int h = (int) (MyMath.random(minObstacleSize, maxHeight, randomGenerate));

                    int x, y;
                    double random = MyMath.random(0.0, 1.0, randomGenerate);
                    if (random > 0.75) {
                        x = r.x;
                        y = r.y + r.h - h;
                    } else if (random > 0.5) {
                        x = r.x;
                        y = r.y;
                    } else if (random > 0.25) {
                        x = r.x + r.w - w;
                        y = r.y;
                    } else {
                        x = r.x + r.w - w;
                        y = r.y + r.h - h;
                    }

                    object = new Rectangle2D.Double(x, y, w, h);

                    boolean passes = true;
                    // test for adjacency to corridors
                    for (Rectangle2D openSpace : openSpaces) {
                        if (object.intersects(openSpace)) {
                            passes = false;
                            break;
                        }
                    }

                    if (passes) {
                        objects.add(object);
                        break;
                    }

                }
            }
        }
        // // Object in middle of room (can't work atm)
        // int xSpace = r.w - 2 * pathThickness;
        // int ySpace = r.h - 2 * pathThickness;
        // if (xSpace > pathThickness && ySpace > pathThickness) {
        // int _w = MyMath.round(MyMath.random(pathThickness, xSpace, randomGenerate)),
        // _h = MyMath.round(MyMath.random(pathThickness, ySpace, randomGenerate));
        // objects.add(new Rectangle2D.Double(MyMath.round(random(pathThickness, r.w - 2
        // * pathThickness, randomGenerate)), MyMath.round(random(pathThickness, r.h - 2
        // * pathThickness, randomGenerate)), _w, _h));
        // } else if (xSpace > pathThickness && ySpace > 2) {
        //
        // } else if (ySpace > pathThickness && xSpace > 2) {
        //
        // }

        return objects;
    }

    // An algorithm for smoothing the edges of a region:
    // For each edge, the midpoint is found and the original vertices
    // are moved towards their associated midpoints
    private List<double[]> smoothEdges(List<double[]> input, double factor) {

        // Create list for the midpoints of all the lines
        List<Point2D> midpoints = new ArrayList<>();
        for (double[] edge : input) {
            double x = (edge[1] + edge[1]) * 0.5;
            double y = (edge[2] + edge[2]) * 0.5;
            midpoints.add(new Point2D.Double(x, y));
        }

        // Create a list of points for the original vertices to move towards
        List<Point2D> anchorPoints = new ArrayList<>();

        // Define this list as the midpoints between the midpoints we made earlier
        Point2D p0 = midpoints.get(midpoints.size() - 1),
                p1 = midpoints.get(0);

        anchorPoints.add(new Point2D.Double(
                (p1.getX() + p0.getX()) * 0.5,
                (p1.getY() + p0.getY()) * 0.5));

        for (int i = 0; i < midpoints.size() - 1; i += 1) {

            Point2D pa = midpoints.get(i),
                    pb = midpoints.get(i + 1);

            anchorPoints.add(new Point2D.Double(
                    (pa.getX() + pb.getX()) * 0.5,
                    (pa.getY() + pb.getY()) * 0.5));

        }

        // Create output list of vertices
        List<double[]> output = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {

            double[] vertex = input.get(i);
            Point2D ap = anchorPoints.get(i);

            // Move original vertices halfway to the anchorPoints
            // double dist = Math.sqrt( (vertex[1]-ap.getX())*(vertex[1]-ap.getX())
            // +(vertex[2]-ap.getY())*(vertex[2]-ap.getY()) );
            double dx = factor * (vertex[1] - ap.getX());
            double dy = factor * (vertex[2] - ap.getY());
            vertex[1] = ap.getX() + dx;
            vertex[2] = ap.getY() + dy;

            // Add the midpoints and new vertices to the output
            output.add(new double[] { vertex[0], ap.getX(), ap.getY() });
            output.add(vertex);

        }

        return output;
    }

    private List<double[]> areaPoints(Area area) {
        // Create list of points that describe the region
        List<double[]> areaPoints = new ArrayList<>();
        double[] coords = new double[6];

        for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
            // "type" will either be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
            int type = pi.currentSegment(coords);

            double[] pathIteratorCoords = { type, coords[0], coords[1] };
            areaPoints.add(pathIteratorCoords);
        }

        return areaPoints;

    }

    private void addExplosion(double x, double y, double radius, int vertices) {

        // int[] x_points = {-2*radius, -radius, radius, 2*radius, 2*radius, radius,
        // -radius, -2*radius};
        // int[] y_points = {radius, 2*radius, 2*radius, radius, -radius, -2*radius,
        // -2*radius, -radius};

        // create a randomized polygon
        Polygon explosion = new Polygon();
        double deltaTheta = 6.283 / vertices;
        double maxRadius = radius * 1.2;
        double minRadius = radius * 0.8;
        for (int i = 0; i < vertices; i++) {
            double angle = i * deltaTheta;
            double length = MyMath.random(minRadius, maxRadius, randomGenerate);
            // TODO: avoid sin and cos
            int px = (int) (Math.cos(angle) * length + x);
            int py = (int) (Math.sin(angle) * length + y);
            explosion.addPoint(px, py);
        }
        Area subtraction = new Area(explosion);

        // Update the regions to accommodate the new explosion
        region.add(subtraction);
        region.subtract(borderRegion);
        inverseRegion.subtract(subtraction);
        inverseRegion.add(borderRegion);

        walls = calculateWalls(areaPoints(region));

    }

    public int getId() {
        return -2;
    }

    public BufferedImage[] getHitAnimation() {
        return HIT_ANIMATION;
    }

    public boolean handleDirectHit(Projectile p) {
        // TEMP until I figure how to test intersection of Area and Line
        if (inverseRegion.contains(p.collideLine.getX1(), p.collideLine.getY1()) ||
                inverseRegion.contains(p.collideLine.getX2(), p.collideLine.getY2())) {

            gsm.audioManager.playSFX(hitSound);
            // gsm.audioManager.playSFX("ric2.wav");
            addExplosion(p.x, p.y, p.explodeRadius, (int) (p.explodeRadius * 1));
            return true;
        }
        return false;
    }

    public boolean handleIndirectHit(Projectile p) {
        addExplosion(p.x, p.y, p.explodeRadius, (int) (p.explodeRadius * 1));
        return false;
    }

    // Class used as a tree generator for new cells
    private class Cell {

        public Cell cellA, cellB;

        public Rect p;

        public Cell(Rect p, int iteration) {

            this.p = p;

            if (iteration + 1 <= iterations) {
                if ((p.w * 0.5 > minRoomSize && p.h * 0.5 < minRoomSize) || p.h < p.w) { // new horizontal box
                    int randomCenter = MyMath.round(
                            MyMath.random(-p.w * splitSizeFactor, p.w * splitSizeFactor, randomGenerate) + p.w * 0.5,
                            2);
                    cellA = new Cell(new Rect(p.x, p.y, randomCenter, p.h), iteration + 1);
                    cellB = new Cell(new Rect(p.x + randomCenter, p.y, p.w - randomCenter, p.h), iteration + 1);
                } else { // new vertical box
                    int randomCenter = MyMath.round(
                            MyMath.random(-p.h * splitSizeFactor, p.h * splitSizeFactor, randomGenerate) + p.h * 0.5,
                            2);
                    cellA = new Cell(new Rect(p.x, p.y, p.w, randomCenter), iteration + 1);
                    cellB = new Cell(new Rect(p.x, p.y + randomCenter, p.w, p.h - randomCenter), iteration + 1);
                }
                if (iteration + 2 == iterations) {
                    cells.add(cellA.cellA.p);
                    cells.add(cellA.cellB.p);
                    cells.add(cellB.cellA.p);
                    cells.add(cellB.cellB.p);
                }
            }

        }
    }

    // A randomly placed corridor between two sister rooms
    // (Can't collide with other corridors/rooms)
    private Rect corridorBetween(List<Rect> others, int leeWay, int path_thickness,
            List<Rect> sister1, List<Rect> sister2) {
        int amount = sister1.size();

        List<Rect> shuffled1 = new ArrayList<>(sister1);
        List<Rect> shuffled2 = new ArrayList<>(sister2);
        Collections.shuffle(shuffled1, randomGenerate);
        Collections.shuffle(shuffled2, randomGenerate);

        Rect corridor;

        for (int i = 0; i < amount; i++) {
            for (int j = 0; j < amount; j++) {

                Rect r1 = shuffled1.get(i);
                Rect r2 = shuffled2.get(j);

                if (Math.abs((r1.y + r1.h * 0.5) - (r2.y + r2.h * 0.5)) <= r1.h * 0.5 + r2.h * 0.5 - path_thickness) { // Horizontally
                                                                                                                       // adjacent
                                                                                                                       // rooms
                    int min = Math.max(r1.y, r2.y);
                    int max = Math.min(r1.y + r1.h, r2.y + r2.h) - path_thickness;
                    List<Integer> options = new ArrayList<>();
                    for (int k = 0; k < max - min; k++) {
                        options.add(min + k);
                    }
                    Collections.shuffle(options, randomGenerate);
                    for (Integer option : options) {
                        corridor = new Rect(r1.x + r1.w, option, r2.x - (r1.x + r1.w), path_thickness);
                        boolean collides = false;
                        for (Rect o : others) {
                            if (o.x + o.w > corridor.x && o.x < corridor.x + corridor.w &&
                                    o.y + o.h > corridor.y - leeWay && o.y < corridor.y + corridor.h + leeWay) {
                                collides = true;
                            }
                        }
                        if (!collides) {
                            openSpaces.add(new Rectangle2D.Double(corridor.x - path_thickness, corridor.y,
                                    path_thickness, path_thickness));
                            openSpaces.add(new Rectangle2D.Double(corridor.x + corridor.w, corridor.y, path_thickness,
                                    path_thickness));
                            return corridor;
                        }
                    }
                } else if (Math.abs((r1.x + r1.w * 0.5) - (r2.x + r2.w * 0.5)) <= r1.w * 0.5 + r2.w * 0.5
                        - path_thickness) { // Vertically adjacent rooms
                    double min = Math.max(r1.x, r2.x);
                    double max = Math.min(r1.x + r1.w, r2.x + r2.w) - path_thickness;
                    List<Integer> options = new ArrayList<>();
                    for (int k = 0; k < max - min; k++) {
                        options.add(MyMath.round(min + k));
                    }
                    Collections.shuffle(options, randomGenerate);
                    for (Integer option : options) {
                        corridor = new Rect(option, r1.y + r1.h, path_thickness, r2.y - (r1.y + r1.h));
                        boolean collides = false;
                        for (Rect o : others) {
                            if (o.x + o.w > corridor.x - leeWay && o.x < corridor.x + corridor.w + leeWay &&
                                    o.y + o.h > corridor.y && o.y < corridor.y + corridor.h) {
                                collides = true;
                            }
                        }
                        if (!collides) {
                            openSpaces.add(new Rectangle2D.Double(corridor.x, corridor.y - path_thickness,
                                    path_thickness, path_thickness));
                            openSpaces.add(new Rectangle2D.Double(corridor.x, corridor.y + corridor.h, path_thickness,
                                    path_thickness));
                            return corridor;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void draw(Graphics2D g) {

        g.setStroke(STROKE);
        g.setColor(STROKE_COLOR);
        g.draw(inverseRegion);

        g.setColor(fillColor);
        g.fill(inverseRegion);

    }

}
