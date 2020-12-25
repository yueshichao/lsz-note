# tapd二次开发
## 小表查出大表索引字段
- tapd_bugs 7万条数据
关键字段：workspace_id, bug_id, module_name

- tapd_bug_changes 77万条数据
关键字段：workspace_id, bug_id, old_value, new_value

- 根据workspace_id, module_name, old_value, new_value查询统计

```sql
SELECT
	d.workspace_id,
	d.`name` AS axis_key,
	count( * ) AS amount
FROM
	(
SELECT
	a.workspace_id,
	b.`name`,
	c.field,
	c.new_value 
FROM
	`tapd_bugs` AS a
	LEFT JOIN tapd_projects AS b ON a.workspace_id = b.project_id
	LEFT JOIN tapd_bug_changes AS c ON a.bug_id = c.bug_id 
WHERE
	1 = 1 
	AND a.workspace_id IN ( SELECT project_id FROM tapd_ext_user_project WHERE user_id = 1 ) 
	) AS d 
WHERE
	d.new_value = 'reopened' 
	AND d.field = 'status' 
GROUP BY
	d.workspace_id
```

## 2020-04-13问题
```sql
select * from tapd_bugs as a
where bug_module in (select module_name from tapd_ext_module_config)
```

```sql
select * from tapd_bugs as a
where bug_module in ('XX1', 'XX2', 'XX3', 'XX4')
```

其中第二条sql语句查询条件即为tapd_ext_module_config全部内容
[使用exists会有不同](https://www.cnblogs.com/emilyyoucan/p/7833769.html)


## 2020-04-23
1. 
```sql
select a.project_id, b.bug_id, b.old_value, b.new_value from tapd_ext_user_project as a
LEFT JOIN (
select * from tapd_bug_changes where 1=1
and field = 'current_owner'
) as b on a.project_id = b.workspace_id;
```

2. 
```sql
select a.project_id, b.bug_id, b.old_value, b.new_value from tapd_projects as a
LEFT JOIN (
select * from tapd_bug_changes where 1=1
and field = 'current_owner'
) as b on a.project_id = b.workspace_id;
```

3. 
```sql
select a.project_id, b.bug_id, b.old_value, b.new_value from tapd_ext_user_project as a
LEFT JOIN tapd_bug_changes as b 
on a.project_id = b.workspace_id
where  b.field = 'current_owner';
```

4. 
```sql
select a.project_id, b.bug_id, b.old_value, b.new_value from tapd_ext_user_project as a
LEFT JOIN tapd_bug_changes as b 
on a.project_id = b.workspace_id and b.field = 'current_owner';
```

> 表tapd_bug_changes数据70w+，tapd_ext_user_project小表（20+），tapd_projects表（1600+），1语句10s，2语句4s，3语句1s，4语句7s


## 2020-04-26
- 修改表名：
```sql
alter table table_pre rename as table_now
```

## 2020-04-27
- 复制表数据
  - insert into table_copy select * from table_old; # 要求table_copy表必须存在
  - create table table_copy (select * from table_old); # mysql不支持select * into table_copy from table_old

## 2020-05-08
### 1
- url表
```sql
CREATE TABLE `url`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
);
insert into url values(null, 'http://www.mysql.com');
```
- 复制出十万左右数据
```sql
-- 指数级增长
INSERT INTO url select null, CONCAT(SUBSTR(url, 1, 11), substring(MD5(RAND()),1,10), '.com') from url
```

- 查找url
```sql
-- 该数据为数据库随机生成的某一条，查询时间300ms左右
select * from url where url = 'http://www.ba5843f71e.com';
```

- 间接的hash索引
```sql
-- 新增一列，值为url的hash值，并设置索引
alter table url add url_crc bigint(20);
update url set url_crc = CRC32(url);
alter table url add index index_url_crc(url_crc);
```

- 查询语句
```sql
-- 带上url_crc，查询时间30ms
select * from url where url = 'http://www.ba5843f71e.com' and url_crc = CRC32('http://www.ba5843f71e.com');
```

> 代价是每次插入需要同时插入hash值，可以程序插入
```sql
-- 触发器插入，不推荐，对于增删频繁的表，代价太大
CREATE TRIGGER hash_url_to_crc32_ins BEFORE INSERT ON url FOR EACH ROW
BEGIN	
	SET NEW.url_crc = CRC32( NEW.url );	
END;
```
### 2

- 列如果参与表达式、函数运算，就不会使用索引


## 2020-05-12
### 1
- 创建约束
```sql
ALTER TABLE <数据表名> ADD CONSTRAINT <唯一约束名> UNIQUE(<列名>);
```

- 删除唯一约束
```sql
ALTER TABLE <表名> DROP INDEX <唯一约束名>;
```
> mysql中，唯一约束通过唯一索引实现

### 2
- 列不重复率
```sql
select count(distinct user_id) / count(*) from tapd_ext_user_project;
```
> 在建立索引时，distinct值越大，该值越大，也就比其他列更适合做索引

## 2020-05-20

```sql
SELECT
	bug_id 
FROM
	tapd_bug_changes as tbc
WHERE 1 = 1 
	AND workspace_id in ( SELECT project_id FROM tapd_ext_user_project WHERE 1 = 1 AND user_id = @argument0 ) 
```

```sql
SELECT
	bug_id 
FROM
	tapd_bug_changes as tbc
WHERE 1 = 1 
	AND workspace_id IN ('61729320', '40947857', '37559760', '57970398', '36937028', '36530263', '66639406', '50116754', '54766630', '53963891', '32732890', '31052822', '58034050', '36248210', '54925659', '44113684', '40985714', '53362849', '48657023', '68328436', '55703675', '43123801', '57472799', '43098574', '35890954', '69933051')
```
- IN 内查询条件看起来一致，但第1句1.2s，第2句40ms，第1句没走索引，原因在于workspace_id编码与project_id编码不一致，一个utf8，一个utf8mb4


## 2020-05-22

MYSQL数据迁移

```bash
mysqldump -h源数据库IP -uroot -p123456 -P3306 --default-character-set=utf8  --databases 数据库名 | mysql -h目标数据库IP -uroot -p123456 -P3306
```

count中使用条件
```sql
select count(IF(phone != '', true, null)) from user;
```

## 2020-07-07
- 下面SQL语句会使左连接失效吗？
```sql
select * from a
left join b on a.id = b.id
where b.created > 'XXXX'
```
> 从结果看会，会使a,b之间变成inner join
如果想要左连接不失效，把条件放在on后面

- MySQL求差集，用left join，能求出**左表-右表**的差集

- count\sum区别
  - count(条件表达式)，只要条件不为null就+1
  - sum(条件表达式)，只有条件为true时+1

- group by主键之后，同一个元组仍是可select的(MySQL 5.7)
```sql
select * from a group by id
```

## 2020-08-10
MYSQL查看数据库最后修改时间
```sql
select TABLE_NAME, UPDATE_TIME from information_schema.TABLES WHERE table_schema = '数据库名';
```

## 2020-09-08
MySQL（5.7.25）不允许先select，再直接update的操作，例如
```sql
delete from `user` where id in (
	select id from `user`
)
```
需要改写，加一张中间表
```sql
delete from `user` where id in (
	select id from (select id from `user`) a
)
```

## 2020-09-22
```sql
-- 该日为周几，西方认为周日为一周的第一天
SELECT DAYOFWEEK('2020-01-05');
-- 该日为一年中的第几周，从0开始计数
SELECT WEEK('2020-01-05');
```
`DAYOFWEEK('2020-01-05')`查询结果为**1**  

`WEEK('2020-01-05')`查询结果也为**1**，原因在于默认以第一个周日为第一周的开始，之前的会返回0  
例如`WEEK('2020-01-01')`就会返回0  
如果查询的是`2017-01-01`，当日也是周日，则会返回1


想要以周一作为一周的开始，加参数
```sql
SELECT WEEK('2020-01-01', 1); -- 返回1
```

参考：https://blog.csdn.net/qq_21995733/article/details/78989074

## 2020-10-13
计算两天之间的工作日
1. 定义工作日表
```sql
CREATE TABLE `work_day`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `day` date NULL DEFAULT NULL,
  `status` int(10) NULL DEFAULT NULL COMMENT '工作日 - 0，法定节假日 - 1，休息日加班 - 2，休息日 - 3',
  `is_rest_day` tinyint(1) NULL DEFAULT 0 COMMENT '工作日 - 0，休息日 - 1',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 367 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
```

2. MySQL自定义函数
```sql
-- 定义函数
DELIMITER $
DROP FUNCTION IF EXISTS work_day_diff;
CREATE FUNCTION work_day_diff(s date, e date) RETURNS INT
BEGIN
DECLARE c INT DEFAULT 0;
	SELECT
		count( * ) INTO c 
	FROM
		work_day 
	WHERE
		`day` BETWEEN s 
		AND e 
		AND is_rest_day = 0;
return c;
END $
> date类型仅支持YYYY-MM-dd，若要支持时分秒，需使用datetime

-- 调用函数
select work_day_diff('2020-09-29', '2020-10-13');
```

## 2020-10-21
MySQL的字符串处理函数真的好弱  
现有一列字段需要处理，字段类型text，里面存储的是unicode16进制字符串  
如`\u4e50`表示字符`乐`  
本来直接使用百度来的函数  
```sql
-- unicode转字符串
DROP FUNCTION IF EXISTS unicode_decode;
delimiter ;;
CREATE FUNCTION unicode_decode(content text) RETURNS text
DETERMINISTIC

BEGIN
    DECLARE code1,code2  varchar(20);
    DECLARE n_index,s_index smallint unsigned default 0;
    DECLARE result,tmp_txt text;
    DECLARE temp varchar(1);
    SET s_index=LOCATE("\\u", content,1);
    set result = "";
    while s_index>0 DO 
         set code1 = conv(substring(content,s_index+2,2),16,10);
        set code2 = conv(substring(content,s_index+4,2),16,10);
        set temp = convert(char(code1,code2) USING 'ucs2');
        set tmp_txt = substring(content,n_index+1,s_index - (n_index+1));
        set result = concat(result,tmp_txt,temp);
        set n_index = s_index+5;
        set s_index = LOCATE("\\u", content, s_index+1);
    END while ; 
    set tmp_txt = substring(content,n_index+1);
    set result = concat(result,tmp_txt);
    RETURN result;
END
;;
delimiter ;
```

但是发布时一直失败，flyway那过不去，运维不给create function权限
没有办法，观察数据发现，人名最多三个汉字，一般在末尾加英文字符  
写成正则大概就是这样：```(\\u\w{0,4}){1,3}.+```
```sql
set @s = '\u4e50\u5927\u70ae_xyz';
-- set @s = '\u4e50592770ae';

select 
case when length(@s) - length(replace(@s, '\u', '')) = 0 
then @s
when length(@s) - length(replace(@s, '\u', '')) = 1
then INSERT(@s,LOCATE('\u',@s),LOCATE('\u',@s) + 4, convert(char(conv(SUBSTRING(@s, LOCATE('\u',@s) + 1, 4), 16, 10)) USING 'ucs2')) 
when length(@s) - length(replace(@s, '\u', '')) = 2
then 
	CONCAT(
		convert(char(conv(SUBSTRING(@s, LOCATE('\u',@s) + 1, 4), 16, 10)) USING 'ucs2'),
		convert(char(conv(SUBSTRING(SUBSTRING_INDEX(@s,'\u',-1),1, 4), 16, 10)) USING 'ucs2'),
		SUBSTRING(SUBSTRING_INDEX(@s,'\u',-1), 5)
	)
when length(@s) - length(replace(@s, '\u', '')) = 3
then 
	CONCAT(
		convert(char(conv(SUBSTRING(@s, LOCATE('\u',@s) + 1, 4), 16, 10)) USING 'ucs2'),
		convert(char(conv(SUBSTRING(REPLACE(SUBSTRING_INDEX(@s,'\u',-2), SUBSTRING_INDEX(@s,'\u',-1),''), 1, 4), 16, 10)) USING 'ucs2'),
		convert(char(conv(SUBSTRING(SUBSTRING_INDEX(@s,'\u',-1),1, 4), 16, 10)) USING 'ucs2'),
		SUBSTRING(SUBSTRING_INDEX(@s,'\u',-1), 5)
	)
end;
```
我佛了，编码不一致报错，最后不得不建张新表（原表数据是json，关键还是url编码，unicode编码后的数据），每次同步完数据，增量更新到新表。
写这个功能花了一天，就是不知道以后维护这个功能需要什么代价。


## 2020-10-27
今天往一个表里加数据，怎么也insert不进去，明明每次insert都是`> Affected rows: 1`  
用代码提交返回了id，但查不到数据  
一开始认为是jpa事务，数据还在缓存里，但是发现flush后还是不存在  
后来CSDN的一篇问答解决了，尝试截断(truncate)表后可以插入数据了

## 2020-10-28
完善了数据同步操作部分的代码，新增一个表记录同步开始、结束、进度、异常。  
在清洗数据时发现插入失败，基本猜得到是编码问题，因为源数据时utf8编码的，某个字段用的unicode编码后的字符串存储的  
报错信息：
```log
SQL Error: 1366, SQLState: HY000
Incorrect string value: '\xF0\x9F\x9A\x9B:&...' for column 'value_after' at row 3
```
定位到了这个字符：🚛
我本以为把value_after字段改成utf8mb4就可以了，没想到还是不行  
尝试过后发现，手动插入可以，但是jpa报错
需要配置Druid的连接：`spring.datasource.druid.connection-init-sqls: SET NAMES utf8mb4`


## 2020-12-17
mysql 使用IF语句时，IF(condition, value1, value2)  
当value1为date类型，value2是datetime类型时，即使结果为value2，value2也会被转为date类型  
但是，在我脱离复杂查询语句，单独使用时却发现结果准确(条件false时，右边表达式值仍为datetime)  
```sql
select IF(1=0, DATE('2020-10-21'), DATE_ADD('2020-12-25 17:41:22', INTERVAL 10 MINUTE))
```


# Oracle笔记
建表：JOBS
```sql
CREATE TABLE "HR"."JOBS" (
  "JOB_ID" VARCHAR2(10 BYTE) NOT NULL ,
  "JOB_TITLE" VARCHAR2(35 BYTE) NOT NULL ,
  "MIN_SALARY" NUMBER(6) ,
  "MAX_SALARY" NUMBER(6) 
)
INSERT INTO "HR"."JOBS" VALUES ('AD_PRES', 'President', '20080', '40000');
INSERT INTO "HR"."JOBS" VALUES ('AD_VP', 'Administration Vice President', '15000', '30000');
INSERT INTO "HR"."JOBS" VALUES ('AD_ASST', 'Administration Assistant', '3000', '6000');
INSERT INTO "HR"."JOBS" VALUES ('FI_MGR', 'Finance Manager', '8200', '16000');
INSERT INTO "HR"."JOBS" VALUES ('FI_ACCOUNT', 'Accountant', '4200', '9000');
INSERT INTO "HR"."JOBS" VALUES ('AC_MGR', 'Accounting Manager', '8200', '16000');
INSERT INTO "HR"."JOBS" VALUES ('AC_ACCOUNT', 'Public Accountant', '4200', '9000');
INSERT INTO "HR"."JOBS" VALUES ('SA_MAN', 'Sales Manager', '10000', '20080');
INSERT INTO "HR"."JOBS" VALUES ('SA_REP', 'Sales Representative', '6000', '12008');
INSERT INTO "HR"."JOBS" VALUES ('PU_MAN', 'Purchasing Manager', '8000', '15000');
INSERT INTO "HR"."JOBS" VALUES ('PU_CLERK', 'Purchasing Clerk', '2500', '5500');
INSERT INTO "HR"."JOBS" VALUES ('ST_MAN', 'Stock Manager', '5500', '8500');
INSERT INTO "HR"."JOBS" VALUES ('ST_CLERK', 'Stock Clerk', '2008', '5000');
INSERT INTO "HR"."JOBS" VALUES ('SH_CLERK', 'Shipping Clerk', '2500', '5500');
INSERT INTO "HR"."JOBS" VALUES ('IT_PROG', 'Programmer', '4000', '10000');
INSERT INTO "HR"."JOBS" VALUES ('MK_MAN', 'Marketing Manager', '9000', '15000');
INSERT INTO "HR"."JOBS" VALUES ('MK_REP', 'Marketing Representative', '4000', '9000');
INSERT INTO "HR"."JOBS" VALUES ('HR_REP', 'Human Resources Representative', '4000', '9000');
INSERT INTO "HR"."JOBS" VALUES ('PR_REP', 'Public Relations Representative', '4500', '10500');
```

- 最大薪水前5的职业
```sql
SELECT
	* 
FROM
	( SELECT * FROM jobs ORDER BY MAX_SALARY DESC ) 
WHERE
	ROWNUM <= 5
```

## 分页
- 不需要排序的分页
```sql
SELECT
	tt.* 
FROM
	( SELECT ROWNUM rn, t.* FROM jobs t WHERE ROWNUM <= 15 ) tt 
WHERE
	tt.rn >= 10
```

- 需要排序的分页（先排序，再分页）
```sql
SELECT
	tt.* 
FROM
	( SELECT ROWNUM rn, t.* FROM ( SELECT * FROM jobs ORDER BY MAX_SALARY DESC ) t WHERE ROWNUM <= 15 ) tt 
WHERE
	tt.rn >= 10
```