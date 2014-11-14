(function () {
    var occi = angular.module('occi', []);

    occi.service('Occi', function ($cookies) {


        this.attributesToOcciFormat = function (instance) {
            var attributesAsString = "";
            for (var key in instance.attributes) {
                if (key == "provider" || key == "application" || key.indexOf("occi") > -1) {
                    attributesAsString += key + "=" + instance.attributes[key] + ",";
                }
            }
            attributesAsString = attributesAsString.substring(0, attributesAsString.length - 1);
            return attributesAsString;
        }
    });

})();