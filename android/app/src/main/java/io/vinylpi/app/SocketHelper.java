package io.vinylpi.app;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketHelper {
    private static final String TAG = "SocketHelper";
    private static final int PI_DEVICE_SOCKET_PORT = 3001;
    private Socket socket = null;
    private String mAddress;
    private OnDataReceivedListener onDataReceivedListener;

    public SocketHelper() {}

    public interface OnDataReceivedListener {
        public void onDataReceived(String message);
    }

    public void connect(String address) {
        this.mAddress = address;
        new SocketConnectionTask().execute(mAddress);
    }

    public void Disconnect() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (java.io.IOException e) {
            Log.d(TAG, "IO exception disconnecting from " + mAddress);
        }
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        onDataReceivedListener = listener;
    }

    private class SocketConnectionTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... address) {
            try {
                socket = new Socket(InetAddress.getByName(mAddress), PI_DEVICE_SOCKET_PORT);
                socket.setKeepAlive(true);

                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

                //DataInputStream input = new DataInputStream(socket.getInputStream());
                String line = "";
                while ((line = in.readLine()) != null) {
                    line.toString();
                    onDataReceivedListener.onDataReceived(line);
                }
            } catch (java.net.UnknownHostException e) {
                Log.d(TAG, "Unknown host " + mAddress);
            } catch (java.io.IOException e) {
                Log.d(TAG, "IO exception connecting to " + mAddress);
                e.printStackTrace();
            } finally {
                /*if (socket != null) {
                    try {
                        socket.close();
                        socket = null;
                    } catch (java.io.IOException e) {
                        Log.d(TAG, "IO exception closing socket on connection for " + mAddress);
                    }
                }*/
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

        }
    }
}
