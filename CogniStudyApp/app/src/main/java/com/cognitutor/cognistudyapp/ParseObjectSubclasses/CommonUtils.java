package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseCloud;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Kevin on 2/29/2016.
 */
public class CommonUtils {
    public static <T> Task<T> getCompletionTask(T result) {
        TaskCompletionSource<T> completionSource = new TaskCompletionSource<T>();
        completionSource.setResult(result);
        return completionSource.getTask();
    }

    public static String getSubjectFromCategory(String category) {
        for(String subject : Constants.Subject.getSubjects()) {
            List<String> categoriesInSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
            if(categoriesInSubject.contains(category)) {
                return subject;
            }
        }
        return null;
    }

    public static void sendChallengeNotification(String notificationType, String challengeId, String senderBaseUserId,
                                                 String receiverBaseUserId, int user1Or2) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(Constants.CloudCodeFunction.SendChallengeNotification.notificationType, notificationType);
        params.put(Constants.CloudCodeFunction.SendChallengeNotification.challengeId, challengeId);
        params.put(Constants.CloudCodeFunction.SendChallengeNotification.senderBaseUserId, senderBaseUserId);
        params.put(Constants.CloudCodeFunction.SendChallengeNotification.receiverBaseUserId, receiverBaseUserId);
        params.put(Constants.CloudCodeFunction.SendChallengeNotification.user1Or2, user1Or2);
        ParseCloud.callFunctionInBackground(Constants.CloudCodeFunction.SEND_CHALLENGE_NOTIFICATION, params)
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if (task.isFaulted()) {
                            task.getError().printStackTrace();
                        }
                        return null;
                    }
                });
    }
}
