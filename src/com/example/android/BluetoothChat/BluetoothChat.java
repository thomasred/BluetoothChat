/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity implements SensorEventListener {
    // Debugging
    private static final String TAG = "Bluetooth CAR";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView mTitle;
    //private ListView mConversationView;
    //private EditText mOutEditText;
    //private Button mSendButton;
    
  //  private byte Init_send_val = 0x00;
    private byte Init_send_valF = 0x00;
    private byte Init_send_valB = 0x00;
    private byte Init_send_valD = 0x00;
    
    //public static final int LED_ON = 0x1;
    //public static final int LED_OFF = 0x2;
    public static final int Right = 0x3;
    public static final int Left = 0x4;
    
    public static final int Front = 0x1;
    public static final int Back = 0x2;
    public static final int Stop = 0x00;
    
    private ImageButton mimageButton1;//up
    private ImageButton mimageButton2;//down
    
    private Handler mHandlerTime = new Handler();

    
    

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    //private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private SensorManager sensorManager; //在實機上




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        
       
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        
        //取得感測器服務(Sensor service)
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);//在實機上
        
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mimageButton1 = (ImageButton)findViewById(R.id.imageButton1);    //up      
        mimageButton2 = (ImageButton)findViewById(R.id.imageButton2);   //down    
           
      
        
        mimageButton1.setOnTouchListener(new ImageButton.OnTouchListener(){
	    	public boolean onTouch(View v, MotionEvent event) {    
	            if(event.getAction() == MotionEvent.ACTION_DOWN){       
	               Init_send_valF = Front ;      
	            	 Log.v(TAG,"User press the button");
	            }
	            else{
	            	Init_send_valF = Stop;
	            }
	            return false;       
	    	}       
        });
        
        mimageButton2.setOnTouchListener(new ImageButton.OnTouchListener(){
	    	public boolean onTouch(View v, MotionEvent event) {    
	            if(event.getAction() == MotionEvent.ACTION_DOWN){       
	               Init_send_valB = Back ;      
	            	 Log.v(TAG,"User press the button");
	            }
	            else{
	            	Init_send_valB = Stop;
	            }
	            return false;       
	    	}       
        }); 
        
        


		mHandlerTime.removeCallbacks(car_timer);
		mHandlerTime.postDelayed(car_timer, 400);
      
        
    }
    
    
	private final Runnable car_timer = new Runnable()
	  {
	    public void run()
	    {
	      if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
	    	    if((Init_send_valF==Stop) && (Init_send_valB== Stop) &&(Init_send_valD== Stop)){
	    	    	// Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF0, 0x03};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);
	    	    }
	    	    else if(Init_send_valF == Front  &&  Init_send_valD == Right)
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF1, 0x03};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);
		            
		        }
	    	    else if(Init_send_valF == Front  &&  Init_send_valD == Left)
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF2, 0x03};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);
		            
		        }
	    	    else if(Init_send_valF == Front && Init_send_valD == Stop)
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF3, 0x03};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);
		            
		        }
	    	    else if(Init_send_valB == Back &&  Init_send_valD == Right)
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF4, 0x00};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);        	
		        }
		        else if(Init_send_valB == Back &&  Init_send_valD == Left)
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF5, 0x00};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);        	
		        }
	    	    else if(Init_send_valB == Back && Init_send_valD == Stop)
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF6, 0x00};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);        	
		        }
	    	    else if(Init_send_valD == Right )
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF7, 0x03};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);
		            
		        }
	    	    else if(Init_send_valD == Left )
		        {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = new byte[] {(byte) 0xAA, (byte) 0xBB, 0x02, (byte) 0xF8, 0x03};
		            mChatService.write(send);
		            // Reset out string buffer to zero and clear the edit text field
		            mOutStringBuffer.setLength(0);
		            
		        }
	    	    
		        
	      }

	        mHandlerTime.postDelayed(this, 400);
	       
	    }
	  };
//==========================================================================================    

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        
        //註冊感應器
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
       
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }
    
       



    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //mConversationView = (ListView) findViewById(R.id.in);
        //mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        //mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        //mSendButton = (Button) findViewById(R.id.button_send);
        //mSendButton.setOnClickListener(new OnClickListener() {
        //    public void onClick(View v) {
                // Send a message using content of the edit text widget
                //TextView view = (TextView) findViewById(R.id.edit_text_out);
                //String message = view.getText().toString();
                //sendMessage(message);
       //     }
       // });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
      
        //解除感應器註冊
        sensorManager.unregisterListener(this);//在實機上
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    //mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		//event:onSensorChanged傳入sensor的數值
		  float[] values = event.values;  //[0]=x [1]=y [2]=z
		  
		//判斷感測器x軸的數值
		  if(values[0]>2){
			  Init_send_valD = Right;
		  }
		  else if(values[0]<-2){
			  Init_send_valD = Left;
		  }
		  else{
			  Init_send_valD = Stop;
		  }
	}

}
