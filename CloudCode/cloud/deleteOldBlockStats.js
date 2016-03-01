var m9 = require('cloud/deleteUtils.js');

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
				success: function(saved) {
					int day = blockNums.get(CURRENT_DAY);
					int triday = blockNums.get(CURRENT_TRIDAY);
					var studentQuery = new Parse.Query("Student");
					studentQuery.find({
						success: function(students) {
							var promises = [];
							for(var i=0; i<students.length; i++) {
								promises.push(decrementStudentStats(students[i]));
							}
							Parse.Promise.when(promises).then(
								function(results) {
									deleteOldStats(day, triday).then(
										function(success) {
											status.success("Finished!");
										}, function(error) { status.error("Error deleting old stats"); }
										);
								}, function(errors) { logErrors(errors); status.error("Error deleting decrementing student stats"); }
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
			var relation = student.relation("studentCategoryDayStats");
			var query = relation.query()
								.equalTo("blockNum", day);
			query.count({
				success: function(number) {
					if(number == 0)
						promise.resolve("No blockStats for current day");
				}, error: function(error) { promise.reject("Error counting blockStats for current day"); }
			});
			var catRollingStatsList = student.get("studentCategoryRollingStats");
			for(var i=0; i<catRollingStatsList.length; i++) {
				var catRolStats = catRollingStatsList[i];
				//TODO: does this have to be fetched?
				var cat = catRolStats.get("category");

				var SEVEN_DAYS_AGO = day - 7;
				var THIRTY_DAYS_AGO = day - 30;

				var weekQuery = relation.query()
									.equalTo("category", cat);
									.equalTo("blockNum", SEVEN_DAYS_AGO);
				weekQuery.first({
					success: function(blockStat) {
						var tot = blockStat.get("total");
						var correct = blockStat.get("correct");
						catRolStats.increment("total", -tot);
						catRolStats.increment("correct", -correct);

						//TODO: Finish
					}, error: function(error) { } //If not found, do nothing
				});
			}
		}, error: function(error) { promise.reject("Error fetching student"); }
	});
	return promise;
}

function deleteOldStats(day, triday) {

	var dayClasses = ["StudentCategoryDayStats", "StudentSubjectDayStats"];
	var tridayClasses = ["StudentCategoryTridayStats", "StudentSubjectTridayStats"];

	var bigPromise = new Parse.Promise();
	var promises = [];
	promises.push(deleteAllObjectsFromClasses(dayClasses, "blockNum", day));
	promises.push(deleteAllObjectsFromClasses(tridayClasses, "blockNum", triday));

	Parse.Promise.when(promises).then(function(results) {
		bigPromise.resolve("All objects deleted");
	},
	function(errors) {
		bigPromise.reject("Error deleting objects");
	});
	return bigPromise;
}