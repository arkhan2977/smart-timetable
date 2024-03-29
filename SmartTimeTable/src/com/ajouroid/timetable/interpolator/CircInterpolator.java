package com.ajouroid.timetable.interpolator;

import com.ajouroid.timetable.interpolator.EasingType.Type;

import android.view.animation.Interpolator;


public class CircInterpolator implements Interpolator {

	private Type type;

	public CircInterpolator(Type type) {
		this.type = type;
	}

	public float getInterpolation(float t) {
		if (type == Type.IN) {
			return in(t);
		} else
		if (type == Type.OUT) {
			return out(t);
		} else
		if (type == Type.INOUT) {
			return inout(t);
		}
		return 0;
	}

	private float in(float t) {
		return (float) -(Math.sqrt(1 - t*t) - 1);
	}
	private float out(float t) {
		return (float) Math.sqrt(1 - (t-=1)*t);
	}
	private float inout(float t) {
		t *= 2;
		if (t < 1) {
			return (float) (-0.5f * (Math.sqrt(1 - t*t) - 1));
		} else {
			return (float) (0.5f * (Math.sqrt(1 - (t-=2)*t) + 1));
		}
	}
}
