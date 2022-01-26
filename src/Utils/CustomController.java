/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Utils;

import java.lang.reflect.InvocationTargetException;
import sim.display.GUIState;

import javax.swing.*;

public class CustomController {
    GUIState simulation;
    long randomSeed;
    Thread playThread;
    final Object playThreadLock;
    boolean threadShouldStop;
    public static final int PS_STOPPED = 0;
    public static final int PS_PLAYING = 1;
    public static final int PS_PAUSED = 2;
    int playState;
    boolean isClosing;
    final Object isClosingLock;
    boolean incrementSeedOnStop;
    Runnable blocker;

    public GUIState getSimulation() {
        return this.simulation;
    }

    public CustomController(final GUIState simulation) {
        this.randomSeed = simulation.state.seed();
        this.playThreadLock = new Object();
        this.threadShouldStop = false;
        this.playState = 0;
        this.isClosing = false;
        this.isClosingLock = new Object();
        this.incrementSeedOnStop = true;
        this.blocker = new Runnable() {
            public void run() {
            }
        };
        this.simulation = simulation;
    }

    boolean getThreadShouldStop() {
        synchronized(this.playThreadLock) {
            return this.threadShouldStop;
        }
    }

    void setThreadShouldStop(boolean stop) {
        synchronized(this.playThreadLock) {
            this.threadShouldStop = stop;
        }
    }

    void setPlayState(int state) {
        synchronized(this.playThreadLock) {
            this.playState = state;
        }
    }

    public int getPlayState() {
        synchronized(this.playThreadLock) {
            return this.playState;
        }
    }

    void startSimulation() {
        this.simulation.state.setSeed(this.randomSeed);
        this.simulation.start();
    }

    /** @deprecated */
    public void setIncrementSeedOnPlay(boolean val) {
        this.setIncrementSeedOnStop(val);
    }

    /** @deprecated */
    public boolean getIncrementSeedOnPlay() {
        return this.getIncrementSeedOnStop();
    }

    public void setIncrementSeedOnStop(boolean val) {
        this.incrementSeedOnStop = val;
    }

    public boolean getIncrementSeedOnStop() {
        return this.incrementSeedOnStop;
    }

    public synchronized void pressStop() {
        if (this.getPlayState() != PS_STOPPED) {
            this.killPlayThread();
            this.simulation.finish();
            this.setPlayState(PS_STOPPED);
            if (this.getIncrementSeedOnStop()) {
                this.randomSeed = (long)((int)(this.randomSeed + 1L));
            }
        }
    }

    public synchronized void pressPause() {
        this.pressPause(true);
    }

    synchronized void pressPause(boolean shouldStepSimulationIfStopped) {
        if (this.getPlayState() == PS_PLAYING) {
            this.killPlayThread();
            this.setPlayState(PS_PAUSED);
        } else if (this.getPlayState() == PS_PAUSED) {
            if (!this.simulation.step()) {
                this.pressStop();
            }
        } else if (this.getPlayState() == PS_STOPPED) {
            if (shouldStepSimulationIfStopped) {
                if (!this.simulation.step()) {
                    this.pressStop();
                }
            }
            this.setPlayState(PS_PAUSED);
        }
    }

    public synchronized void pressPlay() {
        if (this.getPlayState() == PS_STOPPED) {
            //this.startSimulation();
            this.spawnPlayThread();
            this.setPlayState(PS_PLAYING);
        } else if (this.getPlayState() == PS_PAUSED) {
            this.spawnPlayThread();
            this.setPlayState(PS_PLAYING);
        }
    }

    synchronized void killPlayThread() {
        this.setThreadShouldStop(true);

        try {
            if (this.playThread != null) {
                do {
                    try {
                        synchronized(this.simulation.state.schedule) {
                            this.playThread.interrupt();
                        }
                    } catch (SecurityException var4) {
                    }

                    this.playThread.join(50L);
                } while(this.playThread.isAlive());

                this.playThread = null;
            }
        } catch (InterruptedException var5) {
            System.err.println("WARNING: This should never happen: " + var5);
        }

    }

    synchronized void spawnPlayThread() {
        this.setThreadShouldStop(false);
        Runnable run = new Runnable() {
            public void run() {
                try {
                    if (!Thread.currentThread().isInterrupted() && !CustomController.this.getThreadShouldStop()) {
                        try {
                            CustomController.this.blocker.run();
                            //SwingUtilities.invokeAndWait(CustomController.this.blocker);
                        } catch (Exception var9) {
                            try {
                                Thread.currentThread().interrupt();
                            } catch (SecurityException var8) {
                            }
                        }
                    }

                    CustomController.this.simulation.state.nameThread();
                    boolean result = true;

                    while(!CustomController.this.getThreadShouldStop()) {
                        result = CustomController.this.simulation.step();
                        if (!Thread.currentThread().isInterrupted() && !CustomController.this.getThreadShouldStop()) {
                            try {
                                CustomController.this.blocker.run();
                                //SwingUtilities.invokeAndWait(CustomController.this.blocker);
                            } catch (Exception var5) {
                                try {
                                    Thread.currentThread().interrupt();
                                } catch (SecurityException var4) {
                                }
                            }
                        }

                        if (!result || CustomController.this.getThreadShouldStop()) {
                            break;
                        }

                        if (!result) {
                            Runnable run1 = (new Runnable() {
                                public void run() {
                                    try {
                                        CustomController.this.pressStop();
                                    } catch (Exception var2) {
                                        System.err.println("This should never happen: " + var2);
                                    }

                                }
                            });
                            run1.run();
                        }
                    }
                } catch (Exception var12) {
                    var12.printStackTrace();
                }

            }
        };
        this.playThread = new Thread(run);
        this.playThread.start();
    }

}
