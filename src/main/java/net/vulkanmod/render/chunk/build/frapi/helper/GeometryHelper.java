/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.vulkanmod.render.chunk.build.frapi.helper;

import static net.minecraft.class_3532.method_20390;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.class_2350;
import net.minecraft.class_2350.class_2351;
import net.minecraft.class_2350.class_2352;
import net.minecraft.class_777;
import org.joml.Vector3fc;

/**
 * Static routines of general utility for renderer implementations.
 * Renderers are not required to use these helpers, but they were
 * designed to be usable without the default renderer.
 */
public abstract class GeometryHelper {
	private GeometryHelper() { }

	/** set when a quad touches all four corners of a unit cube. */
	public static final int CUBIC_FLAG = 1;

	/** set when a quad is parallel to (but not necessarily on) a its light face. */
	public static final int AXIS_ALIGNED_FLAG = CUBIC_FLAG << 1;

	/** set when a quad is coplanar with its light face. Implies {@link #AXIS_ALIGNED_FLAG} */
	public static final int LIGHT_FACE_FLAG = AXIS_ALIGNED_FLAG << 1;

	/** how many bits quad header encoding should reserve for encoding geometry flags. */
	public static final int FLAG_BIT_COUNT = 3;

	private static final float EPS_MIN = 0.0001f;
	private static final float EPS_MAX = 1.0f - EPS_MIN;

	/**
	 * Analyzes the quad and returns a value with some combination
	 * of {@link #AXIS_ALIGNED_FLAG}, {@link #LIGHT_FACE_FLAG} and {@link #CUBIC_FLAG}.
	 * Intended use is to optimize lighting when the geometry is regular.
	 * Expects convex quads with all points co-planar.
	 */
	public static int computeShapeFlags(QuadView quad) {
		class_2350 lightFace = quad.lightFace();
		int bits = 0;

		if (isQuadParallelToFace(lightFace, quad)) {
			bits |= AXIS_ALIGNED_FLAG;

			if (isParallelQuadOnFace(lightFace, quad)) {
				bits |= LIGHT_FACE_FLAG;
			}
		}

		if (isQuadCubic(lightFace, quad)) {
			bits |= CUBIC_FLAG;
		}

		return bits;
	}

	/**
	 * Returns true if quad is parallel to the given face.
	 * Does not validate quad winding order.
	 * Expects convex quads with all points co-planar.
	 */
	public static boolean isQuadParallelToFace(class_2350 face, QuadView quad) {
		int i = face.method_10166().ordinal();
		final float val = quad.posByIndex(0, i);
		return method_15347(val, quad.posByIndex(1, i)) && method_15347(val, quad.posByIndex(2, i)) && method_15347(val, quad.posByIndex(3, i));
	}

	/**
	 * True if quad - already known to be parallel to a face - is actually coplanar with it.
	 * For compatibility with vanilla resource packs, also true if quad is outside the face.
	 *
	 * <p>Test will be unreliable if not already parallel, use {@link #isQuadParallelToFace(class_2350, QuadView)}
	 * for that purpose. Expects convex quads with all points co-planar.
	 */
	public static boolean isParallelQuadOnFace(class_2350 lightFace, QuadView quad) {
		final float x = quad.posByIndex(0, lightFace.method_10166().ordinal());
		return lightFace.method_10171() == class_2352.field_11056 ? x >= EPS_MAX : x <= EPS_MIN;
	}

	/**
	 * Returns true if quad is truly a quad (not a triangle) and fills a full block cross-section.
	 * If known to be true, allows use of a simpler/faster AO lighting algorithm.
	 *
	 * <p>Does not check if quad is actually coplanar with the light face, nor does it check that all
	 * quad vertices are coplanar with each other.
	 *
	 * <p>Expects convex quads with all points co-planar.
	 */
	public static boolean isQuadCubic(class_2350 lightFace, QuadView quad) {
		int a, b;

		switch (lightFace) {
		case field_11034:
		case field_11039:
			a = 1;
			b = 2;
			break;
		case field_11036:
		case field_11033:
			a = 0;
			b = 2;
			break;
		case field_11035:
		case field_11043:
			a = 1;
			b = 0;
			break;
		default:
			// handle WTF case
			return false;
		}

		return confirmSquareCorners(a, b, quad);
	}

	/**
	 * Used by {@link #isQuadCubic(class_2350, QuadView)}.
	 * True if quad touches all four corners of unit square.
	 *
	 * <p>For compatibility with resource packs that contain models with quads exceeding
	 * block boundaries, considers corners outside the block to be at the corners.
	 */
	private static boolean confirmSquareCorners(int aCoordinate, int bCoordinate, QuadView quad) {
		int flags = 0;

		for (int i = 0; i < 4; i++) {
			final float a = quad.posByIndex(i, aCoordinate);
			final float b = quad.posByIndex(i, bCoordinate);

			if (a <= EPS_MIN) {
				if (b <= EPS_MIN) {
					flags |= 1;
				} else if (b >= EPS_MAX) {
					flags |= 2;
				} else {
					return false;
				}
			} else if (a >= EPS_MAX) {
				if (b <= EPS_MIN) {
					flags |= 4;
				} else if (b >= EPS_MAX) {
					flags |= 8;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		return flags == 15;
	}

	/**
	 * Identifies the face to which the quad is most closely aligned.
	 * This mimics the value that {@link class_777#getDirection()} returns, and is
	 * used in the vanilla renderer for all diffuse lighting.
	 *
	 * <p>Derived from the quad face normal and expects convex quads with all points co-planar.
	 */
	public static class_2350 lightFace(QuadView quad) {
		final Vector3fc normal = quad.faceNormal();
		switch (GeometryHelper.longestAxis(normal)) {
		case field_11048:
			return normal.x() > 0 ? class_2350.field_11034 : class_2350.field_11039;

		case field_11052:
			return normal.y() > 0 ? class_2350.field_11036 : class_2350.field_11033;

		case field_11051:
			return normal.z() > 0 ? class_2350.field_11035 : class_2350.field_11043;

		default:
			// handle WTF case
			return class_2350.field_11036;
		}
	}

	/**
	 * Simple 4-way compare, doesn't handle NaN values.
	 */
	public static float min(float a, float b, float c, float d) {
		final float x = a < b ? a : b;
		final float y = c < d ? c : d;
		return x < y ? x : y;
	}

	/**
	 * Simple 4-way compare, doesn't handle NaN values.
	 */
	public static float max(float a, float b, float c, float d) {
		final float x = a > b ? a : b;
		final float y = c > d ? c : d;
		return x > y ? x : y;
	}

	/**
	 * @see #longestAxis(float, float, float)
	 */
	public static class_2351 longestAxis(Vector3fc vec) {
		return longestAxis(vec.x(), vec.y(), vec.z());
	}

	/**
	 * Identifies the largest (max absolute magnitude) component (X, Y, Z) in the given vector.
	 */
	public static class_2351 longestAxis(float normalX, float normalY, float normalZ) {
		class_2351 result = class_2351.field_11052;
		float longest = Math.abs(normalY);
		float a = Math.abs(normalX);

		if (a > longest) {
			result = class_2351.field_11048;
			longest = a;
		}

		return Math.abs(normalZ) > longest
				? class_2351.field_11051 : result;
	}
}
