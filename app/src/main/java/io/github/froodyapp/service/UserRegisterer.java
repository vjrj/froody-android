package io.github.froodyapp.service;

import android.content.Context;

import java.util.List;
import java.util.Map;

import io.github.froodyapp.App;
import io.github.froodyapp.api.api.UserApi;
import io.github.froodyapp.api.invoker.ApiCallback;
import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.FroodyUser;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;

public class UserRegisterer {
    //########################
    //## Statics
    //########################
    public static void userRegister(final Context c) {
        if (c == null) {
            return;
        }
        final AppSettings settings = new AppSettings(c);
        if (!settings.hasFroodyUserId()) {
            UserApi userApi = new UserApi();
            try {
                userApi.userRegisterGetAsync(new ApiCallback<FroodyUser>() {
                    @Override
                    public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                        App.log(getClass(), "ERROR: Can't register user " + e.getMessage());
                    }

                    @Override
                    public void onSuccess(FroodyUser result, int statusCode, Map<String, List<String>> responseHeaders) {
                        if (result != null) {
                            settings.setFroodyUserId(result.getUserId());
                            AppCast.FROODY_USER_REGISTERED.send(c, result);
                        }
                    }

                    public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                    }

                    public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                    }
                });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
}
