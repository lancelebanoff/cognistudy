var common = require('cloud/common.js');

Parse.Cloud.define("removeTutor", function(request, response) {

	Parse.Cloud.useMasterKey();
	var privateStudentDataId = request.params.privateStudentDataId;
	var tutorPublicDataId = request.params.tutorPublicDataId;
	var query = new Parse.Query("PrivateStudentData");
	query.get(privateStudentDataId, {
	  success: function(privateStudentData) {
	    // The object was retrieved successfully.
		var tutorQuery = new Parse.Query("PublicUserData");
		tutorQuery.get(tutorPublicDataId, {
		  success: function(tutorPublicDataId) {
		    // The object was retrieved successfully.
			privateStudentData.remove("tutors", tutorPublicDataId);

			privateStudentData.save(null, {
			  success: function(privateStudentData) {
			    // Execute any logic that should take place after the object is saved.
			    response.success('Tutor removed from tutors in PrivateStudentData with objectId: ' + tutorPublicDataId.id);
			  },
			  error: function(privateStudentData, error) {
			    // Execute any logic that should take place if the save fails.
			    // error is a Parse.Error with an error code and message.
			    response.error('Failed to save when removing tutor from tutors in PrivateStudentData, with error code: ' + error.message);
			  }
			});
		  },
		  error: function(tutorPublicDataId, error) {
		    // The object was not retrieved successfully.
		    // error is a Parse.Error with an error code and message.
		    response.error('Failed to get tutorPublicData, with error code: ' + error.message);
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