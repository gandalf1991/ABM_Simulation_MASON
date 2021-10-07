package Utils;

import sim.field.grid.AbstractGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.DoubleBag;
import sim.util.Int2D;
import sim.util.IntBag;
import sim.util.LocationLog;

public class FloatGrid2D extends AbstractGrid2D {
    private static final long serialVersionUID = 1L;
    public float[][] field;

    public float[][] getField() {
        return this.field;
    }

    public FloatGrid2D(int width, int height) {
        this.width = width;
        this.height = height;
        this.field = new float[width][height];
    }

    public FloatGrid2D(int width, int height, float initialValue) {
        this(width, height);
        this.setTo(initialValue);
    }

    public FloatGrid2D(FloatGrid2D values) {
        this.setTo(values);
    }

    public FloatGrid2D(float[][] values) {
        this.setTo(values);
    }

    public final void set(int x, int y, float val) {
        this.field[x][y] = val;
    }

    public final float get(int x, int y) {
        return this.field[x][y];
    }

    public final FloatGrid2D setTo(float thisMuch) {
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] = thisMuch;
            }
        }
        return this;
    }

    public FloatGrid2D setTo(float[][] field) {
        if (field == null) {
            throw new RuntimeException("DoubleGrid2D set to null field.");
        } else {
            int w = field.length;
            int h = 0;
            if (w != 0) {
                h = field[0].length;
            }

            int i;
            for(i = 0; i < w; ++i) {
                if (field[i].length != h) {
                    throw new RuntimeException("DoubleGrid2D initialized with a non-rectangular field.");
                }
            }

            this.width = w;
            this.height = h;
            this.field = new float[w][h];

            for(i = 0; i < w; ++i) {
                this.field[i] = (float[])((float[])field[i].clone());
            }

            return this;
        }
    }

    public final FloatGrid2D setTo(FloatGrid2D values) {
        int width;
        if (this.width == values.width && this.height == values.height) {
            for(width = 0; width < this.width; ++width) {
                System.arraycopy(values.field[width], 0, this.field[width], 0, this.height);
            }
        } else {
            width = this.width = values.width;
            this.height = values.height;
            this.field = new float[width][];

            for(int x = 0; x < width; ++x) {
                this.field[x] = (float[])((float[])values.field[x].clone());
            }
        }

        return this;
    }

    public final float[] toArray() {
        float[][] field = this.field;
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;
        float[] vals = new float[width * height];
        int i = 0;

        for(int x = 0; x < width; ++x) {
            fieldx = field[x];

            for(int y = 0; y < height; ++y) {
                vals[i++] = fieldx[y];
            }
        }

        return vals;
    }

    public final float max() {
        float max = -1.0f / 0.0f;
        int width = this.width;
        int height = this.height;
        float[] fieldx = null;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                if (max < fieldx[y]) {
                    max = fieldx[y];
                }
            }
        }

        return max;
    }

    public final float min() {
        float min = 1.0f / 0.0f;
        int width = this.width;
        int height = this.height;
        float[] fieldx = null;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                if (min > fieldx[y]) {
                    min = fieldx[y];
                }
            }
        }

        return min;
    }

    public final float mean() {
        long count = 0L;
        float mean = 0.0f;
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                mean += fieldx[y];
                ++count;
            }
        }

        return count == 0L ? 0.0f : mean / (float)count;
    }

    public final FloatGrid2D upperBound(float toNoMoreThanThisMuch) {
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                if (fieldx[y] > toNoMoreThanThisMuch) {
                    fieldx[y] = toNoMoreThanThisMuch;
                }
            }
        }

        return this;
    }

    public final FloatGrid2D lowerBound(float toNoLowerThanThisMuch) {
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                if (fieldx[y] < toNoLowerThanThisMuch) {
                    fieldx[y] = toNoLowerThanThisMuch;
                }
            }
        }

        return this;
    }

    public final FloatGrid2D add(float withThisMuch) {
        int width = this.width;
        int height = this.height;
        if (withThisMuch == 0.0D) {
            return this;
        } else {
            float[] fieldx = null;

            for(int x = 0; x < width; ++x) {
                fieldx = this.field[x];

                for(int y = 0; y < height; ++y) {
                    fieldx[y] += withThisMuch;
                }
            }

            return this;
        }
    }

    public final FloatGrid2D add(IntGrid2D withThis) {
        this.checkBounds(withThis);
        int[][] otherField = withThis.field;
        float[] fieldx = null;
        int[] ofieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];
            ofieldx = otherField[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] += (float)ofieldx[y];
            }
        }

        return this;
    }

    public final FloatGrid2D add(FloatGrid2D withThis) {
        this.checkBounds(withThis);
        float[][] otherField = withThis.field;
        float[] fieldx = null;
        float[] ofieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];
            ofieldx = otherField[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] += ofieldx[y];
            }
        }

        return this;
    }

    public final FloatGrid2D multiply(float byThisMuch) {
        if (byThisMuch == 1.0D) {
            return this;
        } else {
            float[] fieldx = null;
            int width = this.width;
            int height = this.height;

            for(int x = 0; x < width; ++x) {
                fieldx = this.field[x];

                for(int y = 0; y < height; ++y) {
                    fieldx[y] *= byThisMuch;
                }
            }

            return this;
        }
    }

    public final FloatGrid2D multiply(IntGrid2D withThis) {
        this.checkBounds(withThis);
        int[][] otherField = withThis.field;
        float[] fieldx = null;
        int[] ofieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];
            ofieldx = otherField[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] *= (float)ofieldx[y];
            }
        }

        return this;
    }

    public final FloatGrid2D multiply(FloatGrid2D withThis) {
        this.checkBounds(withThis);
        float[][] otherField = withThis.field;
        float[] fieldx = null;
        float[] ofieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];
            ofieldx = otherField[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] *= ofieldx[y];
            }
        }

        return this;
    }

    public final FloatGrid2D floor() {
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] = Utilities.floor(fieldx[y]);
            }
        }

        return this;
    }

    public final FloatGrid2D ceiling() {
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] = Utilities.ceil(fieldx[y]);
            }
        }

        return this;
    }

    public final FloatGrid2D truncate() {
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] = (float)((int)fieldx[y]);
            }
        }

        return this;
    }

    public final FloatGrid2D rint() {
        float[] fieldx = null;
        int width = this.width;
        int height = this.height;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                fieldx[y] = Utilities.rint(fieldx[y]);
            }
        }

        return this;
    }

    public final void replaceAll(float from, float to) {
        int width = this.width;
        int height = this.height;
        float[] fieldx = null;

        for(int x = 0; x < width; ++x) {
            fieldx = this.field[x];

            for(int y = 0; y < height; ++y) {
                if (fieldx[y] == from) {
                    fieldx[y] = to;
                }
            }
        }

    }

    /** @deprecated */
    public void getNeighborsMaxDistance(int x, int y, int dist, boolean toroidal, DoubleBag result, IntBag xPos, IntBag yPos) {
        this.getMooreNeighbors(x, y, dist, toroidal ? 2 : 0, true, result, xPos, yPos);
    }

    public DoubleBag getMooreNeighbors(int x, int y, int dist, int mode, boolean includeOrigin, DoubleBag result, IntBag xPos, IntBag yPos) {
        if (xPos == null) {
            xPos = new IntBag();
        }

        if (yPos == null) {
            yPos = new IntBag();
        }

        this.getMooreLocations(x, y, dist, mode, includeOrigin, xPos, yPos);
        return this.getObjectsAtLocations(xPos, yPos, result);
    }

    /** @deprecated */
    public void getNeighborsHamiltonianDistance(int x, int y, int dist, boolean toroidal, DoubleBag result, IntBag xPos, IntBag yPos) {
        this.getVonNeumannNeighbors(x, y, dist, toroidal ? 2 : 0, true, result, xPos, yPos);
    }

    public DoubleBag getVonNeumannNeighbors(int x, int y, int dist, int mode, boolean includeOrigin, DoubleBag result, IntBag xPos, IntBag yPos) {
        if (xPos == null) {
            xPos = new IntBag();
        }

        if (yPos == null) {
            yPos = new IntBag();
        }

        this.getVonNeumannLocations(x, y, dist, mode, includeOrigin, xPos, yPos);
        return this.getObjectsAtLocations(xPos, yPos, result);
    }

    /** @deprecated */
    public void getNeighborsHexagonalDistance(int x, int y, int dist, boolean toroidal, DoubleBag result, IntBag xPos, IntBag yPos) {
        this.getHexagonalNeighbors(x, y, dist, toroidal ? 2 : 0, true, result, xPos, yPos);
    }

    public DoubleBag getHexagonalNeighbors(int x, int y, int dist, int mode, boolean includeOrigin, DoubleBag result, IntBag xPos, IntBag yPos) {
        if (xPos == null) {
            xPos = new IntBag();
        }

        if (yPos == null) {
            yPos = new IntBag();
        }

        this.getHexagonalLocations(x, y, dist, mode, includeOrigin, xPos, yPos);
        return this.getObjectsAtLocations(xPos, yPos, result);
    }

    public DoubleBag getRadialNeighbors(int x, int y, int dist, int mode, boolean includeOrigin, DoubleBag result, IntBag xPos, IntBag yPos) {
        return this.getRadialNeighbors(x, y, dist, mode, includeOrigin, 1026, true, result, xPos, yPos);
    }

    public DoubleBag getRadialNeighbors(int x, int y, int dist, int mode, boolean includeOrigin, int measurementRule, boolean closed, DoubleBag result, IntBag xPos, IntBag yPos) {
        if (xPos == null) {
            xPos = new IntBag();
        }

        if (yPos == null) {
            yPos = new IntBag();
        }

        this.getRadialLocations(x, y, (float)dist, mode, includeOrigin, measurementRule, closed, xPos, yPos);
        return this.getObjectsAtLocations(xPos, yPos, result);
    }

    void reduceObjectsAtLocations(IntBag xPos, IntBag yPos, DoubleBag result) {
        if (result == null) {
            result = new DoubleBag();
        } else {
            result.clear();
        }

        for(int i = 0; i < xPos.numObjs; ++i) {
            assert LocationLog.it(this, new Int2D(xPos.objs[i], yPos.objs[i]));

            float val = this.field[xPos.objs[i]][yPos.objs[i]];
            result.add(val);
        }

    }

    DoubleBag getObjectsAtLocations(IntBag xPos, IntBag yPos, DoubleBag result) {
        if (result == null) {
            result = new DoubleBag();
        } else {
            result.clear();
        }

        for(int i = 0; i < xPos.numObjs; ++i) {
            assert LocationLog.it(this, new Int2D(xPos.objs[i], yPos.objs[i]));

            float val = this.field[xPos.objs[i]][yPos.objs[i]];
            result.add(val);
        }

        return result;
    }

    public DoubleBag getMooreNeighbors(int x, int y, int dist, int mode, boolean includeOrigin) {
        return this.getMooreNeighbors(x, y, dist, mode, includeOrigin, (DoubleBag)null, (IntBag)null, (IntBag)null);
    }

    public DoubleBag getVonNeumannNeighbors(int x, int y, int dist, int mode, boolean includeOrigin) {
        return this.getVonNeumannNeighbors(x, y, dist, mode, includeOrigin, (DoubleBag)null, (IntBag)null, (IntBag)null);
    }

    public DoubleBag getHexagonalNeighbors(int x, int y, int dist, int mode, boolean includeOrigin) {
        return this.getHexagonalNeighbors(x, y, dist, mode, includeOrigin, (DoubleBag)null, (IntBag)null, (IntBag)null);
    }

    public DoubleBag getRadialNeighbors(int x, int y, int dist, int mode, boolean includeOrigin) {
        return this.getRadialNeighbors(x, y, dist, mode, includeOrigin, (DoubleBag)null, (IntBag)null, (IntBag)null);
    }
}
