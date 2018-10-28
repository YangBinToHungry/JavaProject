 //品牌控制层 
app.controller('baseController' ,function($scope){	
	
    //重新加载列表 数据
    $scope.reloadList=function(){
    	//切换页码  
    	$scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);	   	
    }
    
	//分页控件配置 
	$scope.paginationConf = {
         currentPage: 1,
         totalItems: 10,
         itemsPerPage: 10,
         perPageOptions: [10, 20, 30, 40, 50],
         onChange: function(){
        	 $scope.reloadList();//重新加载
     	 }
	}; 
	
	$scope.selectIds=[];//选中的ID集合 

	//更新复选
	$scope.updateSelection = function($event, id) {		
		if($event.target.checked){//如果是被选中,则增加到数组
			$scope.selectIds.push( id);			
		}else{
			var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除 
		}
	}

	//将一个数组形式的字符串  循环遍历 将里面某一个属性的值 提出来，通过特定的符号来拼接返回

    /**
	 *
     * @param list [{"id":13,"text":"长虹"},{"id":14,"text":"海尔"},{"id":4,"text":"小米"}]  是一个字符串
     * @param key  要取的哪一个属性  text
     */
	$scope.jsonToString=function (list,key) {
		//1.将字符串转成json对象
		/*var objxx = {};
		objxx.id=1;
		objxx['id']=1;
		console.log(objxx);*/

		var fromJson = angular.fromJson(list);
		var str="";
		for (var i=0;i<fromJson.length;i++){
			var obj = fromJson[i];//{"id":13,"text":"长虹"}
			str+=obj[key]+",";
		}
		if(str.length>0){
           str= str.substring(0,str.length-1);
        }

		return str;
    }

    //从集合中按照key查询对象
    $scope.searchObjectByKey=function(list,key,keyValue){
        for(var i=0;i<list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }
	
});	