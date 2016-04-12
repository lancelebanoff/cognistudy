var common = require("cloud/common.js");

Parse.Cloud.afterSave("Response", function(request) {

	Parse.Cloud.useMasterKey();	

	var response = request.object;
	var baseUserId = response.get("baseUserId");
	var isNew = common.isNewObject(response); 

	if(!isNew)
		return;

	var query = new Parse.Query(Parse.User);
	query.get(baseUserId, {
		success: function(user) {

			var acl = new Parse.ACL(user);
			common.getStudentTutorRole(baseUserId).then(
				function(role) {

					acl.setRoleReadAccess(role, true);
					response.setACL(acl);
					response.save();

				}, function(error) { console.error(error);
			});
		}, error: function(error) { console.error(error); }
	});
});