var common = require("cloud/common.js");

Parse.Cloud.afterSave("Response", function(request) {

	Parse.Cloud.useMasterKey();	

	var response = request.object;
	var baseUserId = response.get("baseUserId");
	var isNew = common.isNewObject(response); 

	if(!isNew) {
		console.log("Response is not new");
		return;
	}

	console.log("Response is new");

	var query = new Parse.Query(Parse.User);
	query.get(baseUserId, {
		success: function(user) {

			var acl = new Parse.ACL(user);
			common.getStudentTutorRole(baseUserId).then(
				function(role) {

					acl.setRoleReadAccess(role, true);
					response.setACL(acl);
					response.save({
						success: function(success) {
							console.log("Response ACL updated successfully");
							return;
						}, error: function(error) { console.error(error); }
					});
				}, function(error) { console.error(error);
			});
		}, error: function(error) { console.error(error); }
	});
});