package io.vinylpi.app;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.nsd.NsdManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.nsd.NsdServiceInfo;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        DeviceListFragment.OnListFragmentInteractionListener {

    public final static String EXTRA_PI_DEVICE = "io.vinylpi.app.PI_DEVICE";

    private NsdManager.DiscoveryListener mDiscoveryListener;
    private android.net.nsd.NsdManager mNsdManager;
    private static final String TAG = "MainActivity";
    private static final String SERVICE_TYPE = "_http._tcp";
    private static final int PI_DEVICE_HTTP_PORT = 3000;
    private static final int PI_DEVICE_SOCKET_PORT = 3001;
    private ArrayList<PiDevice> piDevices;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.vinylpi.app.R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(io.vinylpi.app.R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(io.vinylpi.app.R.id.fab_scan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                fab.hide();

                TextView scanMessageTextView = (TextView) findViewById(io.vinylpi.app.R.id.txt_scan_message);
                scanMessageTextView.setVisibility(View.GONE);

                LinearLayout scanStatusLayout = (LinearLayout) findViewById(io.vinylpi.app.R.id.ll_scan_status);
                scanStatusLayout.setVisibility(View.VISIBLE);

                //initializeDiscoveryListener();
                new ScanNetworkTask().execute();

            }
        });

        /*DrawerLayout drawer = (DrawerLayout) findViewById(io.vinylpi.app.R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, io.vinylpi.app.R.string.navigation_drawer_open, io.vinylpi.app.R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(io.vinylpi.app.R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);*/

        piDevices = new ArrayList<PiDevice>();
    }

    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(io.vinylpi.app.R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(io.vinylpi.app.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == io.vinylpi.app.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        /*int id = item.getItemId();

        if (id == io.vinylpi.app.R.id.nav_camera) {
            // Handle the camera action
        } else if (id == io.vinylpi.app.R.id.nav_gallery) {

        } else if (id == io.vinylpi.app.R.id.nav_slideshow) {

        } else if (id == io.vinylpi.app.R.id.nav_manage) {

        } else if (id == io.vinylpi.app.R.id.nav_share) {

        } else if (id == io.vinylpi.app.R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(io.vinylpi.app.R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);*/
        return true;

    }

    public void initializeDiscoveryListener() {
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                /* if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("NsdChat")){
                    mNsdManager.resolveService(service, mResolveListener);
                }*/
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };

        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public static void printReachableHosts(InetAddress inetAddress) throws SocketException, UnknownHostException {
        String ipAddress = inetAddress.toString();
        ipAddress = ipAddress.substring(1, ipAddress.lastIndexOf('.')) + ".";
        for (int i = 0; i < 256; i++) {
            String otherAddress = ipAddress + String.valueOf(i);
            try {
                if (InetAddress.getByName(otherAddress.toString()).isReachable(1)) {
                    System.out.println(otherAddress);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        if (this != null) {
            this.tearDown();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (this != null) {
            mNsdHelper.registerService(mConnection.getLocalPort());
            mNsdHelper.discoverServices();
        }*/
    }

    @Override
    protected void onDestroy() {
        this.tearDown();
       // mConnection.tearDown();
        super.onDestroy();
    }

    // NsdHelper's tearDown method
    public void tearDown() {
        //mNsdManager.unregisterService(mRegistrationListener);
        if (mNsdManager != null)
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    @Override
    public void onListFragmentInteraction(PiDevice item) {
        Toast.makeText(this, item.getDeviceName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra(EXTRA_PI_DEVICE, item);
        startActivity(intent);
    }

    private class ScanNetworkTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... v) {
            String ipAddress = null;
            try {
                ipAddress = (Inet4Address.getLocalHost()).toString();

                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                Enumeration<InetAddress> inetAddresses;
                NetworkInterface networkInterface;
                InetAddress inetAddress;

                while (networkInterfaces.hasMoreElements()) {
                    networkInterface = (NetworkInterface) networkInterfaces.nextElement();

                    inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        inetAddress = (InetAddress) inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                            Log.d(TAG, "getHostAddress: " + inetAddress.getHostAddress().toString());
                            Log.d(TAG, "getHostName: " + inetAddress.getHostName().toString());
                            Log.d(TAG, "getAddress: " + inetAddress.getAddress().toString());
                            ipAddress = inetAddress.getHostAddress().toString();
                        }
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }

           /* try {
                try {
                    Socket socket = new Socket(InetAddress.getByName("192.168.1.131"), 3001);

                    //serverSocket.bind(socketAddress);
                    PiDevice newDevice = getDevice("192.168.1.131");
                    piDevices.add(newDevice);
                    Log.d(TAG, "192.168.1.131");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Port not available on " + "192.168.1.131");
                }
            }   catch (Exception e) {
                    e.printStackTrace();
            }*/

            Log.d(TAG, "IP Address: " + ipAddress);
            ipAddress = ipAddress.substring(0, ipAddress.lastIndexOf('.')) + ".";
            for (int i = 130; i < 132; i++) {
                String otherAddress = ipAddress + String.valueOf(i);
                //Log.d(TAG, otherAddress);
                try {
                    if (InetAddress.getByName(otherAddress.toString()).isReachable(2000)) {
                        Log.d(TAG, otherAddress + " is up");

                        if (piDeviceAvailable(otherAddress)) {
                            PiDevice newDevice = getDevice(otherAddress.toString());

                            if (newDevice != null)
                                piDevices.add(newDevice);
                        }

                    } else {
                        Log.d(TAG, otherAddress + " is down");
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Void v) {
            Log.d(TAG, "Finished scanning");

            // If no devices found display the default scan message, otherwise display device list.
            if (piDevices.isEmpty()) {
                TextView scanMessageTextView = (TextView) findViewById(io.vinylpi.app.R.id.txt_scan_message);
                scanMessageTextView.setVisibility(View.VISIBLE);
            } else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DeviceListFragment itemFragment = DeviceListFragment.newInstance(piDevices);
                fragmentTransaction.add(R.id.rl_content_main, itemFragment);
                fragmentTransaction.commit();
            }

            LinearLayout scanStatusLayout = (LinearLayout) findViewById(io.vinylpi.app.R.id.ll_scan_status);
            scanStatusLayout.setVisibility(View.GONE);

            final FloatingActionButton fab = (FloatingActionButton) findViewById(io.vinylpi.app.R.id.fab_scan);
            fab.show();
        }

        private boolean piDeviceAvailable(String address) {
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getByName(address), PI_DEVICE_SOCKET_PORT);
                Log.d(TAG, "Can connect to pi device");
                return true;
            } catch (java.net.UnknownHostException e) {

            } catch (java.io.IOException e) {

            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (java.io.IOException e) {

                    }
                }
            }
            Log.d(TAG, "Cannot connect to pi device");
            return false;
        }

        private PiDevice getDevice(String ipAddress) throws IOException {
            InputStream inputStream = null;
            HttpURLConnection conn = null;
            PiDevice piDevice = null;
            int connections = -1;
            String deviceName = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            //int len = 500;

            try {
                String deviceUrl = "http://" + ipAddress + ":" + PI_DEVICE_HTTP_PORT + "/device";
                URL url = new URL(deviceUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Accept", "text/json");
                //conn.setRequestProperty("Accept", "text/plain");

                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                //String contentAsString = readIt(inputStream, inputStream.available());
                //Log.d(TAG, contentAsString);

                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("device")) {
                        deviceName = reader.nextString();
                    } else if (name.equals("connections")) {
                        connections = reader.nextInt();
                    }
                }
                reader.endObject();

                //Parcel parcel = Parcel.obtain();
                //parcel.writeString(deviceName);
                //parcel.writeInt(connections);
                piDevice = new PiDevice();
                piDevice.setDeviceName(deviceName);
                piDevice.setConnections(connections);

            } catch (java.net.ConnectException e) {
                //e.printStackTrace();
                Log.d(TAG, "Connection refused to PI device");
            } catch (java.net.ProtocolException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            }
            return piDevice;
        }

        // Reads an InputStream and converts it to a String.
        private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
           StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF08");
            return writer.toString();
        }
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        //return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
        //        PROJECTION, SELECTION, null, null);
        return null;
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        //mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        //mAdapter.swapCursor(null);
    }
}
