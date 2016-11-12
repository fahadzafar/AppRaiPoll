package com.parse.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;
import com.parse.operation.ParseOperation.Compare;
import com.parse.pulltorefresh.PullToRefreshListView;
import com.parse.pulltorefresh.PullToRefreshListView.OnRefreshListener;

public class ListFragmentQuestions extends ListFragment
		implements
			OnRefreshListener,
			OnScrollListener,
			OnTouchListener {
	static ProgressDialog progress_dialogue_;
	public List<ParseObject> question_objects;
	public static Context con;
	private Toast refreshMsg, lastItemMsg;
	private PullToRefreshListView mPullToRefreshListView;
	private float mInitialY;

	public static boolean scrollDown = false;
	public static boolean paginatedTask = false;

	public ListQuestionAdapter listQAdapter;

	public static ProgressDialog itemClickProgress = null;

	public ListFragmentQuestions() {
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mPullToRefreshListView = ((PullToRefreshListView) getListView());
		mPullToRefreshListView.setOnRefreshListener(this);

		getListView().setOnScrollListener(this);
		getListView().setOnTouchListener(this);

		refreshMsg = Toast.makeText(con, "Loading more items...",
				Toast.LENGTH_SHORT);
		lastItemMsg = Toast.makeText(con, "Loading more data...",
				Toast.LENGTH_SHORT);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		listQAdapter = new ListQuestionAdapter(getActivity(), question_objects);
		// Set up the list view adapter.
		setListAdapter(listQAdapter);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.list_fragment, container, false);
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		itemClickProgress = new ProgressDialog(getActivity());
		itemClickProgress.setMessage("Loading...");
		itemClickProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		itemClickProgress.setIndeterminate(true);

		// You retrieve the object you set in the adapter. We are using
		// a ParseObject adapter so we cast everything into ParseObject
		// first before using it.
		new ItemClick(getListView(), position, getActivity()).execute();

	}

	public void onRefresh() {
		// SharedData.QuerySkip = 0;
		new GetRefreshDataTask().execute();
	}

	class GetRefreshDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Simulates a background job.
			try {
				if (DashboardActivity.CURRENT_TAB == DashboardActivity.HOME_QUESTIONS) {
					addNewQuestionsToList(DashboardActivity.HOME_QUESTIONS);
				} else if (DashboardActivity.CURRENT_TAB == DashboardActivity.PERSONAL_QUESTIONS) {
					addNewQuestionsToList(DashboardActivity.PERSONAL_QUESTIONS);
				}// else if (DashboardActivity.CURRENT_TAB ==
					// DashboardActivity.FRIEND_QUESTIONS) {
					// addNewQuestionsToList(DashboardActivity.FRIEND_QUESTIONS);
				// }
				else if (DashboardActivity.CURRENT_TAB == DashboardActivity.PROMOTION_QUESTIONS) {
					addNewQuestionsToList(DashboardActivity.PROMOTION_QUESTIONS);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		void addNewQuestionsToList(int index) {
			// If the tabFrgmentData[index] is null, or its size = 0 means
			// nothing is bing displayed.
			int currentTop = 0;
			ParseObject topViewableObject = null;
			ArrayList<ParseObject> currentlyBeingViewed = null;
			Date usingThisDate = null;
			if (DashboardActivity.tabFragmentData[index] != null
					&& DashboardActivity.tabFragmentData[index].size() != 0) {
				topViewableObject = (ParseObject) DashboardActivity.tabFragmentData[index]
						.get(currentTop);
				currentlyBeingViewed = DashboardActivity.tabFragmentData[index];
			}
			// If there is a top element in the list, get it to compare
			// its date for the query. Otherwise it is initialized to
			// null
			// and the date will be ignored in the retreival.
			if (topViewableObject != null) {
				usingThisDate = topViewableObject.getCreatedAt();
			}

			ArrayList<ParseObject> newlyRetreivedData = new ArrayList<ParseObject>();
			switch (index) {
				case DashboardActivity.HOME_QUESTIONS :
					newlyRetreivedData = (ArrayList<ParseObject>) ParseOperation
							.GetAllQuestions(con, topViewableObject,
									Compare.GREATER_THAN,
									DashboardActivity.SPINNER_INDEX,
									currentlyBeingViewed);
					break;
				case DashboardActivity.PERSONAL_QUESTIONS :
					newlyRetreivedData = (ArrayList<ParseObject>) ParseOperation
							.GetPersonalQuestions(con, topViewableObject,
									Compare.GREATER_THAN,
									DashboardActivity.SPINNER_INDEX,
									currentlyBeingViewed);
					break;
				//case DashboardActivity.FRIEND_QUESTIONS :
					/*
					 * newlyRetreivedData = (ArrayList<ParseObject>)
					 * ParseOperation .GetAllQuestionsPostedByUser(
					 * SharedData.current_user, con, usingThisDate,
					 * Compare.GREATER_THAN);
					 */
					//break;
				case DashboardActivity.PROMOTION_QUESTIONS :
					newlyRetreivedData = (ArrayList) ParseOperation
							.GetPromotedQuestions(con, currentlyBeingViewed);
					break;
			}

			for (ParseObject newQuestion : newlyRetreivedData) {
				// The questions with date greater than the top question
				// date have been retreived. Just add them to the top
				// and keep incrementing top to update the insert
				// location.
				DashboardActivity.tabFragmentData[index].add(currentTop,
						newQuestion);
				currentTop++;
			}
			Log.e("INSIDE", "tap to refresh");
			ParseOperation
					.GetTotalVotesForQuestionsAndUpdateHashmap(DashboardActivity.tabFragmentData[index]);

		}
		protected void onPostExecute(Void result) {

			// Call onRefreshComplete when the list has been refreshed.
			mPullToRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

	private class GetPaginatedDataTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// Simulates a background job.
			try {
				if (DashboardActivity.CURRENT_TAB == DashboardActivity.HOME_QUESTIONS) {
					addOldQuestionsToList(DashboardActivity.HOME_QUESTIONS);
				} else if (DashboardActivity.CURRENT_TAB == DashboardActivity.PERSONAL_QUESTIONS) {
					addOldQuestionsToList(DashboardActivity.PERSONAL_QUESTIONS);
				//} else if (DashboardActivity.CURRENT_TAB == DashboardActivity.FRIEND_QUESTIONS) {
				//	addOldQuestionsToList(DashboardActivity.FRIEND_QUESTIONS);
				} else if (DashboardActivity.CURRENT_TAB == DashboardActivity.PROMOTION_QUESTIONS) {
					addOldQuestionsToList(DashboardActivity.PROMOTION_QUESTIONS);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		// TODO (Han) : Fix these two duplicate functions by merging them
		// into one.
		void addOldQuestionsToList(int index) {
			// Don't add if number of polls shown is less than the default limit
			if (DashboardActivity.tabFragmentData[index].size() < SharedData.QueryLimit) {
				Log.e("GetPaginatedDataTask", "tabFragmentData size less than "
						+ SharedData.QueryLimit);
				return;
			}
			// If the tabFrgmentData[index] is null, or its size = 0 means
			// nothing is being displayed.
			else if (DashboardActivity.tabFragmentData[index] == null
					|| DashboardActivity.tabFragmentData[index].size() == 0) {
				Log.e("GetPaginatedDataTask",
						"tabFragmentData is null or its size is 0");
				return;
			} else {
				Log.e("GetPaginatedDataTask", "tabFragmentData size:"
						+ DashboardActivity.tabFragmentData[index].size()
						+ " equals " + SharedData.QueryLimit);

				int currentBottom = 0;
				ParseObject lastViewableObject = null;
				ArrayList<ParseObject> currentlyBeingViewed = null;
				Date usingThisDate = null;

				currentBottom = DashboardActivity.tabFragmentData[index].size() - 1;
				lastViewableObject = (ParseObject) DashboardActivity.tabFragmentData[index]
						.get(currentBottom);
				// SharedData.QuerySkip = currentBottom+1;
				// Log.e("QuerySkip", "" + SharedData.QuerySkip);

				currentlyBeingViewed = DashboardActivity.tabFragmentData[index];

				// If there is a top element in the list, get it to compare
				// its date for the query. Otherwise it is initialized to
				// null and the date will be ignored in the retrieval.
				if (lastViewableObject != null) {
					usingThisDate = lastViewableObject.getCreatedAt();
				}

				ArrayList<ParseObject> newlyRetreivedData = new ArrayList<ParseObject>();
				switch (index) {
					case DashboardActivity.HOME_QUESTIONS :
						newlyRetreivedData = (ArrayList<ParseObject>) ParseOperation
								.GetAllQuestions(con, lastViewableObject,
										Compare.LESSER_THAN,
										DashboardActivity.SPINNER_INDEX,
										currentlyBeingViewed);
						Log.e("newlyRetreivedData",
								"" + newlyRetreivedData.size());
						break;
					case DashboardActivity.PERSONAL_QUESTIONS :
						newlyRetreivedData = (ArrayList<ParseObject>) ParseOperation
								.GetPersonalQuestions(con, lastViewableObject,
										Compare.LESSER_THAN,
										DashboardActivity.SPINNER_INDEX,
										currentlyBeingViewed);
						Log.e("newlyRetreivedData",
								"" + newlyRetreivedData.size());
						break;
					//case DashboardActivity.FRIEND_QUESTIONS :
						/*
						 * newlyRetreivedData = (ArrayList<ParseObject>)
						 * ParseOperation .GetAllQuestionsPostedByUser(
						 * SharedData.current_user, con, usingThisDate,
						 * Compare.LESSER_THAN);
						 */
						//Log.e("newlyRetreivedData",
						//		"" + newlyRetreivedData.size());
						//break;
					case DashboardActivity.PROMOTION_QUESTIONS :
						newlyRetreivedData = (ArrayList) ParseOperation
								.GetPromotedQuestions(con, currentlyBeingViewed);
						break;
				}

				for (ParseObject newQuestion : newlyRetreivedData) {
					// The questions with date greater than the top question
					// date have been retreived. Just add them to the top
					// and keep incrementing top to update the insert
					// location.
					DashboardActivity.tabFragmentData[index].add(newQuestion);
				}
			}
			ParseOperation
					.GetTotalVotesForQuestionsAndUpdateHashmap(DashboardActivity.tabFragmentData[index]);
		}
		protected void onPostExecute(Void result) {
			paginatedTask = false;
			// Need to avoid error when we change the listview and
			// don't notify the UI thread that it's changed
			listQAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}
	}

	public void onScroll(AbsListView lw, int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {
		switch (lw.getId()) {
			case android.R.id.list :
				final int lastItem = firstVisibleItem + visibleItemCount;
				if (scrollDown && lastItem == totalItemCount) {
					scrollDown = false;
					paginatedTask = true;
					new GetPaginatedDataTask().execute();
					lastItemMsg.show();
				}
		}
	}

	public boolean onTouch(View arg0, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mInitialY = event.getY();
			case MotionEvent.ACTION_MOVE :
				if (event.getY() - mInitialY > 0.0) {
					scrollDown = false;
				} else if (event.getY() - mInitialY < 0.0) {
					if (scrollDown == false && paginatedTask == false) {
						scrollDown = true;
					}
				}
		}
		return false;
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	private class ItemClick extends AsyncTask<Void, Void, Void> {
		private ListView lv;
		private int pos;
		private Activity a;

		public ItemClick(ListView lv, int pos, Activity a) {
			this.lv = lv;
			this.pos = pos;
			this.a = a;
		}

		protected Void doInBackground(Void... arg0) {
			Helper.onQuestionClicked(lv, pos, a);
			return null;
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		protected void onPreExecute() {
			itemClickProgress.show();
		}

		protected void onProgressUpdate(Void... values) {
		}
	}
}
