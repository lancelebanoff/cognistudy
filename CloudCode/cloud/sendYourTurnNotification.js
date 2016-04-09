var common = require("cloud/common.js");

Parse.Cloud.define("sendYourTurnNotification", function(request, response) {

	Parse.Cloud.useMasterKey();

	var challengeId = request.params.challengeId;
	var receiverBaseUserId = request.params.receiverBaseUserId;
	var senderName = request.params.senderName;
	var user1Or2 = request.params.user1Or2;

	var data = createNotificationData(challengeId, senderName);
	common.sendPushNotification(receiverBaseUserId, data).then(
		function(success) {
			response.success();
		}, function(error) {
			response.error(error);
	});
});

function createNotificationData(challengeId, user1Or2, senderName) {
	var data = {};
	data.title = "Your turn!";
	data.alert = senderName + " finished their turn. Time to play!";
	data.ACTIVITY = "CHALLENGE_ACTIVITY";
	data.FRAGMENT = "MAIN_FRAGMENT";

	data.challengeId = challengeId;

	data.intentExtras = {};
	data.intentExtras.CHALLENGE_ID = challengeId;
	data.intentExtras.USER1OR2 = user1Or2;

	return data;
}
