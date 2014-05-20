// Customize attribute display
app.factory('config', function() {
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
            'occi.compute.value':{
                visible: true,
                label: "A compute value"
            },
            'flavor':{
                visible: true,
                label: "Flavor (SMALL,LARGE)",
                required: true
            }
        }
    };
});
