package com.parse.activities;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.helper.ViewGroupUtils;
import com.parse.navdrawerhelper.NavDrawerItem;
import com.parse.navdrawerhelper.NavDrawerListAdapter;
import com.parse.objects.AppUser;
import com.parse.operation.ParseOperation;
import com.parse.operation.ParseOperation.Compare;
import com.parse.test.FakeDataActivity;

public class DashboardActivity extends Activity
		implements
		TabListener,
		OnScrollListener,
		OnItemClickListener {

	private FragmentTransaction mFragmentTransaction;
	private Fragment mCurrentFragment;

	public static final int HOME_QUESTIONS = 0;
	public static final int PERSONAL_QUESTIONS = 1;
	// public static final int FRIEND_QUESTIONS = 2;
	public static final int PROMOTION_QUESTIONS = 2;

	public static int SPINNER_INDEX = 0;

	public static int CURRENT_TAB = 0;

	public boolean firstTimeLoading = true;

	private static boolean CACHING = true;

	// Contains the list of all the questions for to be displayed.
	public static ListFragmentQuestions displayQuestionsList_ = new ListFragmentQuestions();

	// These lists hold each set of questions per tab. Size of this array of
	// lists is equal to the number of tabs on the dashboard.
	public static ArrayList tabFragmentData[] = new ArrayList[4];

	// navigation bar variables
	private DrawerLayout mDrawerLayout;
	public static ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String[] mNavigationTitles;
	private TypedArray navMenuIcons;
	private ArrayList<NavDrawerItem> navDrawerItems;
	public NavDrawerListAdapter adapter;

	private static Spinner dashboardSpinner;
	private ArrayAdapter<String> homeAdapter;
	private ArrayAdapter<String> personalAdapter;
	private ArrayAdapter<String> friendAdapter;

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dashboard);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Navigation drop down
		homeAdapter = getSpinOptionAdapter(getResources(), getBaseContext(),
				R.array.dashboard_spinner_home_items);
		personalAdapter = getSpinOptionAdapter(getResources(),
				getBaseContext(), R.array.dashboard_spinner_personal_items);
		friendAdapter = getSpinOptionAdapter(getResources(), getBaseContext(),
				R.array.dashboard_spinner_friend_items);

		int titleId = Resources.getSystem().getIdentifier("action_bar_title",
				"id", "android");
		View titleView = findViewById(titleId);
		// attach listener to this spinner for handling spinner selection change
		dashboardSpinner = (Spinner) getLayoutInflater().inflate(
				R.layout.dashboard_home_spinner, null);
		dashboardSpinner = (Spinner) getLayoutInflater().inflate(
				R.layout.dashboard_personal_spinner, null);
		attachListenerToSpinner(dashboardSpinner);

		// replaces viewgroup
		ViewGroupUtils.replaceView(titleView, dashboardSpinner);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText("Home")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Personal")
				.setTabListener(this));
		// actionBar.addTab(actionBar.newTab().setText("Friends")
		// .setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Promotion")
				.setTabListener(this));

		// Set up the add question button
		/*
		 * Button bn = (Button) findViewById(R.id.dashboard_btn_ask_question);
		 * bn.getBackground().setAlpha(80);
		 * bn.setTextColor(Color.parseColor("#ffffff"));
		 * bn.setOnClickListener(new View.OnClickListener() { public void
		 * onClick(View v) { Helper.LaunchActivity(getApplicationContext(),
		 * AskQuestionActivity.class); } });
		 */

		// load navigation drawer items
		mNavigationTitles = getResources().getStringArray(
				R.array.navigation_options);
		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Add Poll
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Search
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[1], navMenuIcons
				.getResourceId(1, -1)));
		// Search Category
		// navDrawerItems.add(new NavDrawerItem(mNavigationTitles[2],
		// navMenuIcons
		// .getResourceId(2, -1)));
		// int subscribedCategories = (ArrayList<Integer>)
		// AppUser.getCurrentUserSubscribedCategories().size();
		// Subscriptions
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[2], navMenuIcons
				.getResourceId(2, -1), true, String.valueOf(AppUser
				.getCurrentUserNumberOfSubscribedCategories())));
		// Friend Manager
		// navDrawerItems.add(new NavDrawerItem(mNavigationTitles[4],
		// navMenuIcons
		// .getResourceId(4, -1), true, "50+"));
		// Community
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[3], navMenuIcons
				.getResourceId(3, -1)));
		// Profile
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[4], navMenuIcons
				.getResourceId(4, -1)));
		// Feedback
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[5], navMenuIcons
				.getResourceId(5, -1)));
		// About Us
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[6], navMenuIcons
				.getResourceId(6, -1)));
		// Log Out
		navDrawerItems.add(new NavDrawerItem(mNavigationTitles[7], navMenuIcons
				.getResourceId(7, -1)));

		// Fake Data
		//navDrawerItems.add(new NavDrawerItem(mNavigationTitles[8], navMenuIcons
		//		.getResourceId(8, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		mDrawerList.setOnItemClickListener(this);
		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
				// accessibility
				R.string.app_name // nav drawer close - description for
				// accessibility
		) {
			public void onDrawerClosed(View view) {
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		displayQuestionsList_.con = getApplicationContext();
	}

	void attachListenerToSpinner(Spinner sp) {
		sp.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
									   View selectedItemView, int position, long id) {
				// Log.i("spin_index:" + SPINNER_INDEX, ", position:" + position);
				if (SPINNER_INDEX != position) {
					SPINNER_INDEX = position;
					SharedData.refreshThisTab[CURRENT_TAB] = true;
					loadThisTab(CURRENT_TAB, SPINNER_INDEX);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// Toast.makeText(getApplicationContext, "herf",
				// Toast.LENGTH_LONG).show();
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_dashboard, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		} else if (item.getItemId() == R.id.dashboard_addpoll) {
			Helper.LaunchActivity(getApplicationContext(),
					AskQuestionActivity.class);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// ParseOperation.LogOutCurrentUser(getApplicationContext());
		finish();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	static void updateVoteInUI(ArrayList<ParseObject> mylist) {
		for (int i = 0; i < mylist.size(); i++) {
			String listId = mylist.get(i).getObjectId();
			if (SharedData.quetionVotes.containsKey(listId)) {
				mylist.get(i).put(
						"total_votes",
						SharedData.quetionVotes.get(listId).getInt(
								"total_votes"));
			}
		}
	}

	// Updates all the total_vote counts in the visible tabs for each question
	// that is displayed.
	public static void updateVoteCountInAllVisibleTabs() {
		if (tabFragmentData[HOME_QUESTIONS] != null) {
			updateVoteInUI(tabFragmentData[HOME_QUESTIONS]);
		}
		if (tabFragmentData[PERSONAL_QUESTIONS] != null) {
			updateVoteInUI(tabFragmentData[PERSONAL_QUESTIONS]);
		}
		// if (tabFragmentData[FRIEND_QUESTIONS] != null) {
		// updateVoteInUI(tabFragmentData[FRIEND_QUESTIONS]);
		// }
		if (tabFragmentData[PROMOTION_QUESTIONS] != null) {
			updateVoteInUI(tabFragmentData[PROMOTION_QUESTIONS]);
		}

	}
	public static List<ParseObject> LoadCachedValue(final Context con,
													int index, int spinnderIndex) {
		boolean[] forceRefresh = SharedData.refreshThisTab;
		if ((tabFragmentData[HOME_QUESTIONS] == null || !CACHING || forceRefresh[HOME_QUESTIONS])
				&& index == HOME_QUESTIONS) {
			tabFragmentData[HOME_QUESTIONS] = (ArrayList) ParseOperation
					.GetAllQuestions(con, null, Compare.WASTE, spinnderIndex,
							null);
			forceRefresh[HOME_QUESTIONS] = false;
			ParseOperation
					.GetTotalVotesForQuestionsAndUpdateHashmap(tabFragmentData[index]);

		} else if ((tabFragmentData[PERSONAL_QUESTIONS] == null || !CACHING || forceRefresh[PERSONAL_QUESTIONS])
				&& index == PERSONAL_QUESTIONS) {
			tabFragmentData[PERSONAL_QUESTIONS] = (ArrayList) ParseOperation
					.GetPersonalQuestions(con, null, Compare.WASTE,
							spinnderIndex, null);
			forceRefresh[PERSONAL_QUESTIONS] = false;
			ParseOperation
					.GetTotalVotesForQuestionsAndUpdateHashmap(tabFragmentData[index]);
			// } else if ((tabFragmentData[FRIEND_QUESTIONS] == null || !CACHING
			// || forceRefresh[FRIEND_QUESTIONS])
			// && index == FRIEND_QUESTIONS) {
			// tabFragmentData[FRIEND_QUESTIONS] = new ArrayList<ParseObject>();
			// forceRefresh[FRIEND_QUESTIONS] = false;
			// ParseOperation
			// .GetTotalVotesForQuestionsAndUpdateHashmap(tabFragmentData[index]);
		} else if ((tabFragmentData[PROMOTION_QUESTIONS] == null || !CACHING || forceRefresh[PROMOTION_QUESTIONS])
				&& index == PROMOTION_QUESTIONS) {
			tabFragmentData[PROMOTION_QUESTIONS] = (ArrayList) ParseOperation
					.GetPromotedQuestions(con, null);
			forceRefresh[PROMOTION_QUESTIONS] = false;
			ParseOperation
					.GetTotalVotesForQuestionsAndUpdateHashmap(tabFragmentData[index]);
		}

		// Update the votes in the hashmap.

		SharedData.refreshThisTab = forceRefresh;
		return tabFragmentData[index];
	}
	@Override
	public void onTabSelected(ActionBar.Tab tab,
							  FragmentTransaction fragmentTransaction) {
		// In order to avoid a null pointer exception because all the spinners
		// dont have the same number of values. Changing a tab will reset
		// spinner to 0.
		if (tab.getPosition() != CURRENT_TAB)
			SPINNER_INDEX = 0;

		// Reset the question query parameters if a different tab is
		// selected. This function is called only when the tab changes, not
		// when a tab is reselected.
		SharedData.QuerySkip = 0;
		SharedData.QueryLimit = SharedData.QUERY_LIMIT_PER_PAGE;

		// Sets current tab
		DashboardActivity.CURRENT_TAB = tab.getPosition();
		loadThisTab(CURRENT_TAB, SPINNER_INDEX);
	}

	static ArrayAdapter<String> getSpinOptionAdapter(final Resources res,
													 final Context con, int id) {
		String[] spinOptions = res.getStringArray(id);
		ArrayAdapter<String> spinOptionAdapter = new ArrayAdapter<String>(con,
				R.layout.spinner_item, spinOptions);
		spinOptionAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinOptionAdapter;
	}

	void loadThisTab(int tabIndex, int spinnderIndex) {

		// Get the correct questions the user needs to see. They are coming from
		// the cached value, or being requested and cached.
		if (tabIndex == HOME_QUESTIONS) {
			displayQuestionsList_.question_objects = LoadCachedValue(
					getApplicationContext(), HOME_QUESTIONS, spinnderIndex);
			dashboardSpinner.setVisibility(View.VISIBLE);
			dashboardSpinner.setAdapter(homeAdapter);
			attachListenerToSpinner(dashboardSpinner);
			dashboardSpinner.setSelection(SPINNER_INDEX);
		} else if (tabIndex == PERSONAL_QUESTIONS) {
			displayQuestionsList_.question_objects = LoadCachedValue(
					getApplicationContext(), PERSONAL_QUESTIONS, spinnderIndex);
			dashboardSpinner.setVisibility(View.VISIBLE);
			dashboardSpinner.setAdapter(personalAdapter);
			dashboardSpinner.setSelection(SPINNER_INDEX);
			attachListenerToSpinner(dashboardSpinner);
			// } else if (tabIndex == FRIEND_QUESTIONS) {
			// displayQuestionsList_.question_objects = LoadCachedValue(
			// getApplicationContext(), FRIEND_QUESTIONS, spinnderIndex);
			// dashboardSpinner.setVisibility(View.VISIBLE);
			// dashboardSpinner.setAdapter(friendAdapter);
			// dashboardSpinner.setSelection(SPINNER_INDEX);
		} else if (tabIndex == PROMOTION_QUESTIONS) {
			displayQuestionsList_.question_objects = LoadCachedValue(
					getApplicationContext(), PROMOTION_QUESTIONS, spinnderIndex);
			dashboardSpinner.setVisibility(View.GONE);
		}

		// Set the fragment since the correct data has been retrieved or used
		// from the cached object.
		SetFragmentAndData();
	}

	void SetFragmentAndData() {
		mFragmentTransaction = this.getFragmentManager().beginTransaction();
		if (mCurrentFragment != null) {
			mFragmentTransaction.remove(mCurrentFragment);
		}
		mFragmentTransaction.add(R.id.dashboard_testn, displayQuestionsList_);
		mFragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		mFragmentTransaction.commit();
		mCurrentFragment = displayQuestionsList_;
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
								FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
								FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	private void selectItem(int position) {
		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		// Toast.makeText(getApplicationContext(), mNavigationTitles[position],
		// Toast.LENGTH_SHORT).show();
		if (mNavigationTitles[position].equalsIgnoreCase("New Poll")) {
			Helper.LaunchActivity(getApplicationContext(),
					AskQuestionActivity.class);
		} else if (mNavigationTitles[position].equalsIgnoreCase("Profile")) {
			Helper.LaunchActivity(getApplicationContext(),
					ViewUserProfileActivity.class);
		} else if (mNavigationTitles[position].equalsIgnoreCase("Feedback")) {
			Helper.LaunchActivity(getApplicationContext(),
					SuggestionActivity.class);
		} else if (mNavigationTitles[position].equalsIgnoreCase("About Us")) {
			Helper.LaunchActivity(getApplicationContext(),
					AboutUsActivity.class);
		} else if (mNavigationTitles[position].equalsIgnoreCase("Log Out")) {
			ParseOperation.LogOutCurrentUser(getApplicationContext());
			Helper.LaunchActivity(getApplicationContext(), LoginActivity.class);
			finish();
		} else if (mNavigationTitles[position].equalsIgnoreCase("Fake Data")) {
			Helper.LaunchActivity(getApplicationContext(),
					FakeDataActivity.class);
		} else if (mNavigationTitles[position].equalsIgnoreCase("Community")) {
			Helper.LaunchActivity(getApplicationContext(),
					CommunityActivity.class);
		} else if (mNavigationTitles[position]
				.equalsIgnoreCase("Friend Manager")) {
			Helper.LaunchActivity(getApplicationContext(),
					FriendManagerActivity.class);
		} else if (mNavigationTitles[position]
				.equalsIgnoreCase("Subscriptions")) {
			Helper.LaunchActivity(getApplicationContext(),
					SubscriptionActivity.class);
		} else if (mNavigationTitles[position].equalsIgnoreCase("Search")) {
			Helper.LaunchActivity(getApplicationContext(), SearchActivity.class);
		} else if (mNavigationTitles[position]
				.equalsIgnoreCase("Search Category")) {
			Helper.LaunchActivity(getApplicationContext(),
					SearchCategoryActivity.class);
		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		selectItem(position);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (firstTimeLoading) {
			firstTimeLoading = false;
		} else {
			// Go through the list of dirty tabs and refresh them.
			for (int i = 0; i < SharedData.refreshThisTab.length; i++) {
				if (SharedData.refreshThisTab[i]
						&& DashboardActivity.CURRENT_TAB == i) {
					loadThisTab(i, SPINNER_INDEX);
				}
			}
		}
		displayQuestionsList_.listQAdapter.notifyDataSetChanged();

		// Set the subscriptoin topic numbers.
		navDrawerItems.get(2).setCount(
				String.valueOf(AppUser
						.getCurrentUserNumberOfSubscribedCategories()));
	}

}
