(function () {
    var catalog = angular.module('catalog', ['appConfig', 'occi']);

    catalog.controller('CatalogCtrl', function ($http, $scope, $modal, config, notificationService, Occi) {
        var catalog = this;
        this.templates = {}

        $http.get('/ca/api/occi/template').success(function (data) {
            catalog.templates = data.resources;
        });

        this.createService = function (data) {
            $scope.newInstance = data
            $scope.config = config
            var modalInstance = $modal.open({
                templateUrl: 'partials/create.html',
                scope: $scope
            });

            modalInstance.result.then(function (newInstance) {
                var config = {headers: {}};
                config.headers['X-OCCI-Attribute'] = Occi.attributesToOcciFormat(newInstance);

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

    catalog.filter('capitalize', function () {
        return function (input) {
            if (input != null)
                input = input.toLowerCase();
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }
    });

    catalog.filter('attributeIsVisible', function (config) {
        return function (items) {
            var filtered = {};
            angular.forEach(items, function (value, key) {
                if (config.attributes[key] && !config.attributes[key].visible) return;
                filtered[key] = value;
            });
            return filtered;
        }
    });

}());