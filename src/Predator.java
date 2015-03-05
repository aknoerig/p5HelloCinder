import java.awt.Color;

import processing.core.PApplet;
import toxi.geom.Vec3D;

public class Predator {
	PApplet p;

	int mLen;
	float mInvLen;
	Vec3D[] mPos;

	Vec3D mVel;
	Vec3D mVelNormal;
	Vec3D mAcc;

	Color mColor;

	Vec3D mNeighborPos;
	int mNumNeighbors;

	float mDecay;
	float mRadius;
	float mLength;
	float mMaxSpeed, mMaxSpeedSqrd;
	float mMinSpeed, mMinSpeedSqrd;
	float mHunger;

	boolean mIsHungry;
	boolean mIsDead;

	Predator(Vec3D pos, Vec3D vel, PApplet p) {
		this.p = p;

		mLen = 5;
		mInvLen = 1.0f / (float) mLen;

		mPos = new Vec3D[5];
		for (int i = 0; i < mLen; ++i) {
			mPos[i] = pos;
		}

		mVel = vel;
		mVelNormal = new Vec3D(Vec3D.Y_AXIS);
		mAcc = new Vec3D(Vec3D.ZERO);

		mNeighborPos = new Vec3D(Vec3D.ZERO);
		mNumNeighbors = 0;
		mMaxSpeed = p.random(4.0f, 4.5f);
		mMaxSpeedSqrd = mMaxSpeed * mMaxSpeed;
		mMinSpeed = p.random(1.0f, 1.5f);
		mMinSpeedSqrd = mMinSpeed * mMinSpeed;

		mColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);

		mDecay = 0.99f;
		mRadius = 2.0f;
		mLength = 25.0f;
		mHunger = 1.0f;

		mIsDead = false;
		mIsHungry = true;
	}

	void pullToCenter(final Vec3D center) {
		Vec3D dirToCenter = mPos[0].sub(center);
		float distToCenter = dirToCenter.magnitude();
		float maxDistance = 600.0f;

		if (distToCenter > maxDistance) {
			float pullStrength = 0.0001f;
			mVel.subSelf(dirToCenter.getNormalized().scale(
					((distToCenter - maxDistance) * pullStrength)));
		}
	}

	void update(boolean flatten) {
		mVel.addSelf(mAcc);

		if (flatten)
			mAcc.z = 0.0f;
		mVel.addSelf(mAcc);
		mVelNormal = mVel.getNormalized();

		limitSpeed();

		for (int i = mLen - 1; i > 0; i--) {
			mPos[i] = mPos[i - 1];
		}
		mPos[0].addSelf(mVel);

		if (flatten)
			mPos[0].z = 0.0f;

		mVel.scaleSelf(mDecay);

		mAcc = new Vec3D(Vec3D.ZERO);
		mNeighborPos = new Vec3D(Vec3D.ZERO);
		mNumNeighbors = 0;

		mHunger += 0.001f;
		mHunger = PApplet.constrain(mHunger, 0.0f, 1.0f);

		if (mHunger > 0.5f)
			mIsHungry = true;
	}

	void limitSpeed() {
		float maxSpeed = mMaxSpeed + mHunger * 3.0f;
		float maxSpeedSqrd = maxSpeed * maxSpeed;
		float vLengthSqrd = mVel.magSquared();
		if (vLengthSqrd > maxSpeedSqrd) {
			mVel = mVelNormal.scale(maxSpeed);

		} else if (vLengthSqrd < mMinSpeedSqrd) {
			mVel = mVelNormal.scale(mMinSpeed);
		}
	}

	void draw() {
		Vec3D vel = mVelNormal.scale(mLength);
		
		p.stroke(mColor.getRGB());
		p.strokeWeight(3.0f + mHunger);
		Vec3D start = mPos[0].sub(mVel);
		Vec3D end = mPos[0];
		p.line(start.x, start.y, start.z, end.x, end.y, end.z);
	}

	void drawTail() {
		p.stroke(mColor.getRGB());
		p.strokeWeight(5);
		p.line(mPos[0].x, mPos[0].y, mPos[0].z, 
				mPos[1].x, mPos[1].y, mPos[1].z);
	}

	void addNeighborPos(Vec3D pos) {
		mNeighborPos.addSelf(pos);
		mNumNeighbors++;
	}
}
