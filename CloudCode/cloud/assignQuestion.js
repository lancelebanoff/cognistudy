Parse.Cloud.define("assignQuestion", function(request, response) {
	
	Parse.Cloud.useMasterKey();
	var pushQuery = new Parse.Query(Parse.Installation);
	//pushQuery.matchesQuery('user', userQuery);

	pushQuery.equalTo("userIds", request.params.baseUserId);

	pushQuery.find({success: function(results) {
		console.log("Size of results = " + results.length);
	}, error: function(error) {
		console.log(error);
	}});

	Parse.Push.send({
		where: pushQuery,
		data: {
			title: "Assigned Question",
			alert: "New Assigned Question",
			ACTIVITY: "SUGGESTED_QUESTION_ACTIVITY",
			FRAGMENT: "SUGGESTED_QUESTION_LIST_FRAGMENT",
		}
	}, {
		success: function() {
			response.success("Successful push");
		},
		error: function(error) {
			response.error("Unsuccessful push");
		}
	});
});