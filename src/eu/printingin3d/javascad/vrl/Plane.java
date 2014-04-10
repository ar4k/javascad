/**
 * Plane.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package eu.printingin3d.javascad.vrl;

// # class Plane
import java.util.ArrayList;
import java.util.List;

import eu.printingin3d.javascad.coords.Coords3d;

/**
 * Represents a plane in 3D space.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Plane {

    /**
     * EPSILON is the tolerance used by {@link #splitPolygon(Polygon, List, List, List, List)} to decide if a point is on the plane.
     */
    private static final double EPSILON = 1e-6;

    /**
     * Normal vector.
     */
    private final Coords3d normal;
    /**
     * Square of the distance to origin.
     */
    private final double dist;

    /**
     * Constructor. Creates a new plane defined by its normal vector and the
     * distance to the origin.
     *
     * @param normal plane normal
     * @param dist distance from origin
     */
    public Plane(Coords3d normal, double dist) {
        this.normal = normal;
        this.dist = dist;
    }

    /**
     * Creates a nedist plane defined by the the specified points.
     *
     * @param a first point
     * @param b second point
     * @param c third point
     * @return a nedist plane
     */
    public static Plane createFromPoints(Coords3d a, Coords3d b, Coords3d c) {
    	Coords3d n = b.move(a.inverse()).cross(c.move(a.inverse())).unit();
        return new Plane(n, n.dot(a));
    }

    /**
     * Flips this plane.
     */
    public Plane flip() {
    	return new Plane(normal.inverse(), -dist);
    }

    /**
     * Splits a {@link Polygon} by this plane if needed. After that it puts the
     * polygons or the polygon fragments in the appropriate lists
     * ({@code front}, {@code back}). Coplanar polygons go into either
     * {@code coplanarFront}, {@code coplanarBack} depending on their
     * orientation with respect to this plane. Polygons in front or back of this
     * plane go into either {@code front} or {@code back}.
     *
     * @param polygon polygon to split
     * @param coplanarFront "coplanar front" polygons
     * @param coplanarBack "coplanar back" polygons
     * @param front front polygons
     * @param back back polgons
     */
    public void splitPolygon(
            Polygon polygon,
            List<Polygon> coplanarFront,
            List<Polygon> coplanarBack,
            List<Polygon> front,
            List<Polygon> back) {
        final int COPLANAR = 0;
        final int FRONT = 1;
        final int BACK = 2;
        final int SPANNING = 3;

        // Classify each point as well as the entire polygon into one of the above
        // four classes.
        int polygonType = 0;
        List<Integer> types = new ArrayList<>();
        for (Coords3d v : polygon.getVertices()) {
            double t = this.normal.dot(v) - this.dist;
            int type = (t < -Plane.EPSILON) ? BACK : (t > Plane.EPSILON) ? FRONT : COPLANAR;
            polygonType |= type;
            types.add(type);
        }

        // Put the polygon in the correct list, splitting it when necessary.
        switch (polygonType) {
            case COPLANAR:
                (this.normal.dot(polygon.getPlane().normal) > 0 ? coplanarFront : coplanarBack).add(polygon);
                break;
            case FRONT:
                front.add(polygon);
                break;
            case BACK:
                back.add(polygon);
                break;
            case SPANNING:
                List<Coords3d> f = new ArrayList<>();
                List<Coords3d> b = new ArrayList<>();
                for (int i = 0; i < polygon.getVertices().size(); i++) {
                    int j = (i + 1) % polygon.getVertices().size();
                    int ti = types.get(i);
                    int tj = types.get(j);
                    Coords3d vi = polygon.getVertices().get(i);
                    Coords3d vj = polygon.getVertices().get(j);
                    if (ti != BACK) {
                        f.add(vi);
                    }
                    if (ti != FRONT) {
                        b.add(vi);
                    }
                    if ((ti | tj) == SPANNING) {
                        double t = (this.dist - this.normal.dot(vi)) / this.normal.dot(vj.move(vi.inverse()));
                        Coords3d v = vi.lerp(vj, t);
                        f.add(v);
                        b.add(v);
                    }
                }
                if (f.size() >= 3) {
                    front.add(new Polygon(f));
                }
                if (b.size() >= 3) {
                    back.add(new Polygon(b));
                }
                break;
        }
    }

	public Coords3d getNormal() {
		return normal;
	}
}