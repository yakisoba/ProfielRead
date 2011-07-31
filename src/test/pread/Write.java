package test.pread;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import test.pread.R.id;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Write extends Activity implements OnClickListener {
	NfcAdapter mAdapter;
	static String mMessage = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profiel_write);
		mAdapter = NfcAdapter.getDefaultAdapter(this);

		findViewById(R.id.input).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		if (mMessage != null) {
			Log.d("NFC", "nullÇ∂Ç·Ç»Ç¢");
			onNewIntent(getIntent());
		} else {
			Log.d("NFC", "null");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.input:
			String name = (((EditText) findViewById(id.editName)).getText())
					.toString();
			String height = (((EditText) findViewById(id.editHeight)).getText())
					.toString();
			String weight = (((EditText) findViewById(id.editWeight)).getText())
					.toString();
			String hobby = (((EditText) findViewById(id.editHobby)).getText())
					.toString();
			String music = (((EditText) findViewById(id.editMusic)).getText())
					.toString();
			String sex = (String) getText(R.id.sex);
			String age = (String) getText(R.id.age);
			String blood = (String) getText(R.id.blood);

			Toast.makeText(this, "NFCÉ^ÉOÇÇ©Ç¥ÇµÇƒâ∫Ç≥Ç¢", Toast.LENGTH_SHORT).show();

			mMessage = name + "/" + sex + "/" + age + "/" + blood + "/"
					+ height + "/" + weight + "/" + hobby + "/" + music;

			Log.d("NFC", mMessage);

			// èëÇ´çûÇ›èàóù
			enableForegroundDispatch();

			break;
		case R.id.cancel:
			(((EditText) findViewById(id.editName)).getText()).clear();
			(((EditText) findViewById(id.editHeight)).getText()).clear();
			(((EditText) findViewById(id.editWeight)).getText()).clear();
			(((EditText) findViewById(id.editHobby)).getText()).clear();
			(((EditText) findViewById(id.editMusic)).getText()).clear();
			((TextView) findViewById(id.age)).setText("ñ¢ê›íË");
			((TextView) findViewById(id.sex)).setText("ñ¢ê›íË");
			((TextView) findViewById(id.blood)).setText("ñ¢ê›íË");
			break;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}

	public void enableForegroundDispatch() {
		IntentFilter[] filters = makeFilter();
		String[][] techLists = makeTechLists();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass()), 0);
		mAdapter.enableForegroundDispatch(this, pendingIntent, filters,
				techLists);
		Log.d("NFC", "efd");
	}

	IntentFilter[] makeFilter() {
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		IntentFilter[] filters = new IntentFilter[] { ndef, tech, tag };
		return filters;
	}

	String[][] makeTechLists() {
		String[] ndef = new String[] { Ndef.class.getName() };
		String[] ndefFormatable = new String[] { NdefFormatable.class.getName() };
		String[][] techLists = new String[][] { ndef, ndefFormatable };
		return techLists;
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("NFC", "new intent");

		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			writeNdefMessage(tag);
			finish();
		}		
	}

	void writeNdefMessage(Tag tag) {
		Log.d("NFC", mMessage);
		NdefRecord record = newTextRecord(mMessage, Locale.JAPANESE, true);
		NdefMessage message = new NdefMessage(new NdefRecord[] { record });

		try {
			if (Arrays.asList(tag.getTechList()).contains(
					NdefFormatable.class.getName())) {
				NdefFormatable ndef = NdefFormatable.get(tag);
				try {
					if (!ndef.isConnected()) {
						ndef.connect();
					}
					ndef.format(message);
					showSuccessToast();
				} finally {
					ndef.close();
				}
			} else if (Arrays.asList(tag.getTechList()).contains(
					Ndef.class.getName())) {
				Ndef ndef = Ndef.get(tag);
				try {
					if (!ndef.isConnected()) {
						ndef.connect();
					}

					if (ndef.isWritable()) {
						ndef.writeNdefMessage(message);
						showSuccessToast();
					} else {
						showNotWritableToast();
					}
				} finally {
					ndef.close();
				}
			}
		} catch (FormatException e) {
			showFailureToast();
		} catch (IOException e) {
			showFailureToast();
		}
	}

	void showSuccessToast() {
		Toast.makeText(this, "èëÇ´çûÇ›ê¨å˜", Toast.LENGTH_SHORT).show();
		Log.d("NFC", "success");
	}

	void showFailureToast() {
		Toast.makeText(this, "èëÇ´çûÇ›é∏îs", Toast.LENGTH_SHORT).show();
		Log.d("NFC", "faile");
	}

	void showNotWritableToast() {
		Toast.makeText(this, "èëÇ´çûÇ›é∏îs", Toast.LENGTH_SHORT).show();
		Log.d("NFC", "faile");
	}

	public static NdefRecord newTextRecord(String text, Locale locale,
			boolean encodeInUtf8) {
		byte[] langBytes = locale.getLanguage().getBytes(
				Charset.forName("US-ASCII"));

		Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset
				.forName("UTF-16");
		byte[] textBytes = text.getBytes(utfEncoding);

		int utfBit = encodeInUtf8 ? 0 : (1 << 7);
		char status = (char) (utfBit + langBytes.length);

		byte[] data = new byte[1 + langBytes.length + textBytes.length];
		data[0] = (byte) status;
		System.arraycopy(langBytes, 0, data, 1, langBytes.length);
		System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
				textBytes.length);

		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
				new byte[0], data);
	}

}