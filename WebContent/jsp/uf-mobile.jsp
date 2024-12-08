<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
<title>common</title>
<style type="text/css">
.uf-hide{display:none}
.uf-widest{width:100%}
.uf-text-center{text-align:center}
.uf-text-left{text-align:left}
.uf-text-right{text-align:right}
.uf-text-color{color:#ff00ff}
.uf-bg-color{background-color:#ff00ff}
.uf-size-1.2{font-size:1.2rem}
.uf-size-1.3{font-size:1.3rem}
.uf-size-1.5{font-size:1.5rem}
.uf-size-1.7{font-size:1.7rem}
.uf-size-1.8{font-size:1.8rem}
.uf-size-2{font-size:2rem}
.uf-bolder{font-weight: bolder}
.uf-guide{text-align:center;cursor:pointer}
.uf-pop-up-box-back{position:fixed;top:0;left:0;width:100%;height:100%;opacity:0.6;background-color:#808080;display:none;z-index:1}
.uf-pop-up-box{position:fixed;left:0;bottom:5rem;width:30rem;background-color:white;display:none;z-index:2}
.uf-current{text-decoration:underline;font-weight:bold}
.uf-img-back{position:fixed;top:0;left:0;width:100%;height:100%;background-color:black;display:none;z-index:3}
.uf-img-back img{position:absolute;top:50%;left:50%;width:100%;transform:translate(-50%,-50%)}
.uf-input{border:none;border-bottom:1px solid #c0c0c0;width:25rem}
.uf-full-back{position:fixed;top:0;left:0;width:100%;height:100%;display:none;z-index:2}
.uf-no-net{position: fixed;top: 0;left: 10rem;color: red;display: none}
.uf-deadline{height:10rem;margin-top:5rem;width:100%;text-align:center}
</style>
</head>
<body>
	<span class="uf-no-net">网络异常，请检查网络！</span>
	<!-- 弹出框的半透明背景 -->
	<div class="uf-pop-up-box-back"></div>
	<div class="uf-img-back"></div>
	<div class="uf-full-back"></div>
<script src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
<script src="<%=request.getContextPath()%>/js/echarts.min.js"></script>
<script type="text/javascript">
$('html').css('font-size', $(window).width()/30 + 'px');//设置rem为网页宽度的三十分之一(手机页面)
$(function() {
	/*导航菜单
		用法：
		1.菜单父级的class加上uf-menu
		2.每个菜单class加上uf-guide
		3.菜单控制的块加上菜单本身的class
		4.菜单控制的块的父级class加上uf-guide-list
		5.菜单父级要与菜单控制的块的父级平级
	*/
	$('.uf-guide').click(function(){
		$(this).addClass('uf-current');
		$(this).siblings().removeClass('uf-current');
		var jqObj = $('.uf-guide-list .' + $(this).attr('class').split(' ')[0]);
		jqObj.siblings().hide();
		jqObj.show();
	});
	$('.uf-menu').each(function(){
		$(this).find('.uf-guide').first().click();
	});
	//点击灰色区域则关闭弹出框
	$('.uf-pop-up-box-back').click(function(){
		$('.uf-pop-up-box').hide();
		$('.uf-pop-up-box-back').hide();
	});
	$('.uf-img-back').click(function(){
		$(this).hide();
	});
});

//打开弹出框
function openBox(jqObj){
	$('.uf-pop-up-box-back').show();
	jqObj.show();
}

//查看大图
function openImg(src){
	$('.uf-img-back').html('<img src="'+src+'">');
	$('.uf-img-back').show();
}

//ajax封装
function ajax(ajaxObj){
	$.ajax({
		url: '<%=request.getContextPath()%>/' + ajaxObj.url,
		type: ajaxObj.data?'POST':'GET',
		data: ajaxObj.data,
		async: ajaxObj.async,
		success: function(result){
			if(result == ''){
				if(ajaxObj.ignoreNull) return;
				alert('系统繁忙，请稍后重试');
			}else if(result == 'Login please!'){
				isLogon();
			}else{
				var json;
				if(result.indexOf('{')==0||result.indexOf('[')==0){
					try{
						json=JSON.parse(result.replace(/[\n\r]/g,'&#10;').replace(/	/g,'&emsp;&emsp;').replace(/\\/g,'\\\\'));
					}catch (e) {}
				}				
				if(typeof json != 'object'){
					json=result;
	            }
				try{
					if(json.code == 'wrong'){
						alert(json.msg);
						ajaxObj.wrong(json);
					}else{
						ajaxObj.right(json);
					}
				}catch (e) {}
			}
		},
		error: function(result){
			$('.uf-no-net').show();
			window.setTimeout(function(){
				$('.uf-no-net').hide();
			}, 4000);
		}
	});
	return true;
}

//获取当前时间(yyyy-MM-dd HH:mm:ss)
function now10(){
	var myDate = new Date();
	var year = myDate.getFullYear();
	var month = myDate.getMonth() + 1;
	var date = myDate.getDate();
	return year + '-' + fillDateTime(month) + "-" + fillDateTime(date);
}
function fillDateTime(t) {
    return t < 10 ? '0' + t : '' + t;
}

//当页面需要显示用户输入的信息时，需要用此方法转义，防html注入，也为了更好展示
function htmlEncode(value){
	return value.replace(/</g,'&lt;')
	.replace(/>/g,'&gt;')
	.replace(/[\n\r]/g,'<br/>')
	.replace(/&#10;/g,'<br/>')
	.replace(/ /g,'&ensp;')
	.replace(/	/g,'&emsp;&emsp;');
}

//判断中文
function isChinese(str){
	return new RegExp("[\u4E00-\u9FA5]+").test(str);
}
//判断字母(英文)
function isEnglish(str){
	return new RegExp("[A-Za-z]+").test(str);
}
//判断数字或字符
function isNumber(str){
	return new RegExp("[0-9]+").test(str);
}
</script>
</body>
</html>