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

> 表tapd_bug_changes数据70w+，tapd_ext_user_project小表（20+），tapd_projects大表（1600+），1语句10s，2语句4s，3语句1s，4语句7s


## 2020-04-26
- 修改表名：
```sql
alter table table_pre rename as table_now
```


- 第一个sql比第二个快？

```sql
select a.tapd_username as `name`, b.department_id from tapd_ext_username_to_uid as a, ldap_users as b
where a.ldap_user_uid = b.uid
```

```sql
select a.tapd_username as `name`, b.department_id from tapd_ext_username_to_uid as a
left join ldap_users as b on a.ldap_user_uid = b.uid
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


### 2020-05-22

MYSQL数据迁移

```bash
mysqldump -h源数据库IP -uroot -p123456 -P3306 --default-character-set=utf8  --databases 数据库名 | mysql -h目标数据库IP -uroot -p123456 -P3306
```

count中使用条件
```sql
select count(IF(phone != '', true, null)) from user;
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