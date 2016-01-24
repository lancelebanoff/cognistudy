Parse.Cloud.define("deleteStudent", function(request, response) {
	Parse.Cloud.useMasterKey();
	var query = new Parse.Query(Parse.User);
	var id = request.params.userId;
	query.equalTo("objectId", id);
	query.find({ useMasterKey: true,
		success: function(results) {
			if(results.length == 0) {
				response.success("no user found for id" + id);
			}
			else {
				var objects = [];
				var user = results[0];
				objects.push(user);
				var publicUserData = user.get("publicUserData");
				publicUserData.fetch({ useMasterKey: true,
					success: function(publicUserData) {
						objects.push(publicUserData);
						var student = publicUserData.get("student");
						student.fetch({
							success: function(student) {
								objects.push(student);

								var ok = deleteStats(objects, student);
								if(ok)
									console.log("Delete stats returned successfully");
								else
									console.log("Delete stats returned fail");
								console.log("After deleteStats, size of objects is " + objects.length);

								var privateStudentData = student.get("privateStudentData");
								privateStudentData.fetch({ useMasterKey: true,
									success: function(privateStudentData) {
										objects.push(privateStudentData);
										Parse.Object.destroyAll(objects).then(function(success) {
											response.success("All objects deleted");
										}, function(error) {
											response.error("destroyAll failed");
										});
									},
									error: function() {
										response.error("privateStudentData lookup failed");
									}
								});
							},
							error: function() {
								response.error("student fetch failed");
							}
						});
					},
					error: function() {
						response.error("publicUserData fetch failed");
					}
				});
			}
		},
		error: function() {
			response.error("user lookup failed");
		}
	});
});

function deleteStats(objects, student) {
	var catStats = student.get("studentCategoryStats");
	console.log("adding studentCategoryStats");
	if(!addStats(objects, catStats))
		return false;

	var subStats = student.get("studentSubjectStats");
	console.log("adding studentSubjectStats");
	if(!addStats(objects, subStats))
		return false;

	return true;
}

function addStats(objects, stats) {
	for(var i=0; i<stats.length; i++) {
		stats[i].fetch({
			success: function(row) {
				objects.push(row);
			},
			error: function() {
				console.log("Error fetching row");
				return false;
			}
		});
	}
	console.log("Size of objects is " + objects.length);
	return true;
}