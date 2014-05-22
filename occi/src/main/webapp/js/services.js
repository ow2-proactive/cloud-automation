(function () {
    var services = angular.module('services', []);

    services.controller('ServicesCtrl', function ($http, $q) {
        var controller = this;
        this.services = [];
        this.computes = $http.get('/ca/api/occi/compute');
        this.storages = $http.get('/ca/api/occi/platform');
        $q.all([controller.computes, controller.storages]).then(function (values) {
            for (var i = 0; i < values.length; i++) {
                controller.services = controller.services.concat(values[i].data.resources);
            }
        });
    });

    services.controller('ServiceDetailsCtrl', function ($http, $q, $routeParams) {
        var controller = this;
        this.compute = $http.get('/ca/api/occi/compute/' + $routeParams.serviceId).
            success(function (data) {
                controller.service = data;
            });
    });

}());