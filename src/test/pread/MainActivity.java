package test.pread;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public void onResume() {
		super.onResume();

		// NFC利用可能チェック
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
		boolean hasNfc = getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_NFC);
		if (adapter != null && adapter.isEnabled()) {
			// NFC Ready!
			findViewById(R.id.read).setOnClickListener(this);
			findViewById(R.id.write).setOnClickListener(this);
		} else if (hasNfc) {
			// NFC 搭載だけどONになってない
			Toast.makeText(this, "NFCを有効にしてください", Toast.LENGTH_LONG).show();
			Intent intent = new Intent();
			intent.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
			startActivity(intent);
		} else {
			findViewById(R.id.read).setEnabled(false);
			findViewById(R.id.write).setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		// ボタンが押されたら対応するActivityに遷移する.
		Class<? extends Activity> clazz;
		switch (v.getId()) {
		case R.id.read:
			clazz = Read.class;
			break;
		case R.id.write:
			clazz = Write.class;
			break;
		default:
			throw new IllegalStateException();
		}
		Intent intent = new Intent(this, clazz);
		startActivity(intent);
	}

}