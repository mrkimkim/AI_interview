package portfolio.projects.mrkimkim.ai_interview.NetworkModule;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import portfolio.projects.mrkimkim.ai_interview.GlobalApplication;
import portfolio.projects.mrkimkim.ai_interview.R;
import portfolio.projects.mrkimkim.ai_interview.Utils.Functions;

public class NetworkService extends Service {
    int max_buffer_size = 2048;

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public NetworkService getService() { return NetworkService.this; }
    }

    private ICallback mCallback;
    public interface ICallback {
        public void successToSend();
        public void successToReceive(byte[] bytes);
    }

    public void registerCallback(ICallback callback) { mCallback = callback;}
    public void unregisterCallback() {mCallback = null;}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // 데이터 송수신을 위한 멤버 함수
    private void sendCmd(byte[] bytes, OutputStream outputStream) {
        try {
            // 0 ~ 3 : opcode
            // 4 ~ 8 : datasize
            outputStream.write(bytes);
        } catch (IOException e) {
            Log.d("NetworkService :", "커맨드 전송 중 에러 발생");
            e.printStackTrace();
        }
    }

    public static byte[] getHeader(int operation) {
        byte[] opcode = new byte[4];
        opcode[0] = (byte)(operation >> 24);
        opcode[1] = (byte)(operation >> 16);
        opcode[2] = (byte)(operation >> 8);
        opcode[3] = (byte)(operation);
        return opcode;
    }

    public static byte[] getHeader(int operation, long question_idx, long size) {
        byte[] opcode = new byte[20];
        opcode[0] = (byte)(operation >> 24);
        opcode[1] = (byte)(operation >> 16);
        opcode[2] = (byte)(operation >> 8);
        opcode[3] = (byte)(operation);

        System.arraycopy(Functions.longToBytes(question_idx), 0, opcode, 4, 8);
        System.arraycopy(Functions.longToBytes(size), 0, opcode, 12, 8);
        return opcode;
    }

    // Activity에서 호출 가능한 데이터 송수신 함수
    public void sendData(byte[] bytes) {
        class t_sendData implements Runnable {
            byte[] bytes;
            t_sendData(byte[] b) {bytes = b;}

            @Override
            public void run() {
                try {
                    Socket t_socket = new Socket();
                    t_socket.connect(new InetSocketAddress(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port))), 1000);
                    InputStream networkDataReader = t_socket.getInputStream();
                    OutputStream networkDataWriter = t_socket.getOutputStream();

                    // 송신 명령을 보냄
                    sendCmd(getHeader(0, 0, bytes.length), networkDataWriter);

                    // 데이터 송신
                    int offset = 0;
                    while (offset < bytes.length) {
                        networkDataWriter.write(bytes, offset, Math.min(max_buffer_size, bytes.length - offset));
                        offset += max_buffer_size;
                    }

                    t_socket.close();
                    networkDataReader.close();
                    networkDataWriter.close();

                    mCallback.successToSend();
                } catch (IOException e) {
                    Log.d("NetworkService : ", "데이터 전송 중 에러 발생");
                    e.printStackTrace();
                }
            }
        }
        Runnable t = new t_sendData(bytes);
        t.run();
    }

    public byte[] receive(InputStream inputStream, int size) {
        final int bufsize = 1024;
        int recv = 0;
        try {
            byte[] packet = new byte[size];
            while(recv < size) {
                int len = inputStream.read(packet, recv, Math.min(bufsize, size - recv));
                if (len > 0) recv += len;
            }
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void receiveData() {
        class t_receiveData implements Runnable{
            int data_length;
            byte[] temp = new byte[4];
            byte[] buffer;

            @Override
            public void run() {
                try {
                    Socket t_socket = new Socket();
                    t_socket.connect(new InetSocketAddress(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port))), 1000);
                    InputStream networkDataReader = t_socket.getInputStream();
                    OutputStream networkDataWriter = t_socket.getOutputStream();

                    // 수신 명령을 보냄
                    sendCmd(getHeader(1, 0, 0), networkDataWriter);

                    // 데이터 사이즈를 받아옴.
                    networkDataReader.read(temp, 0, 4);
                    data_length = (((int)temp[0] & 0xff << 24) | ((int)temp[1] & 0xff << 16) |((int)temp[2] & 0xff << 8) |((int)temp[3] & 0xff));

                    // 데이터 수신부
                    int offset = 0;
                    buffer = new byte[data_length];
                    while (offset < data_length) {
                        networkDataReader.read(buffer, offset, Math.min(max_buffer_size, data_length - offset));
                        offset += max_buffer_size;
                    }

                    t_socket.close();
                    networkDataReader.close();
                    networkDataWriter.close();

                    mCallback.successToReceive(buffer);
                } catch (IOException e) {
                    Log.d("NetworkService : ", "데이터 수신 중 에러 발생");
                }
            }
        }
        Runnable t = new t_receiveData();
        t.run();
    }

    public static void Auth_User(InputStream networkReader, OutputStream networkWriter) {
        // 유저 인증 토큰을 전송함.
        try {
            byte[] userId = Functions.longToBytes(GlobalApplication.mUserInfoManager.getUserProfile().getId());
            byte[] userToken = GlobalApplication.mUserInfoManager.getAppToken();
            networkWriter.write(userId);
            networkWriter.write(userToken);
            networkWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
