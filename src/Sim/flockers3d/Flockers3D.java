/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Sim.flockers3d;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;
import java.util.HashMap;

public class Flockers3D extends SimState {
    private static final long serialVersionUID = 1;

    public static double width = 400;
    public static double height = 400;
    public static double lenght = 400;
    public static int numFlockers = 1000;
    public static int deadFlockers = 0;
    public static double cohesion = 1.0;
    public static double avoidance = 1.0;
    public static double randomness = 1.0;
    public static double consistency = 1.0;
    public static double momentum = 1.0;
    public static double deadFlockerProbability = 0.2;
    public static double neighborhood = 10;
    public static double jump = 0.7;  // how far do we move in a timestep?
    public static double AVOID_DISTANCE = 10.0;
    public static Continuous3D flockers;                                                                                                    // REMOVED '= new Continuous3D(neighborhood/1.5d,width,height,lenght);'

    public static HashMap<Integer, Stoppable> agents_stoppables = new HashMap<>();                                                          // ADDED Stoppable to kill Flockers

    public static double getCohesion() { return cohesion; }
    public static void setCohesion(double val) { if (val >= 0.0) cohesion = val; }
    public static double getAvoidance() { return avoidance; }
    public static void setAvoidance(double val) { if (val >= 0.0) avoidance = val; }
    public static double getRandomness() { return randomness; }
    public static void setRandomness(double val) { if (val >= 0.0) randomness = val; }
    public static double getConsistency() { return consistency; }
    public static void setConsistency(double val) { if (val >= 0.0) consistency = val; }
    public static double getMomentum() { return momentum; }
    public static void setMomentum(double val) { if (val >= 0.0) momentum = val; }
    public static int getNumFlockers() { return numFlockers; }
    public static void setNumFlockers(int val) { if (val >= 1) numFlockers = val; }
    public static double getWidth() { return width; }
    public static void setWidth(double val) { if (val > 0) width = val; }
    public static double getHeight() { return height; }
    public static void setHeight(double val) { if (val > 0) height = val; }
    public static double getNeighborhood() { return neighborhood; }
    public static void setNeighborhood(int val) { if (val > 0) neighborhood = val; }
    public static double getDeadFlockerProbability() { return deadFlockerProbability; }
    public static void setDeadFlockerProbability(double val) { if (val >= 0.0 && val <= 1.0) deadFlockerProbability = val; }

   
    public static Double3D[] getLocations() {
        if (flockers == null) return new Double3D[0];
        Bag b = flockers.getAllObjects();
        if (b==null) return new Double3D[0];
        Double3D[] locs = new Double3D[b.numObjs];
        for(int i =0; i < b.numObjs; i++)
            locs[i] = flockers.getObjectLocation(b.objs[i]);
        return locs;
        }
    public static Double3D[] getInvertedLocations() {
        if (flockers == null) return new Double3D[0];
        Bag b = flockers.getAllObjects();
        if (b==null) return new Double3D[0];
        Double3D[] locs = new Double3D[b.numObjs];
        for(int i =0; i < b.numObjs; i++)
            {
            locs[i] = flockers.getObjectLocation(b.objs[i]);
            locs[i] = new Double3D(locs[i].y, locs[i].x, locs[i].z);
            }
        return locs;
    }


    /** Creates a Flockers simulation with the given random number seed. */
    public Flockers3D(long seed) {
    	super(seed);
    }
    
    public void start() {
        super.start();

        // set up the flockers field.  It looks like a discretization
        // of about neighborhood / 1.5 is close to optimal for us.  Hmph,
        // that's 16 hash lookups! I would have guessed that
        // neighborhood * 2 (which is about 4 lookups on average)
        // would be optimal.  Go figure.

        // make a bunch of flockers and schedule 'em.  A few will be dead
        for(int x=0; x<numFlockers; x++) {
            Double3D location = new Double3D(random.nextDouble() * width, random.nextDouble() * height, random.nextDouble() * lenght);
            Flocker3D flocker = new Flocker3D(x, location);
            if (random.nextBoolean(deadFlockerProbability)) {flocker.dead = true; deadFlockers++;}
            flockers.setObjectLocation(flocker, location);
            flocker.flockers = flockers;
            flocker.theFlock = this;
            agents_stoppables.put(x, schedule.scheduleRepeating(flocker));                                 // ADDED insertion in Stoppable collection
        }
    }


    public void scheduleAgain(){
        // Schedule flockers
        deadFlockers = 0;
        for(int x=0; x < numFlockers; x++) {
            Flocker3D flocker = (Flocker3D) Flockers3D.flockers.getAllObjects().get(x);
            flocker.loc = new Double3D(random.nextDouble() * Flockers3D.width, random.nextDouble() * Flockers3D.height, random.nextDouble() * Flockers3D.lenght);
            if (random.nextBoolean(deadFlockerProbability)) {flocker.dead = true; deadFlockers++;}
            flockers.setObjectLocation(flocker, flocker.loc);
            agents_stoppables.put(x, schedule.scheduleRepeating(flocker));
        }
    }

    public static void main(String[] args) {
        doLoop(Flockers3D.class, args);
        System.exit(0);
    }
}
