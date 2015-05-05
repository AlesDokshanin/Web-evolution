package web;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

enum WebMutationType {
    SKELETON_ANGLE, SKELETON_DISTANCE, TRAPPING_NET
}

public class Web implements Comparable<Web> {
    static final Random random = new Random();
    private static final int MIN_SIDES = 10;
    private static final int MAX_SIDES = 20;
    private static final int CHILDREN_COUNT = 3;
    private static final Color CAUGHT_FLY_COLOR = new Color(94, 104, 205, 128);
    private static final Color UNCAUGHT_FLY_COLOR = new Color(37, 205, 7, 128);
    private static final int MAX_TRAPPING_NET_LENGTH_UPPER = 200 * 1000;
    private static final int MAX_TRAPPING_NET_LENGTH_LOWER = 10 * 1000;
    private static final double trappingNetCirclesDispersion = 7.0;
    public static int MIN_FLIES_COUNT = 10;
    public static int MAX_FLIES_COUNT = 1000;
    public static boolean drawFlies = false;
    public static boolean normalFliesDistribution = true;
    private static float percentOfNormalDistributedFlies = 0.25f;
    static int width = 800;
    static int height = 800;
    private static final Point center = new Point(width / 2, height / 2);
    private static final int minSkeletonDistanceFromCenter = (int) (Math.min(height, width) / 3.5);
    static final int minTrappingNetCircleDistance = Math.min(height, width) / 50;
    private static final int flySize = Math.min(width, height) / 50;
    static int maxTrappingNetLength = 100 * 1000;
    static int webSidesCount = 15;
    static final double minAngleBetweenSkeletonLines = 2 * Math.PI / (2 * webSidesCount);
    static int fliesCount = 100;
    int trappingNetLength;
    int generation = 1;
    double efficiency = 0;
    WebSkeleton skeleton;
    ArrayList<TrappingNetCircle> trappingNet;
    private  ArrayList<Fly> flies;
    public Web() {
        generateFlies();
        build();
        calculateTrappingNetLength();
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

        generateFlies();

        trappingNetLength = w.trappingNetLength;

    }

    public static int getSidesCount() {
        return webSidesCount;
    }

    public static void setSidesCount(int count) throws IllegalArgumentException {
        if (count < MIN_SIDES || count > MAX_SIDES)
            throw new IllegalArgumentException("Web sides count should be in range ["
                    + MIN_SIDES + ", " + MAX_SIDES + "]");
        webSidesCount = count;
    }

    public static int getMaxTrappingNetLength() {
        return maxTrappingNetLength;
    }

    public static void setMaxTrappingNetLength(int length) {
        if(length < MAX_TRAPPING_NET_LENGTH_LOWER || length > MAX_TRAPPING_NET_LENGTH_UPPER) {
            throw new IllegalArgumentException("Max trapping length should be in range ["
                    + MAX_TRAPPING_NET_LENGTH_LOWER + ", " + MAX_TRAPPING_NET_LENGTH_UPPER + "]");
        }
        maxTrappingNetLength = length;
    }

    public static int getFliesCount() {
        return fliesCount;
    }

    public static void setFliesCount(int count) throws IllegalArgumentException {
        if(count < MIN_FLIES_COUNT || count > MAX_FLIES_COUNT ){
           throw new IllegalArgumentException("Flies count should be in range ["
                   + MIN_FLIES_COUNT + ", " + MAX_FLIES_COUNT + "]");
        }
        fliesCount = count;
    }

    public int getGeneration() {
        return generation;
    }

    @Override
    public int compareTo(Web web) {
        return Double.compare(this.efficiency, web.efficiency);
    }

    private void calculateTrappingNetLength() {
        trappingNetLength = 0;

        for(TrappingNetCircle circle : trappingNet)
            trappingNetLength += circle.length;
    }

    private void generateFlies() {
        flies = new ArrayList<Fly>(fliesCount);
        for (int i = 0; i < fliesCount; i++) {
            if(normalFliesDistribution || i <= fliesCount * percentOfNormalDistributedFlies)
                flies.add(new Fly(true));
            else flies.add(new Fly(false));
        }

    }

    public double getEfficiency() {
        return efficiency;
    }

    int getTrappingNetLength() {
        return trappingNetLength;
    }

    private void build() {
        skeleton = new WebSkeleton();
        trappingNet = new ArrayList<TrappingNetCircle>();
        skeleton.generate();
        generateTrappingNet();
        calculateTrappingNetLength();

        calculateEfficiency();
    }

    ArrayList<Web> reproduce() {
        ArrayList<Web> children = new ArrayList<Web>(CHILDREN_COUNT);
        for (int i = 0; i < CHILDREN_COUNT - 1; i++) {
            Web child = new Web(this);
            child.mutate();
            children.add(child);
        }
        generation++;
        generateFlies();
        calculateEfficiency();
        children.add(this);

        return children;
    }

    void calculateEfficiency() {
        int caught = 0;
        for (Fly fly : flies) {
            if (fly.checkIfCaught()) {
                caught++;
            }
        }
        efficiency = (double) caught * 10000 / trappingNetLength;
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
            c.save();

        calculateTrappingNetLength();
    }

    protected void draw(Graphics2D g) {
        skeleton.draw(g);
        drawTrappingNet(g);
        if (drawFlies) {
            drawFlies(g);
        }
    }

    private void drawFlies(Graphics2D g) {
        for (Fly f: flies)
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
        int length;
        boolean fits = false;

        public TrappingNetCircle() {
            points = new ArrayList<PolarPoint>();
            generateTrappingNetCircle();
        }

        public TrappingNetCircle(TrappingNetCircle c) {
            this.points = new ArrayList<PolarPoint>(c.points.size());
            for (PolarPoint p : c.points)
                this.points.add(new PolarPoint(p));
            this.save();
        }

        public boolean fitsToWeb() {
            return fits;
        }

        public Polygon getPolygon() {
            return polygon;
        }

        void calculateLength() {
            this.length = 0;

            for (int i = 0; i < polygon.xpoints.length; i++) {
                length += Math.sqrt(polygon.xpoints[i] * polygon.xpoints[i] + polygon.ypoints[i] * polygon.ypoints[i]);
            }
        }

        void save() {
            polygon = getPolygonFromPolarPointsList(points);
            polygon.translate(center.x, center.y);
            calculateLength();
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
            save();
            fits = true;
        }
    }

    class Fly {
        final Rectangle2D rect;
        boolean caught;

        public Fly(boolean normalDistribution) {
            int x, y;
            if (normalDistribution) {
                x = random.nextInt() % (width / 2 - flySize);
                y = random.nextInt() % (height / 2 - flySize);
            }
            else {
                x = random.nextInt() % (width / 4 - flySize) + width / 4;
                y = random.nextInt() % (height / 4 - flySize) + height / 4;
            }
            rect = new Rectangle2D.Float(x, y, flySize, flySize);
        }

        public Fly(Fly f) {
            this.rect = new Rectangle2D.Float((float) f.rect.getX(), (float) f.rect.getY(), flySize, flySize);
        }

        public boolean checkIfCaught() {
            for (TrappingNetCircle innerCircle : trappingNet) {
                for (int i = 0; i < innerCircle.points.size(); i++) {
                    Point a = innerCircle.points.get(i).getCartesianPoint();
                    Point b = innerCircle.points.get((i + 1) % innerCircle.points.size()).getCartesianPoint();
                    Line2D line2D = new Line2D.Float(a, b);
                    if (intersectsWithLine(line2D)) {
                        caught = true;
                        return true;
                    }
                }
            }
            caught = false;
            return false;
        }

        boolean intersectsWithLine(Line2D line) {
            return rect.intersectsLine(line);
        }


        public void draw(Graphics2D g) {
            if(caught)
                g.setColor(CAUGHT_FLY_COLOR);
            else g.setColor(UNCAUGHT_FLY_COLOR);

            g.setBackground(new Color(0, 0, 0));
            g.fillRect((int) rect.getX() + center.x, (int) rect.getY() + center.y, flySize, flySize);
            g.drawRect((int) rect.getX() + center.x, (int) rect.getY() + center.y, flySize, flySize);
        }
    }

    public class WebSkeleton {
        final ArrayList<PolarPoint> points;
        Polygon polygon;

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
            int shift = minTrappingNetCircleDistance;
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
        if(web.trappingNetLength > Web.maxTrappingNetLength)
            return;

        Web.TrappingNetCircle netCircle = web.new TrappingNetCircle();
        if (netCircle.fitsToWeb()) {
            netCircle.save();
            web.trappingNet.add(netCircle);
            web.trappingNetLength += netCircle.length;
        }
    }
}

class SkeletonAngleMutation extends WebMutation {

    SkeletonAngleMutation(Web web) {
        super(web);
    }

    @Override
    protected void mutate() {
        int index;
        do {
            index = getVectorIndex();

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

    private static final int TRAPPING_NET_DISPERSION = 1;

    TrappingNetMutation(Web web) {
        super(web);
    }

    protected void deleteRandomCircle(){
        int index = Web.random.nextInt(web.trappingNet.size());
        int deletedCircleLength = web.trappingNet.get(index).length;
        web.trappingNet.remove(index);
        web.trappingNetLength -= deletedCircleLength;
    }

    @Override
    protected void mutate() {
        // The index of trapping net circle
        int index = Web.random.nextInt(web.trappingNet.size());
        int oldCircleLength = web.trappingNet.get(index).length;

        for (int i = 0; i < Web.webSidesCount; i++) {
            // 'i' is the index of vector
            // lowerBound and upperBound are absolute limits for current step
            int lowerBound, upperBound;

            if (index == 0)
                lowerBound = Web.minTrappingNetCircleDistance;
            else
                lowerBound = web.trappingNet.get(index - 1).points.get(i).distance + Web.minTrappingNetCircleDistance;

            if (index == web.trappingNet.size() - 1)
                upperBound = web.skeleton.points.get(i).distance - Web.minTrappingNetCircleDistance;
            else
                upperBound = web.trappingNet.get(index + 1).points.get(i).distance - Web.minTrappingNetCircleDistance;

            // lowerDistance and upperDistance both are the most distant values, that are reachable from current
            // position with given TRAPPING_NET_DISPERSION
            int currentDistance = web.trappingNet.get(index).points.get(i).distance;
            int lowerDistance = currentDistance - TRAPPING_NET_DISPERSION * Web.minTrappingNetCircleDistance;
            int upperDistance = currentDistance + TRAPPING_NET_DISPERSION * Web.minTrappingNetCircleDistance;

            lowerBound = Math.max(lowerBound, lowerDistance);
            upperBound = Math.min(upperBound, upperDistance);

            web.trappingNet.get(index).points.get(i).distance = lowerBound + (int) (Web.random.nextDouble() * (upperBound - lowerBound));
        }

        web.trappingNet.get(index).save();
        web.trappingNetLength = web.trappingNetLength - oldCircleLength + web.trappingNet.get(index).length;

        super.mutate();
    }
}