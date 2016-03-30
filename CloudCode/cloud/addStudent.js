var common = require('cloud/common.js');

Parse.Cloud.define("addStudent", function(request, response) {

	Parse.Cloud.useMasterKey();
	var tutorPublicDataId = request.params.tutorPublicDataId;
	var studentPublicDataId = request.params.studentPublicDataId;
	var query = new Parse.Query("PrivateTutorData");
	query.get(tutorPublicDataId, {
	  success: function(publicTutorData) {
	    // The object was retrieved successfully.
	  	var tutor = publicTutorData.get("tutor");
	  	tutor.fetch({ useMasterKey: true,
	  		success: function(tutor) {
	  			var privateTutorData = tutor.get("privateTutorData");
	  			privateTutorData.fetch({ useMasterKey: true,
	  				success: function(privateTutorData) {
					    // The object was retrieved successfully.
						var studentQuery = new Parse.Query("PublicUserData");
						studentQuery.get(studentPublicDataId, {
						  success: function(studentPublicData) {
						    // The object was retrieved successfully.
							privateTutorData.addUnique("students", studentPublicData);

							privateTutorData.save(null, {
							  success: function(privateTutorData) {
							    // Execute any logic that should take place after the object is saved.
							    response.success('Student added to students in PrivateTutorData with objectId: ' + studentPublicData.id);
							  },
							  error: function(privateTutorData, error) {
							    // Execute any logic that should take place if the save fails.
							    // error is a Parse.Error with an error code and message.
							    response.error('Failed to save when adding student to students in PrivateTutorData, with error code: ' + error.message);
							  }
							});
						  },
						  error: function(studentPublicData, error) {
						    // The object was not retrieved successfully.
						    // error is a Parse.Error with an error code and message.
						    response.error('Failed to get studentPublicData, with error code: ' + error.message);
						  }
						});
	  				},
	  				error: function() {
					    response.error('Failed to fetch privateTutorData, with error code: ' + error.message);
	  				}
	  			});
	  		},
	  		error: function() {
			    response.error('Failed to fetch tutor, with error code: ' + error.message);
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