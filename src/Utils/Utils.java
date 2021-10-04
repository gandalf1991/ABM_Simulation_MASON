/*
 	Written by Pietro Russo
*/

package Utils;

import sun.misc.FloatConsts;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class Utils {


    // METHODS
    public static byte[] compressMessage(byte[] payload) throws IOException {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(rstBao);
        zos.write(payload);
        zos.close();

        return rstBao.toByteArray();
    }

    public static float floor(float a){
        return floorOrCeil(a, -1.0f, 0.0f, -1.0f);
    }
    public static float ceil(float a){
        return floorOrCeil(a, -0.0f, 1.0f, 1.0f);
    }
    public static float floorOrCeil(float a, float negativeBoundary, float positiveBoundary, float sign) {
        int exponent = Math.getExponent(a);

        if (exponent < 0) {
            /*
             * Absolute value of argument is less than 1.
             * floorOrceil(-0.0) => -0.0
             * floorOrceil(+0.0) => +0.0
             */
            return ((a == 0.0) ? a :
                    ( (a < 0.0) ?  negativeBoundary : positiveBoundary) );
        } else if (exponent >= 52) {
            /*
             * Infinity, NaN, or a value so large it must be integral.
             */
            return a;
        }
        // Else the argument is either an integral value already XOR it
        // has to be rounded to one.
        assert exponent >= 0 && exponent <= 51;

        int doppel = Float.floatToRawIntBits(a);
        int mask   = FloatConsts.SIGNIF_BIT_MASK >> exponent;

        if ( (mask & doppel) == 0L )
            return a; // integral value
        else {
            float result = Float.intBitsToFloat(doppel & (~mask));
            if (sign*a > 0.0)
                result = result + sign;
            return result;
        }
    }
    public static float rint(float a) {

        float twoToThe52 = (float)(1L << 52); // 2^52
        float sign = Math.copySign(1.0f, a); // preserve sign info
        a = Math.abs(a);

        if (a < twoToThe52) { // E_min <= ilogb(a) <= 51
            a = ((twoToThe52 + a ) - twoToThe52);
        }

        return sign * a; // restore original sign
    }

}
