package com.ajouroid.timetable.interpolator;

import com.ajouroid.timetable.interpolator.EasingType.Type;

import android.view.animation.Interpolator;


public class ExpoInterpolator implements Interpolator {

	private Type type;

	public ExpoInterpolator(Type type) {
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
		return (float) ((t==0) ? 0 : Math.pow(2, 10 * (t - 1)));
	}
	private float out(float t) {
		return (float) ((t>=1) ? 1 : (-Math.pow(2, -10 * t) + 1));
	}
	private float inout(float t) {
		if (t == 0) {
			return 0;
		}
		if (t >= 1) {
			return 1;
		}
		t *= 2;
		if (t < 1) {
			return (float) (0.5f * Math.pow(2, 10 * (t - 1)));
		} else {
			return (float) (0.5f * (-Math.pow(2, -10 * --t) + 2));
		}
	}
}
