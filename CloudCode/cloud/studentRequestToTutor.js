var common = require('cloud/common.js');

Parse.Cloud.define("studentRequestToTutor", function(request, response) {

	Parse.Cloud.useMasterKey();
	var privateTutorDataId = request.params.privateTutorDataId;
	var studentId = request.params.studentId;
	var query = new Parse.Query("PrivateTutorData");
	query.get(privateTutorDataId, {
	  success: function(privateTutorData) {
	    // The object was retrieved successfully.
		var studentQuery = new Parse.Query("Student");
		studentQuery.get(studentId, {
		  success: function(student) {
		    // The object was retrieved successfully.
			privateTutorData.addUnique("requestsFromTutors", student);

			privateTutorData.save(null, {
			  success: function(privateTutorData) {
			    // Execute any logic that should take place after the object is saved.
			    response.success('Student added to RequestsFromStudents in PrivateTutorData with objectId: ' + student.id);
			  },
			  error: function(privateTutorData, error) {
			    // Execute any logic that should take place if the save fails.
			    // error is a Parse.Error with an error code and message.
			    response.error('Failed to save when adding student to RequestsFromStudents in PrivateTutorData, with error code: ' + error.message);
			  }
			});
		  },
		  error: function(student, error) {
		    // The object was not retrieved successfully.
		    // error is a Parse.Error with an error code and message.
		    response.error('Failed to get student, with error code: ' + error.message);
		  }
		});
	  },
	  error: function(privateTutorData, error) {
	    // The object was not retrieved successfully.
	    // error is a Parse.Error with an error code and message.
	    response.error('Failed to get PrivateTutorData, with error code: ' + error.message);
	  }
	});
});