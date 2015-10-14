/**
 * Author : Harish D
 */
package vgl.iisc.volray.display;

public class Trackball {

	private static final float TRACKBALLSIZE = 0.8f;

	private static void vSet(float[] v, float x, float y, float z) {
		v[0] = x;
		v[1] = y;
		v[2] = z;
	}

	private static float[] vSub(float[] src1, float[] src2) {
		float[] dst = new float[3];
		dst[0] = src1[0] - src2[0];
		dst[1] = src1[1] - src2[1];
		dst[2] = src1[2] - src2[2];
		return dst;
	}

	private static void vCopy(float[] v1, float[] v2) {
		v2[0] = v1[0];
		v2[1] = v1[1];
		v2[2] = v1[2];
	}

	public static float[] vCross(float[] v1, float[] v2) {
		float[] v = new float[4];

		v[0] = (v1[1] * v2[2]) - (v1[2] * v2[1]);
		v[1] = (v1[2] * v2[0]) - (v1[0] * v2[2]);
		v[2] = (v1[0] * v2[1]) - (v1[1] * v2[0]);
		return v;
	}

	private static float vLength(float[] v) {
		return (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
	}

	private static void vScale(float[] v, float div) {
		v[0] *= div;
		v[1] *= div;
		v[2] *= div;
	}

	private static void vNormal(float[] v) {
		vScale(v, 1.0f / vLength(v));
	}

	public static float vDot(float[] v1, float[] v2) {
		return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
	}

	private static float[] vAdd(float[] src1, float[] src2) {
		float[] dst = new float[4];
		dst[0] = src1[0] + src2[0];
		dst[1] = src1[1] + src2[1];
		dst[2] = src1[2] + src2[2];
		return dst;
	}
	
	/**
	 * Project an x,y pair onto a sphere of radius r OR a hyperbolic sheet if we
	 * are away from the center of the sphere.
	 */
	private static float projectToSphere(float r, float x, float y) {
		float d, t, z;

		d = (float) Math.sqrt(x * x + y * y);
		if (d < r * 0.70710678118654752440f) { /* Inside sphere */
			z = (float) Math.sqrt(r * r - d * d);
		} else { /* On hyperbola */
			t = r / 1.41421356237309504880f;
			z = t * t / d;
		}
		return z;
	}

	/**
	 * Quaternions always obey: a^2 + b^2 + c^2 + d^2 = 1.0 If they don't add up
	 * to 1.0, dividing by their magnitued will renormalize them.
	 */
	private static void normalizeQuat(float[] q) {
		int i;
		float mag;

		mag = (q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
		for (i = 0; i < 4; i++) {
			q[i] /= mag;
		}
	}

	/**
	 * Pass the x and y coordinates of the last and current positions of the
	 * mouse, scaled so they are from (-1.0 ... 1.0).
	 * 
	 * The resulting rotation is returned as a quaternion rotation.
	 * 
	 * Ok, simulate a track-ball. Project the points onto the virtual trackball,
	 * then figure out the axis of rotation, which is the cross product of P1 P2
	 * and O P1 (O is the center of the ball, 0,0,0) Note: This is a deformed
	 * trackball-- is a trackball in the center, but is deformed into a
	 * hyperbolic sheet of rotation away from the center. This particular
	 * function was chosen after trying out several variations.
	 * 
	 * It is assumed that the arguments to this routine are in the range (-1.0
	 * ... 1.0)
	 */
	public static float[] trackBall(float p1x, float p1y, float p2x, float p2y) {
		float[] a; /* Axis of rotation */
		float phi; /* how much to rotate about axis */
		float[] p1 = new float[3];
		float[] p2 = new float[3];
		float[] d;
		float t;

		if (p1x == p2x && p1y == p2y) {
			/* Zero rotation */
			float[] q = new float[4];
			// vZero(q);
			q[3] = 1.0f;
			return q;
		}

		/*
		 * First, figure out z-coordinates for projection of P1 and P2 to
		 * deformed sphere
		 */
		vSet(p1, p1x, p1y, projectToSphere(TRACKBALLSIZE, p1x, p1y));
		vSet(p2, p2x, p2y, projectToSphere(TRACKBALLSIZE, p2x, p2y));

		/*
		 * Now, we want the cross product of P1 and P2
		 */
		a = vCross(p2, p1);

		/*
		 * Figure out how much to rotate around that axis.
		 */
		d = vSub(p1, p2);
		t = vLength(d) / (2.0f * TRACKBALLSIZE);

		/*
		 * Avoid problems with out-of-control values...
		 */
		if (t > 1.0f)
			t = 1.0f;
		if (t < -1.0f)
			t = -1.0f;
		phi = (float) (2.0f * Math.asin(t));

		return axisToQuat(a, phi);
	}

	private static int count = 0;

	private static int RENORMCOUNT = 97;

	/**
	 * Given two quaternions, add them together to get a third quaternion.
	 * Adding quaternions to get a compound rotation is analagous to adding
	 * translations to get a compound translation. When incrementally adding
	 * rotations, the first argument here should be the new rotation, the second
	 * and third the total rotation (which will be over-written with the
	 * resulting new total rotation).
	 */
	public static float[] addQuats(float[] q1, float[] q2) {
		float[] dest = new float[4];

		float[] t1 = new float[4];
		float[] t2 = new float[4];
		float[] t3 = new float[4];
		float[] tf = new float[4];

		vCopy(q1, t1);
		vScale(t1, q2[3]);

		vCopy(q2, t2);
		vScale(t2, q1[3]);

		t3 = vCross(q2, q1);
		tf = vAdd(t1, t2);
		tf = vAdd(t3, tf);
		tf[3] = q1[3] * q2[3] - vDot(q1, q2);

		dest[0] = tf[0];
		dest[1] = tf[1];
		dest[2] = tf[2];
		dest[3] = tf[3];

		if (++count > RENORMCOUNT) {
			count = 0;
			normalizeQuat(dest);
		}
		return dest;
	}

	/**
	 * A useful function, builds a rotation matrix in Matrix based on given
	 * quaternion.
	 */
	public static float[] buildRotMatrix(float[] q) {
		float[] m = new float[16];

		m[0] = 1.0f - 2.0f * (q[1] * q[1] + q[2] * q[2]);
		m[1] = 2.0f * (q[0] * q[1] - q[2] * q[3]);
		m[2] = 2.0f * (q[2] * q[0] + q[1] * q[3]);
		m[3] = 0.0f;

		m[4] = 2.0f * (q[0] * q[1] + q[2] * q[3]);
		m[5] = 1.0f - 2.0f * (q[2] * q[2] + q[0] * q[0]);
		m[6] = 2.0f * (q[1] * q[2] - q[0] * q[3]);
		m[7] = 0.0f;

		m[8] = 2.0f * (q[2] * q[0] - q[1] * q[3]);
		m[9] = 2.0f * (q[1] * q[2] + q[0] * q[3]);
		m[10] = 1.0f - 2.0f * (q[1] * q[1] + q[0] * q[0]);
		m[11] = 0.0f;

		m[12] = 0.0f;
		m[13] = 0.0f;
		m[14] = 0.0f;
		m[15] = 1.0f;

		return m;
	}

	/**
	 * This function computes a quaternion based on an axis (defined by the
	 * given vector) and an angle about which to rotate. The angle is expressed
	 * in radians. The result is put into the third argument.
	 */
	private static float[] axisToQuat(float[] a, float phi) {
		float[] q = new float[4];
		vNormal(a);
		vCopy(a, q);
		vScale(q, (float) Math.sin(phi / 2.0f));
		q[3] = (float) Math.cos(phi / 2.0f);
		return q;
	}
}
