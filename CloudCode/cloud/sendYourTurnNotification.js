var common = require("cloud/common.js");

Parse.Cloud.define("sendYourTurnNotification", function(request, response) {

	Parse.Cloud.useMasterKey();

	var challengeId = request.params.challengeId;
	var receiverBaseUserId = request.params.receiverBaseUserId;
	var senderBaseUserId = request.params.senderBaseUserId;
	var user1Or2 = request.params.user1Or2;

	var query = new Parse.Query("PublicUserData")
											.equalTo("baseUserId", senderBaseUserId);

	query.first({
		success: function(publicUserData) {
			var senderName = publicUserData.get("displayName");
			var data = createNotificationData(challengeId, user1Or2, senderName);
			common.sendPushNotification(receiverBaseUserId, data).then(
				function(success) {
					response.success();
				}, function(error) {
					response.error(error);
				});
		}, error: function(error) { response.reject(error); }
	});
});

function createNotificationData(challengeId, user1Or2, senderName) {
	var data = {};
	data.title = "Your turn!";
	data.alert = senderName.toString() + " finished their turn. Time to play!";
	data.ACTIVITY = "CHALLENGE_ACTIVITY";
	data.FRAGMENT = "MAIN_FRAGMENT";

	data.challengeId = challengeId;

	data.intentExtras = {};
	data.intentExtras.CHALLENGE_ID = challengeId;
	data.intentExtras.USER1OR2 = user1Or2;

	return data;
}
