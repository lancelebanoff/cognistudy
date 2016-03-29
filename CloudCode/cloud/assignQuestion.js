Parse.Cloud.define("assignQuestion", function(request, response) {
	
	Parse.Cloud.useMasterKey();
	var pushQuery = new Parse.Query(Parse.Installation);
	//pushQuery.matchesQuery('user', userQuery);

	pushQuery.contains("userIds", request.params.baseUserId);

	Parse.Push.send({
		where: pushQuery,
		data: {
			title: "Assigned Question",
			alert: "New Assigned Question",
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