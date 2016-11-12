package com.parse.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.account.AccountHandler;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.AppUser;
import com.parse.objects.Question;
import com.parse.operation.ParseOperation;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity
{

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUser;
	private String mPassword;

	// UI references.
	private EditText mUserView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private CheckBox mRememberView;
	private TextView mLearnMoreView;

	// save password
	private AppUser registered_user = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		ParseAnalytics.trackAppOpened(getIntent());

		// Set up the login form.
		mUser = getIntent().getStringExtra(EXTRA_EMAIL);
		mUserView = (EditText) findViewById(R.id.email);
		mUserView.setText(mUser);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
												  KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});


		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

		TextView passReset = (TextView) findViewById(R.id.login_password_reset);
		passReset.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				showPasswordResetDialogue();
			}
		});

		// checkbox
		mRememberView = (CheckBox) findViewById(R.id.checkbox_me);
		if(AccountHandler.getRememberMe(getApplicationContext()) == true)
		{
			mUserView.setText(AccountHandler.getUserName(getApplicationContext()));
			mPasswordView.setText(AccountHandler.getUserPassword(getApplicationContext()));
			mRememberView.setChecked(true);
		}

		mLearnMoreView = (TextView) findViewById(R.id.login_learn_more);
		mLearnMoreView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						LearnMoreActivity.class);
				startActivity(i);
			}
		});
	}

	void showPasswordResetDialogue() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Reset Password");

		// Set up the input
		final EditText login = new EditText(this);
		final EditText email = new EditText(this);

		login.setText("");
		// Specify the type of input expected; this, for example, sets the input
		// as a password, and will mask the text
		login.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_NORMAL);
		email.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);

		login.clearFocus();
		email.clearFocus();
		login.setHint("Username");
		email.setHint("Email Address");
		addDefaultText(login, "Username");
		addDefaultText(email, "Email Address");

		ll.addView(login);
		ll.addView(email);
		builder.setView(ll);

		builder.setCancelable(false);
		builder.setPositiveButton("Send email",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						ParseOperation.ResetPassword(
								email.getText().toString(), login.getText()
										.toString(), getApplicationContext());
					}
				});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	void addDefaultText(final EditText nameEdit, final String nameDefaultValue) {
		nameEdit.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (nameEdit.getText().toString().equals(nameDefaultValue)) {
					nameEdit.setText("");
				}
				return false;
			}

		});

		nameEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus
						&& TextUtils.isEmpty(nameEdit.getText().toString())) {
					nameEdit.setText(nameDefaultValue);
				} else if (hasFocus
						&& nameEdit.getText().toString()
						.equals(nameDefaultValue)) {

				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_login_activity, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUserView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUser = mUserView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		if(TextUtils.isEmpty(mPassword) && TextUtils.isEmpty(mUser) && registered_user == null)
		{
			Intent i = new Intent(getApplicationContext(),
					RegisterActivity.class);
			startActivity(i);
		}
		else
		{
			boolean cancel = false;
			View focusView = null;

			// Check for a valid password.
			if (TextUtils.isEmpty(mPassword)) {
				mPasswordView.setError(getString(R.string.error_field_required));
				focusView = mPasswordView;
				cancel = true;
			} else if (mPassword.length() < 7) {
				mPasswordView.setError("Password have at least 7 characters");
				focusView = mPasswordView;
				cancel = true;
			}

			// Check for a valid email address.
			if (TextUtils.isEmpty(mUser)) {
				mUserView.setError(getString(R.string.error_field_required));
				focusView = mUserView;
				cancel = true;
			}

			if (cancel) {
				// There was an error; don't attempt login and focus the first
				// form field with an error.
				focusView.requestFocus();
			} else {
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				mAuthTask.execute((Void) null);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	protected void onStart() {
		super.onStart();

		// -_-
		//setRandomQuote();

		// Check to see if you are online.
		Boolean networkState = Helper.AmIOnline(getApplicationContext());

		if (networkState == false) {
			Helper.ShowDialogue("No internet detected",
					"Please connect to the internet and relaunch.",
					getApplicationContext());
			finish();
			return;
		}

		// Check to see if account exists
		if (AccountHandler.getUserName(LoginActivity.this).length() == 0) {
			// Do nothing, login activity is shown
		} else {
			registered_user = new AppUser();
			registered_user.username = AccountHandler
					.getUserName(getApplicationContext());
			registered_user.password = AccountHandler
					.getUserPassword(getApplicationContext());
			mPasswordView.setText(registered_user.password);
			mUserView.setText(registered_user.username);
			attemptLogin();
		}

	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
	{
		private String errString = "";

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			SetLoginFormData();

			if (registered_user != null)
			{
				ParseUser user;
				try
				{
					user = ParseUser.logIn(registered_user.username, registered_user.password);
					if (user != null) {
						// Update new user in the shared objects.
						SharedData.current_user = user;

						if(mRememberView.isChecked())
						{
							// Add to shared preferences
							AccountHandler.setUserNameAndPassword(getApplicationContext(), registered_user.username,
									registered_user.password);
						}
						else
						{
							AccountHandler.clear(getApplicationContext());
						}
						return true;
					}
					else
					{
						errString = "Something awful happened :(";
						return false;
					}
				}
				catch (ParseException e1)
				{
					errString = e1.getMessage();
					return false;
				}
			}

			// TODO: register the new account here.
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;

			if (success) {
				finish();
				Helper.LaunchActivity(getApplicationContext(),
						DashboardActivity.class);
			} else {
				AccountHandler.clear(getApplicationContext());
				registered_user = null;
				mPasswordView.setError(errString);
				mPasswordView.requestFocus();
			}
			showProgress(false);
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	public void SetLoginFormData() {
		if(registered_user == null)
		{
			registered_user = new AppUser();

			registered_user.username = mUserView.getText().toString();
			registered_user.password = mPasswordView.getText().toString();
		}
	}

	public void onCheckboxClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();

		if(view.getId() == R.id.checkbox_me)
		{
			Log.e("", ""+checked);
		}
	}

	public void setRandomQuote()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery("HanTest");
		try {
			List<ParseObject> objects = query.find();
			Log.e("", ""+objects.size());
			TextView tv = (TextView) findViewById(R.id.login_textview);

			tv.setText(objects.get((int) (Math.random() * 6)).getString("quotes"));
		} catch (com.parse.ParseException e1) {
			Helper.ShowDialogue("Error loading all questions", e1.getMessage(),
					getApplicationContext());
		}


	}
}
/*public class LoginActivity extends Activity {

	public static ProgressDialog loginProgress = null;
	private AlertDialog alert11;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		ParseAnalytics.trackAppOpened(getIntent());
		TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

		// Listening to register new account link
		registerScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Switching to Register screen
				Intent i = new Intent(getApplicationContext(),
						RegisterActivity.class);
				startActivity(i);
			}
		});

		loginProgress = new ProgressDialog(this);

		// Submit user name and password entered on the form.
		Button loginButon = (Button) findViewById(R.id.btnLogin);
		loginButon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				new LoginUser().execute();
			}
		});
		TextView passReset = (TextView) findViewById(R.id.login_password_reset);
		passReset.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				showPasswordResetDialogue();
			}
		});
		
		AlertDialog.Builder builder1 = new AlertDialog.Builder(getApplicationContext());
        builder1.setMessage("Write your message here.");
        alert11 = builder1.create();        
	}

	void showPasswordResetDialogue() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Reset Password");

		// Set up the input
		final EditText login = new EditText(this);
		final EditText email = new EditText(this);

		login.setText("");
		// Specify the type of input expected; this, for example, sets the input
		// as a password, and will mask the text
		login.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_NORMAL);
		email.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);

		login.clearFocus();
		email.clearFocus();
		login.setText("Username");
		email.setText("Email Address");
		addDefaultText(login, "Username");
		addDefaultText(email, "Email Address");

		ll.addView(login);
		ll.addView(email);
		builder.setView(ll);

		builder.setCancelable(false);
		builder.setPositiveButton("Send email",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						ParseOperation.ResetPassword(
								email.getText().toString(), login.getText()
										.toString(), getApplicationContext());
					}
				});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	void addDefaultText(final EditText nameEdit, final String nameDefaultValue) {
		nameEdit.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (nameEdit.getText().toString().equals(nameDefaultValue)) {
					nameEdit.setText("");
				}
				return false;
			}
		});

		nameEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus
						&& TextUtils.isEmpty(nameEdit.getText().toString())) {
					nameEdit.setText(nameDefaultValue);
				} else if (hasFocus
						&& nameEdit.getText().toString()
								.equals(nameDefaultValue)) {

				}
			}
		});
	}
	private class LoginUser extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... arg0) {
			AppUser reg_user = GetLoginFormData();
			if (reg_user != null) {
				loginAndPerformNextAction(reg_user);
			}
			return null;
		}

		protected void onPreExecute() {
			loginProgress.setMessage("Log In... ");
			loginProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			loginProgress.setIndeterminate(true);
			loginProgress.show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Check to see if you are online.
		Boolean networkState = Helper.AmIOnline(getApplicationContext());

		if (networkState == false) {
			Helper.ShowDialogue("No internet detected",
					"Please connect to the internet and relaunch.",
					getApplicationContext());
			finish();
			return;
		}

		// Check to see if account exists
		if (AccountHandler.getUserName(LoginActivity.this).length() == 0) {
			// Do nothing, login activity is shown
		} else {
			AppUser reg_user = new AppUser();
			reg_user.username = AccountHandler
					.getUserName(getApplicationContext());
			reg_user.password = AccountHandler
					.getUserPassword(getApplicationContext());;
			loginAndPerformNextAction(reg_user);
		}

	}

	public void loginAndPerformNextAction(AppUser reg_user) {
		if (ParseOperation.LoginUser(reg_user, getApplicationContext())) {
			Helper.LaunchActivity(getApplicationContext(),
					DashboardActivity.class);
			finish();
		} else {
			if(LoginActivity.loginProgress != null)
				if(LoginActivity.loginProgress.isShowing())
					LoginActivity.loginProgress.dismiss();
			AccountHandler.clear(getApplicationContext());
			
			runOnUiThread(new Runnable() 
			{
				public void run() 
				{
					Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT).show();  			   
				}
			}); 
		}
	}
	public AppUser GetLoginFormData() {
		AppUser registered_user = new AppUser();

		// Extract all the values from the view input fields.
		EditText tv = (EditText) findViewById(R.id.login_username);
		if (Helper.isEmpty(tv.getText().toString())) {
			Helper.ShowDialogue("Error", "Enter a login name",
					getApplicationContext());
			return null;
		}
		registered_user.username = tv.getText().toString();

		tv = (EditText) findViewById(R.id.login_password);
		if (Helper.isEmpty(tv.getText().toString())) {
			Helper.ShowDialogue("Error", "Enter a password",
					getApplicationContext());
			return null;
		}
		registered_user.password = tv.getText().toString();
		return registered_user;
	}
}*/
