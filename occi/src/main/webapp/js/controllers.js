var appControllers = angular.module('appControllers', []);

appControllers.controller('HeaderController', function ($scope, $location) {
    $scope.isActive = function (viewLocation) {
        console.log($location.path())
        return viewLocation === $location.path();
    };
});

appControllers.controller('CatalogCtrl', function ($scope, $http) {

    $http.get('/ca/api/occi/template').success(function (data) {
//        filters = {}
//        filters.category = {}
//        filters.provider = {}
//        filters.application = {}
//        query = { attributes: {}}
//        angular.forEach(data.resources, function (resource, key) {
//            angular.forEach(resource.attributes, function (attribute, attributeKey) {
//                if (resource.attributes[attributeKey]) {
//                    query.attributes[attributeKey] = true
//                    if(!filters[attributeKey]) {
//                        filters[attributeKey] = {}
//                    }
//                    filters[attributeKey][attribute] = true
//                }
//
//            });
//        });
//        $scope.filters = filters;

        $scope.templates = data.resources;
//        $scope.query = query;

    });

//    $scope.doFilter = function (query) {
//        return function (resource) {
//            console.log(item)
//            console.log(query)
//            angular.forEach(resource.attributes, function (attribute, attributeKey) {
//                if(query[attributeKey])
//
//            });
//            if (query.attributes.category[item.attributes.category]) {
//                return true;
//            }
//            return false;
//        };
//    };

});

appControllers.controller('InstantiateTemplateCtrl', function ($scope, $modal, $http, notificationService, config) {

    $scope.newInstance = {}
    $scope.config = config
    $scope.open = function (data) {

        $scope.newInstance = data;
        var modalInstance = $modal.open({
            templateUrl: 'partials/create.html',
            scope: $scope
        });

        modalInstance.result.then(function (newInstance) {
            var config = {headers: {}};
            config.headers['X-OCCI-Attribute'] = attributesToOcciFormat(newInstance);

            $http.post('/ca/api/occi/' + newInstance.attributes.category, {}, config).
                success(function (data) {
                    notificationService.success("Success!")
                    console.log("done creating");
                    console.log(data);
                })
                .error(function (data, status) {
                    notificationService.error("Failed to instantiate template.")
                    console.log("FAILED");
                    console.log(data);
                    console.log(status);
                });
        }, function () {
            // nothing to do if modal closed
        });

    };
});

appControllers.filter('capitalize', function () {
    return function (input, scope) {
        if (input != null)
            input = input.toLowerCase();
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
});

appControllers.filter('attributeIsVisible', function (config) {
    return function (items) {
        var filtered = {};
        angular.forEach(items, function(value, key) {
            if(config.attributes[key] && !config.attributes[key].visible) return;
            filtered[key] = value;
        });
        return filtered;
    }
});

function attributesToOcciFormat(newInstance) {
    var attributesAsString = "";
    for (var i in newInstance.attributes) {
        attributesAsString += i + "=" + newInstance.attributes[i] + ",";
    }
    attributesAsString = attributesAsString.substring(0, attributesAsString.length - 1);
    return attributesAsString;
}


appControllers.controller('ServicesCtrl', function ($scope, $http, $q) {
    $scope.services = [];
    $scope.computes = $http.get('/ca/api/occi/compute');
    $scope.storages = $http.get('/ca/api/occi/platform');
    $q.all([$scope.computes, $scope.storages]).then(function (values) {
        for (var i = 0; i < values.length; i++) {
            $scope.services = $scope.services.concat(values[i].data.resources);
        }
    });
});