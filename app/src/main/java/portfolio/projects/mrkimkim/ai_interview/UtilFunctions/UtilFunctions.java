package portfolio.projects.mrkimkim.ai_interview.UtilFunctions;

import java.nio.ByteBuffer;

/**
 * Created by JHG on 2017-12-31.
 */

public class UtilFunctions {
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    };

    public static byte[] concatBytes(byte[] a, byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(a.length + b.length);
        buffer.put(a);
        buffer.put(b);
        return buffer.array();
    }
}
