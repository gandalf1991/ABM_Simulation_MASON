/*
  Copyright 2009 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package Sim.antsforage;

import Utils.FloatGrid2D;
import sim.engine.*;
import sim.field.grid.*;
import sim.util.*;

import java.util.ArrayList;
import java.util.HashMap;


public /*strictfp*/ class AntsForage extends SimState {                                                                                     // MODIFIED to use floats

    private static final long serialVersionUID = 1;

    public static int GRID_HEIGHT = 100;
    public static int GRID_WIDTH = 100;

    public static ArrayList<Int2D> HOME_POS = new ArrayList<Int2D>();                                                                           // MODIFIED HOME_POS/FOOD_POS/OBST_POS to be more flexible
    public static ArrayList<Int2D> FOOD_POS = new ArrayList<Int2D>();
    public static ArrayList<Int2D> OBST_POS = new ArrayList<Int2D>();

    public static HashMap<Integer, Stoppable> agents_stoppables = new HashMap<>();                                                          // ADDED Stoppable to kill Ants

    public static final float IMPOSSIBLY_BAD_PHEROMONE = -1;
    public static final float LIKELY_MAX_PHEROMONE = 3;
        
    public static final int HOME = 1;
    public static final int FOOD = 2;

    public static int numAnts = 1000;
    public static float evaporationConstant = 0.999f;
    public static float reward = 1.0f;
    public static float updateCutDown = 0.9f;
    public static float diagonalCutDown = computeDiagonalCutDown();
    public static float computeDiagonalCutDown() { return ((Number)Math.pow(updateCutDown, Math.sqrt(2))).floatValue(); }
    public static float momentumProbability = 0.8f;
    public static float randomActionProbability = 0.1f;

    public static final int ALGORITHM_VALUE_ITERATION = 1;
    public static final int ALGORITHM_TEMPORAL_DIFERENCE = 2;
    public static final int ALGORITHM = ALGORITHM_VALUE_ITERATION;


    // some properties
    public int getNumAnts() { return numAnts; }
    public void setNumAnts(int val) {if (val > 0) numAnts = val; }
        
    public double getEvaporationConstant() { return evaporationConstant; }
    public void setEvaporationConstant(float val) {if (val >= 0 && val <= 1.0) evaporationConstant = val; }

    public double getReward() { return reward; }
    public void setReward(float val) {if (val >= 0) reward = val; }

    public double getCutDown() { return updateCutDown; }
    public void setCutDown(float val) {if (val >= 0 && val <= 1.0) updateCutDown = val;  diagonalCutDown = computeDiagonalCutDown(); }
    public Object domCutDown() { return new Interval(0.0, 1.0); }

    public double getMomentumProbability() { return momentumProbability; }
    public void setMomentumProbability(float val) {if (val >= 0 && val <= 1.0) momentumProbability = val; }
    public Object domMomentumProbability() { return new Interval(0.0, 1.0); }

    public double getRandomActionProbability() { return randomActionProbability; }
    public void setRandomActionProbability(float val) {if (val >= 0 && val <= 1.0) randomActionProbability = val; }
    public Object domRandomActionProbability() { return new Interval(0.0, 1.0); }

    public static IntGrid2D sites = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
    public static FloatGrid2D toFoodGrid = new FloatGrid2D(GRID_WIDTH, GRID_HEIGHT,0);                                                // MODIFIED to FloatGrid2D
    public static FloatGrid2D toHomeGrid = new FloatGrid2D(GRID_WIDTH, GRID_HEIGHT,0);                                                // MODIFIED to FloatGrid2D
    public static SparseGrid2D buggrid = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
    public static IntGrid2D obstacles = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);

    public AntsForage(long seed) {
        super(seed);
    }
        
    public void start() {
        super.start();  // clear out the schedule

        // make new grids
        sites = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
        toFoodGrid = new FloatGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
        toHomeGrid = new FloatGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
        buggrid = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
        obstacles = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT, 0);

        // initialize the grid with the home and food sites
        for (Int2D p : HOME_POS) {
            sites.field[p.x][p.y] = HOME;
        }
        for(Int2D p : FOOD_POS){
            sites.field[p.x][p.y] = FOOD;
        }
        // initialize ants
        for(int x=0; x < numAnts; x++) {
            Ant ant = new Ant(x, reward);
            buggrid.setObjectLocation(ant, HOME_POS.get((x%HOME_POS.size())).x, HOME_POS.get((x%HOME_POS.size())).y);                                 // MODIFIED spawn logic to be compatible with multiple homes
            ant.last = buggrid.getObjectLocation(ant);
            agents_stoppables.put(x, schedule.scheduleRepeating(Schedule.EPOCH + x, 0, ant, 1));                                 // ADDED insertion in Stoppable collection
        }
        // Schedule evaporation to happen after the ants move and update
        schedule.scheduleRepeating(Schedule.EPOCH,1, new Steppable() {
            public void step(SimState state) { toFoodGrid.multiply(evaporationConstant); toHomeGrid.multiply(evaporationConstant);}
        }, 1);
    }

    public void scheduleAgain(){
        // Schedule ants
        for(int x=0; x < numAnts; x++) {
            Ant ant = (Ant) buggrid.allObjects.get(x);
            buggrid.setObjectLocation(ant, HOME_POS.get((x%HOME_POS.size())).x, HOME_POS.get((x%HOME_POS.size())).y);
            ant.last = buggrid.getObjectLocation(ant);
            ant.reward = reward;
            ant.hasFoodItem = false;
            agents_stoppables.put(x, schedule.scheduleRepeating(Schedule.EPOCH + x, 0, (Ant)buggrid.allObjects.get(x), 1));
        }
        // Schedule evaporation to happen after the ants move and update
        schedule.scheduleRepeating(Schedule.EPOCH,1, new Steppable() {
            public void step(SimState state) { toFoodGrid.multiply(evaporationConstant); toHomeGrid.multiply(evaporationConstant);}
        }, 1);
    }

    public static void main(String[] args) {
        doLoop(AntsForage.class, args);
        System.exit(0);
    }

}
    
    
    
    
    
