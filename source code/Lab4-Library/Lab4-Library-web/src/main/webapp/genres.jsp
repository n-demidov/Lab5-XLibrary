<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@include file="jspf/header.jspf" %>

<div class="container">
    <ol class="breadcrumb">
        <li><a href="${pageContext.request.contextPath}">Навигация</a></li>
        <li class="active">Жанры</li>
    </ol>

    <h1>Жанры</h1>
    
    <!-- Кнопки добавления -->
    <div>
        <a href="genre?type=add" target="_blank">
            Добавить жанр
        </a>
        <hr>
    </div>
    
    <!-- Таблица жанров -->
    <table class="table table-striped table-bordered table-hover table-condensed">
        <tr>
            <th>ID</th>
            <th>Название</th>
        </tr>
        <c:forEach items="${genres}" var="genre" varStatus="status">
            <tr>
                <td>${genre.id}</td>
                <td>
                    <a href="genre?id=${genre.id}" target="_blank">
                        ${genre.name}
                    </a>
                </td>
            </tr>
        </c:forEach>
    </table>
    
</div>

<%@include file="jspf/footer.jspf" %>