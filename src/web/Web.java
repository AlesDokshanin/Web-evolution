package web;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Web {
    private static final int MIN_SIDES = 10;
    private static final int MAX_SIDES = 20;

    protected static int width = 600;
    protected static int height = 600;

    static int webSidesCount = 15;

    private static final Point center = new Point(width / 2, height / 2);
    protected static final int minSkeletonDistanceFromCenter = (int) (Math.min(height, width) / 3.5);
    protected static final int minTrappingNetCircleDistance = Math.min(height, width) / 75;
    protected static final int flySize = Math.min(width, height) / 50;
    protected static final int fliesCount = 1000;
    protected static final double minAngleBetweenSkeletonLines = 2 * Math.PI / (2 * webSidesCount);
    protected static final double trappingNetCirclesDispersion = 7.0;
    protected static final Random random = new Random();

    protected int generation = 0;
    private double efficiency = 0;

    protected WebSkeleton skeleton;
    protected ArrayList<TrappingNetCircle> trappingNet;
    protected ArrayList<Fly> flies;
    protected ArrayList<Fly> caughtFlies;

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

    protected void generateFlies() {
        flies = new ArrayList<Fly>(fliesCount);
        for (int i = 0; i < fliesCount; i++) {
            flies.add(new Fly());
        }
    }

    public double getEfficiency() {
        return efficiency;
    }

    void build(){
        skeleton = new WebSkeleton();
        trappingNet = new ArrayList<TrappingNetCircle>();
        skeleton.generate();
        generateTrappingNet();

        calculateEfficiency();
    }

    protected void calculateEfficiency() {
        caughtFlies = new ArrayList<Fly>();
        int caught = 0;
        for (Fly fly: flies) {
            if (fly.gotCaught()) {
                caught++;
                caughtFlies.add(fly);
            }
        }
        efficiency = (double) caught / fliesCount;
    }


    protected void generateTrappingNet() {
        trappingNet.clear();

        while (true) {
            TrappingNetCircle circle = new TrappingNetCircle();
            if (circle.fitsToWeb())
                trappingNet.add(circle);
            else break;
        }
    }

    protected void updateTrappingNet() {
        for (int i = 0; i < webSidesCount; i++) {
            double newAngle = skeleton.points.get(i).angle;
            for (TrappingNetCircle circle : trappingNet) {
                circle.points.get(i).angle = newAngle;
            }
        }

        for(TrappingNetCircle c: trappingNet)
            c.generatePolygon();
    }

    protected void draw(Graphics2D g) {
        skeleton.draw(g);
        drawTrappingNet(g);
        if (drawFlies)
            drawCaughtFlies(g);
    }

    protected void drawCaughtFlies(Graphics2D g) {
        for (Fly f : caughtFlies)
            f.draw(g);
    }

    protected void drawCenterPoint(Graphics2D g) {
        g.fillOval(center.x - 2, center.y - 2, 4, 4);
    }

    protected Polygon getPolygonFromPolarPointsList(ArrayList<PolarPoint> list) {
        int[] xPoints = new int[list.size()];
        int[] yPoints = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            Point p = list.get(i).getCartesianPoint();
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }

        return new Polygon(xPoints, yPoints, list.size());
    }

    protected void drawTrappingNet(Graphics2D g) {
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
//        WebMutationType mutationType = WebMutationType.SKELETON_ANGLE;

        WebMutation mutation;

        switch (mutationType) {
            case SKELETON_ANGLE:
                mutation = new SkeletonAngleMutation(this);
                break;
            case SKELETON_DISTANCE:
                mutation = new SkeletonDistanceMutation(this);
                break;
            default:
                throw new RuntimeException("Unexpected mutation type");
        }

        System.out.println("Applying " + mutation.title);
        mutation.mutate();
    }

    protected class TrappingNetCircle {
        protected final ArrayList<PolarPoint> points;
        protected Polygon polygon;

        public boolean fitsToWeb() {
            return fits;
        }

        protected boolean fits = false;

        public TrappingNetCircle() {
            points = new ArrayList<PolarPoint>();
            generateTrappingNetCircle();
        }

        public Polygon getPolygon() {
            return polygon;
        }

        protected void generatePolygon() {
            int n = points.size();
            int[] xPoints = new int[n];
            int[] yPoints = new int[n];
            for (int i = 0; i < n; i++) {
                Point p = points.get(i).getCartesianPoint();
                xPoints[i] = p.x;
                yPoints[i] = p.y;
            }
            polygon = new Polygon(xPoints, yPoints, n);
            polygon.translate(center.x, center.y);
        }

        protected void generateTrappingNetCircle() {
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

    protected class Fly {
        protected final Rectangle2D rect;

        public Fly() {
            int x = random.nextInt() % (width / 2 - flySize);
            int y = random.nextInt() % (height / 2 - flySize);
            rect = new Rectangle2D.Float(x, y, flySize, flySize);
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

        protected boolean intersectsWithLine(Line2D line) {
            return rect.intersectsLine(line);
        }


        public void draw(Graphics2D g) {
            g.setColor(new Color(94, 104, 205, 128));
            g.setBackground(new Color(0, 0, 0));
            g.fillRect((int) rect.getX() + center.x, (int) rect.getY() + center.y, flySize, flySize);
            g.drawRect((int) rect.getX() + center.x, (int) rect.getY() + center.y, flySize, flySize);
        }
    }

    protected class WebSkeleton {
        protected Polygon polygon;
        protected final ArrayList<PolarPoint> points;

        public WebSkeleton() {
            points = new ArrayList<PolarPoint>();
        }

        protected void generate() {
            do {
                generateSkeletonPoints();

                Collections.sort(points);

                generateSkeletonPolygon();
            }
            while(!isValid());
        }

        protected void generateSkeletonPoints() {
            points.clear();

            for (int i = 0; i < webSidesCount; i++) {
                PolarPoint p = generateSkeletonPoint();
                while (!skeletonPointIsValid(p, points))
                    p = generateSkeletonPoint();
                points.add(p);
            }
        }

        protected PolarPoint generateSkeletonPoint() {
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

        protected boolean skeletonPointIsValid(PolarPoint p, ArrayList<PolarPoint> otherPoints) {
            // Validating the angle between other points' vectors
            for (PolarPoint listPoint : otherPoints) {
                if (Math.abs(p.angle - listPoint.angle) < minAngleBetweenSkeletonLines)
                    return false;
            }
            return true;
        }

        protected boolean isValid() {
            return centerFitsGoodIntoPolygon();
        }

        protected boolean centerFitsGoodIntoPolygon() {
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

        protected void draw(Graphics2D g) {
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(128, 128, 128));
            g.drawPolygon(polygon);
            int[] xPoints = polygon.xpoints;
            int[] yPoints = polygon.ypoints;
            for (int i = 0; i < xPoints.length; i++) {
                //g.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                g.drawLine(xPoints[i], yPoints[i], center.x, center.y);
                g.drawString(String.valueOf(i), xPoints[i], yPoints[i]);
            }
        }

        protected void generateSkeletonPolygon() {
            skeleton.polygon = getPolygonFromPolarPointsList(skeleton.points);
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

    public Point getCartesianPoint() {
        return new Point((int) (Math.cos(angle) * distance), (int) (Math.sin(angle) * distance));
    }

    @Override
    public int compareTo(PolarPoint polarPoint) {
        return Double.compare(angle, polarPoint.angle);
    }
}

enum WebMutationType {
    SKELETON_ANGLE, SKELETON_DISTANCE
}

abstract class WebMutation {

    protected final Web web;
    protected String title = "WebMutation";

    WebMutation(Web web) {
        this.web = web;
    }

    protected void mutate() {
        web.generation++;
        web.calculateEfficiency();
    }
}

class SkeletonAngleMutation extends WebMutation {

    SkeletonAngleMutation(Web web) {
        super(web);
        this.title = "SkeletonAngleMutation";
    }

    @Override
    protected void mutate() {
        do {
            int index = getRandomVectorIndex();

            double lowerBound, upperBound;
            lowerBound = (index == 0) ? web.skeleton.points.get(web.skeleton.points.size() - 1).angle : web.skeleton.points.get(index - 1).angle;
            upperBound = (index == web.skeleton.points.size() - 1) ? web.skeleton.points.get(0).angle : web.skeleton.points.get(index + 1).angle;

            lowerBound += Web.minAngleBetweenSkeletonLines;
            upperBound -= Web.minAngleBetweenSkeletonLines;

            if(lowerBound > upperBound)
                upperBound += 2*Math.PI;

            web.skeleton.points.get(index).angle = ((lowerBound + Web.random.nextDouble() * ((upperBound-lowerBound))) % (2*Math.PI));
        } while (!web.skeleton.isValid());

        web.skeleton.generateSkeletonPolygon();
        web.updateTrappingNet();

        super.mutate();
    }

    private int getRandomVectorIndex() {
        return Web.random.nextInt(Web.webSidesCount);
    }
}

class SkeletonDistanceMutation extends WebMutation {

    SkeletonDistanceMutation(Web web) {
        super(web);
        this.title = "SkeletonDistanceMutation";
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
        } while (!web.skeleton.isValid());

        web.skeleton.generateSkeletonPolygon();

        super.mutate();
    }

    private int  getVectorIndex() {
        return Web.random.nextInt(Web.webSidesCount);
    }
}