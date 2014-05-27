(function () {
    var app = angular.module('app', [
        'catalog',
        'services',
        'ngRoute',
        'ngCookies',
        'ui.bootstrap',
        'ui.notify'
    ]);


    app.config(['$httpProvider', function ($httpProvider) {
        $httpProvider.defaults.headers.common['Accept'] = 'application/json';
    }]);

    app.config(['notificationServiceProvider', function (notificationServiceProvider) {
        notificationServiceProvider.setDefaults({
            history: false,
            delay: 4000,
            styling: 'bootstrap',
            closer: true,
            closer_hover: false
        });
    }]);


    app.config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.
                when('/catalog', {
                    templateUrl: 'partials/catalog.html',
                    controller: 'CatalogCtrl',
                    controllerAs: 'catalog'
                }).
                when('/services', {
                    templateUrl: 'partials/services.html',
                    controller: 'ServicesCtrl',
                    controllerAs: 'controller'
                }).
                when('/services/:category/:serviceId', {
                    templateUrl: 'partials/service-details.html',
                    controller: 'ServiceDetailsCtrl',
                    controllerAs: 'controller'
                }).
                otherwise({
                    redirectTo: '/services'
                });
        }]);

    app.controller('NavBarController', function ($location) {
        this.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });

    app.filter('toLogo', function () {
        return function (template) {
            var imgFile = 'default';
            if (template.attributes.application) {
                imgFile = template.attributes.application
            } else if (template.attributes.provider) {
                imgFile = template.attributes.provider
            }
            return "img/catalog/" + imgFile + ".png";
        };
    });

})();