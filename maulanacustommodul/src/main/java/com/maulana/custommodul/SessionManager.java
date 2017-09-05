package com.maulana.custommodul;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;
	
	// Editor for Shared preferences
	Editor editor;
	
	// Context
	Context context;
	
	// Shared pref mode
	int PRIVATE_MODE = 0;
	
	// Sharedpref file name
	private static final String PREF_NAME = "GmediaUser";
	
	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";
	public static final String TAG_UID = "uid";
	public static final String TAG_NIK = "nik";
	public static final String TAG_NAMA = "nama";
	public static final String TAG_PASSWORD = "password";
	public static final String TAG_TOKEN = "token";
	public static final String TAG_EXP = "expired_at";
	public static final String TAG_LEVEL = "level";

	// Constructor
	public SessionManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void createLoginSession(String uid,String nik, String nama, String password, String token, String exp, String level){

		editor.putBoolean(IS_LOGIN, true);
		
		editor.putString(TAG_UID, uid);
		
		editor.putString(TAG_NIK, nik);

		editor.putString(TAG_NAMA, nama);

		editor.putString(TAG_PASSWORD, password);

		editor.putString(TAG_TOKEN, token);

		editor.putString(TAG_EXP, exp);

		editor.putString(TAG_LEVEL, level);
		// commit changes
		editor.commit();
	}	

	
	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getUserDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		// user uid
		user.put(TAG_UID, pref.getString(TAG_UID, null));
		
		// user nik
		user.put(TAG_NIK, pref.getString(TAG_NIK, null));

		user.put(TAG_NAMA, pref.getString(TAG_NAMA, null));

		user.put(TAG_PASSWORD, pref.getString(TAG_PASSWORD, null));

		user.put(TAG_TOKEN, pref.getString(TAG_TOKEN, null));

		user.put(TAG_EXP, pref.getString(TAG_EXP, null));

		user.put(TAG_LEVEL, pref.getString(TAG_LEVEL, null));
		// return user
		return user;
	}

	public String getUser(){
		return pref.getString(TAG_NAMA, null);
	}

	public String getLevel(){
		return pref.getString(TAG_LEVEL, null);
	}

	/**
	 * Clear session details
	 * */
	public void logoutUser(Intent logoutIntent){
		// Clearing all data from Shared Preferences
		try {
			editor.clear();
			editor.commit();
		}catch (Exception e){
			e.printStackTrace();
		}

		logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(logoutIntent);
	}
	
	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn(){
		if(getUserDetails().get(TAG_NAMA) != null){
			return true;
		}else{
			return false;
		}
		/*return pref.getBoolean(IS_LOGIN, false);*/
	}
}
