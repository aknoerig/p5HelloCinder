import processing.core.PApplet;
import processing.opengl.*;
import controlP5.*;


public class Birds extends PApplet {
	ControlP5 controlP5;

	// Flock array
	int birdCount = 200;
	Bird[]birds;
	float[]x, y, z;
	float[]rx, ry, rz;
	float birdSizeMin, birdSizeMax;
	float[]spd, rot;
	float spdMin, spdMax;
	float rotMin, rotMax;

	boolean paused = false;

	public void setup() {
	  size(640, 640, OPENGL);
	  noStroke();
	  controlP5 = new ControlP5(this);
	  //                  name, min, max, default, x, y, width, height
	  controlP5.addSlider("birds",0,1000,200,  10,10,200,10).setId(0);
	  //                  name, min, max, defMin, defMax, x, y, width, height
	  controlP5.addRange("birdSize",0f,50f,5f,30f,  10,30,200,10).setId(1);
	  controlP5.addRange("flap speed",0f,5f,0.1f,3.75f,  10,50,200,10).setId(2);
	  controlP5.addRange("rotation",0f,0.2f,0.025f,0.15f,  10,70,200,10).setId(3);
	  recreate();
	}

	void recreate() {
	  createBirds();
	  feedBirds();
	}
	
	void createBirds() {
	  birds = new Bird[birdCount];
	  for (int i = 0; i < birdCount; i++){
		float birdSize = random(birdSizeMin, birdSizeMax);
	    birds[i] = new Bird(
	    	random(-300, 300), random(-300, 300), random(-500, -2500), // pos 
	    	birdSize, birdSize); // size
	  }
	}
	
	void feedBirds() {
	  x = new float[birdCount];
	  y = new float[birdCount];
	  z = new float[birdCount];
	  rx = new float[birdCount];
	  ry = new float[birdCount];
	  rz = new float[birdCount];
	  spd = new float[birdCount];
	  rot = new float[birdCount];
	  for (int i = 0; i < birdCount; i++){
	    x[i] = random(20, 340);
	    y[i] = random(30, 350);
	    z[i] = random(1000, 4800);
	    rx[i] = random(-160, 160);
	    ry[i] = random(-55, 55);
	    rz[i] = random(-20, 20);
	    spd[i] = random(spdMin, spdMax);
	    rot[i] = random(rotMin, rotMax);
	  }
	}


	public void draw() {
	  if (!paused) {
	    background(0);
	    lights();
	    for (int i = 0; i < birdCount; i++){
	      birds[i].setFlight(x[i], y[i], z[i], rx[i], ry[i], rz[i]);
	      birds[i].setWingSpeed(spd[i]);
	      birds[i].setRotSpeed(rot[i]);
	      birds[i].fly();
	    }
	  }
	}

	public void controlEvent(ControlEvent e) {
	  switch(e.controller().id()) {
	    case(0):
	      birdCount = (int)(e.controller().value());
	      recreate();
	      break;
	    case(1):
	      birdSizeMin = e.controller().arrayValue()[0];
	      birdSizeMax = e.controller().arrayValue()[1];
	      recreate();
	      break;
	    case(2):
	      spdMin = e.controller().arrayValue()[0];
	      spdMax = e.controller().arrayValue()[1];
	      feedBirds();
	      break;
	    case(3):
		  rotMin = e.controller().arrayValue()[0];
		  rotMax = e.controller().arrayValue()[1];
		  feedBirds();
		  break;
	  }
	}
	
	void togglePause() {
	  paused = !paused;
	}

	public void keyPressed() {
	  switch (key) {
	    case(' '):
	      togglePause();
	      break;
	  }
	}
	
	
	////////////// B I R D ////////////////////////
	
	
	public class Bird {
		// Properties
		float offsetX, offsetY, offsetZ;
		float w, h;
		final int leftWingTopFill = color(236, 101, 83);
		final int leftWingBottomFill = color(249, 208, 83);
		final int rightWingTopFill = color(132, 26, 55);
		final int rightWingBottomFill = color(245, 171, 90);

		float flapSpeed; // wing flapping speed
		float rotSpeed; // rotation speed
		float radiusX, radiusY, radiusZ; // ??
		float rotX, rotY, rotZ; // bird rotation
		float ang, ang2, ang3, ang4; // motion progress

		// Constructors
		Bird() {
			this(0, 0, 0, 60, 80);
		}

		Bird(float offsetX, float offsetY, float offsetZ, float w, float h) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
			this.h = h;
			this.w = w;
		}

		void setFlight(float radiusX, float radiusY, float radiusZ, float rotX,
				float rotY, float rotZ) {
			this.radiusX = radiusX;
			this.radiusY = radiusY;
			this.radiusZ = radiusZ;

			this.rotX = rotX;
			this.rotY = rotY;
			this.rotZ = rotZ;
		}

		void setWingSpeed(float flapSpeed) {
			this.flapSpeed = flapSpeed;
		}

		void setRotSpeed(float rotSpeed) {
			this.rotSpeed = rotSpeed;
		}

		void fly() {
			pushMatrix();
			float px, py, pz;

			// Flight
			px = sin(radians(ang3)) * radiusX;
			py = cos(radians(ang3)) * radiusY;
			pz = sin(radians(ang4)) * radiusZ;

			translate(width / 2 + offsetX + px, height / 2 + offsetY + py, -700
					+ offsetZ + pz);

			rotateX(sin(radians(ang2)) * rotX);
			rotateY(sin(radians(ang2)) * rotY);
			rotateZ(sin(radians(ang2)) * rotZ);

			// Body
			// fill(bodyFill);
			// box(w/5, h, w/5);

			// Left wing
			pushMatrix();
			rotateY(sin(radians(ang)) * 20);
			// top
			fill(leftWingTopFill);
			beginShape();
			vertex(0, -h / 3);
			vertex(0, -h);
			vertex(w / 2, -h * 2 / 3f);
			vertex(w / 2, 0);
			endShape(CLOSE);
			// bottom
			pushMatrix();
			translate(0, 0, -0.01f);
			fill(leftWingBottomFill);
			beginShape();
			vertex(0, -h / 3);
			vertex(0, -h);
			vertex(w / 2, -h * 2 / 3f);
			vertex(w / 2, 0);
			endShape(CLOSE);
			popMatrix();

			popMatrix();

			// Right wing
			pushMatrix();
			rotateY(sin(radians(ang)) * -20);
			// top
			fill(rightWingTopFill);
			beginShape();
			vertex(-w / 2, 0);
			vertex(-w / 2, -h * 2 / 3f);
			vertex(0, -h);
			vertex(0, -h / 3);
			endShape(CLOSE);
			// bottom
			pushMatrix();
			translate(0, 0, -0.01f);
			fill(rightWingBottomFill);
			beginShape();
			vertex(-w / 2, 0);
			vertex(-w / 2, -h * 2 / 3f);
			vertex(0, -h);
			vertex(0, -h / 3);
			endShape(CLOSE);
			popMatrix();

			popMatrix();

			// Wing flap
			ang += flapSpeed;
			if (ang > 3) {
				flapSpeed *= -1;
			}
			if (ang < -3) {
				flapSpeed *= -1;
			}
			ang2 += rotSpeed; // bird orientation
			ang3 += 1.25; // x-/y-movement within the flock
			ang4 += 0.55; // flock z-axis
			popMatrix();
		}

	}

}


