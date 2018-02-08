package com.ksug.isaffacebooklogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

//1324402434332191

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ImageView imagePic, frd_profile_pic;
    private TextView txtEmail, txtBirthday, txtFriends;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ksug.isaffacebooklogin.R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();

        imagePic = (ImageView) findViewById(R.id.profile_pic);
        frd_profile_pic = (ImageView) findViewById(R.id.frd_profile_pic);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtBirthday = (TextView) findViewById(R.id.txtBirthday);
        txtFriends = (TextView) findViewById(R.id.txtFriend);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        //loginButton.setReadPermissions(Arrays.asList(EMAIL));
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(LoginActivity.this);
                mDialog.setMessage("Retrieving data......");
                mDialog.show();

                String accessToken = loginResult.getAccessToken().getToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();
                        Log.e("Login response", object.toString());
                        getData(object);
                        getFriendList(object);

                    }
                });

                Bundle fbData = new Bundle();
                fbData.putString("fields", "id,name,first_name,gender,last_name,link,location,locale,timezone,updated_time,email,birthday,friends");
                request.setParameters(fbData);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        if(AccessToken.getCurrentAccessToken() != null) {
            txtEmail.setText(AccessToken.getCurrentAccessToken().getUserId());
        }

    }

    private void getFriendList(JSONObject object) {
        try {
            final String graphPath = "/" + object.getString("id") + "/taggable_friends/";
            GraphRequestAsyncTask newRequest = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), graphPath,new GraphRequest.Callback() {

                @Override
                public void onCompleted(GraphResponse response) {

                    parseResponse(response.getJSONObject());

                }
            }).executeAsync();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseResponse(JSONObject friends) {
        try {
            JSONArray friendsArray = (JSONArray) friends.get("data");
            if (friendsArray != null) {
                for (int i = 0; i < friendsArray.length(); i++) {
                    try {
                    Log.e("friendsArray id  ",friendsArray.getJSONObject(i).get(
                            "id")
                            + "");
                    Log.e("friendsArray name  ",friendsArray.getJSONObject(i).get(
                            "name")
                            + "");
                        JSONObject picObject = new JSONObject(friendsArray
                                .getJSONObject(i).get("picture") + "");
                        String picURL = (String) (new JSONObject(picObject
                                .get("data").toString())).get("url");

                        Picasso.with(this).load(picURL).into(frd_profile_pic);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // facebook use paging if have "next" this mean you still have friends if not start load fbFriends list
                String next = friends.getJSONObject("paging")
                        .getString("next");
                if (next != null) {

                } else {
                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }



    private void getData(JSONObject object) {
        try {
            URL profile_pic = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=100&height=100");

            Picasso.with(this).load(profile_pic.toString()).into(imagePic);
            txtEmail.setText(object.getString("email"));
            txtBirthday.setText(object.getString("birthday"));
            txtBirthday.setText("Friends: " + object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
