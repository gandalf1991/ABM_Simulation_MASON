package Utils;

import sim.util.Int2D;
import sim.util.MutableDouble2D;
import sim.util.MutableInt2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;


public class Float2D implements Serializable {
    private static final long serialVersionUID = 1L;
    public final float x;
    public final float y;
    static final float infinity = 1.0f / 0.0f;

    public Float2D() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Float2D(Int2D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
    }

    public Float2D(MutableInt2D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
    }

    public Float2D(MutableDouble2D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
    }

    public Float2D(Point p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
    }

    public Float2D(Point2D.Double p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
    }

    public Float2D(Point2D.Float p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
    }

    public Float2D(Point2D p) {
        this.x = (float)p.getX();
        this.y = (float)p.getY();
    }

    public Float2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public final float getX() {
        return this.x;
    }

    public final float getY() {
        return this.y;
    }

    public String toString() {
        return "Float2D[" + this.x + "," + this.y + "]";
    }

    public String toCoordinates() {
        return "(" + this.x + ", " + this.y + ")";
    }

    public Point2D.Double toPoint2D() {
        return new Point2D.Double(this.x, this.y);
    }

    public final int hashCode() {
        float x = this.x;
        float y = this.y;
        if (x == -0.0D) {
            x = 0.0f;
        }

        if (y == -0.0D) {
            y = 0.0f;
        }

        if ((float)((int)x) == x && (float)((int)y) == y) {
            int y_ = (int)y;
            int x_ = (int)x;
            y_ += ~(y_ << 15);
            y_ ^= y_ >>> 10;
            y_ += y_ << 3;
            y_ ^= y_ >>> 6;
            y_ += ~(y_ << 11);
            y_ ^= y_ >>> 16;
            return x_ ^ y_;
        } else {
            long key = java.lang.Float.floatToIntBits(y);
            key += ~(key << 32);
            key ^= key >>> 22;
            key += ~(key << 13);
            key ^= key >>> 8;
            key += key << 3;
            key ^= key >>> 15;
            key += ~(key << 27);
            key ^= key >>> 31;
            key ^= java.lang.Float.floatToIntBits(x);
            return (int)(key ^ key >> 32);
        }
    }

    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Float2D) {
            Float2D other = (Float2D)obj;
            return (this.x == other.x || java.lang.Double.isNaN(this.x) && java.lang.Double.isNaN(other.x)) && (this.y == other.y || java.lang.Double.isNaN(this.y) && java.lang.Double.isNaN(other.y));
        } else if (obj instanceof MutableDouble2D) {
            MutableDouble2D other = (MutableDouble2D)obj;
            return (this.x == other.x || java.lang.Double.isNaN(this.x) && java.lang.Double.isNaN(other.x)) && (this.y == other.y || java.lang.Double.isNaN(this.y) && java.lang.Double.isNaN(other.y));
        } else if (obj instanceof Int2D) {
            Int2D other = (Int2D)obj;
            return (float)other.x == this.x && (float)other.y == this.y;
        } else if (!(obj instanceof MutableInt2D)) {
            return false;
        } else {
            MutableInt2D other = (MutableInt2D)obj;
            return (float)other.x == this.x && (float)other.y == this.y;
        }
    }

    public float distance(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public float distance(Float2D p) {
        float dx = this.x - p.x;
        float dy = this.y - p.y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public float distance(Int2D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public float distance(MutableInt2D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public float distance(Point2D p) {
        float dx = this.x - (float)p.getX();
        float dy = this.y - (float)p.getY();
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public float distanceSq(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public float distanceSq(Float2D p) {
        float dx = this.x - p.x;
        float dy = this.y - p.y;
        return dx * dx + dy * dy;
    }

    public float distanceSq(Int2D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        return dx * dx + dy * dy;
    }

    public float distanceSq(MutableInt2D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        return dx * dx + dy * dy;
    }

    public float distanceSq(Point2D p) {
        float dx = this.x - (float)p.getX();
        float dy = this.y - (float)p.getY();
        return dx * dx + dy * dy;
    }

    public float manhattanDistance(float x, float y) {
        float dx = Math.abs(this.x - x);
        float dy = Math.abs(this.y - y);
        return dx + dy;
    }

    public float manhattanDistance(Float2D p) {
        float dx = Math.abs(this.x - p.x);
        float dy = Math.abs(this.y - p.y);
        return dx + dy;
    }

    public float manhattanDistance(Int2D p) {
        float dx = Math.abs(this.x - (float)p.x);
        float dy = Math.abs(this.y - (float)p.y);
        return dx + dy;
    }

    public float manhattanDistance(MutableDouble2D p) {
        float dx = (float)Math.abs(this.x - p.x);
        float dy = (float)Math.abs(this.y - p.y);
        return dx + dy;
    }

    public float manhattanDistance(MutableInt2D p) {
        float dx = Math.abs(this.x - (float)p.x);
        float dy = Math.abs(this.y - (float)p.y);
        return dx + dy;
    }

    public float manhattanDistance(Point2D p) {
        float dx = Math.abs(this.x - (float)p.getX());
        float dy = Math.abs(this.y - (float)p.getY());
        return dx + dy;
    }

    public final Float2D add(Float2D other) {
        return new Float2D(this.x + other.x, this.y + other.y);
    }

    public final Float2D subtract(Float2D other) {
        return new Float2D(this.x - other.x, this.y - other.y);
    }

    public final float length() {
        return (float)Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public final float angle() {
        return (float)Math.atan2(this.y, this.x);
    }

    public final float lengthSq() {
        return this.x * this.x + this.y * this.y;
    }

    public final Float2D multiply(float val) {
        return new Float2D(this.x * val, this.y * val);
    }

    public final Float2D resize(float dist) {
        if (dist == 0.0D) {
            return new Float2D(0.0f, 0.0f);
        } else if (dist != 1.0f / 0.0 && dist != -1.0f / 0.0 && dist == dist) {
            if ((this.x != 0.0D || this.y != 0.0D) && this.x != 1.0f / 0.0 && this.x != -1.0f / 0.0 && this.x == this.x && this.y != 1.0f / 0.0 && this.y != -1.0f / 0.0 && this.y == this.y) {
                float temp = this.length();
                return new Float2D(this.x * dist / temp, this.y * dist / temp);
            } else {
                throw new ArithmeticException("Cannot resize a vector with infinite or NaN values, or of length 0, except to length 0");
            }
        } else {
            throw new ArithmeticException("Cannot resize to distance " + dist);
        }
    }

    public final Float2D normalize() {
        return this.resize(1.0f);
    }

    public final float dot(Float2D other) {
        return other.x * this.x + other.y * this.y;
    }

    public float perpDot(Float2D other) {
        return -this.y * other.x + this.x * other.y;
    }

    public final Float2D negate() {
        return new Float2D(-this.x, -this.y);
    }

    public final Float2D rotate(float theta) {
        float sinTheta = (float)Math.sin(theta);
        float cosTheta = (float)Math.cos(theta);
        float x = this.x;
        float y = this.y;
        return new Float2D(cosTheta * x + -sinTheta * y, sinTheta * x + cosTheta * y);
    }
}

