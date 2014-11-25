package web;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Web {

    private static int skeletonSides = 8;
    public void setSkeletonSides(int sides) throws IllegalArgumentException {
        if(sides <= 2)
            throw new IllegalArgumentException("Number of web skeleton's sides should be more than 2.");
        skeletonSides = sides;
    }


    ArrayList<Point> innerLinesCoors;
    ArrayList<Point> skeletonCoords;

    int generation = 0;
    double efficiency = 0;

    private int width;
    private int height;
    private Point center;

    private class PolarPoint implements Comparable<PolarPoint> {
        public double angle;
        public int distance;

        public PolarPoint(double angle, int distance) {
            this.angle = angle;
            this.distance = distance;
        }

        public Point getCartesianPoint() {
            return new Point((int)(Math.cos(angle) * distance), (int)(Math.sin(angle) * distance));
        }

        @Override
        public int compareTo(PolarPoint polarPoint) {
            return Double.compare(angle, polarPoint.angle);
        }
    }

    public Web(int width, int height) {
        innerLinesCoors = new ArrayList<Point>();
        skeletonCoords = new ArrayList<Point>();
        this.width = width;
        this.height = height;

        center = new Point(width / 2, height / 2);
        generate();
    }

    private void generate() {
        generateSkeleton();
        generateInnerLines();
    }

    private void generateInnerLines() {
        innerLinesCoors.clear();
    }

    private void generateSkeleton() {
        skeletonCoords.clear();

        ArrayList<PolarPoint> skeletonPolarPoints = new ArrayList<PolarPoint>();
        for (int i = 0; i < skeletonSides; i++) {
            Random r = new Random();
            double angle = r.nextDouble() * 2 * Math.PI; // Generate angle in range [0, 2 * PI]
            int distance_min = Math.min(width, height) / 5;
            int distance_max = 0;
            Point bound;
            while(distance_max <= distance_min) {
                bound = new Point((int) (0.5 * width * Math.cos(angle)),(int) (0.5 * height * Math.sin(angle)));
                distance_max = (int) bound.distance(0, 0);
            }
            int distance = (int) (distance_min + (distance_max - distance_min) * r.nextDouble());

            // TODO prevent generating points close to each other and with same (almost) angle.
            skeletonPolarPoints.add(new PolarPoint(angle, distance));
        }

        Collections.sort(skeletonPolarPoints);
        for(int i = 0; i < skeletonPolarPoints.size(); i++) {
            Point p = skeletonPolarPoints.get(i).getCartesianPoint();
            p.x += center.x;
            p.y += center.y;
            skeletonCoords.add(p);
        }

        Polygon skeleton = getPolygonFromPointsArray(skeletonCoords);
        if(!skeleton.contains(center)) {
            generateSkeleton();
        }
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

        for(Point p : skeletonCoords) {
            g.fillOval(p.x - 5, p.y - 5, 10, 10);
        }
    }

    private void drawLines(Graphics2D g){
        Polygon p = getPolygonFromPointsArray(innerLinesCoors);
        g.drawPolygon(p);
    }

    private Polygon getPolygonFromPointsArray(ArrayList<Point> list) {
        int[] xPoints = new int[list.size()];
        int[] yPoints = new int[list.size()];

        for(int i = 0; i < list.size(); i++){
            xPoints[i] = list.get(i).x;
            yPoints[i] = list.get(i).y;
        }

        return new Polygon(xPoints, yPoints, xPoints.length);
    }
}
