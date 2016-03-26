var common = require('cloud/common.js');

Parse.Cloud.define("tutorRequestToStudent", function(request, response) {

	Parse.Cloud.useMasterKey();
	var privateStudentDataId = request.params.privateStudentDataId;
	var tutorId = request.params.tutorId;
	var query = new Parse.Query("PrivateStudentData");
	query.get(privateStudentDataId, {
	  success: function(privateStudentData) {
	    // The object was retrieved successfully.
		var tutorQuery = new Parse.Query("Tutor");
		tutorQuery.get(tutorId, {
		  success: function(tutor) {
		    // The object was retrieved successfully.
			privateStudentData.addUnique("requestsFromTutors", tutor);

			privateStudentData.save(null, {
			  success: function(privateStudentData) {
			    // Execute any logic that should take place after the object is saved.
			    response.success('Tutor added to RequestsFromTutors in PrivateStudentData with objectId: ' + tutor.id);
			  },
			  error: function(privateStudentData, error) {
			    // Execute any logic that should take place if the save fails.
			    // error is a Parse.Error with an error code and message.
			    response.error('Failed to save when adding tutor to RequestsFromTutors in PrivateStudentData, with error code: ' + error.message);
			  }
			});
		  },
		  error: function(tutor, error) {
		    // The object was not retrieved successfully.
		    // error is a Parse.Error with an error code and message.
		    response.error('Failed to get tutor, with error code: ' + error.message);
		  }
		});
	  },
	  error: function(privateStudentData, error) {
	    // The object was not retrieved successfully.
	    // error is a Parse.Error with an error code and message.
	    response.error('Failed to get PrivateStudentData, with error code: ' + error.message);
	  }
	});
});