<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@include file="jspf/header.jspf" %>

<c:set var="bookTitle" value="${isAddBook ? 'Добавить книгу' : book.name}" />

<div class="container">
    <ol class="breadcrumb">
        <li><a href="${pageContext.request.contextPath}">Навигация</a></li>
        <li><a href="books">Книги</a></li>
        <li class="active">${bookTitle}</li>
    </ol>
    
    <h1>${bookTitle}</h1>
    
    <!-- Панель с информацией об ошибке -->
    <c:choose>
        <c:when test="${errMsg != null}">
            <div class="alert alert-danger" role="alert">
                ${errMsg}
            </div>
        </c:when>    
    </c:choose>

    <!-- Форма с данными -->
    <form class="form form-horizontal" action="${fullUrl}" method="POST">
        <div class="form-group">
            <div class="col-sm-2">Название:</div>
            <div class="col-sm-10">
                <input class="form-control" type="input" name="name" value="${book.name}">
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-2">Автор:</div>
            <div class="col-sm-10">
                <input class="form-control" type="input" name="author" value="${book.author}">
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-2">Жанр:</div>
            <div class="col-sm-10">
                <div>
                    <a href="genre?id=${book.genre.id}" target="_blank">
                        ${book.genre.name}
                    </a>
                </div>
                <select class="form-control" name="genre_id">
                    <c:forEach items="${genres}" var="genre" varStatus="status">
                        <option
                            value="${genre.id}"
                            <c:if test="${genre.id eq book.genre.id}">
                                selected
                            </c:if>>
                            ${genre.name}
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-2">Издатель:</div>
            <div class="col-sm-10"><input class="form-control" type="input" name="publisher" value="${book.publisher}"></div>
        </div>
        <div class="form-group">
            <div class="col-sm-2">ISBN</div>
            <div class="col-sm-10"><input class="form-control" type="input" name="isbn" value="${book.isbn}"></div>
        </div>
        <div class="form-group">
            <div class="col-sm-2">Кол-во страниц:</div>
            <div class="col-sm-10"><input class="form-control" type="input" name="page_count" value="${book.pageCount}"></div>
        </div>
        <div class="form-group">
            <div class="col-sm-2">Описание:</div>
            <div class="col-sm-10">
                <textarea class="form-control" rows="10" name="description">${book.description}</textarea>
            </div>
        </div>
        
        <!-- Кнопки действий -->
        <div class="well">
            <c:choose>
                <c:when test="${!isAddBook}">
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