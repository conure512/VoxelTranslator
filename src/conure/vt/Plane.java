package conure.vt;
/**Represents a 2D cross-section of 3D space, defined by a starting point P
 * and two vectors, vA and vB, pointing away from it. A point (x,y) on the plane
 * can be expressed in the enclosing 3D space as P + x*vA + y*vB.*/
public class Plane {
	private final double[] P,vA,vB;
	/**Generates the plane that shares 3 points in space.
	 * @param a - First point
	 * @param b - Second point
	 * @param c - Third point*/
	public Plane(double[] a,double[] b,double[] c) {
		P=a;
		vA=new double[3];
		vB=new double[3];
		for(int i=0;i<3;i++) {
			vA[i]=b[i]-a[i];
			vB[i]=c[i]-a[i];
		}
	}
	/**Gives the length of one "step" in the plane's x-direction.
	 * @return The length (modulus) of vA.*/
	public double getXScale() {
		double scale=0;
		for(int i=0;i<3;i++)
			scale+=vA[i]*vA[i];
		return Math.sqrt(scale);
	}
	/**Gives the length of one "step" in the plane's y-direction.
	 * @return The length (modulus) of vB.*/
	public double getYScale() {
		double scale=0;
		for(int i=0;i<3;i++)
			scale+=vB[i]*vB[i];
		return Math.sqrt(scale);
	}
	/**Converts a point (x,y) on the plane to a point in the enclosing 3D space.
	 * @param x - planar x-coordinate
	 * @param y - planar y-coordinate
	 * @return A 3-element array representing the point's location in 3D space.*/
	public double[] getRealCoords(double x,double y) {
		double[] coords=new double[3];
		for(int i=0;i<3;i++)
			coords[i]=P[i]+x*vA[i]+y*vB[i];
		return coords;
	}
}