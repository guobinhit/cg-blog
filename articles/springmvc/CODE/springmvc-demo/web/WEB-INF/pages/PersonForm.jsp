<%--
  Created by IntelliJ IDEA.
  User: 维C果糖
  Date: 2017/5/21
  Time: 13:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>人员列表</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/personform.action" method="post">

    <table>
        <tr>
            <td>编号:</td>
            <td><input tyep="text" name="id"></td>
        </tr>
        <tr>
            <td>姓名:</td>
            <td><input tyep="text" name="name"></td>
        </tr>
        <tr>
            <td>年龄:</td>
            <td><input tyep="text" name="age"></td>
        </tr>
        <tr>
            <td colspan="2"><input type="button" name="btnOK" value="submit"></td>
        </tr>
    </table>
</form>

</body>
</html>