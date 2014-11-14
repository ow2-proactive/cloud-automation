(function () {
    var appConfig = angular.module('appConfig', []);
    // Customize attribute display

    appConfig.factory('config', function () {
        return {
            attributes: {
                'category': {
                    visible: false
                },
                'provider': {
                    visible: false
                },
                'occi.core.id': {
                    visible: false
                },
                'occi.compute.id': {
                    visible: false
                },
                'operation': {
                    visible: false
                },
                'action.state': {
                    visible: false
                },
                'occi.core.location': {
                    visible: false
                },
                'action': {
                    visible: false
                },
                'application': {
                    visible: false
                },
                'action.from-states': {
                    visible: false
                },
                'occi.compute.value': {
                    visible: true,
                    label: "A compute value"
                },
                'occi.platform.value': {
                    visible: true,
                    label: "A platform value"
                },
                'flavor': {
                    visible: true,
                    label: "Flavor (SMALL,LARGE)",
                    required: true
                },
                'occi.stop.method': {
                    visible: true,
                    label: "Stop Method",
                    required: true
                }
            }
        };
    });
}());