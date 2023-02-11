-- 创建用户活跃表
create table if not exists acitve_user(
	rid varcahr(10),
	uname varchar(50),
	dt varchar(20),
	num int,
	primary key(rid,dt,uname)
);


-- 创建弹幕数量表
create table if not exists barrage_num(
	rid varcahr(10),
	dt varchar(20),
	num int,
	primary key(rid,dt)
);


-- 创建相同弹幕表
create table if not exists barrage_top(
	rid varcahr(10),
	content varchar(255),
	dt varchar(20),
	num int,
	primary key(rid,dt,content)
);


-- 创建用户等级表
create table if not exists user_level(
	rid varcahr(10),
	level int,
	dt varchar(20),
	num int,
	primary key(rid,dt)
);
