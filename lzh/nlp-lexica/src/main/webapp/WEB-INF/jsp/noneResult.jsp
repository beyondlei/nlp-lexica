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
		<s:if test="%{inlang == 'zh'}"><option value = "zh" selected>中文</option></s:if><s:else><option value = "zh">中文</option></s:else>
		<s:if test="%{inlang == 'en'}"><option value = "en" selected>English</option></s:if><s:else><option value = "en">English</option></s:else>
		<s:if test="%{inlang == 'de'}"><option value = "de" selected>Deutsch</option></s:if><s:else><option value = "de">Deutsch</option></s:else>
		<s:if test="%{inlang == 'es'}"><option value = "es" selected>Español</option></s:if><s:else><option value = "es">Español</option></s:else>
		<s:if test="%{inlang == 'ca'}"><option value = "ca" selected>Català</option></s:if><s:else><option value = "ca">Català</option></s:else>
		<s:if test="%{inlang == 'sl'}"><option value = "sl" selected>Slovenščina</option></s:if><s:else><option value = "sl">Slovenščina</option></s:else>
	</select>
	
	<select name="outlang">
		<option value = "en">output language</option>
		<s:if test="%{outlang == 'zh'}"><option value = "zh" selected>中文</option></s:if><s:else><option value = "zh">中文</option></s:else>
		<s:if test="%{outlang == 'en'}"><option value = "en" selected>English</option></s:if><s:else><option value = "en">English</option></s:else>
		<s:if test="%{outlang == 'de'}"><option value = "de" selected>Deutsch</option></s:if><s:else><option value = "de">Deutsch</option></s:else>
		<s:if test="%{outlang == 'es'}"><option value = "es" selected>Español</option></s:if><s:else><option value = "es">Español</option></s:else>
		<s:if test="%{outlang == 'ca'}"><option value = "ca" selected>Català</option></s:if><s:else><option value = "ca">Català</option></s:else>
		<s:if test="%{outlang == 'sl'}"><option value = "sl" selected>Slovenščina</option></s:if><s:else><option value = "sl">Slovenščina</option></s:else>
	</select>

	<select name="resultNum">
		<option value = "100">number of results</option>
		<s:if test="%{resultNum == 100}"><option value = "100" selected>100 results</option></s:if><s:else><option value = "100">100 results</option></s:else>
		<s:if test="%{resultNum == 1000}"><option value = "1000" selected>1000 results</option></s:if><s:else><option value = "1000">1000 results</option></s:else>
		<s:if test="%{resultNum > 1000}"><option value = <%=Integer.MAX_VALUE%> selected>all results</option></s:if><s:else><option value = <%=Integer.MAX_VALUE%>>all results</option></s:else>
	</select>
	
	<select name="type">
		<option value = "1">search type</option>
		<s:if test="%{type == 1}"><option value = "1" selected>resource</option></s:if><s:else><option value = "1">resource</option></s:else>
		<s:if test="%{type == 2}"><option value = "2" selected>label</option></s:if><s:else><option value = "2">label</option></s:else>
		<s:if test="%{type == 3}"><option value = "3" selected>word</option></s:if><s:else><option value = "3">word</option></s:else>
	</select>

	<input type="text" name="searcher" value="<s:property value="searcher" />">
	<input type="submit" value="search">
	<s:if test="%{compare ==  1}"><input type="checkbox" name="compare" value="1" checked>Compare with <a href="http://wiki.dbpedia.org/Datasets/NLP">DBpedia NLP Datasets</a></s:if>
	<s:else><input type="checkbox" name="compare" value="1">Compare with <a href="http://wiki.dbpedia.org/Datasets/NLP">DBpedia NLP Datasets</a></s:else>
</s:form>

<p><small>
Examples:
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=1&searcher=New York">New York (Resource)</s:a>, 
<s:a href="index.action?inlang=zh&outlang=en&resultNum=100&type=1&searcher=纽约">纽约 (Resource)</s:a>,
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=2&searcher=football">football (Label)</s:a>,
<s:a href="index.action?inlang=en&outlang=en&resultNum=100&type=3&searcher=ipad">ipad (Word)</s:a>
</small>

<hr>
<br />
<font color="red">There is no result by your search. Please try again!</font>
</body>
</html>