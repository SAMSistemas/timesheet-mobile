package com.samsistemas.timesheet.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.VolleyError;
import com.samsistemas.timesheet.R;
import com.samsistemas.timesheet.activity.base.BaseAppCompatActivity;
import com.samsistemas.timesheet.adapter.ClientAdapter;
import com.samsistemas.timesheet.adapter.ProjectAdapter;
import com.samsistemas.timesheet.adapter.TaskTypeAdapter;
import com.samsistemas.timesheet.facade.JobLogFacade;
import com.samsistemas.timesheet.facade.base.OnDataFetchListener;
import com.samsistemas.timesheet.loader.ClientsLoader;
import com.samsistemas.timesheet.loader.JobLogLoader;
import com.samsistemas.timesheet.loader.ProjectsLoader;
import com.samsistemas.timesheet.loader.TaskTypeLoader;
import com.samsistemas.timesheet.model.Client;
import com.samsistemas.timesheet.model.JobLog;
import com.samsistemas.timesheet.model.Person;
import com.samsistemas.timesheet.model.Project;
import com.samsistemas.timesheet.model.TaskType;
import com.samsistemas.timesheet.navigation.MenuNavigator;
import com.samsistemas.timesheet.util.ToolbarUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.samsistemas.timesheet.util.SharedPreferenceKeys.FILENAME;
import static com.samsistemas.timesheet.util.SharedPreferenceKeys.USERNAME;
import static com.samsistemas.timesheet.util.SharedPreferenceKeys.PASSWORD;

import static com.samsistemas.timesheet.util.LoaderId.TASK_TYPE_LOADER_ID;
import static com.samsistemas.timesheet.util.LoaderId.CLIENT_LOADER_ID;
import static com.samsistemas.timesheet.util.LoaderId.PROJECT_LOADER_ID;
import static com.samsistemas.timesheet.util.LoaderId.JOBLOG_LOADER_ID;

import static com.samsistemas.timesheet.util.AppConstants.DATE_TEMPLATE;
import static com.samsistemas.timesheet.util.AppConstants.DATE_KEY;
import static com.samsistemas.timesheet.util.AppConstants.JOBLOG_ID_KEY;
import static com.samsistemas.timesheet.util.AppConstants.EDIT_MODE_KEY;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author jonatan.salas
 */
public class AddHoursActivity extends BaseAppCompatActivity {
    private static final String LOG_TAG = AddHoursActivity.class.getSimpleName();

    @Bind(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.hours_spinner)
    Spinner mHourSpinner;

    @Bind(R.id.task_spinner)
    Spinner mTaskSpinner;

    @Bind(R.id.client_spinner)
    Spinner mClientSpinner;

    @Bind(R.id.project_spinner)
    Spinner mProjectSpinner;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Bind(R.id.description)
    EditText mDescription;

    @Bind(R.id.solicitude)
    EditText mSolicitudeNumber;

    private ClientAdapter mClientAdapter;
    private ProjectAdapter mProjectAdapter;
    private TaskTypeAdapter mTaskAdapter;

    private CharSequence mHourSelected;
    //    private Client mClientSelected;
    private Project mProjectSelected;
    private TaskType mTaskTypeSelected;

    private String mDateString = "";
    private Boolean mEditMode = false;
    private long mJobLogId;

    private JobLog mJobLog = new JobLog();

    @Override
    public int getLayoutResourceId() {
        return R.layout.activity_add_hours;
    }

    @Override
    public void setUserInterface() {
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            ToolbarUtil.styleWithBackButton(actionBar, getString(R.string.action_add_hour));
        }

        mToolbarLayout.setTitleEnabled(false);

        mTaskSpinner.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent), PorterDuff.Mode.SRC_ATOP);

        final ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(getApplication(), R.array.hours, android.R.layout.simple_spinner_dropdown_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mHourSpinner.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent), PorterDuff.Mode.SRC_ATOP);
        mHourSpinner.setAdapter(hourAdapter);

        mClientSpinner.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent), PorterDuff.Mode.SRC_ATOP);
        mProjectSpinner.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void initialize() {
        initClientsLoader();
        initProjectsLoader();
        initTaskTypeLoader();

        final Intent intent = getIntent();

        if (null != intent) {
            mDateString = intent.getStringExtra(DATE_KEY);
            mEditMode = intent.getBooleanExtra(EDIT_MODE_KEY, false);

            if (mEditMode) {
                mJobLogId = intent.getLongExtra(JOBLOG_ID_KEY, 0);
                initJobLogLoader();
            }
        }
    }

    @Override
    public void populateViews() {
    }

    @Override
    public void setListeners() {
        mHourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mHourSelected = (CharSequence) parent.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Snackbar.make(parent, "You need to select some hours", Snackbar.LENGTH_SHORT).show();
            }
        });
        mTaskSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTaskTypeSelected = (TaskType) parent.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Snackbar.make(parent, "You need to select some TaskType", Snackbar.LENGTH_SHORT).show();
            }
        });
        mClientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mClientSelected = (Client) parent.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Snackbar.make(parent, "You need to select some Client", Snackbar.LENGTH_SHORT).show();
            }
        });
        mProjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mProjectSelected = (Project) parent.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Snackbar.make(parent, "You need to select some Project", Snackbar.LENGTH_SHORT).show();
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences prefs = getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
                final String username = prefs.getString(USERNAME, "");
                final String password = prefs.getString(PASSWORD, "");

//                if (!mProjectSelected.getClient().getName().equals(mClientSelected.getName())) {
//                    Snackbar.make(mFab, "Ups, the client you selected, does not match!", Snackbar.LENGTH_SHORT).show();

                String description = mDescription.getText().toString().trim();
                String solicitude = mSolicitudeNumber.getText().toString().trim();
                int solicitudeNumber = 0;

                if (!solicitude.isEmpty()) {
                    solicitudeNumber = Integer.valueOf(solicitude);
                }

                Date date = null;

                try {
                    date = new SimpleDateFormat(DATE_TEMPLATE, Locale.getDefault()).parse(mDateString);
                } catch (ParseException ex) {
                    Log.e(LOG_TAG, ex.getMessage(), ex.getCause());
                }

                final Person person = new Person();

                person.setUsername(username)
                        .setPassword(password);

                mJobLog.setId(mJobLogId)
                        .setHours(prettyHours(mHourSelected))
                        .setObservations(description)
                        .setSolicitude(solicitudeNumber)
                        .setWorkDate(date)
                        .setPerson(person.setUsername(username)
                                .setPassword(password))
                        .setProject(mProjectSelected)
                        .setTaskType(mTaskTypeSelected);

                new SaveJobLogAsyncTask(getApplicationContext()).execute(mJobLog);
//                }
            }
        });
    }

    private String prettyHours(CharSequence hoursChars) {
        String prettyHours = "";
        Double hours = new Double(hoursChars.toString());
        if ((hours % 2) == 0) {
            prettyHours = String.valueOf(hours.intValue());
        } else {
            prettyHours = hours.toString();
        }
        return prettyHours;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        MenuNavigator.newInstance().navigate(this);
    }

    private void initTaskTypeLoader() {
        getSupportLoaderManager().initLoader(TASK_TYPE_LOADER_ID, null, new LoaderManager.LoaderCallbacks<List<TaskType>>() {

            @Override
            public Loader<List<TaskType>> onCreateLoader(int id, Bundle args) {
                return (id == TASK_TYPE_LOADER_ID) ? new TaskTypeLoader(getApplicationContext()) : null;
            }

            @Override
            public void onLoadFinished(Loader<List<TaskType>> loader, List<TaskType> data) {
                if (null != data && !data.isEmpty()) {
                    mTaskAdapter = new TaskTypeAdapter(getApplicationContext(), data);
                    mTaskSpinner.setAdapter(mTaskAdapter);
                    mTaskAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLoaderReset(Loader<List<TaskType>> loader) {
                if (!loader.isReset()) {
                    loader.reset();
                }
            }
        }).forceLoad();
    }

    private void initClientsLoader() {
        getSupportLoaderManager().initLoader(CLIENT_LOADER_ID, null, new LoaderManager.LoaderCallbacks<List<Client>>() {

            @Override
            public Loader<List<Client>> onCreateLoader(int id, Bundle args) {
                return (id == CLIENT_LOADER_ID) ? new ClientsLoader(getApplicationContext()) : null;
            }

            @Override
            public void onLoadFinished(Loader<List<Client>> loader, List<Client> data) {
                if (null != data && !data.isEmpty()) {
                    mClientAdapter = new ClientAdapter(getApplicationContext(), data);
                    mClientSpinner.setAdapter(mClientAdapter);
                    mClientAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLoaderReset(Loader<List<Client>> loader) {
                if (!loader.isReset()) {
                    loader.reset();
                }
            }
        }).forceLoad();
    }

    private void initProjectsLoader() {
        getSupportLoaderManager().initLoader(PROJECT_LOADER_ID, null, new LoaderManager.LoaderCallbacks<List<Project>>() {

            @Override
            public Loader<List<Project>> onCreateLoader(int id, Bundle args) {
                return (id == PROJECT_LOADER_ID) ? new ProjectsLoader(getApplicationContext()) : null;
            }

            @Override
            public void onLoadFinished(Loader<List<Project>> loader, List<Project> data) {
                if (null != data && !data.isEmpty()) {
                    mProjectAdapter = new ProjectAdapter(getApplicationContext(), data);
                    mProjectSpinner.setAdapter(mProjectAdapter);
                    mProjectAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLoaderReset(Loader<List<Project>> loader) {
                if (!loader.isReset()) {
                    loader.reset();
                }
            }
        }).forceLoad();
    }

    private void initJobLogLoader() {
        getSupportLoaderManager().initLoader(JOBLOG_LOADER_ID, null, new LoaderManager.LoaderCallbacks<JobLog>() {

            @Override
            public Loader<JobLog> onCreateLoader(int id, Bundle args) {
                return (id == JOBLOG_LOADER_ID) ? new JobLogLoader(getApplicationContext(), mJobLogId) : null;
            }

            @Override
            public void onLoadFinished(Loader<JobLog> loader, JobLog data) {
                if (null != data && mEditMode) {
                    final String[] hours = getApplicationContext().getResources().getStringArray(R.array.hours);
                    int hourPosition = 0;

                    for (int i = 0; i < hours.length; i++) {
                        if (hours[i].equals(data.getHours())) {
                            hourPosition = i;
                        }
                    }

                    mHourSpinner.setSelection(hourPosition);

                    long taskId = data.getTaskType().getId();
                    mTaskSpinner.setSelection(mTaskAdapter.getPositionById(taskId));

                    long clientId = data.getProject().getClient().getId();
                    mClientSpinner.setSelection(mClientAdapter.getPositionById(clientId));

                    long projectId = data.getProject().getId();
                    mProjectSpinner.setSelection(mProjectAdapter.getPositionById(projectId));

                    mDescription.setText(data.getObservations());
                    mSolicitudeNumber.setText(String.valueOf(data.getSolicitude()));
                }
            }

            @Override
            public void onLoaderReset(Loader<JobLog> loader) {
                if (!loader.isReset()) {
                    loader.reset();
                }
            }
        }).forceLoad();
    }

    public class SaveJobLogAsyncTask extends AsyncTask<JobLog, Void, Boolean> implements OnDataFetchListener<Boolean> {
        private final Context mContext;
        private boolean result = false;

        public SaveJobLogAsyncTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected Boolean doInBackground(JobLog... params) {
            final JobLogFacade facade = JobLogFacade.newInstance();
            final JobLog jobLog = params[0];
            if (validateFields(jobLog)) {
                if (!mEditMode) {
                    facade.insert(mContext, jobLog, this);
                } else {
                    facade.update(mContext, jobLog, this);
                }
            }

            boolean value = false;

            try {
                Thread.sleep(1500);
                value = getResult();
            } catch (InterruptedException ex) {

            }

            return value;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result && !mEditMode) {
                Snackbar.make(mTaskSpinner, "Good luck, joblog was successfully inserted", Snackbar.LENGTH_SHORT).show();
                onBackPressed();
            } else if (result && mEditMode) {
                Snackbar.make(mTaskSpinner, "Good luck, joblog was successfully updated", Snackbar.LENGTH_SHORT).show();
                onBackPressed();
            } else if (mEditMode) {
                Snackbar.make(mTaskSpinner, "Bad luck, joblog wasn't updated", Snackbar.LENGTH_SHORT)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new SaveJobLogAsyncTask(getApplicationContext()).execute(mJobLog);
                            }
                        })
                        .show();
            } else {
                Snackbar.make(mTaskSpinner, "Bad luck, joblog wasn't inserted", Snackbar.LENGTH_SHORT)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new SaveJobLogAsyncTask(getApplicationContext()).execute(mJobLog);
                            }
                        })
                        .show();
            }
        }

        @Override
        public void onSuccess(@NonNull Boolean response) {
            setResult(response);
        }

        @Override
        public void onError(@NonNull Exception error) {
            if (error instanceof VolleyError) {
                byte[] bytes = ((VolleyError) error).networkResponse.data;
                String stringBytes = new String(bytes);
                try {
                    Log.e(LOG_TAG, "This is the error --------> " + new JSONObject(stringBytes).toString());
                } catch (JSONException ex) {
                    Log.e(LOG_TAG, "This is the with json parsing --------> " + ex.getMessage(), ex.getCause());
                }

            } else {
                Log.e(LOG_TAG, "This is the error --------> " + error.getMessage(), error.getCause());
            }
        }

        void setResult(boolean result) {
            this.result = result;
        }

        boolean getResult() {
            return this.result;
        }
    }

    private boolean validateFields(JobLog jobLog) {
        return !(jobLog.getObservations().equals("") || jobLog.getSolicitude() == 0);
    }
}
