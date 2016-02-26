Parse.Cloud.define("deleteStudent", function(request, response) {

	Parse.Cloud.useMasterKey();

	var baseUserId = request.params.baseUserId;

	var classes = ["PinnedObject", "PrivateStudentData", "PublicUserData", "Student", "StudentCategoryDayStats",
		"StudentCategoryTridayStats", "StudentCategoryMonthStats", "StudentSubjectDayStats", "StudentSubjectTridayStats",
		"StudentSubjectMonthStats", "StudentCategoryRollingStats", "StudentSubjectRollingStats", "StudentTotalRollingStats"];

	var promises = [];
	promises.push(deleteAllObjectsOn("User", "objectId", baseUserId));
	for(var i=0; i<classes.length; i++) {
		promises.push(deleteAllObjectsOn(classes[i], "baseUserId", baseUserId));
	}

	Parse.Promise.when(promises).then(function(results) {
		response.success("All objects deleted");
	},
	function(errors) {
		for(var e=0; e<errors.length; e++) {
			console.log(errors[e]);
		}
		response.error("Error deleting objects");
	});
});

function deleteAllObjectsOn(className, key, value) {

	Parse.Cloud.useMasterKey();

	var promise = new Parse.Promise();

	var query;
	if(className === "User")
		query = new Parse.Query(Parse.User);
	else
		query = new Parse.Query(className);
	query.equalTo(key, value);

	query.find({useMasterKey: true,
		success: function(results) {
			console.log("Found " + results.length + " objects of class " + className);
			Parse.Object.fetchAll(results).then(function(fetchedResults) {
				Parse.Object.destroyAll(fetchedResults).then(function(success) {
					console.log(className + "objects deleted");
					promise.resolve();
				}, function(error) {
					promise.reject("Error deleting " + className);
				});
			}, function(error) {
				promise.reject("Error fetching " + className);
			});
		},
		error: function(error) {
			console.log("No " + className + " objects found");
			promise.resolve();
		}
	});
	return promise;
}