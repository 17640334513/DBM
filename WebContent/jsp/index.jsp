<%@page import="util.PropUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>执行sql</title>
<%@include file="uf-mobile.jsp"%>
<style type="text/css">

</style>
</head>
<body class="uf-text-center">
<select id="dataSource">
<%
String[] dataSources = PropUtil.get("DATA_SOURCES").split(",");
for(String dataSource : dataSources){
%>
	<option value="<%=dataSource%>"><%=dataSource%></option>
<%} %>
</select>
<br>
<textarea id="sql" rows="20" cols="30" class="uf-widest"></textarea>
<button id="execute" class="uf-input">执行sql语句</button>
<table id="result" border="1" class="uf-widest"></table>
<script type="text/javascript">

var datas;
var keyMap;

$(function(){
	$('#execute').click(function(){
		ajax({
			url: 'Admin/executeSql.o',
			data: {
				dataSource: $('#dataSource').val(),
				sql: $('#sql').val()
			},
			right: function(json){
				if(json.data.length == 0){
					$('#result').html('<tr><td>no result</td></tr>');
					return;
				}
				datas = json.data;
				keyMap = {};
				var map0 = datas[0];
				var i = 0;
				for(var key in map0){
					keyMap[i] = key;
					i ++;
		　　　　	}
				loadTable();
			}
		});
	});
});

function left(i){
	var key = keyMap[i];
	var leftKey = keyMap[i - 1];
	keyMap[i - 1] = key;
	keyMap[i] = leftKey;
	loadTable();
}
function right(i){
	var key = keyMap[i];
	var rightKey = keyMap[i + 1];
	keyMap[i] = rightKey;
	keyMap[i + 1] = key;
	loadTable();
}

function loadTable(){
	var tableHtml = '<tr>';
	var map0 = datas[0];
	var keyCount = Object.keys(keyMap).length;
	for(var i = 0; i < keyCount; i ++){
		var key = keyMap[i];
		if(keyCount == 1){
			tableHtml += '<td><span>' + key + '</span></td>';
		}else if(i == 0){
			tableHtml += '<td><span>' + key + '</span><button onclick="right(' + i + ')">→</button></td>';
		}else if(i == keyCount - 1){
			tableHtml += '<td><button onclick="left(' + i + ')">←</button><span>' + key + '</span></td>';
		}else{
			tableHtml += '<td><button onclick="left(' + i + ')">←</button><span>' + key + '</span><button onclick="right(' + i + ')">→</button></td>';
		}
	}
	tableHtml += '</tr>';
	for(var map of datas){
		tableHtml += '<tr>';
		for(var i = 0; i < keyCount; i ++){
			var key = keyMap[i];
			tableHtml += '<td>' + map[key] + '</td>';
　　　　}
		tableHtml += '</tr>';
	}
	$('#result').html(tableHtml);
}
</script>
</body>
</html>