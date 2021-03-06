package edu.uw.main.ui.chat.chatCreate;

import android.app.Application;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import edu.uw.main.MainActivity;
import edu.uw.main.io.RequestQueueSingleton;
import edu.uw.main.model.UserInfoViewModel;
import edu.uw.main.ui.chat.Contacts;

/**
 * The View Model of the Create Chat Page.
 * @author Group 3
 * @version 5/19
 */
public class ChatCreateViewModel extends AndroidViewModel {

    private MutableLiveData<JSONObject> mResponse;

    private MutableLiveData<List<Contacts>> mContactsList;

    private MutableLiveData<List<Contacts>> mUpdateList;

    private UserInfoViewModel mUserModel;

    private String jwt;

    private String userEmail;

    private MutableLiveData<Boolean> usersAdded;

    private int numUser = 0;

    private int size = 0;
//    private edu.uw.main.ui.connection.connectionAdd.ConnectionAddViewModel mCreateModel;

    /**
     * Chile Connection list view model.
     *
     * @param application the application.
     */
    public ChatCreateViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>();
        mResponse.setValue(new JSONObject());
        mContactsList = new MutableLiveData<>();
        mContactsList.setValue(new ArrayList<>());
        usersAdded = new MutableLiveData<>();
        usersAdded.setValue(false);
    }
    /**
     * This method will add a response observer for interacting with the server.
     *
     * @param owner    Owner of the data.
     * @param observer JSON Object to report the result.
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * The connection list observer.
     *
     * @param owner    the owner
     * @param observer the observer.
     */
    public void addChatCreateObserver(@NonNull LifecycleOwner owner,
                                         @NonNull Observer<? super List<Contacts>> observer) {
        mContactsList.observe(owner, observer);
    }

    public void addFinishedObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super Boolean> observer) {
        usersAdded.observe(owner, observer);
    }

    /**
     * Handles the error code when the server has trouble connecting.
     *
     * @param error the server response error.
     */
    private void handleError(final VolleyError error) {
        Log.e("CONNECTION ERROR", error.getLocalizedMessage());
        throw new IllegalStateException(error.getMessage());
    }

    /**
     * Sends email and password to our webservice. Authenticates the credentials.
     *
     * @param nameOfChat - name of the group chat.
     */
    public void connectCreateChatroom(final String nameOfChat, final String jwt, final String userEmail) throws JSONException {
        this.userEmail = userEmail;
        this.jwt = jwt;
        JSONObject body = new JSONObject();
        body.put("name", nameOfChat);
        String url = "https://app-backend-server.herokuapp.com/chats/";
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                this::handleResultCreate,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * Sends a connection request to the web server.
     * @param jwt The Generated Java Web Token.
     */
    public void connectGet(final String jwt) {
        JSONObject body = new JSONObject();
        Log.e("BODY CHECK", body.toString());
        String url =
                "https://app-backend-server.herokuapp.com/connections";
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                this::handleResult,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };
        Log.e("REQUEST CHECK: ", request.toString());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * Sends a connection request to the web server.
     * @param jwt The Generated Java Web Token.
     */
    public void connectCreateChatroom(final String jwt, final int chatID, final String email) {
        String url =
                "https://app-backend-server.herokuapp.com/chats/" + chatID +"/" + email;
        Request request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                this::handleFinishedAdding,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }
    /**
     * Handles the server response success code.
     *
     * @param response the server response.
     */
    private void handleResult(final JSONObject response) {
        mUpdateList = new MutableLiveData<>();
        mUpdateList.setValue(new ArrayList<>());
        try {
            JSONArray jsTemp = response.getJSONArray("email");
            int size = jsTemp.length();
            for (int i = 0; i < size; i++) {
                mUpdateList.getValue().add(new Contacts.Builder(jsTemp.getJSONObject(i).get("email").toString(), false).build());
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        mContactsList.setValue(mUpdateList.getValue());
    }

    private void handleResultCreate(final JSONObject response) {
        size = MainActivity.myContacts.size() + 1;
        try {
            int id = response.getInt("chatID");
            int check = MainActivity.myContacts.size();
            for (int i = 0; i < MainActivity.myContacts.size(); i++) {
                connectCreateChatroom(jwt, id, MainActivity.myContacts.get(i).getUsername());
            }
            connectCreateChatroom(jwt, id, userEmail);
            MainActivity.myContacts.clear();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void handleFinishedAdding(final JSONObject response) {
        numUser++;
        if (numUser == size) {
            numUser = 0;
            usersAdded.setValue(true);
        }
    }

    public void setUsersAdded(final boolean bool) {
        usersAdded.setValue(bool);
    }
}
