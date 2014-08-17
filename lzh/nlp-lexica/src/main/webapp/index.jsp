<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags"%>

<html>
	<head>
		<title>NPL-Search</title>
		<meta name="robots" content="noindex,nofollow">
		
		<style>a{TEXT-DECORATION:none}</style> 
	</head>
<body>
<h2>NPL-Search</h2>

<hr>
<s:form action="index.action" method="post" >
	<select name="inlang">
		<option value = "en" selected>input language</option>
		<option value = "zh">中文</option>
		<option value = "en">English</option>
		<option value = "de">Deutsch</option>
		<option value = "es">Español</option>
		<option value = "ca">Català</option>
		<option value = "sl">Slovenščina</option>
	</select>
	
	<select name="outlang">
		<option value = "en" selected>output language</option>
		<option value = "zh">中文</option>
		<option value = "en">English</option>
		<option value = "de">Deutsch</option>
		<option value = "es">Español</option>
		<option value = "ca">Català</option>
		<option value = "sl">Slovenščina</option>
	</select>

	<select name="resultNum">
		<option value = "100" selected>number of results</option>
		<option value = "100">100 results</option>
		<option value = "1000">1000 results</option>
		<option value = <%=Integer.MAX_VALUE%>>all results</option>
	</select>
	
	<select name="type">
		<option value = "1" selected>search type</option>
		<option value = "1">resource</option>
		<option value = "2">label</option>
		<option value = "3">word</option>
	</select>
	
	<input type="text" name="searcher" value="">
	<input type="submit" value="search">
	<input type="checkbox" name="compare" value="1" checked="checked">Compare with <a href="http://wiki.dbpedia.org/Datasets/NLP">DBpedia NLP Datasets</a>
</s:form>

<p><small>
Examples:
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=1&compare=1&searcher=FIFA U-20 World Cup">FIFA U-20 World Cup,&nbsp;&nbsp;&nbsp;&nbsp;</s:a>
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=2&compare=1&searcher=football">football,&nbsp;&nbsp;&nbsp;&nbsp;</s:a>
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=3&compare=1&searcher=ipad">ipad,&nbsp;&nbsp;&nbsp;&nbsp;</s:a>
</small>

<hr>
</body>
</html>