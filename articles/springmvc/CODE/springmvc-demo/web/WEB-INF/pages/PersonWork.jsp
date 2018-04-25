<%--
  Created by IntelliJ IDEA.
  User: 维C果糖
  Date: 2017/5/21
  Time: 13:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>工作信息 PersonWork</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/demowizard.action" method="post">

    <table>
        <tr>
            <td>年龄:</td>
            <td><input tyep="text" name="id" value="${p.age}"></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" name="_target1" value="上一步"/>
                <input type="submit" name="_cancel" value="取消"/>
                <input type="submit" name="_finish" value="完成"/>
            </td>
        </tr>
    </table>
</form>

</body>
</html>