var common = require('cloud/common.js');

Parse.Cloud.job("deleteOldBlockStats", function(request, status) {
	Parse.Cloud.useMasterKey();

	var CURRENT_DAY = "currentDay";
	var CURRENT_TRIDAY = "currentTriday"; 
	var BASE_USER_ID = "baseUserId";

	var query = new Parse.Query("BlockNums");
	query.first({
		success: function(blockNums) {
			blockNums.increment(CURRENT_DAY);
			blockNums.increment(CURRENT_TRIDAY);
			blockNums.save({
				success: function(blockNums) {
					int day = blockNums.get(CURRENT_DAY);
					int triday = blockNums.get(CURRENT_TRIDAY);
					var studentQuery = new Parse.Query("Student");
					studentQuery.find({
						success: function(students) {
							var promises = [];
							for(var i=0; i<students.length; i++) {
								promises.push(decrementStudentStats(students[i]), day);
							}
							Parse.Promise.when(promises).then(
								function(results) {
									deleteOldStats(day, triday).then(
										function(success) {
											status.success("Finished!");
										}, function(error) { status.error("Error deleting old stats"); }
									);
								}, function(errors) { common.logErrors(errors); status.error("Error deleting decrementing student stats"); }
							);
						}, error: function(error) { status.error("Error retrieving students"); }
					});
				}, error: function(error) { status.error("Error saving blockNums instance"); }
			});
		}, error: function(error) { status.error("Error retrieving BlockNums instance"); }
	});
});

function decrementStudentStats(stud, day) {

	var promise = new Parse.Promise();
	stud.fetch({
		success: function(student) {

			var SEVEN_DAYS_AGO = day - 7;
			var THIRTY_DAYS_AGO = day - 30;

			var promiseList = [];

			promiseList.push(decrementCategoryStats(student, "category", "week", SEVEN_DAYS_AGO));
			promiseList.push(decrementCategoryStats(student, "subject", "week", SEVEN_DAYS_AGO));
			promiseList.push(decrementCategoryStats(student, "category", "month", THIRTY_DAYS_AGO));
			promiseList.push(decrementCategoryStats(student, "subject", "month", THIRTY_DAYS_AGO));

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

function decrementRollingStatsStats(student, subjectOrCat, weekOrMonth, day) {

	var promise = new Parse.Promise();

	var rollingStatsList = student.get("student" + subjectOrCat.capitalizeFirstLetter() + "RollingStats");

	getDayStats(student, subjectOrCat, day).then(
		function(dayStatsList) {

			var savePromises = [];
			for(var i=0; i<dayStatsList.length; i++) {
				var block = dayStatsList[i];
				//TODO: Does block need to be fetched?
				var cat = block.get(subjectOrCat);
				var rollingStats = undefined;
				for(var j=0; j<rollingStatsList; j++) {
					if(rollingStatsList[i].get(subjectOrCat) == cat) {
						rollingStats = rollingStatsList[i];
						//TODO: Does rolling stats need to be fetched?
						break;
					}
				}
				var tot = block.get("total");
				var correct = block.get("correct");
				rollingStats.increment("totalPast" + weekOrMonth.capitalizeFirstLetter(), -tot);
				rollingStats.increment("correctPast" + weekOrMonth.capitalizeFirstLetter(), -correct);
				savePromises.push(rollingStats.save());
			}
			Parse.Promise.when(savePromises).then(
				function(success) {
					promise.resolve();
				}, function(error) { promise.reject(error); }
			);
		}, function(error) { promise.reject(error); }
	);
	return promise;
}

function getDayStats(student, subjectOrCat, day) {
	var relation = student.relation("student" + subjectOrCat.capitalizeFirstLetter() + "DayStats");
	return getStatsFromRelation(relation, day);
}

function getStatsFromRelation(relation, blockNum) {
	var query = relation.query()
						.equalTo("blockNum", day);
	return query.find();
}

function deleteOldStats(day, triday) {

	var THIRTY_DAYS_AGO = day - 30;
	var TEN_TRIDAYS_AGO = triday - 10;

	var dayClasses = ["StudentCategoryDayStats", "StudentSubjectDayStats", "StudentTotalDayStats"];
	var tridayClasses = ["StudentCategoryTridayStats", "StudentSubjectTridayStats", "StudentTotalTridayStats"];

	var bigPromise = new Parse.Promise();
	var promises = [];
	promises.push(common.deleteAllObjectsFromClasses(dayClasses, "blockNum", THIRTY_DAYS_AGO));
	promises.push(common.deleteAllObjectsFromClasses(tridayClasses, "blockNum", TEN_TRIDAYS_AGO));

	Parse.Promise.when(promises).then(function(results) {
		bigPromise.resolve("All objects deleted");
	},
	function(errors) {
		bigPromise.reject("Error deleting objects");
	});
	return bigPromise;
}