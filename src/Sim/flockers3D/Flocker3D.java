/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.flockers3D;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal3d.SimplePortrayal3D;
import sim.util.*;
import javax.vecmath.Quat4d;

import ec.util.*;


public class Flocker3D extends SimplePortrayal3D implements Steppable {

	private static final long serialVersionUID = 1;

	public int ID;
	public Double3D loc = new Double3D(0,0,0);
	public Double3D lastd = new Double3D(0,0,0);
	public Quat4d orientation = new Quat4d();
	public Continuous3D flockers;
	public Flockers3D theFlock;
	public boolean dead = false;

	public Flocker3D(int id, Double3D location) {ID = id; loc = location;}

	public int getID() {
		return ID;
	}
	public void setID(int ID) {
		this.ID = ID;
	}
	public boolean isDead() { return dead; }
	public void setDead(boolean val) { dead = val; }
	public Quat4d getOrientation3D() { return orientation3D(); }
	public void setOrientation3D(Quat4d val){
		orientation = val;
	}
	public Quat4d orientation3D() {

		Double3D forwardVector = loc.add(lastd.negate()).normalize();
		Double3D forward = new Double3D(0,0,1);
		Double3D up = new Double3D(0,1,0);
		Double dot = forward.dot(forwardVector);

		if(Math.abs(dot - (-1.0f)) < 0.000001f) {
			return new Quat4d(up.x,up.y,up.z,Math.PI);
		}
		if(Math.abs(dot - (1.0f)) < 0.000001f) {
			return new Quat4d();
		}

		Double rotAngle = Math.acos(dot);
		Double3D rotAxis = crossProduct(forward, forwardVector).normalize();

		return createFromAxisAngle(rotAxis, rotAngle);
	}
	public Bag getNeighbors() {
		return flockers.getNeighborsExactlyWithinDistance(loc, Flockers3D.neighborhood, true);
	}

	private Double3D crossProduct(Double3D v1, Double3D v2) {

		double var3 = v1.y * v2.z - v1.z * v2.y;
		double var5 = v2.x * v1.z - v2.z * v1.x;

		return new Double3D(var3, var5, v1.x * v2.y - v1.y * v2.x);

	}
	private Quat4d createFromAxisAngle(Double3D axis, Double angle) {

		Double halfAngle = angle * .5f;
		Double s = Math.sin(halfAngle);

		return new Quat4d(axis.x *s, axis.y *s, axis.z * s, Math.cos(halfAngle));
	}

	public Double3D momentum() {
		return lastd;
	}
	public Double3D consistency(Bag b, Continuous3D flockers) {
		if (b==null || b.numObjs == 0) return new Double3D(0,0,0);

		double x = 0;
		double y = 0;
		double z = 0;
		int i = 0;
		int count = 0;
		for(i=0;i<b.numObjs;i++) {
			Flocker3D other = (Flocker3D)(b.objs[i]);
			if (!other.dead) {
				Double3D m = ((Flocker3D)b.objs[i]).momentum();
				count++;
				x += m.x;
				y += m.y;
				z += m.z;
			}
		}
		if (count > 0) { x /= count; y /= count; z /= count;}
		return new Double3D(x,y,z);
	}
	public Double3D cohesion(Bag b, Continuous3D flockers) {
		if (b==null || b.numObjs == 0) return new Double3D(0,0,0);

		double x = 0;
		double y = 0;
		double z = 0;

		int count = 0;
		int i = 0;
		for(i=0;i<b.numObjs;i++)
		{
			Flocker3D other = (Flocker3D)(b.objs[i]);
			if (!other.dead)
			{
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				double dz = flockers.tdz(loc.z,other.loc.z);
				count++;
				x += dx;
				y += dy;
				z += dz;
			}
		}
		if (count > 0) { x /= count; y /= count; z /= count;}
		return new Double3D(-x/10,-y/10,-z/10);
	}
	public Double3D avoidance(Bag b, Continuous3D flockers) {
		if (b==null || b.numObjs == 0) return new Double3D(0,0,0);
		double x = 0;
		double y = 0;
		double z = 0;

		int i=0;
		int count = 0;

		for(i=0;i<b.numObjs;i++)
		{
			Flocker3D other = (Flocker3D)(b.objs[i]);
			if (other != this )
			{
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				double dz = flockers.tdz(loc.z,other.loc.z);
				double lensquared = dx*dx+dy*dy+dz*dz;
				count++;
				x += dx/(lensquared*lensquared + 1);
				y += dy/(lensquared*lensquared + 1);
				z += dz/(lensquared*lensquared + 1);
			}
		}
		if (count > 0) { x /= count; y /= count; z /= count;}

		return new Double3D(((int)flockers.width)*x, ((int)flockers.height)*y, ((int)flockers.length)*z);
	}
	public Double3D randomness(MersenneTwisterFast r) {
		double x = r.nextDouble() * 2 - 1.0;
		double y = r.nextDouble() * 2 - 1.0;
		double z = r.nextDouble() * 2 - 1.0;
		double l = Math.sqrt(x * x + y * y + z * z);
		return new Double3D(0.05*x/l,0.05*y/l,0.05*z/l);
	}
	public Double3D avoidWalls(Bag b, Continuous3D flockers) {
		// Create collection of walls to avoid 2 * dim
		// Calc dist from each wall
		// Then it's done

		Double3D away = new Double3D( 0, 0, 0 );
		Double[] distFromAxis = new Double[3];

		for( int i = 0 ; i < 3 ; i++ ) {
			distFromAxis[i] = Math.sqrt(Math.pow(this.loc.x,2) + Math.pow(this.loc.y,2) + Math.pow(this.loc.z,2));
			if( distFromAxis[i] <= Flockers3D.AVOID_DISTANCE)
			{
				Double3D temp = loc.subtract( new Double3D());
				temp = temp.normalize();
				away = away.add( temp );
			}
		}
		return away.normalize();
	}

	public void step(SimState state) {

		final Flockers3D flock = (Flockers3D)state;
		loc = Flockers3D.flockers.getObjectLocation(this);

		if (dead) return;

		Bag b = getNeighbors();

		Double3D avoid = avoidance(b, Flockers3D.flockers);
		Double3D cohe = cohesion(b, Flockers3D.flockers);
		Double3D rand = randomness(flock.random);
		Double3D cons = consistency(b, Flockers3D.flockers);
		Double3D mome = momentum();

		double dx = Flockers3D.cohesion * cohe.x + Flockers3D.avoidance * avoid.x + Flockers3D.consistency * cons.x + Flockers3D.randomness * rand.x + Flockers3D.momentum * mome.x;
		double dy = Flockers3D.cohesion * cohe.y + Flockers3D.avoidance * avoid.y + Flockers3D.consistency * cons.y + Flockers3D.randomness * rand.y + Flockers3D.momentum * mome.y;
		double dz = Flockers3D.cohesion * cohe.z + Flockers3D.avoidance * avoid.z + Flockers3D.consistency * cons.z + Flockers3D.randomness * rand.z + Flockers3D.momentum * mome.z;

		// renormalize to the given step size
		double dis = Math.sqrt(dx*dx+dy*dy+dz*dz);
		if (dis>0) {
			dx = dx / dis * Flockers3D.jump;
			dy = dy / dis * Flockers3D.jump;
			dz = dz / dis * Flockers3D.jump;
		}

		if (loc.x + dx > Flockers3D.width || loc.x + dx < 0) {
			dx = -dx;
		}
		if (loc.y + dy > Flockers3D.lenght || loc.y + dy < 0) {
			dy = -dy;
		}
		if (loc.z + dz > Flockers3D.height || loc.z + dz < 0) {
			dz = -dz;
		}

		lastd = new Double3D(dx,dy,dz);
		loc = new Double3D(Flockers3D.flockers.stx(loc.x + dx), Flockers3D.flockers.sty(loc.y + dy), Flockers3D.flockers.stz(loc.z + dz));
		Flockers3D.flockers.setObjectLocation(this, loc);
	}
}


