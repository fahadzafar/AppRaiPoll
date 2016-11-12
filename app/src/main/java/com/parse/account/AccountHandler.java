package com.parse.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AccountHandler {

	static final String PREF_USER_NAME = "username";
	static final String PREF_USER_PASSWORD = "password";
	static final String PREF_REMEMBER_ME = "remember";

	static SharedPreferences getSharedPreferences(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	public static void setUserNameAndPassword(Context ctx, String userName, String password) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putString(PREF_USER_NAME, userName);
		editor.putString(PREF_USER_PASSWORD, password);
		editor.putBoolean(PREF_REMEMBER_ME, true);		
		editor.commit();
	}
	
	

	public static String getUserName(Context ctx) {
		return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
	}

	public static String getUserPassword(Context ctx) {
		return getSharedPreferences(ctx).getString(PREF_USER_PASSWORD, "");
	}

	public static boolean getRememberMe(Context ctx) {
		return getSharedPreferences(ctx).getBoolean(PREF_REMEMBER_ME, false);
	}
	
	public static void clear(Context ctx) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.clear(); // clear all stored data
		editor.commit();
	}
}