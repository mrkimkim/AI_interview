package portfolio.projects.mrkimkim.ai_interview.Utils;

import java.nio.ByteBuffer;

/**
 * Created by JHG on 2017-12-31.
 */

public class Functions {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    };

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("%02x ", b&0xff));
        return sb.toString();
    }

    public static long bytesToLong(byte[] buf) {
        long l = ((buf[0] & 0xFFL) << 56) |
                ((buf[1] & 0xFFL) << 48) |
                ((buf[2] & 0xFFL) << 40) |
                ((buf[3] & 0xFFL) << 32) |
                ((buf[4] & 0xFFL) << 24) |
                ((buf[5] & 0xFFL) << 16) |
                ((buf[6] & 0xFFL) <<  8) |
                ((buf[7] & 0xFFL) <<  0) ;
        return l;
    }

    public static int bytesToInt(byte[] b) {
        int ret = (b[0]<<24)&0xff000000|
                (b[1]<<16)&0x00ff0000|
                (b[2]<<8)&0x0000ff00|
                (b[3]<<0)&0x000000ff;
        return ret;
    }

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
