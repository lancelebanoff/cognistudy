var m1 = require('cloud/deleteStudent.js');
var m2 = require('cloud/doesEmailExist.js');
var m3 = require('cloud/userCount.js');
var m4 = require('cloud/deleteChallenge.js');
var m5 = require('cloud/testPush.js');
var m6 = require('cloud/deletePinnedObjects.js');
var m7 = require('cloud/changeUserId.js');
var m8 = require('cloud/deleteStudentV2.js');
var m9 = require('cloud/incrementCatAndSubStats.js');

function isNewObject(object) {
    var createdAt = object.get("createdAt");
    var updatedAt = object.get("updatedAt");
    return (createdAt.getTime() == updatedAt.getTime());
}

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