package Utils;

import sim.util.*;

import java.io.Serializable;

public class Float3D implements Serializable {
    private static final long serialVersionUID = 1L;
    public final float x;
    public final float y;
    public final float z;
    static final float infinity = 1.0f / 0.0f;

    public Float3D() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    public Float3D(Int2D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = 0.0f;
    }

    public Float3D(Int2D p, float z) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = z;
    }

    public Float3D(Int3D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = (float)p.z;
    }

    public Float3D(MutableInt2D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = 0.0f;
    }

    public Float3D(MutableInt2D p, float z) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = z;
    }

    public Float3D(MutableInt3D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = (float)p.z;
    }

    public Float3D(Double2D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = 0.0f;
    }

    public Float3D(Double2D p, float z) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = z;
    }

    public Float3D( Float3D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = (float)p.z;
    }

    public Float3D(MutableDouble2D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = 0.0f;
    }

    public Float3D(MutableDouble2D p, float z) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = z;
    }

    public Float3D(MutableDouble3D p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
        this.z = (float)p.z;
    }

    public Float3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final float getX() {
        return this.x;
    }

    public final float getY() {
        return this.y;
    }

    public final float getZ() {
        return this.z;
    }

    public String toString() {
        return "Float3D[" + this.x + "," + this.y + "," + this.z + "]";
    }

    public String toCoordinates() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public int hashCode() {
        float x = this.x;
        float y = this.y;
        float z = this.z;
        if (x == -0.0f) {
            x = 0.0f;
        }

        if (y == -0.0f) {
            y = 0.0f;
        }

        if (z == -0.0f) {
            z = 0.0f;
        }

        if ((float)((int)x) == x && (float)((int)y) == y && (float)((int)z) == z) {
            int y_ = (int)y;
            int x_ = (int)x;
            int z_ = (int)z;
            z_ += ~(z_ << 15);
            z_ ^= z_ >>> 10;
            z_ += z_ << 3;
            z_ ^= z_ >>> 6;
            z_ += ~(z_ << 11);
            z_ ^= z_ >>> 16;
            z_ ^= y_;
            z_ += 17;
            z_ += ~(z_ << 15);
            z_ ^= z_ >>> 10;
            z_ += z_ << 3;
            z_ ^= z_ >>> 6;
            z_ += ~(z_ << 11);
            z_ ^= z_ >>> 16;
            return x_ ^ z_;
        } else {
            long key = Float.floatToIntBits(z);
            key += ~(key << 32);
            key ^= key >>> 22;
            key += ~(key << 13);
            key ^= key >>> 8;
            key += key << 3;
            key ^= key >>> 15;
            key += ~(key << 27);
            key ^= key >>> 31;
            key ^= Float.floatToIntBits(y);
            key += 17L;
            key += ~(key << 32);
            key ^= key >>> 22;
            key += ~(key << 13);
            key ^= key >>> 8;
            key += key << 3;
            key ^= key >>> 15;
            key += ~(key << 27);
            key ^= key >>> 31;
            key ^= Float.floatToIntBits(x);
            return (int)(key ^ key >> 32);
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof  Float3D) {
             Float3D other = ( Float3D)obj;
            return (this.x == other.x || Double.isNaN(this.x) && Double.isNaN(other.x)) && (this.y == other.y || Double.isNaN(this.y) && Double.isNaN(other.y)) && (this.z == other.z || Double.isNaN(this.z) && Double.isNaN(other.z));
        } else if (obj instanceof MutableDouble3D) {
            MutableDouble3D other = (MutableDouble3D)obj;
            return (this.x == other.x || Double.isNaN(this.x) && Double.isNaN(other.x)) && (this.y == other.y || Double.isNaN(this.y) && Double.isNaN(other.y)) && (this.z == other.z || Double.isNaN(this.z) && Double.isNaN(other.z));
        } else if (obj instanceof Int3D) {
            Int3D other = (Int3D)obj;
            return (float)other.x == this.x && (float)other.y == this.y && (float)other.z == this.z;
        } else if (!(obj instanceof MutableInt3D)) {
            return false;
        } else {
            MutableInt3D other = (MutableInt3D)obj;
            return (float)other.x == this.x && (float)other.y == this.y && (float)other.z == this.z;
        }
    }

    public float distance(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distance( Float3D p) {
        float dx = this.x - p.x;
        float dy = this.y - p.y;
        float dz = this.z - p.z;
        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distance(Int3D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        float dz = this.z - (float)p.z;
        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distance(MutableInt3D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        float dz = this.z - (float)p.z;
        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distanceSq(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distanceSq( Float3D p) {
        float dx = this.x - p.x;
        float dy = this.y - p.y;
        float dz = this.z - p.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distanceSq(Int3D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        float dz = this.z - (float)p.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distanceSq(MutableInt3D p) {
        float dx = this.x - (float)p.x;
        float dy = this.y - (float)p.y;
        float dz = this.z - (float)p.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float manhattanDistance(float x, float y, float z) {
        float dx = Math.abs(this.x - x);
        float dy = Math.abs(this.y - y);
        float dz = Math.abs(this.z - z);
        return dx + dy + dz;
    }

    public float manhattanDistance( Float3D p) {
        float dx = Math.abs(this.x - (float)p.x);
        float dy = Math.abs(this.y - (float)p.y);
        float dz = Math.abs(this.z - (float)p.z);
        return dx + dy + dz;
    }

    public float manhattanDistance(Int3D p) {
        float dx = Math.abs(this.x - (float)p.x);
        float dy = Math.abs(this.y - (float)p.y);
        float dz = Math.abs(this.z - (float)p.z);
        return dx + dy + dz;
    }

    public float manhattanDistance(MutableDouble3D p) {
        float dx = Math.abs(this.x - (float)p.x);
        float dy = Math.abs(this.y - (float)p.y);
        float dz = Math.abs(this.z - (float)p.z);
        return dx + dy + dz;
    }

    public float manhattanDistance(MutableInt3D p) {
        float dx = Math.abs(this.x - (float)p.x);
        float dy = Math.abs(this.y - (float)p.y);
        float dz = Math.abs(this.z - (float)p.z);
        return dx + dy + dz;
    }

    public final  Float3D add( Float3D other) {
        return new  Float3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public final  Float3D subtract( Float3D other) {
        return new  Float3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public final float length() {
        return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public final float lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public final  Float3D multiply(float val) {
        return new  Float3D(this.x * val, this.y * val, this.z * val);
    }

    public final  Float3D resize(float dist) {
        if (dist == 0.0D) {
            return new  Float3D(0.0f, 0.0f, 0.0f);
        } else if (dist != 1.0f / 0.0f && dist != -1.0f / 0.0f && dist == dist) {
            if ((this.x != 0.0f || this.y != 0.0f || this.z != 0.0f) && this.x != 1.0f / 0.0f && this.x != -1.0f / 0.0f && this.x == this.x && this.y != 1.0f / 0.0f && this.y != -1.0f / 0.0f && this.y == this.y && this.z != 1.0f / 0.0f && this.z != -1.0f / 0.0f && this.z == this.z) {
                float temp = this.length();
                return new  Float3D(this.x * dist / temp, this.y * dist / temp, this.z * dist / temp);
            } else {
                throw new ArithmeticException("Cannot resize a vector with infinite or NaN values, or of length 0, except to length 0");
            }
        } else {
            throw new ArithmeticException("Cannot resize to distance " + dist);
        }
    }

    public final  Float3D normalize() {
        return this.resize(1.0f);
    }

    public final float dot( Float3D other) {
        return other.x * this.x + other.y * this.y + other.z * this.z;
    }

    public final  Float3D negate() {
        return new  Float3D(-this.x, -this.y, -this.z);
    }
}
