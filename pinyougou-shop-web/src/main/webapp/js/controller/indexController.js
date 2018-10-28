app.controller('indexController',function ($scope,loginService) {
    //写一个方法 被页面一加载就调用
    $scope.getUserInfo=function () {
        loginService.getUserInfo().success(
            function (response) {//Map
                $scope.userInfo=response;
            }
        )
    }
})