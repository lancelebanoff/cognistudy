Parse.Cloud.beforeSave("Student", function(request, response) {
    Parse.Cloud.useMasterKey();
    var student = request.object;
    if(student.dirty("randomEnabled"))
        student.set("randomEnabledChanged", true);
    else
        student.set("randomEnabledChanged", false);
    response.success();
});

Parse.Cloud.afterSave("Student", function(request) {
    Parse.Cloud.useMasterKey();
    var student = request.object;
    var isNew = isNewObject(student);
    var randomEnabledChanged = student.get("randomEnabledChanged");
    if(!isNew && !randomEnabledChanged) {
        console.log("Nothing to be done");
        return;
    }
    getUserCount()
        .then(function(userCount) {
            if(isNew) {
                userCount.increment("numStudents");
                userCount.increment("totalUsers");
            }
            if(randomEnabledChanged) {
                var amount;
                if(student.get("randomEnabled"))
                    amount = 1;
                else
                    amount = -1;
                userCount.increment("randomEnabled", amount);
            }
            userCount.save();
        },
        function(error) {
            console.log("Error retrieving user count");
        });    
});

Parse.Cloud.afterDelete("Student", function(request) {
    Parse.Cloud.useMasterKey();
    var student = request.object;
    getUserCount()
        .then(function(userCount) {
            userCount.increment("numStudents", -1);
            userCount.increment("totalUsers", -1);
            if(student.get("randomEnabled"))
                userCount.increment("randomEnabled", -1);
            userCount.save();
        },
        function(error) {
            console.log("Error retrieving user count");
        });    
});

function isNewObject(object) {
    var createdAt = object.get("createdAt");
    var updatedAt = object.get("updatedAt");
    return (createdAt.getTime() == updatedAt.getTime());
}

function getUserCount() {
    Parse.Cloud.useMasterKey();
    var promise = new Parse.Promise();
    var query = new Parse.Query("UserCount");
    query.get("DgQ0NUErzw", {
        success: function(object) {
            promise.resolve(object);
        },
        error: function() {
            promise.reject(Error("Error retrieving user count"));
        }
    });
    return promise;
}
