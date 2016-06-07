<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@include file="jspf/header.jspf" %>

<c:set var="genreTitle" value="${isAddGenre ? 'Добавить жанр' : genre.name}" />

<div class="container">
    <ol class="breadcrumb">
        <li><a href="${pageContext.request.contextPath}">Навигация</a></li>
        <li><a href="genres">Жанры</a></li>
        <li class="active">${genreTitle}</li>
    </ol>
    
    <h1>${genreTitle}</h1>
    
    <!-- Панель с информацией об ошибке -->
    <c:choose>
        <c:when test="${errMsg != null}">
            <div class="alert alert-danger" role="alert">
                ${errMsg}
            </div>
        </c:when>    
    </c:choose>

    <!-- Форма с данными -->
    <form class="form form-horizontal" action="genre?id=${genre.id}" method="POST">
        <div class="form-group">
            <div class="col-sm-2">Название:</div>
            <div class="col-sm-10">
                <input class="form-control" type="input" name="name" value="${genre.name}">
            </div>
        </div>
        
        <!-- Кнопки действий -->
        <div class="well">
            <c:choose>
                <c:when test="${!isAddGenre}">
                    <button class="btn btn-danger" type="submit" name="submit_type" value="delete">Удалить</button>
                </c:when>
            </c:choose>
            <button class="btn btn-default" type="submit" name="submit_type" value="save_and_add_another">Сохранить и добавить ещё</button>
            <button class="btn btn-default" type="submit" name="submit_type" value="save_and_stay">Сохранить и остаться</button>
            <button class="btn btn-primary" type="submit" name="submit_type" value="save">Сохранить</button>
        </div>
    </form>
    
</div>

<%@include file="jspf/footer.jspf" %>