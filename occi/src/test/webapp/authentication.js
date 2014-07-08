module.exports = {

    'Login/Logout': function (test) {
        test
            .open('http://localhost:8081/ca/#/catalog')

            .assert.numberOfElements("#catalog .placeholder", 0, "Nothing is displayed when not logged in")
            .assert.visible("#ca-login", "Login field is displayed")
            .assert.visible("#ca-password", "Password field is displayed")

            .type("#ca-login", "admin")
            .type("#ca-password", "admin")
            .click("#ca-login-btn")

            .waitFor(function () {
                return !!$('#connected').is(':visible');
            }, [])
            .assert.visible('#connected', "Username is displayed")
            .assert.text("#connected span").to.contain('Connected as admin', "Username is displayed")
            .assert.numberOfElements("#catalog .placeholder", 2, "Two templates should be displayed")

            .click("#ca-logout-btn")
            .assert.visible('#ca-login', "Login button is displayed once logged out")
            .done();
    }

};