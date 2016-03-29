var common = require('cloud/common.js');

Parse.Cloud.define("removeStudent", function(request, response) {

	Parse.Cloud.useMasterKey();
	var tutorPublicDataId = request.params.tutorPublicDataId;
	var studentPublicDataId = request.params.studentPublicDataId;
	var query = new Parse.Query("PublicUserData");
	query.get(tutorPublicDataId, {
	  success: function(publicTutorData) {
	    // The object was retrieved successfully.
	  	var tutor = publicTutorData.get("tutor");
	  	tutor.fetch({ useMasterKey: true,
	  		success: function(tutor) {
	  			var privateTutorData = tutor.get("privateTutorData");
	  			privateTutorData.fetch({ useMasterKey: true,
	  				success: function(privateTutorData) {
						var studentQuery = new Parse.Query("PublicUserData");
						studentQuery.get(studentPublicDataId, {
						  success: function(studentPublicDataId) {
						    // The object was retrieved successfully.
							privateTutorData.remove("students", studentPublicDataId);

							privateTutorData.save(null, {
							  success: function(privateTutorData) {
							    // Execute any logic that should take place after the object is saved.
							    response.success('student removed from students in PrivateTutorData with objectId: ' + studentPublicDataId.id);
							  },
							  error: function(privateTutorData, error) {
							    // Execute any logic that should take place if the save fails.
							    // error is a Parse.Error with an error code and message.
							    response.error('Failed to save when removing student from students in PrivateTutorData, with error code: ' + error.message);
							  }
							});
						  },
						  error: function(studentPublicDataId, error) {
						    // The object was not retrieved successfully.
						    // error is a Parse.Error with an error code and message.
						    response.error('Failed to get studentPublicData, with error code: ' + error.message);
						  }
						});
	  				},
	  				error: function() {
	  					response.error("Failed to fetch privateTutorData, with error code: " + error.message);
	  				}
	  			})
	  		},
	  		error: function() {
	  			response.error("Failed to fetch tutor, with error code: " + error.message);
	  		}
	  	});
	  },
	  error: function(publicTutorData, error) {
	    // The object was not retrieved successfully.
	    // error is a Parse.Error with an error code and message.
	    response.error('Failed to get PublicTutorData, with error code: ' + error.message);
	  }
	});
});