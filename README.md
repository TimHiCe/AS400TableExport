# AS400TableExport
Exports all tables of an AS400 library to a MySQL/Maria DB.  

## Story
Needed a quick and easy way to onetime export all tables of an AS400 lib to MySQL. Only the tables with content are needed without relations, constraints or indexes.

## What it is
It is a quick and dirty solution for my problem. It is only tested with my library. And it only knows, what it must know to export my lib to mysql 😉

## What the code do
It selects all tables of the given lib from ibmsys in alphabetic order, creates the table on MySQL and copy the full table data to  it. To have a secure primary key it creates an id column  with autoincrement.

## What the code doesn’t do
This code doesn’t copy any indexes or constraints. It doesn’t know every data type. It is not good in logging and it is not very reliable against errors.

## What do you need
Because of license issues you need your one JDBC driver for AS400 and MySQL.
I used:
-	MySQL: mysql-connector-j-8.0.31.jar
-	AS400: jt400_V5R3.jar
