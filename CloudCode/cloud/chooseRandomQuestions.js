var common = require('cloud/common.js');

Parse.Cloud.define("chooseThreeQuestions", function(request, response) {

	var studentId = request.params.studentId;
	var challengeId = request.params.challengeId;
	var categories = request.params.categories;

	var query = new Parse.Query("ChallengeUserData")
						.include("publicUserData.student.studentCategoryRollingStats.answeredQuestionIds")
						.include("thisTurnQuestions"); //TODO: Create this class

	query.get(challengeUserDataId, {
		success: function(challengeUserData) {
			var student = challengeUserData.get("publicUserData").get("student");
			var thisTurnQuestions = challengeUserData.get("thisTurnQuestions");

			getAnsweredQuestionIdsIfValid(student, challengeId, categories).then(
				function(answeredQuestionIds) {

					getRandomQuestion(categories, answeredQuestionIds, false).then(
						function(firstQuestion) {
							if(firstQuestion.inBundle) {
								var bundle = firstQuestion.get("bundle");
								bundle.fetchAllIfNeeded("questions", {
									success: function(questions) {
										if(questions.length < 3) {
											response.error("Error: bundle did not contain 3 questions"); //TODO: Just grab another question here
										}
										thisTurnQuestions.set("questions", questions); //TODO: This field will have to be unset when the user answers the questions
										response.success(questions);
									}, error: function(error) { response.error(error); }
								});
							}
							else {
								answeredQuestionIds.push(firstQuestion.id);
								getRandomQuestion(categories, answeredQuestionIds, true).then(
									function(secondQuestion) {

										answeredQuestionIds.push(secondQuestion.id);
										getRandomQuestion(categories, answeredQuestionIds, true).then(
											function(thirdQuestion) {
												//TODO: Continue here
											}, function(error) { response.error(error);
										});
									}, function(error) { response.error(error);
								});
							}
						}, function(error) { response.error(error);
					});
				}, function(error) {
			});
		}, error: function(error) { }
	});
}

function getRandomQuestion(categories, answeredQuestionIds, skipBundles) {

	var promise = new Parse.Promise();

	var countQuery = new Parse.Query("QuestionCount")
						.containedIn("category", categories);

	countQuery.find({ useMasterKey: true,
		success: function(counts) {

			var numAnswered = answeredQuestionIds.length;
			var total = 0;
			for(var i=0; i<counts.length; i++) {
				countObject = counts[i];
				total += countObject.get("numActive");
			}
			var numRemaining = total - numAnswered;
			var skipNum = Math.floor(Math.random()*numRemaining);

			var query = new Parse.Query("Question")
							.equalTo("isActive", true)
							.containedIn("category", categories)
							.notContainedIn("objectId", answeredQuestionIds);
							.skip(skipNum)
							.limit(1)
							.include("bundle") //TODO: Fix this in database
			if(skipBundles) {
				query = query.equalTo("inBundle", false);
			}

			query.first({
				success: function(result) {
					promise.resolve(result);
				}, error: function(error) { promise.reject("Error getting random question"); }
			});
		}, error: function(error) { promise.reject("Error getting QuestionCount"); }
	});
	return promise;
}

function getAnsweredQuestionIdsIfValid(student, challengeId, categories) {

	var baseUserId = student.get("baseUserId");
	isRequestValid(challengeId, baseUserId).then({
		success: function(isValid) {
			if(!isValid) {
				promise.reject("Not a valid request");
			}
			var rollingStatsList = student.get("studentCategoryRollingStats");
			var list = [];
			for(var i=0; i<rollingStatsList.length; i++) {
				var rollingStats = rollingStatsList[i];
				var cat = rollingStats.get("category");
				for(var j=0; j<categories.length; j++) {
					if(categories[j] == cat) {
						var ansQuestionIdsObject = rollingStats.get("answeredQuestionIds");
						var questionIds = ansQuestionIdsObject.get("questionIds");
						list.push.apply(list, questionIds);
						break;
					}
				}
			}
			promise.resolve(list);
		}, error: function(error) { promise.reject(error); }
	});
	return promise;
}

function isRequestValid(challengeId, baseUserId) {
	//TODO: Also check if the questions have already been chosen for this user
	var promise = new Parse.Promise();
	var query = new Parse.Query("Challenge");
	query.get(challengeId, {
		success: function(challenge) {
			if(challenge.get("curTurnUserId") == baseUserId) {
				promise.resolve(true);
			}
			else {
				promise.resolve(false);
			}

		}, error: function(error) { promise.reject("Error getting challenge with challengeId = " + challengeId); }
	});
	return promise;
}