var common = require('./cloud/common.js');

Parse.Cloud.job("deleteOldBlockStats", function(request, status) {
	Parse.Cloud.useMasterKey();

	var CURRENT_DAY = "currentDay";
	var CURRENT_TRIDAY = "currentTriday"; 
	var BASE_USER_ID = "baseUserId";

	var day;
	var triday;

	var query = new Parse.Query("BlockNums");
	query.first({
		success: function(blockNums) {

			blockNums.increment(CURRENT_DAY);
			day = blockNums.get(CURRENT_DAY);
			if(day % 3 == 0) {
				blockNums.increment(CURRENT_TRIDAY);
			}
			blockNums.save({
				success: function(blockNumsAgain) {
					day = blockNumsAgain.get(CURRENT_DAY);
					triday = blockNumsAgain.get(CURRENT_TRIDAY);
					var studentQuery = new Parse.Query("Student");
												// .equalTo(BASE_USER_ID, "vYbCbizBRC");
					studentQuery.find({
					// studentQuery.first({
						success: function(students) {
							var promises = [];
							console.log("Num students = " + students.length);

							var tenDaysAgo = new Date(); 
							tenDaysAgo.setDate(tenDaysAgo.getDate() - 10);

							promises.push(deleteOldChallenges(tenDaysAgo));
							for(var i=0; i<students.length; i++) {
								promises.push(decrementStudentStats(students[i], day, status));
							}
							// promises.push(decrementStudentStats(students, day, status));

							Parse.Promise.when(promises).then(
								function(results) {
									deleteOldStats(day, triday).then(
										function(success) {
										status.success("Finished!");
										}, function(error) { status.error("Error deleting old stats"); }
									);
								}, function(errors) { common.logErrors(errors); status.error("Error decrementing student stats"); }
							);
						}, error: function(error) { status.error("Error retrieving students"); }
					});
				}, error: function(error) { status.error("Error saving blockNums instance"); }
			});
		}, error: function(error) { status.error("Error retrieving BlockNums instance"); }
	});
});

function deleteOldChallenges(tenDaysAgo) {

	var promise = new Parse.Promise();
	var query = new Parse.Query("Challenge");
	query.lessThan("endDate", tenDaysAgo);
	query.find({
		success: function(challenges) {

			var promises = [];
			for(var i=0; i<challenges.length; i++) {
				var challengeId = challenges[i].id;
				console.log("Found old challenge " + challengeId);
				promises.push(Parse.Cloud.run("deleteChallenge", {"objectId": challengeId})); 
			}
			Parse.Promise.when(promises).then(
				function(success) {
					promise.resolve();
				}, function(error) { promise.reject(error); }
			);
		}, error: function(error) { promise.reject(error); }
	});
	return promise;
}

function decrementStudentStats(stud, day, status) {

	console.log("Reached decrementStudentStats with student baseUserId = " + stud.get("baseUserId"));

	var promise = new Parse.Promise();
	stud.fetch({
		success: function(student) {

			// console.log("day = " + day.toString());

			var SEVEN_DAYS_AGO = day - 7;
			var THIRTY_DAYS_AGO = day - 30;

			// console.log("SEVEN_DAYS_AGO = " + SEVEN_DAYS_AGO.toString());
			// console.log("THIRTY_DAYS_AGO = " + THIRTY_DAYS_AGO.toString());

			var promiseList = [];

			promiseList.push(decrementRollingStats(student, "category", "week", SEVEN_DAYS_AGO));
			promiseList.push(decrementRollingStats(student, "subject", "week", SEVEN_DAYS_AGO));
			promiseList.push(decrementRollingStats(student, "category", "month", THIRTY_DAYS_AGO));
			promiseList.push(decrementRollingStats(student, "subject", "month", THIRTY_DAYS_AGO));

			Parse.Promise.when(promiseList).then(
				function(success) {
					promise.resolve();
				}, function(error) { promise.reject(error); }
				);
		}, error: function(error) { promise.reject("Error fetching student"); }
	});
	return promise;
}

String.prototype.capitalizeFirstLetter = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
}

function decrementRollingStats(student, subjectOrCat, weekOrMonth, day) {

	var promise = new Parse.Promise();

	var rollingStatsList = student.get("student" + subjectOrCat.capitalizeFirstLetter() + "RollingStats");

	var studentBaseUserId = student.get("baseUserId"); ///////////////////
	var column = "student" + subjectOrCat.capitalizeFirstLetter() + "RollingStats";

	Parse.Object.fetchAllIfNeeded(rollingStatsList).then(
		function(rollingStatsList) {

			getDayStats(student, subjectOrCat, day).then(
				function(dayStatsList) {

					// console.log("length of dayStatsList = " + dayStatsList.length);

					var savePromises = [];
					for(var i=0; i<dayStatsList.length; i++) {
						var block = dayStatsList[i];
						var cat = block.get(subjectOrCat);
						var foundRollingStats = undefined;
						for(var j=0; j<rollingStatsList.length; j++) {
							var rollingStats = rollingStatsList[j];
							if(rollingStatsList[j].get(subjectOrCat) == cat) {
								foundRollingStats = rollingStatsList[j];
								break;
							}
						}
						var tot = block.get("total");
						var correct = block.get("correct");
						if(foundRollingStats === undefined) {
							console.log("student " + studentBaseUserId + " foundRollingStats is undefined!");
							console.log(column + ": cat is " + cat);
							console.log("student " + student.get("baseUserId") + ": student" + subjectOrCat.capitalizeFirstLetter() + "RollingStats size = " + rollingStatsList.length);
							console.log("Reached next line");
						}
						foundRollingStats.increment("totalPast" + weekOrMonth.capitalizeFirstLetter(), -tot);
						foundRollingStats.increment("correctPast" + weekOrMonth.capitalizeFirstLetter(), -correct);
						savePromises.push(foundRollingStats.save());
					}
					Parse.Promise.when(savePromises).then(
						function(success) {
							promise.resolve();
						}, function(error) { promise.reject(error); }
					);
				}, function(error) { promise.reject(error); }
			);
		}, function(error) { promise.reject("Error fetching student rolling stats"); }
	);
	return promise;
}

function getDayStats(student, subjectOrCat, day) {
	var relation = student.relation("student" + subjectOrCat.capitalizeFirstLetter() + "DayStats");
	return getStatsFromRelation(relation, day);
}

function getStatsFromRelation(relation, blockNum) {
	var query = relation.query()
	.equalTo("blockNum", blockNum);
	return query.find();
}

function deleteOldStats(day, triday) {

	var THIRTY_DAYS_AGO = day - 30;
	// var TEN_TRIDAYS_AGO = triday - 10;

	var dayClasses = ["StudentCategoryDayStats", "StudentSubjectDayStats", "StudentTotalDayStats"];
	// var tridayClasses = ["StudentCategoryTridayStats", "StudentSubjectTridayStats", "StudentTotalTridayStats"];

	var bigPromise = new Parse.Promise();
	var promises = [];
	promises.push(common.deleteAllObjectsFromClasses(dayClasses, "blockNum", THIRTY_DAYS_AGO));
	// promises.push(common.deleteAllObjectsFromClasses(tridayClasses, "blockNum", TEN_TRIDAYS_AGO));

	Parse.Promise.when(promises).then(function(results) {
		bigPromise.resolve("All objects deleted");
	},
	function(errors) {
		bigPromise.reject("Error deleting objects");
	});
	return bigPromise;
}