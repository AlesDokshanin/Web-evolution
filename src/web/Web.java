package web;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Web {

    private static int skeletonSides = 10;

    public void setSkeletonSides(int sides) throws IllegalArgumentException {
        if (sides <= 2)
            throw new IllegalArgumentException("Number of web skeleton's sides should be more than 2.");
        skeletonSides = sides;
    }


    ArrayList<Point> innerLinesCoords;
    ArrayList<Point> skeletonCoords;

    int generation = 0;
    double efficiency = 0;

    private int width;
    private int height;
    private int minDistance;
    private Point center;
    private double minAngleBetweenSkeletonLines;

    private class PolarPoint implements Comparable<PolarPoint> {
        public double angle;
        public int distance;

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

    public Web(int width, int height) {
        innerLinesCoords = new ArrayList<Point>();
        skeletonCoords = new ArrayList<Point>();
        this.width = width;
        this.height = height;
        minDistance = Math.min(height, width) / 5;
        minAngleBetweenSkeletonLines = 2 * Math.PI / (3 * skeletonSides);

        center = new Point(width / 2, height / 2);
        generate();
    }

    private void generate() {
        generateSkeleton();
        generateInnerLines();
    }

    private void generateInnerLines() {
        innerLinesCoords.clear();
    }

    private void generateSkeleton() {
        skeletonCoords.clear();

        Random r = new Random();

        ArrayList<PolarPoint> skeletonPolarPoints = new ArrayList<PolarPoint>();
        for (int i = 0; i < skeletonSides; i++) {
            PolarPoint p = generateSkeletonPoint(r);
            while(!skeletonPointIsValid(p, skeletonPolarPoints))
                p = generateSkeletonPoint(r);
            skeletonPolarPoints.add(p);
        }

        Collections.sort(skeletonPolarPoints);
        for (int i = 0; i < skeletonPolarPoints.size(); i++) {
            Point p = skeletonPolarPoints.get(i).getCartesianPoint();
            p.x += center.x;
            p.y += center.y;
            skeletonCoords.add(p);
        }

        if (!skeletonIsValid()) {
            generateSkeleton();
        }
    }

    private PolarPoint generateSkeletonPoint(Random random) {
        double angle = random.nextDouble() * 2 * Math.PI; // Generate angle in range [0, 2 * PI]
        int maxDistance = 0;
        Point bound;

        while (maxDistance <= minDistance) {
            bound = new Point((int) (0.5 * width * Math.cos(angle)), (int) (0.5 * height * Math.sin(angle)));
            maxDistance = (int) bound.distance(0, 0);
        }

        int distance = (int) (minDistance + (maxDistance - minDistance) * random.nextDouble());
        return new PolarPoint(angle, distance);
    }

    private boolean skeletonPointIsValid(PolarPoint p, ArrayList<PolarPoint> list) {
        for(PolarPoint listPoint : list) {
            if(Math.abs(p.angle - listPoint.angle) < minAngleBetweenSkeletonLines)
                return false;
            int distance = (int) (p.getCartesianPoint().distance(listPoint.getCartesianPoint()));
            if(distance <= minDistance)
                return false;
        }
        return true;
    }

    private boolean skeletonIsValid() {
        Polygon skeleton = getPolygonFromPointsArray(skeletonCoords);
        int shift = Math.min(width, height) / 10;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Point p = new Point(center.x + dx * shift, center.y + dy * shift);
                if (!skeleton.contains(p)) {
                    return false;
                }
            }
        }

        return true;
    }

    void draw(Graphics2D g) {
        drawSkeleton(g);
        drawLines(g);
        drawCenterPoint(g);
    }

    private void drawCenterPoint(Graphics2D g) {
        g.fillOval(center.x - 2, center.y - 2, 4, 4);
    }

    private void drawSkeleton(Graphics2D g) {
        Polygon polygon = getPolygonFromPointsArray(skeletonCoords);
        g.drawPolygon(polygon);

        for (Point p : skeletonCoords) {
            g.fillOval(p.x - 5, p.y - 5, 10, 10);
        }
    }

    private void drawLines(Graphics2D g) {
        Polygon p = getPolygonFromPointsArray(innerLinesCoords);
        g.drawPolygon(p);
    }

    private Polygon getPolygonFromPointsArray(ArrayList<Point> list) {
        int[] xPoints = new int[list.size()];
        int[] yPoints = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            xPoints[i] = list.get(i).x;
            yPoints[i] = list.get(i).y;
        }

        return new Polygon(xPoints, yPoints, xPoints.length);
    }
}
