var common = require('cloud/common.js');

Parse.Cloud.define("studentRequestToTutor", function(request, response) {

	Parse.Cloud.useMasterKey();
	var publicTutorDataId = request.params.publicTutorDataId;
	var studentId = request.params.studentId;
	var query = new Parse.Query("PublicUserData")
		.include("tutor.privateTutorData");
	query.get(publicTutorDataId, {
	  success: function(publicTutorData) {
  		var privateTutorData = publicTutorData.get("tutor").get("privateTutorData");

	  	// Main code
		var studentQuery = new Parse.Query("Student");
		studentQuery.get(studentId, {
		  success: function(student) {
			privateTutorData.addUnique("requestsFromStudents", student);

			privateTutorData.save(null, {
			  success: function(privateTutorData) {
			    response.success('Student added to RequestsFromStudents in PrivateTutorData with objectId: ' + student.id);
			  },
			  error: function(privateTutorData, error) {
			    response.error('Failed to save when adding student to RequestsFromStudents in PrivateTutorData, with error code: ' + error.message);
			  }
			});
		  },
		  error: function(student, error) {
		    response.error('Failed to get student, with error code: ' + error.message);
		  }
		});
	  },
	  error: function(publicTutorData, error) {
	    response.error('Failed to get publicTutorData, with error code: ' + error.message);
	  }
	});
});