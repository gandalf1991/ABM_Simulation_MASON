package Utils;

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

}
