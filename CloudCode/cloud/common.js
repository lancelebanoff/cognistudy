exports.isNewObject = function(object) {
    var createdAt = object.get("createdAt");
    var updatedAt = object.get("updatedAt");
    return (createdAt.getTime() == updatedAt.getTime());
}

exports.logErrors = function(errors) {
	for(var e=0; e<errors.length; e++) {
		console.log(errors[e]);
	}
}

exports.deleteAllObjectsOn = function(className, key, value) {
	return doDeleteAllObjectsOn(className, key, value);
}

function doDeleteAllObjectsOn(className, key, value) {

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

exports.deleteAllObjectsFromClasses = function(classes, key, value) {

	var bigPromise = new Parse.Promise();
	var promises = [];
	for(var i=0; i<classes.length; i++) {
		promises.push(doDeleteAllObjectsOn(classes[i], key, value));
	}

	Parse.Promise.when(promises).then(function(results) {
		bigPromise.resolve("All objects deleted");
	},
	function(errors) {
		logErrors(errors);
		bigPromise.reject("Error deleting objects");
	});
	return bigPromise;
}

exports.sendPushNotification = function(baseUserId, notiData) {

	var pushQuery = new Parse.Query(Parse.Installation);
	pushQuery.equalTo("userIds", baseUserId);

	return Parse.Push.send({
		where: pushQuery,
		data: notiData 
	}, {
		success: function() {
			console.log("Successful push");
		},
		error: function(error) {
			console.log("Unsuccessful push");
		}
	});
}