package test.pread;

import java.util.Arrays;
import java.util.HashMap;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;

/** NfcAdapter#enableForegroundDispatch を試�? */
public class NdefReadMessage {

	public Tag tag;
	public String format;
	public String name;
	public String sex;
	public String age;
	public String blood;
	public String height;
	public String weight;
	public String hobby;
	public String music;

	public NdefReadMessage(Intent intent) {
		this.tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		if (tag != null) {
			String[] techList = tag.getTechList();
			for (String tech : techList) {
				if (Ndef.class.getName().equals(tech)) {
					this.format = tech;
					ndefread(intent);
				}
			}
		}
	}

	private void ndefread(Intent intent) {
		try {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage[] msgs = null;

			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			}

			if (msgs != null) {
				for (NdefMessage msg : msgs) {
					NdefRecord[] records = msg.getRecords();
					String output = parseTextRecord(records[0]);
					String[] str = output.split("/");
					this.name = str[0];
					this.sex = str[1];
					this.age = str[2];
					this.blood = str[3];
					this.height = str[4];
					this.weight = str[5];
					this.hobby = str[6];
					this.music = str[7];
				}
			} else {
			}
		} catch (Exception e) {
		}
	}

	private String parseTextRecord(NdefRecord record) {
		if (record.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
			throw new IllegalArgumentException("unknown tnf");
		} else if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
			try {
				byte[] payload = record.getPayload();
				String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8"
						: "UTF-16";
				int languageCodeLength = payload[0] & 0x3F;

				@SuppressWarnings("unused")
				String languageCode = new String(payload, 1,
						languageCodeLength, "US-ASCII");
				String text = new String(payload, languageCodeLength + 1,
						payload.length - languageCodeLength - 1, textEncoding);
				return text;
			} catch (Exception e) {
				throw new IllegalStateException("unsupported encoding", e);
			}
		} else {
			throw new IllegalArgumentException("unknown type");
		}
	}
}
