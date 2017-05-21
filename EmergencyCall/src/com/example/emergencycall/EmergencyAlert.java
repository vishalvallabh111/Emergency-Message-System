package com.example.emergencycall;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.gsm.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

@SuppressWarnings("deprecation")
public class EmergencyAlert extends Activity implements OnClickListener {

	final int RADIOBUTTON_ALERTDIALOG = 0;
	final Context context = this;
	Button buttoncontact, save_button, send_info;
	EditText name_edit, message_edit, phoneone, phonetwo, mailone, mailtwo;
	TextView location_details;
	SharedPreferences preferencesmydata, viewSharedPreference;
	SharedPreferences.Editor editor;
	ToggleButton timer;
	View promptsView;
	GPSTracker gps;
	String countryname, cityName, msgtext;
	final String[] timer_radio = { "Every One Minute", "Every Three Minutes",
			"Every Five Minutes", "Every Ten Minutes", "Every Fifteen Minutes",
			"Every Twenty Minutes", "Every Twentyfive Minute",
			"Every Thirty Minutes", "Every One Hour", "Every Two Hours" };

	int timer_numbers[] = { 1, 5, 10, 15, 20, 25, 30, 45, 60, 120 };
	BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

	String[] number = new String[2];
	String gpslocation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.emergency_alert);

		try {
			accessLocation();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "ACTIVE GPS", 3000).show();
		}

		buttoncontact = (Button) findViewById(R.id.cntid);
		
		save_button = (Button) findViewById(R.id.saveid);
		send_info = (Button) findViewById(R.id.sndid);
		name_edit = (EditText) findViewById(R.id.edtnameid);
		message_edit = (EditText) findViewById(R.id.msgid);
		location_details = (TextView) findViewById(R.id.locnameid);
		timer = (ToggleButton) findViewById(R.id.tglbtn);
		location_details.setText(gpslocation+"\n"+cityName);

		viewSharedPreference = getSharedPreferences("MyData",
				MODE_WORLD_WRITEABLE);
		number[0] = viewSharedPreference.getString("PHONEONE", "");
		number[1] = viewSharedPreference.getString("PHONETWO", "");
		name_edit.setText(viewSharedPreference.getString("NAME", ""));
//	location_details.setText(viewSharedPreference.getString("LOCATION", ""));
		message_edit.setText(viewSharedPreference.getString("MESSAGE", "Need Your Help"));
	

		String name = name_edit.getText().toString();
	
		String message = message_edit.getText().toString();

		msgtext = "NAME:-" + name + "\n" + "LOCATION:-"+ gpslocation+"\n"+cityName+"\n"
				+ "MESSAGE:-" + message;
		
		buttoncontact.setOnClickListener(this);
		
		save_button.setOnClickListener(this);
		send_info.setOnClickListener(this);
		
		timer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub

				if (isChecked) {

					showDialog(RADIOBUTTON_ALERTDIALOG);
					

				} else {
					Toast.makeText(getApplicationContext(), "OFF", 3000).show();
				}

			}
		});

	}

	public Dialog onCreateDialog(int id) {
		switch (id) {

		case RADIOBUTTON_ALERTDIALOG:

			AlertDialog.Builder builder2 = new AlertDialog.Builder(
					EmergencyAlert.this).setTitle("Choose a Timer ")
					.setSingleChoiceItems(timer_radio, -1,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									Toast.makeText(
											getApplicationContext(),
											"The selected Timer is "
													+ timer_radio[which],
											Toast.LENGTH_LONG).show();
									sendTimerSms();

									

									// dismissing the dialog when the user makes
									// a selection.
									dialog.dismiss();
								}
							});
			AlertDialog alertdialog2 = builder2.create();
			return alertdialog2;
		}
		return null;

	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		// TODO Auto-generated method stub

		switch (id) {

		case RADIOBUTTON_ALERTDIALOG:
			AlertDialog prepare_radio_dialog = (AlertDialog) dialog;
			ListView list_radio = prepare_radio_dialog.getListView();
			for (int i = 0; i < list_radio.getCount(); i++) {
				list_radio.setItemChecked(i, false);
			}
			break;

		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.cntid:
			LayoutInflater li = LayoutInflater.from(context);
			View promptsView = li.inflate(R.layout.contactsdialog, null);

			phoneone = (EditText) promptsView.findViewById(R.id.phoneoneid);
            phoneone.setText(number[0]);
			phonetwo = (EditText) promptsView.findViewById(R.id.phonetwoid);
			phonetwo.setText(number[1]);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			alertDialogBuilder.setView(promptsView);

			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("Save",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int button1) {
									// TODO Auto-generated method stub

									number[0] = phoneone.getText().toString();
									
									number[1] = phonetwo.getText().toString();

									preferencesmydata = getSharedPreferences(
											"MyData", MODE_WORLD_WRITEABLE);

									editor = preferencesmydata.edit();
									editor.putString("PHONEONE", number[0]);
									editor.putString("PHONETWO", number[1]);

									editor.commit();
									Toast.makeText(getApplicationContext(),
											number[0], Toast.LENGTH_LONG)
											.show();
									Toast.makeText(getApplicationContext(),
											number[1], Toast.LENGTH_LONG)
											.show();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int button2) {
									// TODO Auto-generated method stub

								}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			((Button) alertDialog.findViewById(android.R.id.button1))
					.setBackgroundResource(R.drawable.buttonborder);
			((Button) alertDialog.findViewById(android.R.id.button1))
					.setTextColor(Color.WHITE);
			((Button) alertDialog.findViewById(android.R.id.button2))
					.setBackgroundResource(R.drawable.buttonborder);
			((Button) alertDialog.findViewById(android.R.id.button2))
					.setTextColor(Color.WHITE);
			break;

	

		case R.id.saveid:
			preferencesmydata = getSharedPreferences("MyData",
					MODE_WORLD_WRITEABLE);
			editor = preferencesmydata.edit();
			editor.putString("NAME", name_edit.getText().toString());
			editor.putString("MESSAGE", message_edit.getText().toString());
			//editor.putString("LOCATION", gpslocation);
			editor.commit();
			Toast.makeText(getApplicationContext(), "Saved Successfully",
					Toast.LENGTH_LONG).show();

			break;

		case R.id.sndid:
			
			String name = name_edit.getText().toString();
			
			String message = message_edit.getText().toString();

			msgtext = "NAME:-" + name + "\n" + "LOCATION:-"+ gpslocation+"\n"+cityName+"\n"
					+ "MESSAGE:-" + message;
			sendInformation();

			break;

		default:
			break;
		}

	}

	public void sendInformation() {

		SmsManager sms = SmsManager.getDefault();

		
		PendingIntent piSent = PendingIntent.getBroadcast(this, 0, new Intent(
				"SMS_SENT"), 0);
		PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0,
				new Intent("SMS_DELIVERED"), 0);
		try {
			sms.sendTextMessage(number[0], null, msgtext, piSent, piDelivered);
			Toast.makeText(getApplicationContext(), "SMS SENT TO" + number[0],
					3000).show();
			Toast.makeText(
					getApplicationContext(),
					msgtext,
					Toast.LENGTH_LONG).show();
			sms.sendTextMessage(number[1], null, msgtext, piSent, piDelivered);
			System.out.println("CHECK MSG:::"+msgtext);
			Toast.makeText(getApplicationContext(), "SMS SENT TO" + number[1],
					3000).show();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(),
					"Enter phone number & Message", 3000).show();

			e.printStackTrace();
		}
	}

	public void onResume() {

		super.onResume();
		smsSentReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub

				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS has been sent",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic Failure",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No Service",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio Off",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}

			}

		};

		smsDeliveredReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS Delivered",
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}

		};

		registerReceiver(smsSentReceiver, new IntentFilter("SMS_SENT"));
		registerReceiver(smsDeliveredReceiver,
				new IntentFilter("SMS_DELIVERED"));
	}

	public void onPause() {
		super.onPause();
		unregisterReceiver(smsSentReceiver);
		unregisterReceiver(smsDeliveredReceiver);
	}

	public void sendTimerSms() {
		final Handler handler = new Handler();
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int timeToBlink = 1000 * 60;
				try {
					Thread.sleep(timeToBlink);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				handler.post(new Runnable() {

					@Override
					public void run() {

						sendInformation();

					}
				});
			}
		}).start();
	}

	public void accessLocation() {

		gps = new GPSTracker(EmergencyAlert.this);
		if (gps.canGetLocation()) {

			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();

			gpslocation="Lat: "+latitude+" "+"&"+" "+"Lon: "+longitude;
			

		
			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
			List<Address> addresses = null;
			try {
				try {
					try {
						addresses = gcd.getFromLocation(gps.getLatitude(),
								gps.getLongitude(), 1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (addresses.size() > 0)
					System.out.println("CHECK:::"
							+ addresses.get(0).getLocality());
				cityName = addresses.get(0).getLocality();

			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			// \n is for new line
			Toast.makeText(
					getApplicationContext(),
					"Your Location Name: "+ cityName,
					Toast.LENGTH_LONG).show();
	//	} 
			
		Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

			   try {
			   addresses = geocoder.getFromLocation(latitude, longitude, 1);

			   if(addresses != null) {
			   Address returnedAddress = addresses.get(0);
			   StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
			   for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
			   strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
			  }
			Toast.makeText(getApplicationContext(), strReturnedAddress.toString(), 3000).show();
			}
			else{
				Toast.makeText(getApplicationContext(), "NOT FOUND", 3000).show();
			}
			} catch (IOException e) {
			 // TODO Auto-generated catch block
			  e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Cant get", 3000).show();
			 }
	}

}}