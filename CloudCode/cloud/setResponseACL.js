var common = require("cloud/common.js");

Parse.Cloud.afterSave("Response", function(request) {

	Parse.Cloud.useMasterKey();	

	var response = request.object;
	var baseUserId = response.get("baseUserId");
	var isNew = common.isNewObject(response); 

	if(!isNew)
		return;

	var query = new Parse.Query(Parse.User);
	query.equalTo("objectId", baseUserId);
	query.first({
		success: function(user) {

			var acl = new Parse.ACL(user);
			var roleName = common.getStudentTutorRoleName(baseUserId);
			var query = new Parse.Query(Parse.Role);
			query.equalTo("name", roleName);
			query.first({
				success: function(role) {

					acl.setRoleReadAccess(role, true);
					response.setACL(acl);
					response.save();

				}, function(error) { console.error(error); }
			});
		}, error: function(error) { response.error(error); }
	});
});