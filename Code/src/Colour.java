/* Lab 5 A, Group 51 -- Alex Bhandari-Young and Neil Edelman */

/* This method is O(2^{n-1}), but n is 2, so it's okay. We normalise our
 colours to make it lighting-independent. Compare with experimetal value for
 the different substances, and pick the closest (Cartesan distance to
 normalised colour values.) I better model would be to multiply the components
 by the eigenvalues of their sensitivety like CIE colour model, but we don't
 know this. */

/* import javax.vecmath.Vector3f; <- nxj does not have this, write our own :[ */
/* import java.lang.Comparable; */

//import java.lang.IllegalArgumentException;

import lejos.nxt.LCD;
import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;

public class Colour {
	enum Value { UNKNOWN, STYROFOAM, WOOD };

	/* experimenatly determined over multiple orinatations */
	static Vector3f styrofoam = new Vector3f(0.569778f, 0.546741f, 0.611144f);
	static Vector3f woodblock = new Vector3f(0.795851f, 0.438809f, 0.414174f);

	ColorSensor      cs;
	Vector3f     colour = new Vector3f();
	Vector3f colourDiff = new Vector3f();

	/** constructor
	 @author Neil
	 @param port The port the colour sensor is plugged into */
	public Colour(final SensorPort port) {

		cs = new ColorSensor(port);

		/* the normalisation projects the HSL values onto L = 0.5 so it isn't
		 affected by natural light (3d -> 2d;) barycentric coordinates are the
		 square of the normalised values */
		styrofoam.normalize();
		woodblock.normalize();
	}

	/** gets the colour from the sensor right now
	 @author Neil
	 @return the colour as and rgb [0..1] */
	private Vector3f getColour() {
		ColorSensor.Color c;
		Vector3f          colour;

		/* store it in useless "Color" then transfer it to useful class */
		//cs.setFloodlight(true);
		/*cs.setFloodlight(Color.RED);
		cs.setFloodlight(Color.GREEN);
		cs.setFloodlight(Color.BLUE); oh good grief */
		c      = cs.getColor(); /* 0 - 255 */
		//cs.setFloodlight(false);
		colour = new Vector3f(c.getRed()   / 255f,
							  c.getGreen() / 255f,
							  c.getBlue()  / 255f);
		colour.normalize();

		return colour;
	}

	/** senses the colour at the current location
	 @author Neil
	 @return The most likely colour as a Value enum. */
	public Value getColourValue() {
		Vector3f          colour;
		float             s, w;
		int               percent;

		colour = this.getColour();

		/* compare with styrofoam and wood; I think technically, we should
		 convert to a quaternion to get the great circle distance, but
		 that's kind of like measureing a distance between two points
		 taking into account the curvature of the Earth; it's monotonanic
		 and close */
		colourDiff.sub(colour, styrofoam);
		s = colourDiff.lengthSquared();
		colourDiff.sub(colour, woodblock);
		w = colourDiff.lengthSquared();

		/* display */
		/*LCD.drawString("R "+c.getRed()+"/"+colour.r, 0,1);
		LCD.drawString("G "+c.getGreen()+"/"+colour.g, 0,2);
		LCD.drawString("B "+c.getBlue()+"/"+colour.b, 0,3);*/
		if(s > w) {
			percent = (int)(s / (s + w) * 100f);
//			System.out.println("I'm " + percent + "% sure it's a wooden block");
			return Value.WOOD;
		} else if(w > s) {
			percent = (int)(w / (s + w) * 100f);
//			System.out.println("I'm " + percent + "% sure it's a styrofoam block");
			return Value.STYROFOAM;
		} else {
//			System.out.println("I'm conflicted");
			return Value.UNKNOWN;
		}
	}

	/** how certain we are that the object in front of colour sensor is
	 styrofoam?
	 @author Neil
	 @return The probability [0..1] */
	public float getStyrofoamProbability() {
		Vector3f          colour;
		float             s, w;
		int               percent;

		colour = this.getColour();

		/* compare with styrofoam and wood */
		colourDiff.sub(colour, styrofoam);
		s = colourDiff.lengthSquared();
		colourDiff.sub(colour, woodblock);
		w = colourDiff.lengthSquared();

		return w / (s + w);
	}

}



/* helper class */
/* package javax.vecmath does not exist; aaaaauuuuuuggghht wtf */

/* this is a normalised colour on 19:13 bit fixed point
 3*255^2 = 195 075 (18 bit) */
/* no, that's complicated, just use floats */
class Vector3f /*implements Comparable<ColourNorm> <- only int */ {
	public float r, g, b;
	
	/** empty constructor
	 @author Neil */
	public Vector3f() {
	}
	
	/** fill constructor
	 @author Neil
	 @param r
	 @param g
	 @param b The colour values in [0..1]. */
	public Vector3f(final float r, final float g, final float b) {
		set(r, g, b);
	}
	
	/** fill
	 @author Neil
	 @param r
	 @param g
	 @param b The colour values in [0..1]. */
	public final void set(final float r, final float g, final float b) {
		/* this is a general vector class,
		if(r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1)
			throw new IllegalArgumentException("colour value");*/
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	/** returns the length squared
	 @author Neil
	 @return length squared */
	public float lengthSquared() {
		return r*r + g*g + b*b;
	}
	
	/** returns the length, computes the lenght squared so make sure it's
	 "small"
	 @author Neil
	 @return length */
	public float length() {
		return (float)Math.sqrt(this.lengthSquared());
	}

	/** normalises this vector in place
	 @author Neil */
	public void normalize() {
		float d = length();
		
		/* if it's black as satan, just leave it alone */
		if(d == 0f) return;
		/* invert */
		float e = 1f / d;
		r *= e;
		g *= e;
		b *= e;
	}
	
	/** subtract a Vector3f
	 @author Neil
	 @param x The variable to subatact. */
	public void sub(final Vector3f x) {
		r -= x.r;
		g -= x.g;
		b -= x.g;
	}

	/** set the varible to the subtraction x - y
	 @author Neil
	 @param x +
	 @param y - */
	public void sub(final Vector3f x, final Vector3f y) {
		r = x.r - y.r;
		g = x.g - y.g;
		b = x.g - y.b;
	}

	/** to string
	 @author Neil
	 @return The printed value as a string. */
	public String toString() {
		return "(" + r + ", " + g + ", " + b + ")";
	}

}
