app.service('uploadService',function ($http) {
    this.uploadFile=function () {
        //创建一个H5的form表单对象
        var formData = new FormData();
        formData.append("file",file.files[0]);
        return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            //设置contenttype:undefined 表示会使用多媒体类型的content-type:multpart/form-data
            headers: {'Content-Type':undefined},
            //设置angularjs的传递流的方式   会自动添加分割线 允许添加多个参数。
            transformRequest: angular.identity
        });
    }
})