var app = angular.module('app', [
    'ngRoute',
    'appControllers',
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
                controller: 'CatalogCtrl'
            }).
            when('/services', {
                templateUrl: 'partials/services.html',
                controller: 'ServicesCtrl'
            }).
            otherwise({
                redirectTo: '/services'
            });
    }]);