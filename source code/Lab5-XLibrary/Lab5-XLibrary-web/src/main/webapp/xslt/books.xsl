<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : books.xsl
    Created on : 10 июня 2016 г., 14:58
    Author     : tolik
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title>Просмотр книг</title>
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous" />
            </head>
            <body>
                <xsl:apply-templates mode="books"/>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="books" mode="books">
        <div class="container">
            <h1>Просмотр книг</h1>
            <ul class="list-unstyled">
                <xsl:apply-templates select="." mode="book"/>
            </ul>
        </div>
    </xsl:template>
    
    <xsl:template match="book" mode="book">
        <li>
            <table class="table table-striped table-bordered table-hover table-condensed">
                <tr>
                    <th class="col-md-2">Название</th>
                    <th><xsl:value-of select="name"/></th>
                </tr>
                <tr>
                    <td class="col-md-2">Автор</td>
                    <td><xsl:value-of select="author"/></td>
                </tr>
                <tr>
                    <td class="col-md-2">Описание</td>
                    <td><xsl:value-of select="description"/></td>
                </tr>
                <tr>
                    <td class="col-md-2">Жанр</td>
                    <td><xsl:value-of select="genre/name"/></td>
                </tr>
                <tr>
                    <td class="col-md-2">ISBN</td>
                    <td><xsl:value-of select="isbn"/></td>
                </tr>
                <tr>
                    <td class="col-md-2">Кол-во страниц</td>
                    <td><xsl:value-of select="pageCount"/></td>
                </tr>
                <tr>
                    <td class="col-md-2">Издатель</td>
                    <td><xsl:value-of select="publisher"/></td>
                </tr>
            </table>
        </li>
    </xsl:template>

</xsl:stylesheet>
