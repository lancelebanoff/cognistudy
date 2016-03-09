var common = require('cloud/common.js');

Parse.Cloud.define("chooseThreeQuestionsV2", function(request, response) {

	Parse.Cloud.useMasterKey();

	var challengeUserDataId = request.params.challengeUserDataId;
	var challengeId = request.params.challengeId;
	var category = request.params.category;

	var questionKeys = ["questionContents", "subject", "category"];
	var questionContentsKeys = ["questionText", "image", "author", "answers", "correctAnswer", "explanation"];
	var bundleKeys = ["bundle", "passageText", "image"];

	var query = new Parse.Query("ChallengeUserData")
						.include("publicUserData.student.studentCategoryRollingStats")

	query.get(challengeUserDataId, {
		success: function(challengeUserData) {
			var student = challengeUserData.get("publicUserData").get("student");
			var baseUserId = student.get("baseUserId");
			isRequestValid(challengeId, baseUserId).then(
				function(isValid) {
					if(!isValid) {
						response.error("Not a valid request");
					}

					var rollingStatsList = student.get("studentCategoryRollingStats");
					var rollingStatsToFetch;
					for(var i=0; i<rollingStatsList.length; i++) {
						var rollingStats = rollingStatsList[i];
						var cat = rollingStats.get("category");
						if(cat == category) {
							rollingStatsToFetch = rollingStatsList[i];
							break;
						}
					}
					rollingStatsToFetch.fetch({
						success: function(fetchedRollingStats) {

							var ansQuestionsToFetch = fetchedRollingStats.get("answeredQuestionIds");
							ansQuestionsToFetch.fetch({
								success: function(fetchedAnsQuesIds) {
									var singleQuestionIds = fetchedAnsQuesIds.get("singleQuestionIds");
									var bundleQuestionIds = fetchedAnsQuesIds.get("bundleQuestionIds");
									var allQuestionIds = singleQuestionIds.concat(bundleQuestionIds);

									printAnsweredQuestionIds(allQuestionIds);

									getRandomQuestion(category, allQuestionIds, false).then(
										function(firstQuestion) {
											questionToString(firstQuestion, 1);

											// response.success("Intermediate finish");
											if(firstQuestion.get("inBundle")) {
												var bundle = firstQuestion.get("bundle");
												console.log("Bundle id = " + bundle.id);
												bundle.fetch({
													success: function(bundle) {
														var questions = bundle.get("questions");
														console.log("Size of bundle questions = " + questions.length);
														var ids = [];
														for(var i=0; i<questions.length; i++) {
															questionToString(questions[i], i);
															ids.push(questions[i].id);
														}
														if(questions.length < 3) {
															response.error("Error: bundle did not contain 3 questions"); //TODO: Just grab another question here
														}
														var query = new Parse.Query("Question")
																			.containedIn("objectId", ids)
																			.select(questionKeys)  //The selects don't appear to be working
																			.include("questionContents")
																			.select(questionContentsKeys)
																			.include("bundle")
																			.select(bundleKeys);
														query.find({
															success: function(results) {
																challengeUserData.set("curTurnQuestions", results);
																challengeUserData.save();
																response.success(results);
															}, error: function(error) { response.error(error); } //Error fetching questions
														});
													}, error: function(error) { response.error(error); }
												});
											}
											else {
												var copyOfSingleQuestionIds = singleQuestionIds.slice();
												copyOfSingleQuestionIds.push(firstQuestion.id);
												printAnsweredQuestionIds(copyOfSingleQuestionIds);
												getRandomQuestion(category, copyOfSingleQuestionIds, true).then(
													function(secondQuestion) {
														questionToString(secondQuestion, 2);
														copyOfSingleQuestionIds.push(secondQuestion.id);
														printAnsweredQuestionIds(copyOfSingleQuestionIds);
														getRandomQuestion(category, copyOfSingleQuestionIds, true).then(
															function(thirdQuestion) {
																questionToString(thirdQuestion, 3);
																var ids = [];
																ids.push(firstQuestion.id);
																ids.push(secondQuestion.id);
																ids.push(thirdQuestion.id);
																var query = new Parse.Query("Question")
																					.containedIn("objectId", ids)
																					.select(questionKeys)
																					.include("questionContents")
																					.select(questionContentsKeys);
																query.find({
																	success: function(results) {
																		challengeUserData.set("curTurnQuestions", results);
																		challengeUserData.save();
																		response.success(results);
																	}, error: function(error) { response.error(error); } //Error fetching questions
																});
															}, function(error) { response.error(error); //Error getting third question
														});
													}, function(error) { response.error(error); //Error getting second question
												});
											}
										}, function(error) { console.log("Error getting first question"); response.error(error); //Error getting first question
									});
								}, error: function(error) { console.log("Error fetching AnsweredQuestionIds"); response.error(error); } 
							});
						}, error: function(error) { console.log("Error fetching rolling stats"); response.error(error); } //Error fetching rolling stats
					});
				}, function(error) { console.log("Request not valid"); response.error(error); //Request not valid
			});
		}, error: function(error) { console.log("Error getting ChallengeUserData"); response.error(error); } //Error getting ChallengeUserData 
	});
});

function questionToString(question, num) {
	question.fetch({
		success: function(question) {
			console.log("Question " + num + " objectId = " + question.id);
			console.log("Question " + num + " inBundle = " + question.get("inBundle"));
		}, error: function(error) { }
	});
}

function printAnsweredQuestionIds(answeredQuestionIds) {
	var s = "answeredQuestionIds: ";
	for(var i=0; i<answeredQuestionIds.length; i++) {
		s = s.concat(answeredQuestionIds[i] + ", ");
	}
	console.log(s);
}

function getRandomQuestion(category, answeredQuestionIds, skipBundles) {

	var promise = new Parse.Promise();

	var countQuery = new Parse.Query("CategoryStats")
						.equalTo("category", category);

	countQuery.first({ useMasterKey: true,
		success: function(countObject) {

			var numRemaining;

			countObject.fetch({
				success: function(countObject) {

					var numAnswered = answeredQuestionIds.length;
					var total;
					console.log(countObject.get("category") + " numActive " + countObject.get("numActive"));
					if(!skipBundles)
						total = countObject.get("numActive");
					else
						total = countObject.get("numActiveNotInBundle");
					console.log("Total = " + total);
					numRemaining = total - numAnswered;
					console.log("numRemaining = " + numRemaining);
					if(numRemaining < 1) {
						promise.reject(JSON.stringify({code: 777, message: "Not enough available questions"}));
						return;
					}

					var skipNum = Math.max(0, Math.floor(Math.random()*numRemaining));

					console.log("skipNum = " + skipNum);

					var query = new Parse.Query("Question")
							.equalTo("isActive", true)
							.equalTo("test", true) ////////////////////////////////////TODO: Remove later
							.equalTo("category", category)
							.notContainedIn("objectId", answeredQuestionIds)
							.skip(skipNum)
							.limit(1)

							if(skipBundles) {
								query = query.equalTo("inBundle", false);
							}

							query.first({
								success: function(result) {
									// console.log("In getRandomQuestion: questionId = " + result.id);
									promise.resolve(result);
								}, error: function(error) { promise.reject("Error getting random question"); }
							});
				}, error: function(error) { promise.reject(error); }
			});
		}, error: function(error) { promise.reject("Error getting QuestionCount"); }
	});
	return promise;
}

function isRequestValid(challengeId, baseUserId) {
	//TODO: Also check if the questions have already been chosen for this user
	console.log("Inside isRequestValid");
	var promise = new Parse.Promise();
	var query = new Parse.Query("Challenge");
	query.get(challengeId, {
		success: function(challenge) {
			if(challenge.get("curTurnUserId") == baseUserId) {
				console.log("Valid request");
				promise.resolve(true);
			}
			else {
				console.log("Invalid request");
				promise.resolve(false);
			}

		}, error: function(error) { promise.reject("Error getting challenge with challengeId = " + challengeId); }
	});
	return promise;
}