(function () {
    var services = angular.module('services', ['appConfig', 'occi']);

    services.controller('ServicesCtrl', function ($http, $q, config, notificationService, $route, $scope, $modal, $timeout, $cookieStore, User, Occi) {
        var controller = this;
        this.services = [];
        this.selectedResource = -1;

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

        this.autoRefresh = User.isAutoRefreshEnabled
        this.refresh = function (autoRefresh) {
            User.enableAutoRefresh(autoRefresh)
            if (User.isAutoRefreshEnabled()) {
                this.doRefresh(this.reschedule)
            }
        }

        this.toggle = function (index) {
            if (this.selectedResource == index) {
                this.selectedResource = -1
            } else {
                this.selectedResource = index
            }
        }

        this.isToggled = function (index) {
            return this.selectedResource == index
        }


        this.reschedule = function () {
            if (User.isAutoRefreshEnabled()) {
                $timeout(function () {
                    controller.doRefresh(controller.reschedule)
                }, 2000);
            }
        }

        this.doRefresh = function (callback) {
            var controller = this;
            var computes = $http.get('/ca/api/occi/compute');
            var storages = $http.get('/ca/api/occi/platform');
            $q.all([computes, storages]).then(function (values) {
                controller.services = [];
                for (var i = 0; i < values.length; i++) {
                    for (var j = 0; j < values[i].data.resources.length; j++) {
                        var resource = values[i].data.resources[j];
                        // take only parent resources: computes without links or platforms
                        if (resource.category == 'platform' || !resource.links) {
                            controller.services.push(resource);
                        }
                    }
                }
                if (callback) {
                    callback()
                }
            });
        }

        this.configureAction = function (service, action) {
            $scope.service = service
            $scope.action = action
            $scope.config = config

            if (Object.keys(action.attributes).length != 0) {

                // TODO do not open if no attributes, do it directly
                var modalInstance = $modal.open({
                    templateUrl: 'partials/action.html',
                    scope: $scope
                });

                modalInstance.result.then(function (action) {
                    var postConfig = {headers: {}, params: {action: action.title}};
                    postConfig.headers['X-OCCI-Attribute'] = Occi.attributesToOcciFormat(action);

                    $http.post('/ca/api/occi/' + service.category + '/' + service.uuid, {}, postConfig).success(function (date) {
                        notificationService.success("Action " + action.title + " for " + service.uuid + " performed")
                        controller.doRefresh();
                    });
                }, function () {
                    // nothing to do if modal closed
                });
            } else {
                var postConfig = {params: {action: action.title}};
                $http.post('/ca/api/occi/' + service.category + '/' + service.uuid, {}, postConfig).success(function (date) {
                    notificationService.success("Action " + action.title + " for " + service.uuid + " performed")
                    controller.doRefresh();
                });
            }
        }

        this.doRefresh();
        this.refresh(User.isAutoRefreshEnabled() || false);
    });

    services.controller('ServiceDetailsCtrl', function ($http, $q, $routeParams) {
        var controller = this;
        this.compute = $http.get('/ca/api/occi/' + $routeParams.category + '/' + $routeParams.serviceId).
            success(function (data) {
                controller.service = data;
            });
    });

}());