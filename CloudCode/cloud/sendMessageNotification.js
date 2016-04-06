var common = require("cloud/common.js");

Parse.Cloud.define("sendMessageNotification", function(request, response) {
	
	Parse.Cloud.useMasterKey();

	var senderBaseUserId = request.params.senderBaseUserId;
	var receiverBaseUserId = request.params.receiverBaseUserId;
	var senderName = request.params.senderName;
	var messageText = request.params.messageText;

	var data = createNotificationData(senderName, messageText, senderBaseUserId);
	common.sendPushNotification(receiverBaseUserId, data).then(
		function(success) {
			response.success();
		}, function(error) {
			response.error(error);
	});
});

function createNotificationData(senderName, messageText, senderBaseUserId) {
	var data = {};
	data.title = "Message from " + senderName;
	data.alert = messageText;
	data.ACTIVITY = "CHAT_ACTIVITY";
	data.FRAGMENT = "CONVERSATIONS_FRAGMENT";

	data.conversantBaseUserId = senderBaseUserId;

	data.intentExtras = {};
	data.intentExtras.BASEUSERID = senderBaseUserId;
	data.intentExtras.CONVERSANT_DISPLAY_NAME = senderName;
	data.intentExtras.PARENT_ACTIVITY = "MAIN_ACTIVITY";
	return data;
}
