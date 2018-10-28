app.service('loginService',function ($http) {
    this.getUserInfo=function () {
        return $http.get('../userinfo/getUserInfo.do');
    }
})