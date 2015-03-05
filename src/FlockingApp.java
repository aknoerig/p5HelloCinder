import processing.core.PApplet;
import processing.core.PMatrix3D;
import toxi.geom.Quaternion;
import toxi.geom.Vec3D;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Toggle;

public class FlockingApp extends PApplet {
	private static final int NUM_INITIAL_PARTICLES = 2000;
	private static final int NUM_INITIAL_PREDATORS = 2;
	private static final int NUM_PARTICLES_TO_SPAWN = 1000;

	// PARAMS
	ControlP5 controlP5;

	// CAMERA
	PMatrix3D currCameraMatrix;
	Quaternion mSceneRotation;
	Vec3D mEye, mCenter, mUp;
	float mCameraDistance;

	ParticleController mParticleController;
	float mZoneRadius;
	float mLowerThresh, mHigherThresh;
	float mAttractStrength, mRepelStrength, mOrientStrength;

	boolean mCentralGravity;
	boolean mFlatten;
	boolean mSaveFrames;
	boolean mIsRenderingPrint;

	boolean paused = false;

	public void setup() {
		size(800, 600, OPENGL);
		frameRate(30);
		sphereDetail(5);
		colorMode(RGB, 1.0f);

		mParticleController = new ParticleController(this);

		mCenter = new Vec3D(width * 0.5f, height * 0.5f, 0.0f);
		mCentralGravity = true;
		mFlatten = false;
		mSaveFrames = false;
		mIsRenderingPrint = false;
		mZoneRadius = 80.0f;
		mLowerThresh = 0.5f;
		mHigherThresh = 0.8f;
		mAttractStrength = 0.004f;
		mRepelStrength = 0.01f;
		mOrientStrength = 0.01f;

		// SETUP CAMERA
		mCameraDistance = 750.0f; 
		mEye = new Vec3D( 0.0f, 0.0f, mCameraDistance ); 
		mCenter = new Vec3D(Vec3D.ZERO); 
		mUp = new Vec3D(Vec3D.Y_AXIS);
		perspective(75.0f, width/height, 5.0f, 5000.0f);
		mSceneRotation = new Quaternion();

		// SETUP PARAMS
		// TODO: fix camera for params gui
//	    controlP5 = new ControlP5(this);
//	    controlP5.addToggle("Center Gravity",mCentralGravity,10,0,30,10).setId(2);
//	    controlP5.addToggle("Flatten",mFlatten,100,0,30,10).setId(3);
//	    controlP5.addSlider("Zone Radius",10.0f,100.0f,mZoneRadius,  10,30,200,10).setId(4);
//	    controlP5.addSlider("Lower Thresh",0.025f,1.0f,mLowerThresh,  10,50,200,10).setId(5);
//	    controlP5.addSlider("Higher Thresh",0.025f,1.0f,mHigherThresh,  10,70,200,10).setId(6);
//	    controlP5.addSlider("Attract Strength",0.001f,0.1f,mAttractStrength,  10,100,200,10).setId(7);
//	    controlP5.addSlider("Repel Strength",0.001f,0.1f,mRepelStrength,  10,120,200,10).setId(8);
//	    controlP5.addSlider("Orient Strength",0.001f,0.1f,mOrientStrength,  10,140,200,10).setId(9);

		// CREATE PARTICLE CONTROLLER
		mParticleController.addParticles(NUM_INITIAL_PARTICLES);
		mParticleController.addPredators(NUM_INITIAL_PREDATORS);
	}

	void update() {
		if (mLowerThresh > mHigherThresh) {
			mHigherThresh = mLowerThresh;
		}
		mParticleController.applyForceToPredators(mZoneRadius, 0.4f, 0.7f);
		mParticleController.applyForceToParticles(mZoneRadius, mLowerThresh,
				mHigherThresh, mAttractStrength, mRepelStrength, mOrientStrength);
		if (mCentralGravity) {
			mParticleController.pullToCenter(mCenter);
		}
		mParticleController.update(mFlatten);
		
		mEye = new Vec3D( 0.0f, 0.0f, mCameraDistance );
		camera(mEye.x, mEye.y, mEye.z, 
				mCenter.x, mCenter.y, mCenter.z, 
				mUp.x, mUp.y, mUp.z);
		rotate(mSceneRotation.w, mSceneRotation.x, mSceneRotation.y, mSceneRotation.z);
	}

	public void draw() {
		update();
		background(0);
		lights();
		fill(1.0f);
		mParticleController.draw();
	}
  		
	public void keyPressed() {
		if (key == 'p') {
			mParticleController.addParticles(NUM_PARTICLES_TO_SPAWN);
		} else if (key == ' ') {
			saveFrame();
		}
	}

	public void controlEvent(ControlEvent e) {
		switch(e.controller().id()) {
	    case(2):
	    	mCentralGravity = ((Toggle)e.controller()).getState();
		  	break;
	    case(3):
	    	mFlatten = ((Toggle)e.controller()).getState();
		  	break;
	    case(4):
	    	mZoneRadius = e.controller().value();
		  	break;
	    case(5):
	    	mLowerThresh = e.controller().value();
			break;
	    case(6):
	    	mHigherThresh = e.controller().value();
			break;
	    case(7):
	    	mAttractStrength = e.controller().value();
		  	break;
	    case(8):
		    mRepelStrength = e.controller().value();
			break;
	    case(9):
		    mOrientStrength = e.controller().value();
			break;
		}
	}

}
