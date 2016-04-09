var common = require("cloud/common.js");

Parse.Cloud.define("sendYourTurnNotification", function(request, response) {

	Parse.Cloud.useMasterKey();

	var challengeId = request.params.challengeId;
	var receiverBaseUserId = request.params.receiverBaseUserId;
	var senderName = request.params.senderName;

	var data = createNotificationData(challengeId, senderName);
	common.sendPushNotification(receiverBaseUserId, data).then(
		function(success) {
			response.success();
		}, function(error) {
			response.error(error);
	});
});

function createNotificationData(challengeId, senderName) {
	var data = {};
	data.title = "Your turn!";
	data.alert = senderName + " finished their turn. Time to play!";
	data.ACTIVITY = "CHALLENGE_ACTIVITY";
	data.FRAGMENT = "MAIN_FRAGMENT";

	data.challengeId = challengeId;

	return data;
}
