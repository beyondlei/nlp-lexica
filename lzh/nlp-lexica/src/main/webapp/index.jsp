<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags"%>

<html>
	<head>
		<title>NPL&DBpedia-Lexica</title>
		<meta name="robots" content="noindex,nofollow">
		
		<style>a{TEXT-DECORATION:none}</style> 
	</head>
<body>
<h2>NPL&DBpedia-Lexica</h2>

<hr>
<s:form action="index.action" method="post" >
	<select name="inlang">
		<option value = "en">input language</option>
		<option value = "zh">中文</option>
		<option value = "en" selected>English</option>
		<option value = "de">Deutsch</option>
		<option value = "es">Español</option>
		<option value = "ca">Català</option>
		<option value = "sl">Slovenščina</option>
	</select>
	
	<select name="outlang">
		<option value = "en">output language</option>
		<option value = "zh">中文</option>
		<option value = "en" selected>English</option>
		<option value = "de">Deutsch</option>
		<option value = "es">Español</option>
		<option value = "ca">Català</option>
		<option value = "sl">Slovenščina</option>
	</select>

	<select name="resultNum">
		<option value = "100">number of results</option>
		<option value = "100" selected>100 results</option>
		<option value = "1000">1000 results</option>
		<option value = <%=Integer.MAX_VALUE%>>all results</option>
	</select>
	
	<select name="type">
		<option value = "1">search type</option>
		<option value = "1" selected>resource</option>
		<option value = "2">label</option>
		<option value = "3">word</option>
	</select>
	
	<input type="text" name="searcher" value="">
	<input type="submit" value="search">
	<input type="checkbox" name="compare" value="1" checked="checked">Compare with <a href="http://wiki.dbpedia.org/Datasets/NLP">DBpedia NLP Datasets</a>
</s:form>

<p><small>
Examples:
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=1&searcher=New York&compare=1">New York (Resource)</s:a>, 
<s:a href="index.action?inlang=zh&outlang=en&resultNum=100&type=1&searcher=纽约&compare=1">纽约 (Resource)</s:a>,
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=2&compare=1&searcher=football">football (Label)</s:a>,
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=3&compare=1&searcher=ipad">ipad (Word)</s:a>
</small>

<hr>
</body>
</html>