import java.awt.Color;

import processing.core.PApplet;
import toxi.geom.Vec3D;

public class Particle {
	PApplet p;

	Vec3D mPos;
	Vec3D mTailPos;
	Vec3D mVel;
	Vec3D mVelNormal;
	Vec3D mAcc;

	Vec3D mNeighborPos;
	int mNumNeighbors;

	Color mColor;

	float mDecay;
	float mRadius;
	float mLength;
	float mMaxSpeed, mMaxSpeedSqrd;
	float mMinSpeed, mMinSpeedSqrd;
	float mFear;
	float mCrowdFactor;

	boolean mIsDead;
	boolean mFollowed;

	Particle(PApplet p) {
		this.p = p;
	}

	Particle(Vec3D pos, Vec3D vel, boolean followed, PApplet p) {
		this.p = p;

		mPos = pos;
		mTailPos = pos;
		mVel = vel;
		mVelNormal = new Vec3D(Vec3D.Y_AXIS);
		mAcc = new Vec3D(Vec3D.ZERO);

		mNeighborPos = new Vec3D(Vec3D.ZERO);
		mNumNeighbors = 0;
		mMaxSpeed = p.random(2.5f, 4.0f);
		mMaxSpeedSqrd = mMaxSpeed * mMaxSpeed;
		mMinSpeed = p.random(1.0f, 1.5f);
		mMinSpeedSqrd = mMinSpeed * mMinSpeed;

		mColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);

		mDecay = 0.99f;
		mRadius = 1.0f;
		mLength = 5.0f;
		mFear = 1.0f;
		mCrowdFactor = 1.0f;

		mIsDead = false;
		mFollowed = followed;
	}

	void pullToCenter(final Vec3D center) {
		Vec3D dirToCenter = mPos.sub(center);
		float distToCenter = dirToCenter.magnitude();
		float distThresh = 200.0f;

		if (distToCenter > distThresh) {
			dirToCenter.normalize();
			float pullStrength = 0.00025f;
			mVel.subSelf(dirToCenter.scale(
					((distToCenter - distThresh) * pullStrength)));
		}
	}

	void update(boolean flatten) {
		mCrowdFactor -= (mCrowdFactor - (1.0f - mNumNeighbors * 0.02f)) * 0.1f;
		mCrowdFactor = PApplet.constrain(mCrowdFactor, 0.5f, 1.0f);

		mFear -= (mFear - 0.0f) * 0.2f;

		if (flatten)
			mAcc.z = 0.0f;

		mVel.addSelf(mAcc);
		mVelNormal = mVel.getNormalized();

		limitSpeed();

		mPos.addSelf(mVel);
		if (flatten)
			mPos.z = 0.0f;

		mTailPos = mPos.sub(mVelNormal.scale(mLength));
		mVel.scaleSelf(mDecay);

		float c = PApplet.min(mNumNeighbors / 50.0f, 1.0f);
		mColor = Color.getHSBColor(1.0f - c, c, c * 0.5f + 0.5f);

		mAcc = new Vec3D(Vec3D.ZERO);
		mNeighborPos = new Vec3D(Vec3D.ZERO);
		mNumNeighbors = 0;
	}

	void limitSpeed() {
		float maxSpeed = mMaxSpeed + mCrowdFactor;
		float maxSpeedSqrd = maxSpeed * maxSpeed;

		float vLengthSqrd = mVel.magSquared();
		if (vLengthSqrd > maxSpeedSqrd) {
			mVel = mVelNormal.scale(maxSpeed);

		} else if (vLengthSqrd < mMinSpeedSqrd) {
			mVel = mVelNormal.scale(mMinSpeed);
		}
		mVel.scaleSelf(1.0f + mFear);
	}

	void draw() {
		p.stroke(mColor.getRGB());
		p.strokeWeight(2);
		Vec3D start = mPos.sub(mVelNormal.scale(mLength));
		Vec3D end = mPos.sub(mVelNormal.scale(mLength * 0.75f));
		p.line(start.x, start.y, start.z, end.x, end.y, end.z);
	}

	void drawTail() {
		/*
		 * TODO: p.fill(mColor.getRGB()); glVertex3fv( mPos ); p.fill(mColor.getRGB());
		 * glVertex3fv( mTailPos );
		 */
	}

	void addNeighborPos(Vec3D pos) {
		mNeighborPos.addSelf(pos);
		mNumNeighbors++;
	}
}
