import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import toxi.geom.Vec3D;
import toxi.math.noise.PerlinNoise;

public class ParticleController {
	PApplet p;

	PerlinNoise mPerlin;

	ArrayList<Particle> mParticles;
	ArrayList<Predator> mPredators;
	Vec3D mParticleCentroid;
	int mNumParticles;

	ParticleController(PApplet p) {
		this.p = p;
		mPerlin = new PerlinNoise();
		mParticles = new ArrayList<Particle>();
		mPredators = new ArrayList<Predator>();
	}

	void applyForceToParticles(float zoneRadius, float lowerThresh,
			float higherThresh, float attractStrength, float repelStrength,
			float alignStrength) {
		float twoPI = PConstants.PI * 2.0f;
		mParticleCentroid = new Vec3D(Vec3D.ZERO);
		mNumParticles = mParticles.size();

		for (int i = 0; i < mParticles.size(); i++) {
			Particle p1 = mParticles.get(i);

			for (int j = i+1; j < mParticles.size(); j++) {
				Particle p2 = mParticles.get(j);
				
				Vec3D dir = p1.mPos.sub(p2.mPos);
				float distSqrd = dir.magSquared();
				float zoneRadiusSqrd = zoneRadius * p1.mCrowdFactor
						* zoneRadius * p2.mCrowdFactor;

				if (distSqrd < zoneRadiusSqrd) { // Neighbor is in the zone
					float per = distSqrd / zoneRadiusSqrd;
					p1.addNeighborPos(p2.mPos);
					p2.addNeighborPos(p1.mPos);

					// Separation
					if (per < lowerThresh) {
						float F = (lowerThresh / per - 1.0f) * repelStrength;
						dir.normalize();
						dir.scaleSelf(F);

						p1.mAcc.addSelf(dir);
						p2.mAcc.subSelf(dir);
					} 
					// Alignment
					else if (per < higherThresh) {
						float threshDelta = higherThresh - lowerThresh;
						float adjPer = (per - lowerThresh) / threshDelta;
						float F = (1.0f - (PApplet.cos(adjPer * twoPI) * -0.5f + 0.5f))
								* alignStrength;

						p1.mAcc.addSelf(p2.mVelNormal.scale(F));
						p2.mAcc.addSelf(p1.mVelNormal.scale(F));

					} 
					// Cohesion (prep)
					else { 
						float threshDelta = 1.0f - higherThresh;
						float adjPer = (per - higherThresh) / threshDelta;
						float F = (1.0f - (PApplet.cos(adjPer * twoPI) * -0.5f + 0.5f))
								* attractStrength;

						dir.normalize();
						dir.scaleSelf(F);

						p1.mAcc.subSelf(dir);
						p2.mAcc.addSelf(dir);
					}

				}
			}

			mParticleCentroid.addSelf(p1.mPos);
			/*
			 * if( p1.mNumNeighbors > 0 ){ // Cohesion Vec3D neighborAveragePos
			 * = ( p1.mNeighborPos/(float)p1.mNumNeighbors ); p1.mAcc += (
			 * neighborAveragePos - p1.mPos ) * attractStrength; }
			 */

			// ADD PERLIN NOISE INFLUENCE
			float scale = 0.002f;
			float multi = 0.01f;
			// TODO: check if this really creates a good noise
			float perlinX = mPerlin.noise(p1.mPos.x * scale) * multi; 
			float perlinY = mPerlin.noise(p1.mPos.y * scale) * multi; 
			float perlinZ = mPerlin.noise(p1.mPos.z * scale) * multi; 
			p1.mAcc.addSelf(new	Vec3D(perlinX, perlinY, perlinZ));
			

			// CHECK WHETHER THERE IS ANY PARTICLE/PREDATOR INTERACTION
			float eatDistSqrd = 50.0f;
			float predatorZoneRadiusSqrd = zoneRadius * zoneRadius * 5.0f;
			for (Predator predator : mPredators) {

				Vec3D dir = p1.mPos.sub(predator.mPos[0]);
				float distSqrd = dir.magSquared();

				if (distSqrd < predatorZoneRadiusSqrd) {
					if (distSqrd > eatDistSqrd) {
						float F = (predatorZoneRadiusSqrd / distSqrd - 1.0f) * 0.1f;
						p1.mFear += F * 0.1f;
						dir = dir.getNormalized().scale(F);
						p1.mAcc.addSelf(dir);
						if (predator.mIsHungry)
							predator.mAcc.addSelf(dir.scale(0.04f * predator.mHunger));
					} else {
						p1.mIsDead = true;
						predator.mHunger = 0.0f;
						predator.mIsHungry = false;
					}
				}
			}

			
		}
		mParticleCentroid.scaleSelf(1 / (float) mNumParticles);
	}

	void applyForceToPredators(float zoneRadius, float lowerThresh,
			float higherThresh) {
		float twoPI = PConstants.PI * 2.0f;

		for (int i = 0; i < mPredators.size(); i++) {
			Predator P1 = mPredators.get(i);

			for (int j = i+1; j < mPredators.size(); j++) {
				Predator P2 = mPredators.get(j);
				
				Vec3D dir = P1.mPos[0].sub(P2.mPos[0]);
				float distSqrd = dir.magSquared();
				float zoneRadiusSqrd = zoneRadius * zoneRadius * 4.0f;

				if (distSqrd < zoneRadiusSqrd) { // Neighbor is in the zone
					float per = distSqrd / zoneRadiusSqrd;
					if (per < lowerThresh) { // Separation
						float F = (lowerThresh / per - 1.0f) * 0.01f;
						dir.normalize();
						dir.scale(F);

						P1.mAcc.addSelf(dir);
						P2.mAcc.subSelf(dir);
					} else if (per < higherThresh) { // Alignment
						float threshDelta = higherThresh - lowerThresh;
						float adjPer = (per - lowerThresh) / threshDelta;
						float F = (1.0f - PApplet.cos(adjPer * twoPI) * -0.5f + 0.5f) * 0.3f;

						P1.mAcc.addSelf(P2.mVelNormal.scale(F));
						P2.mAcc.addSelf(P1.mVelNormal.scale(F));

					} else { // Cohesion
						float threshDelta = 1.0f - higherThresh;
						float adjPer = (per - higherThresh) / threshDelta;
						float F = (1.0f - (PApplet.cos(adjPer * twoPI) * -0.5f + 0.5f)) * 0.1f;

						dir.normalize();
						dir.scaleSelf(F);

						P1.mAcc.subSelf(dir);
						P2.mAcc.addSelf(dir);
					}
				}
			}
			
		}
	}

	void pullToCenter(final Vec3D center) {
		for (Particle p1 : mParticles) {
			p1.pullToCenter(center);
		}

		for (Predator p1 : mPredators) {
			p1.pullToCenter(center);
		}
	}

	void update(boolean flatten) {
		ArrayList<Particle> removeParticles = new ArrayList<Particle>();
		for (Particle p1 : mParticles) {
			if (p1.mIsDead) {
				removeParticles.add(p1);
			} else {
				p1.update(flatten);
			}
		}
		for (Particle p1 : removeParticles) {
			mParticles.remove(p1);
		}

		for (Predator p1 : mPredators) {
			p1.update(flatten);
		}
	}

	void draw() {
		// DRAW PREDATOR ARROWS
		for (Predator p1 : mPredators) {
			float hungerColor = 1.0f - p1.mHunger;
			p.stroke(1.0f, hungerColor, hungerColor, 1.0f);
			p1.draw();
		}

		// DRAW PARTICLE ARROWS
		for (Particle p1 : mParticles) {
			p1.draw();
		}
	}

	void addPredators(int amt) {
		for (int i = 0; i < amt; i++) {
			Vec3D pos = Vec3D.randomVector().scale(p.random(500.0f, 750.0f));
			Vec3D vel = Vec3D.randomVector();
			mPredators.add(new Predator(pos, vel, p));
		}
	}

	void addParticles(int amt) {
		for (int i = 0; i < amt; i++) {
			Vec3D pos = Vec3D.randomVector().scale(p.random(100.0f, 200.0f));
			Vec3D vel = Vec3D.randomVector();

			boolean followed = false;
			if (mParticles.size() == 0)
				followed = true;

			mParticles.add(new Particle(pos, vel, followed, p));
		}
	}

	void removeParticles(int amt) {
		for (int i = 0; i < amt; i++) {
			mParticles.remove(mParticles.size() - 1);
		}
	}

	Vec3D getPos() {
		return mParticles.iterator().next().mPos;
	}

}
