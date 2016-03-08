var common = require('cloud/common.js');

Parse.Cloud.define("getSubStats", function(request, response) {

	var subjectName = request.params.subjectName;

	var query = new Parse.Query("SubjectStats").equalTo(subjectName);
	query.first({ useMasterKey: true,
		success: function(stats) {
			response.success(stats);
		}, error: function(error) { response.error(error); }
	});
});