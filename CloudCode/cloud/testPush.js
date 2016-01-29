Parse.Cloud.define("sendPush", function(request, response) {

	//var userQuery = new Parse.Query(Parse.User);
	//userQuery.equalTo("objectId", request.params.baseUserId);

	var pushQuery = new Parse.Query(Parse.Installation);
	//pushQuery.matchesQuery('user', userQuery);

	pushQuery.equalTo("userIds", request.params.baseUserId);

	Parse.Push.send({
		where: pushQuery,
		data: {
			title: "Test title",
			alert: "Test content",
			fragment: "MainFragment",
		}
	}, {
		success: function() {
			response.success("Successful push");
		},
		error: function(error) {
			response.error("Unsuccessful push");
		}
	});

/*
	var baseUserId = request.params.baseUserId;
	var receiverQuery = new Parse.Query(Parse.User);
	receiverQuery.get(baseUserId), {
        success: function(receiver) {
        	var installationId = receiver.get("installationId");

        	Parse.Push.send({

        	})
        },
        error: function() {
        }
    });
*/
});