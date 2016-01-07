Parse.Cloud.define("deleteStudent", function(request, response) {
	Parse.Cloud.useMasterKey();
	var query = new Parse.Query("Student");
	var id = request.params.studentId;
	query.equalTo("objectId", id);
	query.find({ useMasterKey: true,
		success: function(results) {
			if(results.length == 0) {
				response.success("no student found for id" + id);
			}
			else {
				var objects = [];
				var student = results[0];
				objects.push(student);
				var privateStudentData = student.get("privateStudentData");
				privateStudentData.fetch({ useMasterKey: true,
					success: function(privateStudentData) {
						objects.push(privateStudentData);
						var publicUserData = student.get("publicUserData");
						publicUserData.fetch({
							success: function(publicUserData) {
								objects.push(publicUserData);
								var baseUserId = publicUserData.get("baseUserId");
								var innerQuery = new Parse.Query(Parse.User);
								innerQuery.get(baseUserId, { useMasterKey: true,
									success: function(results) {
										var user = results;
                                        objects.push(user);
										Parse.Object.destroyAll(objects).then(function(success) {
											response.success("Student objects deleted");
										}, function(error) {
											response.error("destroyAll failed");
										});
									},
									error: function() {
										response.error("user lookup failed");
									}
								});
							},
							error: function() {
								response.error("publicUserData fetch failed");
							}
						});
					},
					error: function() {
						response.error("privateStudentData fetch failed");
					}
				});
			}
		},
		error: function() {
			response.error("student lookup failed");
		}
	});
});
