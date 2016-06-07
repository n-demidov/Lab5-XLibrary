<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@include file="jspf/header.jspf" %>

<!-- Устанавливаем константы -->
<c:set var="baseUrl" value="books?"/>
<c:set var="urlWithFilters" value="${baseUrl}${param.search != null ? 'search='.concat(param.search) : ''}${param.genre != null ? '&genre='.concat(param.genre) : ''}"/>

<div class="container">
    <ol class="breadcrumb">
        <li><a href="${pageContext.request.contextPath}">Навигация</a></li>
        <li class="active">Книги</li>
    </ol>

    <h1>Книги</h1>
    
    <!-- Панель поиска -->
    <div class="well">
        <form class="form form-horizontal" action="books" method="GET">
            <div class="form-group">
                <div class="col-sm-8">
                    <input class="form-control" type="text" name="search" value="${param.search}" placeholder=" Поиск по автору\названию">
                </div>
                <div class="col-sm-3">
                    <select class="form-control" name="genre">
                        <option value="0">Все жанры</option>
                        <c:forEach items="${genres}" var="genre" varStatus="status">
                            <option
                                value="${genre.id}"
                                <c:if test="${genre.id eq param.genre}">
                                    selected
                                </c:if>>
                                ${genre.name}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-sm-1">
                    <button class="btn btn-primary" type="submit">Найти</button>
                </div>
            </div>
            <!-- Скрытые поля с выбранной сортировкой -->
            <input type="hidden" name="sortField" value="${param.sortField}">
            <input type="hidden" name="sortOrder" value="${param.sortOrder}">
            
        </form>
    </div>
    
    <!-- Панель с кнопками действий -->
    <form class="form form-horizontal" action="${fullURI}" method="POST">
        <div class="well">
            <div class="form-group">
                <div class="col-sm-8">
                    <!-- Кнопки действий -->
                    <select id="actions-select" class="form-control" name="action" disabled="disabled">
                        <option value="nothing">--- Выберите действие ---</option>
                        <option value="delete">Удалить</option>
                        <option value="copy">Копировать</option>
                        <option value="export-xml">Экспортировать в XML</option>
                    </select>
                </div>
                <div class="col-sm-2">
                    <button class="btn btn-primary" type="submit">Выполнить</button>
                </div>
                
                <div class="col-sm-2">
                    <!-- Кнопки добавления -->
                    <a class="btn btn-success pull-right" href="book?type=add" target="_blank">
                        Добавить книгу
                    </a>
                </div>
            </div>
        </div>

        <!-- Панель с информацией об ошибке -->
        <c:choose>
            <c:when test="${errMsg != null}">
                <div class="alert alert-danger" role="alert">
                    ${errMsg}
                </div>
            </c:when>    
        </c:choose>
        
        <!-- Таблица книг -->
        <table class="table table-striped table-bordered table-hover table-condensed">
            <!-- Заголовки с сортировкой -->
            <tr>
                <th>
                    <input type="checkbox" id="toggle-all-books" />
                </th>
                
                <c:forEach items="${ORDER_VALUES}" var="sortVal" varStatus="status">
                    <th>
                        <c:choose>
                            <c:when test="${param.sortField eq sortVal}">
                                <c:choose>
                                    <c:when test="${'up' eq param.sortOrder}">
                                        <a href="${urlWithFilters}">
                                            <i class="glyphicon glyphicon-triangle-top"></i><span>${sortVal}</span>
                                        </a>
                                    </c:when>    
                                    <c:otherwise>
                                        <a href="${urlWithFilters}&sortField=${sortVal}&sortOrder=up">
                                            <i class="glyphicon glyphicon-triangle-bottom"></i><span>${sortVal}</span>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>    
                            <c:otherwise>
                                <a href="${urlWithFilters}&sortField=${sortVal}"
                                    >${sortVal}</a>
                            </c:otherwise>
                        </c:choose>
                    </th>
                </c:forEach>
            </tr>
            
            <!-- Значения -->
            <c:forEach items="${books}" var="book" varStatus="status">
                <tr>
                    <td>
                        <input type="checkbox" class="chk-select-book" name="param[]" value="${book.id}" />
                    </td>
                    <td>
                        <a href="book?id=${book.id}" target="_blank">
                            ${book.id}
                        </a>
                    </td>
                    <td>
                        <a href="book?id=${book.id}" target="_blank">
                            ${book.name}
                        </a>
                    </td>
                    <td>
                        <a href="genre?id=${book.genre.id}" target="_blank">
                            ${book.genre.name}
                        </a>
                    </td>
                    <td>${book.author}</td>
                    <td>${book.publisher}</td>
                    <td>${book.isbn}</td>
                    <td>${book.pageCount}</td>
                    <td>${book.description}</td>
                </tr>
            </c:forEach>
        </table>
        
        <input type="hidden" name="from" value="${fullURI}">
    </form>
    
</div>

<%@include file="jspf/footer.jspf" %>

<!-- js-файлы -->
<script src="js/books.js"></script>
