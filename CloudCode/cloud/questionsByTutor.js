var common = require('cloud/common.js');

Parse.Cloud.define("questionsByTutor", function(request, response) {

	var author = request.params.author;

	var PublicUserData = Parse.Object.extend("PublicUserData");
	var authorObject = new PublicUserData();
	authorObject.id = author;

	var contentsQuery = new Parse.Query("QuestionContents").equalTo("author", authorObject);
	var query = new Parse.Query("Question")
		.descending("createdAt")
		.matchesKeyInQuery("questionContents", "objectId", contentsQuery)
		.include("questionContents").include("questionData.reviews").include("bundle");
	query.find({ useMasterKey: true,
		success: function(questions) {
			response.success(questions);
		}, error: function(error) { response.error(error); }
	});
});