package web;

import javax.naming.OperationNotSupportedException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Web {

    private class PolarPoint implements Comparable<PolarPoint> {
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

    private class WebInnerLine {
        private ArrayList<PolarPoint> list;
        private Polygon polygon = null;

        public WebInnerLine() throws OperationNotSupportedException {
            list = new ArrayList<PolarPoint>();
            generateInnerLine();
        }

        public Polygon getPolygon() {
            if (polygon == null)
                generatePolygon();
            return polygon;
        }

        private void generatePolygon() {
            int n = list.size();
            int[] xPoints = new int[n];
            int[] yPoints = new int[n];
            for (int i = 0; i < n; i++) {
                Point p = list.get(i).getCartesianPoint();
                xPoints[i] = p.x;
                yPoints[i] = p.y;
            }
            polygon = new Polygon(xPoints, yPoints, n);
            polygon.translate(center.x, center.y);
        }

        private void generateInnerLine() throws OperationNotSupportedException {
            Random r = new Random();
            for (int i = 0; i < skeletonSides; i++) {
                int lowerBound = minInnerLineDistance;
                if(!innerLines.isEmpty()) {
                    lowerBound += innerLines.get(innerLines.size() - 1).list.get(i).distance;
                }
                int maxDistance = skeletonPoints.get(i).distance - minInnerLineDistance;

                if(lowerBound > maxDistance)
                    throw new OperationNotSupportedException("Can't generate inner line point. Min distance > Max distance ");

                int upperBound = Math.min(maxDistance, lowerBound + (int) (innerLinesDispersionCoeff * minInnerLineDistance));
                double angle = skeletonPoints.get(i).angle;

                int distance = (int) (lowerBound + r.nextDouble() * (upperBound - lowerBound));
                list.add(new PolarPoint(angle, distance));
            }
            innerLines.add(this);
            generatePolygon();
        }
    }

    public static int width = 600;
    public static int height = 600;

    public void setSkeletonSides(int sides) throws IllegalArgumentException {
        if (sides <= 2)
            throw new IllegalArgumentException("Number of web skeleton's sides should be more than 2.");
        skeletonSides = sides;
    }

    private static int skeletonSides = 10;
    private static int innnerLinesCount = 5;

    private Point center = new Point(width / 2, height / 2);

    private static double minAngleBetweenSkeletonLines = 2 * Math.PI / (3 * skeletonSides);
    private static int minSkeletonDistance = Math.min(height, width) / 5;
    private static int minInnerLineDistance = Math.min(height, width) / 50;
    private static int minSkeletonDistanceFromCenter = 2 * minSkeletonDistance;

    private static double innerLinesDispersionCoeff = 4.0;

    private Polygon skeletonPolygon;
    private ArrayList<Polygon> innerLinesPolygons;

    private ArrayList<PolarPoint> skeletonPoints;
    private ArrayList<WebInnerLine> innerLines;

    int generation = 0;

    double efficiency = 0;

    public Web() {
        skeletonPoints = new ArrayList<PolarPoint>();
        innerLines = new ArrayList<WebInnerLine>();

        center = new Point(width / 2, height / 2);
        generate();
    }

    private void generate() {
        generateSkeleton();
        generateInnerLines();
    }

    private void generateInnerLines() {
        innerLines.clear();
        try {
            while(true)
                innerLines.add(new WebInnerLine());
        } catch (OperationNotSupportedException e) {
            // Do nothing. Exception means we can't add more inner lines to our web.
        }

    }

    private void generateSkeleton() {
        skeletonPoints.clear();
        Random r = new Random();

        for (int i = 0; i < skeletonSides; i++) {
            PolarPoint p = generateSkeletonPoint(r);
            while (!skeletonPointIsValid(p, skeletonPoints))
                p = generateSkeletonPoint(r);
            skeletonPoints.add(p);
        }

        Collections.sort(skeletonPoints);

        generateSkeletonPolygon();
        skeletonPolygon.translate(center.x, center.y);

        if (!skeletonIsValid()) {
            generateSkeleton();
        }
    }

    private PolarPoint generateSkeletonPoint(Random random) {
        double angle = random.nextDouble() * 2 * Math.PI; // Generate angle in range [0, 2 * PI]
        int maxDistance = 0;
        Point bound;

        while (maxDistance <= minSkeletonDistance) {
            bound = new Point((int) (0.5 * width * Math.cos(angle)), (int) (0.5 * height * Math.sin(angle)));
            maxDistance = (int) bound.distance(0, 0);
        }



        int distance = (int) (minSkeletonDistanceFromCenter + (maxDistance - minSkeletonDistanceFromCenter) * random.nextDouble());
        return new PolarPoint(angle, distance);
    }

    private boolean skeletonPointIsValid(PolarPoint p, ArrayList<PolarPoint> list) {
        for (PolarPoint listPoint : list) {
            if (Math.abs(p.angle - listPoint.angle) < minAngleBetweenSkeletonLines)
                return false;
            int distance = (int) (p.getCartesianPoint().distance(listPoint.getCartesianPoint()));
            if (p.distance <= minSkeletonDistance)
                return false;
        }
        return true;
    }

    private boolean skeletonIsValid() {
        return skeletonContainsCenter();
    }

    private boolean skeletonContainsCenter() {
        int shift = Math.min(width, height) / 10;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Point p = new Point(center.x + dx * shift, center.y + dy * shift);
                if (!skeletonPolygon.contains(p)) {
                    return false;
                }
            }
        }
        return true;
    }

    void draw(Graphics2D g) {
        drawSkeleton(g);
        drawInnerLines(g);
        drawCenterPoint(g);
    }

    private void drawCenterPoint(Graphics2D g) {
        g.fillOval(center.x - 2, center.y - 2, 4, 4);
    }

    private void drawSkeleton(Graphics2D g) {
        g.drawPolygon(skeletonPolygon);
        int[] xPoints = skeletonPolygon.xpoints;
        int[] yPoints = skeletonPolygon.ypoints;
        for (int i = 0; i < xPoints.length; i++) {
            g.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
            g.drawLine(xPoints[i], yPoints[i], center.x, center.y);
        }
    }

    private void generateSkeletonPolygon() {
        skeletonPolygon = getPolygonFromPolarPointsList(skeletonPoints);
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

    private void drawInnerLines(Graphics2D g) {
        for(WebInnerLine line : innerLines)
            g.drawPolygon(line.getPolygon());
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
