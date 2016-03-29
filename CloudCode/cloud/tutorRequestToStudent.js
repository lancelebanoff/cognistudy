var common = require('cloud/common.js');

Parse.Cloud.define("tutorRequestToStudent", function(request, response) {

	Parse.Cloud.useMasterKey();
	var publicStudentDataId = request.params.publicStudentDataId;
	var tutorId = request.params.tutorId;
	var query = new Parse.Query("PublicUserData")
		.include("student.privateStudentData");
	query.get(publicStudentDataId, {
	  success: function(publicStudentData) {
  		var privateStudentData = publicStudentData.get("student").get("privateStudentData");

	  	// Main code
		var tutorQuery = new Parse.Query("Tutor");
		tutorQuery.get(tutorId, {
		  success: function(tutor) {
			privateStudentData.addUnique("requestsFromTutors", tutor);

			privateStudentData.save(null, {
			  success: function(privateStudentData) {
			    response.success('Tutor added to RequestsFromTutors in PrivateStudentData with objectId: ' + tutor.id);
			  },
			  error: function(privateStudentData, error) {
			    response.error('Failed to save when adding tutor to RequestsFromTutors in PrivateStudentData, with error code: ' + error.message);
			  }
			});
		  },
		  error: function(tutor, error) {
		    response.error('Failed to get tutor, with error code: ' + error.message);
		  }
		});
	  },
	  error: function(publicStudentData, error) {
	    response.error('Failed to get publicStudentData, with error code: ' + error.message);
	  }
	});
});