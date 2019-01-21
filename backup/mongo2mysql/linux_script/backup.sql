CREATE DATABASE IF NOT EXISTS dbname default charset utf8 COLLATE utf8_general_ci;
use dbname;
load data local infile 'bakfilepath'
into table `tablename` character set utf8
fields terminated by ',' optionally enclosed by '"'
lines terminated by '\n'
ignore 1 lines;
