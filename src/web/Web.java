package web;

import java.awt.*;
import java.util.ArrayList;
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

    public Web(int width, int height) {
        innerLinesCoors = new ArrayList<Point>();
        skeletonCoords = new ArrayList<Point>();
        this.width = width;
        this.height = height;

        center = new Point(width / 2, height / 2);
        generate();
    }

    private void generate() {
        skeletonCoords.clear();
        innerLinesCoors.clear();

        for (int i = 0; i < skeletonSides; i++) {
            Random r = new Random();
            double angle = r.nextDouble() * 2 * Math.PI; // Generate angle in range [0, 2 * PI]
            int distance_min = Math.min(width, height) / 20;
            int distance_max = 0;
            Point bound = new Point();
            while(distance_max <= distance_min) {
                bound = new Point((int) (0.5 * width * Math.cos(angle)),(int) (0.5 * height * Math.sin(angle)));
                distance_max = (int) bound.distance(0, 0);
            }

            int distance = (int) (distance_min + (distance_max - distance_min) * r.nextDouble());
            double k = (double) distance / distance_max;
            Point p = new Point((int) (k * bound.x) + center.x, (int) (k * bound.y) + center.y);
            skeletonCoords.add(p);
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
        /*Polygon p = getPolygonFromPointsArray(skeletonCoords);
        g.drawPolygon(p);*/

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
