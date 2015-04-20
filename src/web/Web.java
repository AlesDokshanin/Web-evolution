package web;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Web implements Comparable<Web> {
    private static final int MIN_SIDES = 10;
    private static final int MAX_SIDES = 20;

    private static final int CHILDREN_COUNT = 3;

    protected static int width = 600;
    protected static int height = 600;

    static int webSidesCount = 15;

    private static final Point center = new Point(width / 2, height / 2);
    private static final int minSkeletonDistanceFromCenter = (int) (Math.min(height, width) / 3.5);
    static final int minTrappingNetCircleDistance = Math.min(height, width) / 50;
    private static final int flySize = Math.min(width, height) / 50;
    private static final int fliesCount = 1000;
    static final double minAngleBetweenSkeletonLines = 2 * Math.PI / (2 * webSidesCount);
    private static final double trappingNetCirclesDispersion = 7.0;
    static final Random random = new Random();

    int generation = 1;
    double efficiency = 0;

    public int getGeneration() {
        return generation;
    }

    @Override
    public int compareTo(Web web) {
        return Double.compare(this.efficiency, web.efficiency);
    }

    WebSkeleton skeleton;
    ArrayList<TrappingNetCircle> trappingNet;
    private  ArrayList<Fly> flies;
    private ArrayList<Fly> caughtFlies;

    public static boolean drawFlies = false;

    public static void setSidesCount(int count) throws IllegalArgumentException {
        if (count < MIN_SIDES || count > MAX_SIDES)
            throw new IllegalArgumentException("Web sides count should be in range ["
                    + MIN_SIDES + ", " + MAX_SIDES + "]");
        webSidesCount = count;
    }

    public static int getSidesCount() {
        return webSidesCount;
    }

    public Web() {
        generateFlies();
        build();
    }

    public Web(Web w) {
        // Primitives
        this.generation = w.generation;
        this.efficiency = w.efficiency;

        // Deep copying
        this.skeleton = new WebSkeleton(w.skeleton);

        this.trappingNet = new ArrayList<TrappingNetCircle>(w.trappingNet.size());
        for (TrappingNetCircle c : w.trappingNet)
            this.trappingNet.add(new TrappingNetCircle(c));


        // We just want the same flies ArrayList we won't modify, so just copy the link
        this.flies = w.flies;
        this.flies = new ArrayList<Fly>(fliesCount);
        for(Fly f : w.flies)
            this.flies.add(new Fly(f));

        // We just want to copy links to the caught flies to the new ArrayList, no deep copy needed
//        caughtFlies = new ArrayList<Fly>(w.caughtFlies.size());
//        for (Fly f : w.caughtFlies)
//            caughtFlies.add(f);

        caughtFlies = null;

    }

    private void generateFlies() {
        flies = new ArrayList<Fly>(fliesCount);
        for (int i = 0; i < fliesCount; i++) {
            flies.add(new Fly());
        }
    }

    public double getEfficiency() {
        return efficiency;
    }

    private void build() {
        skeleton = new WebSkeleton();
        trappingNet = new ArrayList<TrappingNetCircle>();
        skeleton.generate();
        generateTrappingNet();

        calculateEfficiency();
    }

    ArrayList<Web> reproduce() {
        ArrayList<Web> children = new ArrayList<Web>(CHILDREN_COUNT);
        for (int i = 0; i < CHILDREN_COUNT; i++) {
            Web child = new Web(this);
            child.mutate();
            children.add(child);
        }

        return children;
    }

    void calculateEfficiency() {
        caughtFlies = new ArrayList<Fly>();
        int caught = 0;
        for (Fly fly : flies) {
            if (fly.gotCaught()) {
                caught++;
                caughtFlies.add(fly);
            }
        }
        efficiency = (double) caught / fliesCount;
    }


    private void generateTrappingNet() {
        trappingNet.clear();

        while (true) {
            TrappingNetCircle circle = new TrappingNetCircle();
            if (circle.fitsToWeb())
                trappingNet.add(circle);
            else break;
        }
    }

    void updateTrappingNet() {
        for (int i = 0; i < webSidesCount; i++) {
            double newAngle = skeleton.points.get(i).angle;
            for (TrappingNetCircle circle : trappingNet) {
                circle.points.get(i).angle = newAngle;
            }
        }

        for (TrappingNetCircle c : trappingNet)
            c.generatePolygon();
    }

    protected void draw(Graphics2D g) {
        skeleton.draw(g);
        drawTrappingNet(g);
        if (drawFlies)
            drawCaughtFlies(g);
    }

    private void drawCaughtFlies(Graphics2D g) {
        for (Fly f : caughtFlies)
            f.draw(g);
    }

    protected void drawCenterPoint(Graphics2D g) {
        g.fillOval(center.x - 2, center.y - 2, 4, 4);
    }

    private Polygon getPolygonFromPolarPointsList(ArrayList<PolarPoint> list) {
        int[] xPoints = new int[list.size()];
        int[] yPoints = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            Point p = list.get(i).getCartesianPoint();
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }

        return new Polygon(xPoints, yPoints, list.size());
    }

    private void drawTrappingNet(Graphics2D g) {
        Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();
        g.setColor(new Color(255, 0, 0));
        g.setStroke(new BasicStroke(2));
        for (TrappingNetCircle circle : trappingNet)
            g.drawPolygon(circle.getPolygon());
        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    protected Polygon getPolygonFromPointsArray(ArrayList<Point> list) {
        int[] xPoints = new int[list.size()];
        int[] yPoints = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            xPoints[i] = list.get(i).x;
            yPoints[i] = list.get(i).y;
        }

        return new Polygon(xPoints, yPoints, xPoints.length);
    }

    protected void mutate() {
        WebMutationType mutationType = WebMutationType.values()[random.nextInt(WebMutationType.values().length)];
        WebMutation mutation;

        switch (mutationType) {
            case SKELETON_ANGLE:
                mutation = new SkeletonAngleMutation(this);
                break;
            case SKELETON_DISTANCE:
                mutation = new SkeletonDistanceMutation(this);
                break;
            case TRAPPING_NET:
                mutation = new TrappingNetMutation(this);
                break;

            default:
                throw new RuntimeException("Unexpected mutation type: " + String.valueOf(mutationType));
        }

        mutation.mutate();
    }

    public class TrappingNetCircle {
        final ArrayList<PolarPoint> points;
        Polygon polygon;

        public boolean fitsToWeb() {
            return fits;
        }

        boolean fits = false;

        public TrappingNetCircle() {
            points = new ArrayList<PolarPoint>();
            generateTrappingNetCircle();
        }

        public TrappingNetCircle(TrappingNetCircle c) {
            this.points = new ArrayList<PolarPoint>(c.points.size());
            for (PolarPoint p : c.points)
                this.points.add(new PolarPoint(p));
            this.generatePolygon();
        }

        public Polygon getPolygon() {
            return polygon;
        }

        void generatePolygon() {

            polygon = getPolygonFromPolarPointsList(points);
            polygon.translate(center.x, center.y);
        }

        void generateTrappingNetCircle() {
            for (int i = 0; i < webSidesCount; i++) {
                int lowerBound = minTrappingNetCircleDistance;
                if (!trappingNet.isEmpty()) {
                    lowerBound += trappingNet.get(trappingNet.size() - 1).points.get(i).distance;
                }
                int maxDistance = skeleton.points.get(i).distance - minTrappingNetCircleDistance;

                if (lowerBound > maxDistance) {
                    fits = false;
                    return;
                }

                int upperBound = Math.min(maxDistance, lowerBound +
                        (int) (trappingNetCirclesDispersion * minTrappingNetCircleDistance));
                double angle = skeleton.points.get(i).angle;

                int distance = (int) (lowerBound + random.nextDouble() * (upperBound - lowerBound));
                points.add(new PolarPoint(angle, distance));
            }
            generatePolygon();
            fits = true;
        }
    }

    class Fly {
        final Rectangle2D rect;

        public Fly() {
            int x = random.nextInt() % (width / 2 - flySize);
            int y = random.nextInt() % (height / 2 - flySize);
            rect = new Rectangle2D.Float(x, y, flySize, flySize);
        }

        public Fly(Fly f) {
            this.rect = new Rectangle2D.Float((float) f.rect.getX(), (float) f.rect.getY(), flySize, flySize);
        }

        public boolean gotCaught() {
            for (TrappingNetCircle innerCircle : trappingNet) {
                for (int i = 0; i < innerCircle.points.size(); i++) {
                    Point a = innerCircle.points.get(i).getCartesianPoint();
                    Point b = innerCircle.points.get((i + 1) % innerCircle.points.size()).getCartesianPoint();
                    Line2D line2D = new Line2D.Float(a, b);
                    if (intersectsWithLine(line2D))
                        return true;
                }
            }
            return false;
        }

        boolean intersectsWithLine(Line2D line) {
            return rect.intersectsLine(line);
        }


        public void draw(Graphics2D g) {
            g.setColor(new Color(94, 104, 205, 128));
            g.setBackground(new Color(0, 0, 0));
            g.fillRect((int) rect.getX() + center.x, (int) rect.getY() + center.y, flySize, flySize);
            g.drawRect((int) rect.getX() + center.x, (int) rect.getY() + center.y, flySize, flySize);
        }
    }

    public class WebSkeleton {
        Polygon polygon;
        final ArrayList<PolarPoint> points;

        public WebSkeleton() {
            points = new ArrayList<PolarPoint>();
        }

        public WebSkeleton(WebSkeleton s) {
            this.points = new ArrayList<PolarPoint>(s.points.size());
            for (PolarPoint p : s.points)
                this.points.add(new PolarPoint(p));
            generateSkeletonPolygon();
        }

        void generate() {
            do {
                generateSkeletonPoints();

                Collections.sort(points);

                generateSkeletonPolygon();
            }
            while (isNotValid());
        }

        void generateSkeletonPoints() {
            points.clear();

            for (int i = 0; i < webSidesCount; i++) {
                PolarPoint p = generateSkeletonPoint();
                while (!skeletonPointIsValid(p, points))
                    p = generateSkeletonPoint();
                points.add(p);
            }
        }

        PolarPoint generateSkeletonPoint() {
            double angle = random.nextDouble() * 2 * Math.PI;
            int maxDistance = 0;
            Point bound;

            while (maxDistance <= minSkeletonDistanceFromCenter) {
                // Calculating the maximum distance from center to panel corner in direction of given angle
                bound = new Point((int) (0.5 * width * Math.cos(angle)), (int) (0.5 * height * Math.sin(angle)));
                maxDistance = (int) bound.distance(0, 0);
            }

            int distance = (int) (minSkeletonDistanceFromCenter +
                    (maxDistance - minSkeletonDistanceFromCenter) * random.nextDouble());
            return new PolarPoint(angle, distance);
        }

        boolean skeletonPointIsValid(PolarPoint p, ArrayList<PolarPoint> otherPoints) {
            // Validating the angle between other points' vectors
            for (PolarPoint listPoint : otherPoints) {
                if (Math.abs(p.angle - listPoint.angle) < minAngleBetweenSkeletonLines)
                    return false;
            }
            return true;
        }

        boolean isNotValid() {
            return !centerFitsGoodIntoPolygon();
        }

        boolean centerFitsGoodIntoPolygon() {
            int shift = Math.min(width, height) / 10;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Point p = new Point(center.x + dx * shift, center.y + dy * shift);
                    if (!polygon.contains(p)) {
                        return false;
                    }
                }
            }
            return true;
        }

        void draw(Graphics2D g) {
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(128, 128, 128));
            g.drawPolygon(polygon);
            int[] xPoints = polygon.xpoints;
            int[] yPoints = polygon.ypoints;
            for (int i = 0; i < xPoints.length; i++) {
//                g.drawString(String.valueOf(i), xPoints[i], yPoints[i]);
                g.drawLine(xPoints[i], yPoints[i], center.x, center.y);
            }
        }

        void generateSkeletonPolygon() {
            polygon = getPolygonFromPolarPointsList(points);
            polygon.translate(center.x, center.y);

        }
    }
}


class PolarPoint implements Comparable<PolarPoint> {
    double angle;
    int distance;

    public PolarPoint(double angle, int distance) {
        this.angle = angle;
        this.distance = distance;
    }

    public PolarPoint(Point p) {
        this.distance = (int) Math.sqrt((p.x * p.x + p.y * p.y));
        this.angle = Math.atan(p.y / p.x);
    }

    public PolarPoint(PolarPoint p) {
        this.angle = p.angle;
        this.distance = p.distance;
    }

    public Point getCartesianPoint() {
        return new Point((int) (Math.cos(angle) * distance), (int) (Math.sin(angle) * distance));
    }

    @Override
    public int compareTo(PolarPoint polarPoint) {
        return Double.compare(angle, polarPoint.angle);
    }
}

enum WebMutationType {
    SKELETON_ANGLE, SKELETON_DISTANCE, TRAPPING_NET
}

abstract class WebMutation {

    final Web web;

    WebMutation(Web web) {
        this.web = web;
    }

    void mutate() {
        tryToAddTrappingNetCircle();

        web.generation++;
        web.calculateEfficiency();
    }

    private void tryToAddTrappingNetCircle() {
        Web.TrappingNetCircle netCircle = web.new TrappingNetCircle();
        if (netCircle.fitsToWeb())
            web.trappingNet.add(netCircle);
    }
}

class SkeletonAngleMutation extends WebMutation {

    SkeletonAngleMutation(Web web) {
        super(web);
    }

    @Override
    protected void mutate() {
        do {
            int index = getVectorIndex();

            double lowerBound, upperBound;
            lowerBound = (index == 0) ? web.skeleton.points.get(web.skeleton.points.size() - 1).angle : web.skeleton.points.get(index - 1).angle;
            upperBound = (index == web.skeleton.points.size() - 1) ? web.skeleton.points.get(0).angle : web.skeleton.points.get(index + 1).angle;

            lowerBound += Web.minAngleBetweenSkeletonLines;
            upperBound -= Web.minAngleBetweenSkeletonLines;

            if (lowerBound > upperBound)
                upperBound += 2 * Math.PI;

            web.skeleton.points.get(index).angle = ((lowerBound + Web.random.nextDouble() * ((upperBound - lowerBound))) % (2 * Math.PI));
        } while (web.skeleton.isNotValid());

        web.skeleton.generateSkeletonPolygon();
        web.updateTrappingNet();

        super.mutate();
    }

    private int getVectorIndex() {
        return Web.random.nextInt(Web.webSidesCount);
    }
}

class SkeletonDistanceMutation extends WebMutation {

    SkeletonDistanceMutation(Web web) {
        super(web);
    }

    @Override
    protected void mutate() {
        do {
            int index = getVectorIndex();

            double angle = web.skeleton.points.get(index).angle;
            Point bound = new Point((int) (0.5 * Web.width * Math.cos(angle)), (int) (0.5 * Web.height * Math.sin(angle)));
            double maxDistance = (int) bound.distance(0, 0);

            int lowerBound = web.trappingNet.get(web.trappingNet.size() - 1).points.get(index).distance + Web.minTrappingNetCircleDistance;
            web.skeleton.points.get(index).distance = (int) (lowerBound + Web.random.nextDouble() * (maxDistance - lowerBound));
        } while (web.skeleton.isNotValid());

        web.skeleton.generateSkeletonPolygon();

        super.mutate();
    }

    private int getVectorIndex() {
        return Web.random.nextInt(Web.webSidesCount);
    }
}

class TrappingNetMutation extends WebMutation {
    TrappingNetMutation(Web web) {
        super(web);
    }

    @Override
    protected void mutate() {
        // The index of trapping net circle
        int index = Web.random.nextInt(web.trappingNet.size());

        for (int i = 0; i < Web.webSidesCount; i++) {
            // 'i' is the index of vector
            int lowerBound, upperBound;

            if (index == 0)
                lowerBound = Web.minTrappingNetCircleDistance;
            else
                lowerBound = web.trappingNet.get(index - 1).points.get(i).distance + Web.minTrappingNetCircleDistance;

            if (index == web.trappingNet.size() - 1)
                upperBound = web.skeleton.points.get(i).distance - Web.minTrappingNetCircleDistance;
            else
                upperBound = web.trappingNet.get(index + 1).points.get(i).distance - Web.minTrappingNetCircleDistance;

            web.trappingNet.get(index).points.get(i).distance = lowerBound + (int) (Web.random.nextDouble() * (upperBound - lowerBound));
        }

        web.trappingNet.get(index).generatePolygon();

        super.mutate();
    }
}