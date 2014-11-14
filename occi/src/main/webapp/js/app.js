(function () {
    var app = angular.module('app', [
        'user',
        'catalog',
        'services',
        'ngRoute',
        'ngCookies',
        'ui.bootstrap',
        'ui.notify'
    ]);


    app.config(['notificationServiceProvider', function (notificationServiceProvider) {
        notificationServiceProvider.setDefaults({
            history: false,
            delay: 3000,
            styling: 'bootstrap',
            closer: true,
            closer_hover: false
        });
    }]);

    app.config(['$httpProvider', function ($httpProvider) {
        $httpProvider.defaults.headers.common['Accept'] = 'application/json';

        $httpProvider.interceptors.push(function ($q, $cookies, User) {
            return {
                // automatically add sessionid to every queries
                'request': function (config) {
                    config.headers = config.headers || {};
                    config.headers.sessionid = User.sessionid;
                    return config;
                },
                // automatically disconnect user if 401
                'responseError': function (rejection) {
                    if (rejection.status == 401) {
                        User.logout()
                    }
                    return $q.reject(rejection);
                }
            };
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

    app.controller('NavBarController', function ($location, $http, $route, User, notificationService) {
        this.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };

        this.login = function (credentials) {
            var formHeaders = {headers: {'Content-Type': 'application/x-www-form-urlencoded'}};
            $http.post('/ca/api/login', $.param(credentials), formHeaders).then(function (res) {
                User.login(credentials, res.data)
                $route.reload()
                notificationService.success("Connected as " + credentials.username)
            });
        }

        this.getConnectedUser = function () {
            return User.user
        }

        this.isConnected = User.isConnected

        this.logout = function () {
            User.logout()
            $route.reload()
        }

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

    app.filter('actionToIcon', function () {
        return function (action) {
            switch (action) {
                case "suspend":
                    return "fa-pause";
                case "stop":
                    return "fa-stop";
                case "start":
                    return "fa-play";
                default:
                    return '';
            }
        };
    });

})();