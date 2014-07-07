(function () {
    var user = angular.module('user', []);

    user.service('User', function ($cookies) {
        this.user = $cookies.username;
        this.sessionid = $cookies.sessionid;
        this.autoRefresh = $cookies.autoRefresh === 'true';

        var service = this;

        this.login = function (credentials, sessionid) {
            service.user = credentials.username
            service.sessionid = sessionid
            $cookies.sessionid = service.sessionid
            $cookies.username = service.user
        }

        this.logout = function () {
            service.user = null;
            $cookies.sessionid = null;
            $cookies.username = null;
        }

        this.isConnected = function () {
            return service.user != null;
        }

        this.isAutoRefreshEnabled = function () {
            return service.autoRefresh
        }

        this.enableAutoRefresh = function (enabled) {
            service.autoRefresh = enabled
            $cookies.autoRefresh = enabled
        }
    });

})();