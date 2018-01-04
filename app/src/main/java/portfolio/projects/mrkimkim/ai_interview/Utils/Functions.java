package portfolio.projects.mrkimkim.ai_interview.Utils;

import java.nio.ByteBuffer;

/**
 * Created by JHG on 2017-12-31.
 */

public class Functions {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

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

    public static String byteHexToString(byte[] a) {
        char[] hexChars = new char[a.length * 2];
        for (int i = 0; i < a.length; ++i) {
            int v = a[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
