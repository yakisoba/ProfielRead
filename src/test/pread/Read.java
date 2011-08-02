package test.pread;

import test.pread.R.id;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Read extends Activity {
	NfcAdapter mAdapter;
	NdefReadMessage ndefmessage;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profiel_read);

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		onNewIntent(getIntent());
	}

	@Override
	public void onResume() {
		super.onResume();
		enableForegroundDispatch();
	}

	@Override
	public void onPause() {
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}

	public void enableForegroundDispatch() {
		IntentFilter[] filters = makeNdefFilter();
		String[][] techLists = makeTechLists();
		// 該当するタグがかざされた時に投げるIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass()), 0);
		mAdapter.enableForegroundDispatch(this, pendingIntent, filters,
				techLists);
	}

	IntentFilter[] makeNdefFilter() {
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException(e);
		}
		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

		// 3種類全てをカバー!
		IntentFilter[] filters = new IntentFilter[] { ndef, tech, tag };
		return filters;
	}

	String[][] makeTechLists() {
		// FeliCaにのみ反応するように…
		String[] tech = new String[] { NfcF.class.getName(), };
		// ACTION_TECH_DISCOVERED 以外には指定しない
		String[][] techLists = new String[][] { null, tech, null };
		return techLists;
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		ndefmessage = new NdefReadMessage(intent);
//		// タグの種類を判別する。
//		if (ndefmessage.tag != null) {
//			finish();
//		}
		// タグの種類を判別する。
		if (ndefmessage.tag != null) {
			((TextView) findViewById(R.id.name)).setText(ndefmessage.name);
			((TextView) findViewById(R.id.sex)).setText(ndefmessage.sex);
			((TextView) findViewById(R.id.age)).setText(ndefmessage.age);
			((TextView) findViewById(R.id.blood)).setText(ndefmessage.blood);
			((TextView) findViewById(R.id.height)).setText(ndefmessage.height);
			((TextView) findViewById(R.id.weight)).setText(ndefmessage.weight);
			((TextView) findViewById(R.id.hobby)).setText(ndefmessage.hobby);
			((TextView) findViewById(R.id.music)).setText(ndefmessage.music);

			((TextView) findViewById(id.tag)).setVisibility(View.INVISIBLE);
		}

	}
}