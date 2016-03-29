var common = require('cloud/common.js');

Parse.Cloud.define("chooseTenQuestions", function(request, response) {
	
	Parse.Cloud.useMasterKey();
	var categories = request.params.categories;
	var questions = [];
	for(var i=0; i<10; i++) {
		getRandomQuestion(categories, [], false).then(
			function(q) {
				questions.push(q);
			}, function(error) { response.error(error); }
		);
	}
	response.success(questions);
});



function getRandomQuestion(categories, answeredQuestionIds, skipBundles) {

	Parse.Cloud.useMasterKey();
	var promise = new Parse.Promise();

	var countQuery = new Parse.Query("CategoryStats")
	.containedIn("category", categories);

	countQuery.find({ useMasterKey: true,
		success: function(counts) {

			var numRemaining;

			Parse.Object.fetchAll(counts).then(

				function(counts) {
					var numAnswered = answeredQuestionIds.length;
					var total = 0;
					for(var i=0; i<counts.length; i++) {
						countObject = counts[i];
						console.log(countObject.get("category") + " numActive " + countObject.get("numActive"));
						if(!skipBundles)
							total += countObject.get("numActive");
						else
							total += countObject.get("numActiveNotInBundle");
					}
					console.log("Total = " + total);
					numRemaining = total - numAnswered;
					console.log("numRemaining = " + numRemaining);

					var skipNum = Math.max(0, Math.floor(Math.random()*numRemaining));

					console.log("skipNum = " + skipNum);

					var query = new Parse.Query("Question")
							.equalTo("isActive", true)
							.equalTo("test", true) ////////////////////////////////////TODO: Remove later
							.containedIn("category", categories)
							.notContainedIn("objectId", answeredQuestionIds)
							.skip(skipNum)
							.limit(1)
							.include("bundle")
							.include("questionContents");

							if(skipBundles) {
								query = query.equalTo("inBundle", false);
							}

							query.first({
								success: function(result) {
									// console.log("In getRandomQuestion: questionId = " + result.id);
									promise.resolve(result);
								}, error: function(error) { promise.reject("Error getting random question"); }
							});
				}, function(error) { promise.reject(error); }
			);
		}, error: function(error) { promise.reject("Error getting QuestionCount"); }
	});
	return promise;
}