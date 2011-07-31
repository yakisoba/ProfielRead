package test.pread;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import test.pread.R.id;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Write extends Activity implements OnClickListener {
	NfcAdapter mAdapter;
	static String mMessage = null;
	static Spinner mSex, mBlood;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profiel_write);
		mAdapter = NfcAdapter.getDefaultAdapter(this);

		findViewById(R.id.input).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		if (mMessage != null) {
			onNewIntent(getIntent());
		}

		setSpinner(R.id.sex, R.array.sex);
		setSpinner(R.id.blood, R.array.blood);

		final Button ageselect = (Button) findViewById(id.age);
		ageselect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new DatePickerDialog(Write.this,
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								int age = calcage(year, monthOfYear, dayOfMonth);
								ageselect.setText(Integer.toString(age));
							}
						}, 2000, 0, 1).show();
			}
		});
	}

	private void setSpinner(int type, int arraytype) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, arraytype, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		};

		if (type == R.id.sex) {
			mSex = (Spinner) findViewById(type);
			mSex.setAdapter(adapter);
			mSex.setOnItemSelectedListener(listener);
		} else if (type == R.id.blood) {
			mBlood = (Spinner) findViewById(type);
			mBlood.setAdapter(adapter);
			mBlood.setOnItemSelectedListener(listener);
		}
	}

	private int calcage(int year, int month, int day) {
		final Calendar calendar = Calendar.getInstance();
		final int t_year = calendar.get(Calendar.YEAR);
		final int t_month = calendar.get(Calendar.MONTH);
		final int t_day = calendar.get(Calendar.DAY_OF_MONTH);

		try {
			int today = (t_year * 10000) + ((t_month + 1) * 100) + (t_day);
			int birthday = (year * 10000) + ((month + 1) * 100) + (day);
			int age = (today - birthday) / 10000;
			return age;
		} catch (Exception e) {
			return -1;
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
			String sex = mSex.getSelectedItem().toString();
			String age = (((Button) findViewById(id.age)).getText().toString());
			String blood = mBlood.getSelectedItem().toString();

			Toast.makeText(this, "NFCタグをかざして下さい", Toast.LENGTH_SHORT).show();

			mMessage = name + "/" + sex + "/" + age + "/" + blood + "/"
					+ height + "/" + weight + "/" + hobby + "/" + music;

			Log.d("NFC", mMessage);

			// 書き込み処理
			enableForegroundDispatch();

			break;
		case R.id.cancel:
			(((EditText) findViewById(id.editName)).getText()).clear();
			(((EditText) findViewById(id.editHeight)).getText()).clear();
			(((EditText) findViewById(id.editWeight)).getText()).clear();
			(((EditText) findViewById(id.editHobby)).getText()).clear();
			(((EditText) findViewById(id.editMusic)).getText()).clear();
			((Spinner) findViewById(id.sex)).setSelection(0);
			((Button) findViewById(id.age)).setText("未設定");
			((Spinner) findViewById(id.blood)).setSelection(0);
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
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			writeNdefMessage(tag);
			finish();
		}
	}

	void writeNdefMessage(Tag tag) {
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
		Toast.makeText(this, "書き込み成功", Toast.LENGTH_SHORT).show();
	}

	void showFailureToast() {
		Toast.makeText(this, "書き込み失敗", Toast.LENGTH_SHORT).show();
	}

	void showNotWritableToast() {
		Toast.makeText(this, "書き込み失敗", Toast.LENGTH_SHORT).show();
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