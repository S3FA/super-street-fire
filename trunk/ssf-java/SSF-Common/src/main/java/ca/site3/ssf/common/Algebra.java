package ca.site3.ssf.common;

import org.apache.commons.math.geometry.Vector3D;

public class Algebra {
	public static final float  FLT_EPSILON = 0.0001f;
	public static final double DBL_EPSILON = 0.00000001;
	
	private Algebra() {
	}
	
	public static boolean Approx(Vector3D v1, Vector3D v2, double epsilon) {
		return Math.abs(v1.getX() - v2.getX()) < epsilon &&
			   Math.abs(v1.getY() - v2.getY()) < epsilon &&
			   Math.abs(v1.getZ() - v2.getZ()) < epsilon;
	}
	
	public static float LerpF(double x0, double x1, float y0, float y1, double x) {
		return (float)(y0 + (y1 - y0) * (x - x0) / (x1 - x0));
	}
	
}
