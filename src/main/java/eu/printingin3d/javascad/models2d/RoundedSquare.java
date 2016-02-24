package eu.printingin3d.javascad.models2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.printingin3d.javascad.context.IColorGenerationContext;
import eu.printingin3d.javascad.coords2d.Coords2d;
import eu.printingin3d.javascad.coords2d.Dims2d;
import eu.printingin3d.javascad.models.SCAD;
import eu.printingin3d.javascad.utils.AssertValue;
import eu.printingin3d.javascad.vrl.FacetGenerationContext;

/**
 * Represents a 2D rounded square - the corners of the square will be rounded by the given radius.
 * @author ivivan <ivivan@printingin3d.eu>
 */
public class RoundedSquare extends Square {
	private final double radius;

	private RoundedSquare(Coords2d move, Dims2d size, double radius) {
		super(move, size);
		AssertValue.isNotNegative(size.getX()-radius*2.0, "The X size should be at least the double of the radius, "
				+ "but X size was "+size.getX()+" and radius was "+radius);
		AssertValue.isNotNegative(size.getY()-radius*2.0, "The Y size should be at least the double of the radius, "
				+ "but Y size was "+size.getY()+" and radius was "+radius);
		this.radius = radius;
	}

	/**
	 * Create a 2D rounded square.
	 * @param size the size of the object including the rounding
	 * @param radius the radius of the rounding
	 * @throws eu.printingin3d.javascad.exceptions.IllegalValueException 
	 * 			if either dimension is less the double the radius
	 */
	public RoundedSquare(Dims2d size, double radius) {
		this(Coords2d.ZERO, size, radius);
	}
	
	@Override
	protected SCAD innerToScad(IColorGenerationContext context) {
		SCAD result = SCAD.EMPTY;
		
		int numberOfItems = 0;
		double straightX = size.getX()-radius*2.0;
		double straightY = size.getY()-radius*2.0;
		if (straightX>0.0) {
			result = result.append(new Square(new Dims2d(straightX, size.getY())).toScad(context));
			numberOfItems++;
		}
		if (straightY>0.0) {
			result = result.append(new Square(new Dims2d(size.getX(), straightY)).toScad(context));
			numberOfItems++;
		}
		
		Set<Coords2d> coordsSet = new HashSet<>(Arrays.asList(
				new Coords2d(+straightX/2.0, +straightY/2.0),
				new Coords2d(+straightX/2.0, -straightY/2.0),
				new Coords2d(-straightX/2.0, +straightY/2.0),
				new Coords2d(-straightX/2.0, -straightY/2.0)
			));
		for (Coords2d c : coordsSet) {
			result = result.append(new Circle(radius).move(c).toScad(context));
			numberOfItems++;
		}

		if (numberOfItems>1) {
			return result.prepend("union() {").append("}");
		}
		return result;
	}
	
	@Override
	public Abstract2dModel move(Coords2d delta) {
		return new RoundedSquare(this.move.move(delta), this.size, this.radius);
	}

	@Override
	protected Collection<Area2d> getInnerPointCircle(FacetGenerationContext context) {
        List<Coords2d> points = new ArrayList<>();

        int numSlices = context.calculateNumberOfSlices(radius)*4;
        for (int i = numSlices; i > 0; i--) {
        	double alpha = Math.PI*2*i/numSlices;
            double x = Math.sin(alpha)*radius;
            x+=Math.signum(x)*(size.getX()/2-radius);
			double y = Math.cos(alpha)*radius;
            y+=Math.signum(y)*(size.getY()/2-radius);
			points.add(new Coords2d(x, y));
        }
        return Collections.singleton(new Area2d(points));
	}

}
