package io.github.froodyapp.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.api.api.UserApi;
import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.FroodyUser;
import io.github.froodyapp.api.model_.ResponseOk;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;

/**
 * Api calls for registering user
 */
public class UserRegisterer extends Thread {
    //########################
    //## Member
    //########################
    private final Context context;

    //########################
    //## Methods
    //########################

    /**
     * Constructor
     *
     * @param context context to post result to
     */
    public UserRegisterer(final Context context) {
        this.context = context;
    }

    // Register user (get unique userId) with server
    public void run() {
        if (context == null) {
            return;
        }

        boolean ok = registerOrCheckUserId();

        // Show a message if couldn't connect
        if (!ok) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Toast.makeText(context, R.string.error_couldnt_connect_or_register, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean registerOrCheckUserId() {
        final AppSettings settings = new AppSettings(context);

        UserApi userApi = new UserApi();
        if (settings.hasFroodyUserId()) {
            long userId = settings.getFroodyUserId();
            try {
                ResponseOk ok = userApi.userIsEnabledGet(userId);
                return ok.getSuccess();
            } catch (ApiException e) {
                App.log(getClass(), "Error: Could not check UserID");
            }
        }

        // Register new ID if no ID yet
        try {
            FroodyUser result = userApi.userRegisterGet();
            if (result != null && result.getUserId() != null) {
                settings.setFroodyUserId(result.getUserId());
                AppCast.FROODY_USER_REGISTERED.send(context, result);
                return true;
            }
        } catch (ApiException e) {
            App.log(getClass(), "Error: Could not register UserID");
        }
        return false;
    }
}
