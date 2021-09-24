/*
  Copyright 2009 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package Sim.antsforage;

import sim.engine.*;
import sim.field.grid.*;
import sim.util.*;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;


public /*strictfp*/ class AntsForage extends SimState {

    private static final long serialVersionUID = 1;

    public static int GRID_HEIGHT = 100;
    public static int GRID_WIDTH = 100;

    public static ArrayList<Int2D> HOME_POS = new ArrayList<Int2D>();
    public static ArrayList<Int2D> FOOD_POS = new ArrayList<Int2D>();
    public static ArrayList<Int2D> OBST_POS = new ArrayList<Int2D>();

    public static Dictionary<Integer, Stoppable> agents_stoppables = new Hashtable<>();                                                     // put it eventually in Ant

    public static final double IMPOSSIBLY_BAD_PHEROMONE = -1;
    public static final double LIKELY_MAX_PHEROMONE = 3;
        
    public static final int HOME = 1;
    public static final int FOOD = 2;

    public static int numAnts = 1000;
    public static double evaporationConstant = 0.999;
    public static double reward = 1.0;
    public static double updateCutDown = 0.9;
    public static double diagonalCutDown = computeDiagonalCutDown();
    public static double computeDiagonalCutDown() { return Math.pow(updateCutDown, Math.sqrt(2)); }
    public static double momentumProbability = 0.8;
    public static double randomActionProbability = 0.1;

    public static final int ALGORITHM_VALUE_ITERATION = 1;
    public static final int ALGORITHM_TEMPORAL_DIFERENCE = 2;
    public static final int ALGORITHM = ALGORITHM_VALUE_ITERATION;


    // some properties
    public int getNumAnts() { return numAnts; }
    public void setNumAnts(int val) {if (val > 0) numAnts = val; }
        
    public double getEvaporationConstant() { return evaporationConstant; }
    public void setEvaporationConstant(double val) {if (val >= 0 && val <= 1.0) evaporationConstant = val; }

    public double getReward() { return reward; }
    public void setReward(double val) {if (val >= 0) reward = val; }

    public double getCutDown() { return updateCutDown; }
    public void setCutDown(double val) {if (val >= 0 && val <= 1.0) updateCutDown = val;  diagonalCutDown = computeDiagonalCutDown(); }
    public Object domCutDown() { return new Interval(0.0, 1.0); }

    public double getMomentumProbability() { return momentumProbability; }
    public void setMomentumProbability(double val) {if (val >= 0 && val <= 1.0) momentumProbability = val; }
    public Object domMomentumProbability() { return new Interval(0.0, 1.0); }

    public double getRandomActionProbability() { return randomActionProbability; }
    public void setRandomActionProbability(double val) {if (val >= 0 && val <= 1.0) randomActionProbability = val; }
    public Object domRandomActionProbability() { return new Interval(0.0, 1.0); }

    public static IntGrid2D sites = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
    public static DoubleGrid2D toFoodGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
    public static DoubleGrid2D toHomeGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
    public static SparseGrid2D buggrid = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
    public static IntGrid2D obstacles = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);

    public AntsForage(long seed) {
        super(seed);
    }
        
    public void start() {
        super.start();  // clear out the schedule

        // make new grids
        sites = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
        toFoodGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
        toHomeGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
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
            buggrid.setObjectLocation(ant, HOME_POS.get((x%HOME_POS.size())).x, HOME_POS.get((x%HOME_POS.size())).y);
            agents_stoppables.put(x, schedule.scheduleRepeating(Schedule.EPOCH + x, 0, ant, 1));
        }

        // Schedule evaporation to happen after the ants move and update
        schedule.scheduleRepeating(Schedule.EPOCH,1, new Steppable() {
            public void step(SimState state) { toFoodGrid.multiply(evaporationConstant); toHomeGrid.multiply(evaporationConstant); }
            }, 1);
    }

    public static void main(String[] args) {
        doLoop(AntsForage.class, args);
        System.exit(0);
    }

}
    
    
    
    
    
