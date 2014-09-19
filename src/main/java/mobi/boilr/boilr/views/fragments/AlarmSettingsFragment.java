package mobi.boilr.boilr.views.fragments;

import java.util.ArrayList;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Pair;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.view.MenuItem;

public abstract class AlarmSettingsFragment extends PreferenceFragment{


	public static final String PREF_VALUE_PRICE_HIT = "price_hit";
	public static final String PREF_VALUE_PRICE_VAR = "price_var";
	public static final String PREF_KEY_EXCHANGE = "exchange";
	public static final String PREF_KEY_TYPE = "type";
	public static final String PREF_KEY_PAIR = "pair";
	public static final String PREF_TYPE_DEFAULT_VALUE = "Price Hit";
	protected static final String PREF_KEY_ALARM_ALERT_SOUND = "pref_key_alarm_alert_sound";
	protected static final String PREF_KEY_ALARM_ALERT_TYPE = "pref_key_alarm_alert_type";
	//protected static final String PREF_KEY_ALARM_UPDATE_INTERVAL_VAR = "pref_key_alarm_update_interval_var";
	public static final String PREF_KEY_ALARM_VIBRATE = "pref_key_alarm_vibrate";

	protected  OnAlarmSettingsPreferenceChangeListener listener = new OnAlarmSettingsPreferenceChangeListener();

	protected class OnAlarmSettingsPreferenceChangeListener implements OnPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.d(preference.getKey() +" " + newValue);
			if(preference.getKey().equals(PREF_KEY_EXCHANGE)){
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary(( listPreference).getEntries()[(listPreference).findIndexOfValue((String) newValue)]);
				if(mBound){
					pairs = mStorageAndControlService.getPairs((String) newValue);
					CharSequence[] sequence = new CharSequence[pairs.size()] ;
					CharSequence[] ids = new CharSequence[pairs.size()] ;

					for(int i = 0 ; i<pairs.size(); i++){
						sequence[i] = pairs.get(i).getCoin() + "/"+pairs.get(i).getExchange();
						ids[i] = String.valueOf(i);
					}
					ListPreference pairListPreference = (ListPreference) findPreference(PREF_KEY_PAIR);
					pairListPreference.setEntries(sequence);
					pairListPreference.setEntryValues(ids);
					pairListPreference.setDefaultValue(ids[0]);
					pairListPreference.setSummary(sequence[0]);
					pairListPreference.setValueIndex(0);
				}else{
					Log.d("Not Bound");
				}
			}else if(preference.getKey().equals(PREF_KEY_PAIR)){	
				preference.setSummary((CharSequence) newValue);
			}else if(preference.getKey().equals(PREF_KEY_TYPE)){
				if(newValue.equals(PREF_VALUE_PRICE_VAR)){
					getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceVarAlarmSettingsFragment()).commit();
				}else if(newValue.equals(PREF_VALUE_PRICE_HIT)){
					getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceHitAlarmSettingsFragment()).commit();
				}
			}else if(preference.getKey().equals(PREF_KEY_ALARM_ALERT_TYPE)) {
				ListPreference alertTypePref = (ListPreference) preference;
				alertTypePref.setSummary(alertTypePref.getEntries()[alertTypePref.findIndexOfValue((String) newValue)]);
				// Change selectable ringtones according to the alert type
				RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
				int ringtoneType = Integer.parseInt((String) newValue);
				alertSoundPref.setRingtoneType(ringtoneType);
				String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
				alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(defaultRingtone, getActivity()));
			}else if(preference.getKey().equals(PREF_KEY_ALARM_ALERT_SOUND)){
				RingtonePreference alertSoundPref = (RingtonePreference) preference;
				//alertSoundPref.setSummary((CharSequence) newValue);
				alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName((String) newValue, getActivity()));
			}else if(preference.getKey().equals(PREF_KEY_ALARM_VIBRATE)){
				defaultVibrateDefinition = false;
			}{
				Log.d("No behavior for " + preference.getKey());
			}
			return true;	
		}
	}




	protected StorageAndControlService mStorageAndControlService;
	protected boolean mBound;
	private ServiceConnection mStorageAndControlServiceConnection;

	private class StorageAndControlServiceConnection implements ServiceConnection{

		private CharSequence exchangeCode;
		private ListPreference pairListPreference;

		public StorageAndControlServiceConnection(CharSequence exchangeCode, ListPreference pairListPreference){
			this.exchangeCode = exchangeCode;
			this.pairListPreference = pairListPreference;

		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;

			// Callback action performed after the service has been bound
			if(mBound) {
				pairs = mStorageAndControlService.getPairs((String) exchangeCode);
				CharSequence[] sequence = new CharSequence[pairs.size()] ;
				CharSequence[] ids = new CharSequence[pairs.size()] ;

				for(int i = 0 ; i<pairs.size(); i++){
					sequence[i] = pairs.get(i).getCoin() + "/"+pairs.get(i).getExchange();
					ids[i] = String.valueOf(i);
				}

				pairListPreference.setEntries(sequence);
				pairListPreference.setEntryValues(ids);
				//pairListPreference.setEntryValues(sequence);
				pairListPreference.setSummary(sequence[0]);
				pairListPreference.setValueIndex(0);;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	protected boolean defaultVibrateDefinition = true;
	protected List<Pair> pairs = new ArrayList<Pair>();

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		addPreferencesFromResource(R.xml.alarm_settings);

		//First Entry as default
		ListPreference listPref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
		CharSequence defaultEntry = listPref.getEntries()[0]; 
		CharSequence exchangeCode = listPref.getEntryValues()[0];
		listPref.setSummary(defaultEntry);
		listPref.setValueIndex(0);
		listPref.setOnPreferenceChangeListener(listener);

		ListPreference pairListPreference = (ListPreference) findPreference(PREF_KEY_PAIR);
		Intent serviceIntent = new Intent(getActivity(), StorageAndControlService.class);
		getActivity().startService(serviceIntent);
		mStorageAndControlServiceConnection = new StorageAndControlServiceConnection(exchangeCode,pairListPreference);
		getActivity().bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);



		listPref.setOnPreferenceChangeListener(listener);

		listPref = (ListPreference) findPreference(PREF_KEY_TYPE);
		listPref.setOnPreferenceChangeListener(listener);

		SharedPreferences sharedPreferences = 	PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		listPref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
		listPref.setSummary(listPref.getEntries()[listPref.findIndexOfValue(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE, ""))]);
		listPref.setValue(null);
		listPref.setOnPreferenceChangeListener(listener);

		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
		//ListPreference alertTypePref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
		alertSoundPref.setDefaultValue(null);
		sharedPreferences.edit().putString(PREF_KEY_ALARM_ALERT_SOUND, "");
		alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""),getActivity()));
		alertSoundPref.setOnPreferenceChangeListener(listener);


		setHasOptionsMenu(true);
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_send_now:
				makeAlarm();
			default:
				return super.onOptionsItemSelected(item);
		} 
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mStorageAndControlService = null;
		mBound = false;
	}

	protected abstract void makeAlarm();
}
