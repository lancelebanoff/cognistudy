var common = require('cloud/common.js');

Parse.Cloud.define("oldestPendingQuestion", function(request, response) {

	var author = request.params.author;
	var alreadyVisited = request.params.alreadyVisited;
	var isAdmin = request.params.isAdmin;

	var PublicUserData = Parse.Object.extend("PublicUserData");
	var authorObject = new PublicUserData();
	authorObject.id = author;

	var contentsQuery = new Parse.Query("QuestionContents").equalTo("author", authorObject);
	var dataQuery = new Parse.Query("QuestionData").equalTo("reviewStatus", "PENDING");
	if(isAdmin) {
		var query = new Parse.Query("Question").notContainedIn("objectId", alreadyVisited)
			.ascending("createdAt").matchesKeyInQuery("questionData", "objectId", dataQuery)
	}
	else {
		var query = new Parse.Query("Question").notContainedIn("objectId", alreadyVisited)
			.ascending("createdAt").matchesKeyInQuery("questionData", "objectId", dataQuery)
			.doesNotMatchKeyInQuery("questionContents", "objectId", contentsQuery);
	}
	query.first({ useMasterKey: true,
		success: function(question) {
			response.success(question);
		}, error: function(error) { response.error(error); }
	});
});