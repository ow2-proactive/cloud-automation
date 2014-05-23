(function () {
    var services = angular.module('services', []);

    services.controller('ServicesCtrl', function ($http, $q, notificationService, $route, $scope, $modal) {
        var controller = this;
        this.services = [];
        this.computes = $http.get('/ca/api/occi/compute');
        this.storages = $http.get('/ca/api/occi/platform');
        $q.all([controller.computes, controller.storages]).then(function (values) {
            for (var i = 0; i < values.length; i++) {
                controller.services = controller.services.concat(values[i].data.resources);
            }
        });

        this.deleteService = function (service) {
            $scope.service = service
            var modalInstance = $modal.open({
                templateUrl: 'partials/service-delete.html',
                scope: $scope
            });

            modalInstance.result.then(function (service) {
                var config = { params: {status: 'done'}}
                $http.delete('/ca/api/occi/' + service.category + '/' + service.uuid, config).success(function (date) {
                    notificationService.success("Service " + service.uuid + " removed")
                    $route.reload()
                });
            }, function () {
                // nothing to do if modal closed
            });


        };
    });

    services.controller('ServiceDetailsCtrl', function ($http, $q, $routeParams) {
        var controller = this;
        this.compute = $http.get('/ca/api/occi/' + $routeParams.category + '/' + $routeParams.serviceId).
            success(function (data) {
                controller.service = data;
            });
    });

}());