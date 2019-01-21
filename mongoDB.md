# 基础部分

## 查看数据库

一个mongodb中可以建立多个数据库。

MongoDB的默认数据库为"db"，该数据库存储在data目录中。

MongoDB的单个实例可以容纳多个独立的数据库，每一个都有自己的集合和权限，不同的数据库也放置在不同的文件中。

**"show dbs"** 命令可以显示所有数据的列表。

执行 **"db"** 命令可以显示当前数据库对象或集合。

运行"use"命令，可以连接到（选择）一个指定的数据库。

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 创建数据库

MongoDB 创建数据库的语法格式如下：

use DATABASE_NAME

如果数据库不存在，则创建数据库，否则切换到指定数据库。

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 删除数据库

MongoDB 删除数据库的语法格式如下：

```javascript
db.dropDatabase()
```

先`use databaseName`进入到库，然后`db.dropDatabase()`删除当前库

### 删除集合

集合删除语法格式如下：

```javascript
db.collection.drop()
```

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 创建集合

MongoDB 中使用 **createCollection()** 方法来创建集合。

语法格式：

```javascript
db.createCollection(name, options)
```

参数说明：

- name: 要创建的集合名称
- options: 可选参数, 指定有关内存大小及索引的选项

options 可以是如下参数：

| 字段        | 类型 | 描述                                                         |
| ----------- | ---- | ------------------------------------------------------------ |
| capped      | 布尔 | （可选）如果为 true，则创建固定集合。固定集合是指有着固定大小的集合，当达到最大值时，它会自动覆盖最早的文档。 **当该值为 true 时，必须指定 size 参数。** |
| autoIndexId | 布尔 | （可选）如为 true，自动在 _id 字段创建索引。默认为 false。   |
| size        | 数值 | （可选）为固定集合指定一个最大值（以字节计）。 **如果 capped 为 true，也需要指定该字段。** |
| max         | 数值 | （可选）指定固定集合中包含文档的最大数量。                   |

在插入文档时，MongoDB 首先检查固定集合的 size 字段，然后检查 max 字段。 

在 MongoDB 中，你不需要创建集合。当你插入一些文档时，MongoDB 会自动创建集合 ，如下会自动创建mycol集合

```javascript
db.mycol.insert({"name" : "nothing"})
```

### 查看集合

查看已有集合，可以使用 show collections 命令 

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 插入文档

文档的数据结构和JSON基本一样。
所有存储在集合中的数据都是BSON格式。
BSON是一种类json的一种二进制形式的存储格式,简称Binary JSON。
使用 insert() 或 save() 方法向集合中插入文档，语法如下：

```
db.COLLECTION_NAME.insert(document)
```

如果要插入的集合不在该数据库中， MongoDB 会自动创建该集合并插入文档 

mongoDB的objectId构成，由12个数构成的bufferArray经过位运算生成的24位字符串

```java
buffer.put(int3(this.timestamp));
buffer.put(int2(this.timestamp));
buffer.put(int1(this.timestamp));
buffer.put(int0(this.timestamp));
buffer.put(int2(this.machineIdentifier));
buffer.put(int1(this.machineIdentifier));
buffer.put(int0(this.machineIdentifier));
buffer.put(short1(this.processIdentifier));
buffer.put(short0(this.processIdentifier));
buffer.put(int2(this.counter));
buffer.put(int1(this.counter));
buffer.put(int0(this.counter));
```

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 修改文档

### update() 方法

update() 方法用于更新已存在的文档。语法格式如下：

db.collection.update(
   <query>,
   <update>,
   {
     upsert: <boolean>,
     multi: <boolean>,
     writeConcern: <document>
   }
)

参数说明：

    query : update的查询条件，类似sql update查询内where后面的。
    update : update的对象和一些更新的操作符（如$,$inc...）等，也可以理解为sql update查询内set后面的
    upsert : 可选，这个参数的意思是，如果不存在update的记录，是否插入objNew,true为插入，默认是false，不插入。
    multi : 可选，mongodb 默认是false,只更新找到的第一条记录，如果这个参数为true,就把按条件查出来多条记录全部更新。
    writeConcern :可选，抛出异常的级别。
### save()方法

save() 方法通过传入的文档来替换已有文档。语法格式如下：

```javascript
db.collection.save(
   <document>,
   {
     writeConcern: <document>
   }
)
```

**参数说明：**

- **document** : 文档数据。
- **writeConcern**  :可选，抛出异常的级别。

在3.2版本开始，MongoDB提供以下更新集合文档的方法：

- db.collection.updateOne() 向指定集合更新单个文档

- db.collection.updateMany() 向指定集合更新多个文档

  首先我们在test集合里插入测试数据

  ```javascript
  use test
  db.test_collection.insert( [
  {"name":"abc","age":"25","status":"zxc"},
  {"name":"dec","age":"19","status":"qwe"},
  {"name":"asd","age":"30","status":"nmn"},
  ] )
  ```

  更新单个文档

  ```javascript
  > db.test_collection.updateOne({"name":"abc"},{$set:{"age":"28"}})
  { "acknowledged" : true, "matchedCount" : 1, "modifiedCount" : 1 }
  > db.test_collection.find()
  { "_id" : ObjectId("59c8ba673b92ae498a5716af"), "name" : "abc", "age" : "28", "status" : "zxc" }
  { "_id" : ObjectId("59c8ba673b92ae498a5716b0"), "name" : "dec", "age" : "19", "status" : "qwe" }
  { "_id" : ObjectId("59c8ba673b92ae498a5716b1"), "name" : "asd", "age" : "30", "status" : "nmn" }
  >
  ```

  更新多个文档

  ```javascript
  > db.test_collection.updateMany({"age":{$gt:"10"}},{$set:{"status":"xyz"}})
  { "acknowledged" : true, "matchedCount" : 3, "modifiedCount" : 3 }
  > db.test_collection.find()
  { "_id" : ObjectId("59c8ba673b92ae498a5716af"), "name" : "abc", "age" : "28", "status" : "xyz" }
  { "_id" : ObjectId("59c8ba673b92ae498a5716b0"), "name" : "dec", "age" : "19", "status" : "xyz" }
  { "_id" : ObjectId("59c8ba673b92ae498a5716b1"), "name" : "asd", "age" : "30", "status" : "xyz" }
  >
  ```

  WriteConcern.NONE:没有异常抛出

  WriteConcern.NORMAL:仅抛出网络错误异常，没有服务器错误异常

  WriteConcern.SAFE:抛出网络错误异常、服务器错误异常；并等待服务器完成写操作。

  WriteConcern.MAJORITY: 抛出网络错误异常、服务器错误异常；并等待一个主服务器完成写操作。

  WriteConcern.FSYNC_SAFE: 抛出网络错误异常、服务器错误异常；写操作等待服务器将数据刷新到磁盘。

  WriteConcern.JOURNAL_SAFE:抛出网络错误异常、服务器错误异常；写操作等待服务器提交到磁盘的日志文件。

  WriteConcern.REPLICAS_SAFE:抛出网络错误异常、服务器错误异常；等待至少2台服务器完成写操作。


🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 删除文档

remove() 方法已经过时了，现在官方推荐使用 deleteOne() 和 deleteMany() 方法。

如删除集合下全部文档：

```
db.inventory.deleteMany({})
```

删除 status 等于 A 的全部文档：

```
db.inventory.deleteMany({ status : "A" })
```

删除 status 等于 D 的一个文档：

```
db.inventory.deleteOne( { status: "D" } )
```

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

```javascript
project 	英 [ˈprɒdʒekt] 美 [ˈprɑ:dʒekt]
vt. 	计划; 放映; 发射; 展现，使突出;
vi. 	伸出，突出;
n. 	项目，工程; 计划，规划; （学生的） 课题;
[例句]Money will also go into local development projects in Vietnam
钱也会用于越南的地方发展项目当中。
[其他] 	第三人称单数：projects 复数：projects 现在分词：projecting 过去式：projected 过去分词：projected 
```

```javascript
projection 	英 [prəˈdʒekʃn] 美 [prəˈdʒɛkʃən]
n. 	预测; 规划，设计; [心] 投射; 突起物;
[例句]They took me into a projection room to see a picture.
他们把我带到放映室去看一张图。
[其他] 	
```

## 查询文档

MongoDB 查询文档使用 find() 方法。

find() 方法以非结构化的方式来显示所有文档。

MongoDB 查询数据的语法格式如下：

```javascript
db.collection.find(query, projection)
```

-  **query** ：可选，使用查询操作符指定查询条件
-  **projection** ：可选，使用投影操作符指定返回的键。查询时返回文档中所有键值， 只需省略该参数即可（默认省略）。

如果你需要以易读的方式来读取数据，可以使用 pretty() 方法，语法格式如下：

```javascript
>db.col.find().pretty()
```

pretty() 方法以格式化的方式来显示所有文档。
除了 find() 方法之外，还有一个 findOne() 方法，它只返回一个文档 。

MongoDB 与 RDBMS Where 语句比较
如果你熟悉常规的 SQL 数据，通过下表可以更好的理解 MongoDB 的条件语句查询：

| 操作       | 格式                     | 范例                                        | RDBMS中的类似语句       |
| ---------- | ------------------------ | ------------------------------------------- | ----------------------- |
| 等于       | `{<key>:<value>`}        | `db.col.find({"by":"菜鸟教程"}).pretty()`   | `where by = '菜鸟教程'` |
| 小于       | `{<key>:{$lt:<value>}}`  | `db.col.find({"likes":{$lt:50}}).pretty()`  | `where likes < 50`      |
| 小于或等于 | `{<key>:{$lte:<value>}}` | `db.col.find({"likes":{$lte:50}}).pretty()` | `where likes <= 50`     |
| 大于       | `{<key>:{$gt:<value>}}`  | `db.col.find({"likes":{$gt:50}}).pretty()`  | `where likes > 50`      |
| 大于或等于 | `{<key>:{$gte:<value>}}` | `db.col.find({"likes":{$gte:50}}).pretty()` | `where likes >= 50`     |
| 不等于     | `{<key>:{$ne:<value>}}`  | `db.col.find({"likes":{$ne:50}}).pretty()`  | `where likes != 50`     |

### AND条件

MongoDB 的 find() 方法可以传入多个键(key)，每个键(key)以逗号隔开，即常规 SQL  的 AND 条件。

语法格式如下：

```javascript
>db.col.find({key1:value1, key2:value2})
```

### OR 条件

MongoDB OR 条件语句使用了关键字 **$or**,语法格式如下：

```javascript
>db.col.find(
   {
      $or: [
         {key1: value1}, {key2:value2}
      ]
   }
)
```

### AND 和 OR 联合使用

以下实例演示了 AND 和 OR 联合使用，类似常规 SQL 语句为： **'where likes>50 AND (by = '菜鸟教程' OR title = 'MongoDB 教程')'**

```javascript
>db.col.find({"likes": {$gt:50}, $or: [{"by": "菜鸟教程"},{"title": "MongoDB 教程"}]})
```

### projection 参数说明

```javascript
补充一下 projection 参数的使用方法
db.collection.find(query, projection)

若不指定 projection，则默认返回所有键，指定 projection 格式如下，有两种模式
db.collection.find(query, {title: 1, by: 1}) // inclusion模式 指定返回的键，不返回其他键
db.collection.find(query, {title: 0, by: 0}) // exclusion模式 指定不返回的键,返回其他键

_id 键默认返回，需要主动指定 _id:0 才会隐藏
两种模式不可混用（因为这样的话无法推断其他键是否应返回）

db.collection.find(query, {title: 1, by: 0}) // 错误
只能全1或全0，除了在inclusion模式时可以指定_id为0
db.collection.find(query, {_id:0, title: 1, by: 1}) // 正确

若不想指定查询条件参数 query 可以 用 {} 代替，但是需要指定 projection 参数：
querydb.collection.find({}, {title: 1})
```

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 查询条件

获取 "col" 集合中 "likes" 大于 100 的数据，你可以使用以下命令：

```javascript
db.col.find({"likes" : {$gt : 100}})
```

类似于SQL语句：

```javascript
select * from col where likes > 100;
```

### 对比查询

| 条件符 | 功能   | 示例                                                         | 说明                              |
| ------ | ------ | ------------------------------------------------------------ | --------------------------------- |
| $gt    | >      | db.persion.find({age: {$gt: 28}}, {_id:0,name: 1})           | 查询age大于28的记录只返回name     |
| $gte   | >=     | db.persion.find({age: {$gte: 28}}, {_id:0,name: 1})          | 查询age大于等于28的记录只返回name |
| $lt    | <      | db.persion.find({age: {$lt: 28}}, {_id:0,name: 1})           | 查询age小于28的记录               |
| $lte   | <=     | db.persion.find({age: {$lte: 28}}, {_id:0,name: 1})          | 查询age小于等于28的记录           |
| $ne    | !=     | db.persion.find({country: {$ne: 'USA'}}, {_id:0,name: 1})    | 查询country不等于USA的记录        |
| $eq    | =      | db.persion.find({country: {$eq: 'USA'}}, {_id:0,name: 1})    | 查询country等于USA的记录          |
| $in    | in     | db.persion.find({country: {$in: ['USA','China']}}, {_id:0,name: 1}) | 查询country包含USA或China的记录   |
| $nin   | not in | db.persion.find({country: {$nin: ['USA','China']}}, {_id:0,name: 1}) | 查询country不包含USA或China的记录 |

### 逻辑查询

| 条件符 | 功能   | 示例                                                         | 说明                                                         |
| ------ | ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| $or    | or     | db.persion.find({$or: [{age: {$gt: 39}}, {age: {$lt: 28}}]}, {_id:0,name: 1}) | 查询age大于39或者age小于28的记录                             |
| $nor   | not or | db.persion.find({$nor: [{age: {$gt: 39}}, {age: {$lt: 28}}]}, {_id:0,name: 1}) | 查询age小于等于39且age大于等于28的记录                       |
| $and   | and    | db.persion.find({$and: [{age: {$lt: 39}}, {age: {$gt: 28}}]}, {_id:0,name: 1}) | 查询age大于28且age小于39的记录等价于db.persion.find({age: {$gt: 28, $lt: 39}}, { _id:0,name: 1}) |
| $not   | not    | db.persion.find({age: {$not: {$gt: 28}}}, { _id:0,name: 1})  | 查询age不大于28的记录                                        |

### 数组查询

插入测试数据

```javascript
> db.persion.insert({book: ['JS','PHP','JAVA']})
> db.persion.insert({book: ['JS','PHP','JAVA','NODEJS']})
```

| 条件符     | 功能           | 示例                                                         | 说明                                     |
| ---------- | -------------- | ------------------------------------------------------------ | ---------------------------------------- |
| $all       | 查询数组包含的 | db.persion.find({book:{$all: ['NODEJS']}})                   | 查询所有集合中book数组里包含NODEJS的结果 |
| $size      | 查询数组长度   | db.persion.find({book:{$size: 3}})                           | 查询所有集合中book数组长度为3的结果      |
| $elemMatch | 组合查询       | db.persion.find({book: {$elemMatch: {$in: ['PHP','NODEJS']}}}) | 查找book中包含PHP或者NODEJS的结果        |

### 分页与排序

**limit 返回指定数据条数**

```javascript
> db.persion.find({$or: [{country: 'USA'},{country: 'China'}]}).limit(5)
```

**skip返回指定跨度的数据**  
 跨越数据量大的时候会有性能问题

```javascript
> db.persion.find({$or: [{country: 'USA'},{country: 'China'}]}).limit(5).skip(10)
```

**sort 返回按照key排序的数据[1,-1]**

```javascript
> db.persion.find({$or: [{country: 'USA'},{country: 'China'}]}).limit(5).skip(10).sort({age: -1})
```

### 游标

利用游标遍历数据

```javascript
var persions = db.persion.find();
while(persions.hasNext()) {
    obj = persions.next();
    print(obj.name)
}
```

### 查询快照

```javascript
> db.persion.find({$query: {name: 'tom_1'}, $snapshot: true})
```

高级查询选项

- $query
- $orderby
- $maxsan: integer最多扫描文档数
- $min: doc查询开始
- $max: doc查询结束
- $hint: doc使用哪个索引
- $explain: boolean 统计
- $snapshot: boolean 一致快照

[obj1, obj2, obj3, obj4]
 游标读取时obj1->obj2 当读取到obj2时对obj2进行操作导致obj占用空间变大mongodb会将它放到最后此时排列为： [obj1, obj3, obj4, obj2]
 游标接下去读取将会读到obj4此时obj3就漏读了 使用快照可以避免这个问题

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## $type 操作符

$type操作符是基于BSON类型来检索集合中匹配的数据类型，并返回结果。

MongoDB 中可以使用的类型如下表所示：

| **类型**                | **数字** | **备注**         |
| ----------------------- | -------- | ---------------- |
| Double                  | 1        |                  |
| String                  | 2        |                  |
| Object                  | 3        |                  |
| Array                   | 4        |                  |
| Binary data             | 5        |                  |
| Undefined               | 6        | 已废弃。         |
| Object id               | 7        |                  |
| Boolean                 | 8        |                  |
| Date                    | 9        |                  |
| Null                    | 10       |                  |
| Regular Expression      | 11       |                  |
| JavaScript              | 13       |                  |
| Symbol                  | 14       |                  |
| JavaScript (with scope) | 15       |                  |
| 32-bit integer          | 16       |                  |
| Timestamp               | 17       |                  |
| 64-bit integer          | 18       |                  |
| Min key                 | 255      | Query with `-1`. |
| Max key                 | 127      |                  |

如果想获取 "col" 集合中 title 为 String （数字2）的数据，可以使用以下命令：

```javascript
db.col.find({"title" : {$type : 2}})
```

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## Limit、Skip、Sort方法

### Limit() 方法

如果需要在MongoDB中读取指定数量的数据记录，可以使用MongoDB的Limit方法，limit()方法接受一个数字参数，该参数指定从MongoDB中`从前往后`读取的记录条数。
limit()方法基本语法如下所示：

```javascript
>db.COLLECTION_NAME.find().limit(NUMBER)
```

注：如果你们没有指定limit()方法中的`NUMBER`参数，则显示集合中的所有数据。 

### Skip() 方法

除了可以使用limit()方法来读取指定数量的数据外，还可以使用skip()方法来跳过指定数量的数据，skip方法同样接受一个数字参数作为跳过的记录条数。
skip() 方法脚本语法格式如下：

```javascript
>db.COLLECTION_NAME.find().limit(NUMBER).skip(NUMBER)
```

以下例子只会显示第二条文档数据

```javascript
>db.col.find({},{"title":1,_id:0}).limit(1).skip(1)
{ "title" : "Java 教程" }
>
```

注：skip()方法默认参数为 0 。

### sort() 方法

在 MongoDB 中使用 sort() 方法对数据进行排序，sort() 方法可以通过参数指定排序的字段，并使用 1 和 -1 来指定排序的方式，其中 1 为升序排列，而 -1 是用于降序排列。
sort()方法基本语法如下所示：

```javascript
>db.COLLECTION_NAME.find().sort({KEY:1})
```

limilt(),skip(),sort()三个放在一起执行的时候，执行的顺序是先 sort(), 然后是 skip()，最后是显示的 limit()。 

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

## 索引

MongoDB使用 createIndex() 方法来创建索引。 
createIndex()方法基本语法格式如下所示：

```javascript
>db.collection.createIndex(keys, options)
```

### keys

键值为你要创建的索引字段，1 为指定按升序创建索引，如果你想按降序来创建索引指定为 -1 即可。

```javascript
>db.col.createIndex({"title":1})
>
```

createIndex() 方法中你也可以设置使用多个字段创建索引（关系型数据库中称作复合索引）。

```javascript
>db.col.createIndex({"title":1,"description":-1})
>
```

### options

可选参数，可选参数列表如下：

| Parameter          | Type          | Description                                                  |
| ------------------ | ------------- | ------------------------------------------------------------ |
| background         | Boolean       | 建索引过程会阻塞其它数据库操作，background可指定以后台方式创建索引，即增加 "background"     可选参数。  "background" 默认值为**false**。 |
| unique             | Boolean       | 建立的索引是否唯一。指定为true创建唯一索引。默认值为**false**. |
| name               | string        | 索引的名称。如果未指定，MongoDB的通过连接索引的字段名和排序顺序生成一个索引名称。 |
| dropDups           | Boolean       | 在建立唯一索引时是否删除重复记录,指定 true 创建唯一索引。默认值为 **false**. |
| sparse             | Boolean       | 对文档中不存在的字段数据不启用索引；这个参数需要特别注意，如果设置为true的话，在索引字段中不会查询出不包含对应字段的文档.。默认值为 **false**. |
| expireAfterSeconds | integer       | 指定一个以秒为单位的数值，完成 TTL设定，设定集合的生存时间。 |
| v                  | index version | 索引的版本号。默认的索引版本取决于mongod创建索引时运行的版本。 |
| weights            | document      | 索引权重值，数值在 1 到 99,999 之间，表示该索引相对于其他索引字段的得分权重。 |
| default_language   | string        | 对于文本索引，该参数决定了停用词及词干和词器的规则的列表。 默认为英语 |
| language_override  | string        | 对于文本索引，该参数指定了包含在文档中的字段名，语言覆盖默认的language，默认值为 language. |

在后台创建索引：

```javascript
db.values.createIndex({open: 1, close: 1}, {background: true})
```

通过在创建索引时加 background:true 的选项，让创建工作在后台执行

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

```javascript
aggregate 	英 [ˈægrɪgət] 美 [ˈæɡrɪɡɪt]
n. 	骨料; 合计; 聚集体; 集料（可成混凝土或修路等用的）;
adj. 	总数的，总计的; 聚合的; [地] 聚成岩的;
vt. 	使聚集，使积聚; 总计达;
[例句]The rate of growth of GNP will depend upon the rate of growth of aggregate demand
国民生产总值的增长率将取决于总需求的增长率。
[其他] 	第三人称单数：aggregates 复数：aggregates 现在分词：aggregating 过去式：aggregated 过去分词：aggregated 
```

## 聚合

MongoDB的一个很大的好处是能够使用MapReduce来吧数据库查询的结果简化成一个与原来的集合完全不同的结构。MapReduce把一个数据库查询的值映射为一个完全不同的形式，然后简化结果，使它们的可用性更好。
	MongoDB有一个MapReduce框架，它也允许你使用聚合来简化吧一个MapReduce操作传输到另一个MapReduce操作的一系列过程。有了MapReduce和聚合，可以用数据生成一些不平凡的业绩。聚合的概念是指，在把MongoDB服务器上的文档汇编为一个结果集时，对它们执行一些列的操作。这比在Node.js应用程序中检索它们和处理它们更高效，因为MongoDB的服务器可以在本地操作数据块。

**aggregate()方法**

Collection对象提供了aggregate()方法来对数据进行聚合操作。aggregate()方法的语法如下

```javascript
>db.COLLECTION_NAME.aggregate(operators,[options],callback)
```

operators参数是聚合运算符的数组，它允许你定义对数据执行什么汇总操作，每一个运算符操作的输出将作为下一个运算符操作的输入（管道）。options参数允许你设置readPreference属性，它定义了从哪里读取数据。callback参数是接受err和res。

**operators中一个operator的构成：聚合运算符对应一个聚合表达式，聚合表达式中有表达式运算符**

```
operators : [{$operator : {_id : "$field", alias : {$expression : value}}}]
```

operators：操作符数组
operator：单个操作符
_id：累加器对象，键名必须为 ` _id`
field：集合中的属性
alias：别名，投影projection
expression：表达式运算符
value：表达式运算符的值

**可以在aggregate()方法上使用的聚合运算符** 

| 运算符   | 说明                                                         |
| -------- | ------------------------------------------------------------ |
| $project | 通过重命名，添加或删除字段重塑文档。你也可以重新计算值，并添加子文档。例如，下面的例子包括title并排除name：{$project:{title:1,name:0}}以下是把name重命名为title的例子:{\$project{title:"$name"}}下面是添加一个新的total字段，并用price和tax字段计算它的值的例子:{\$project{total:{$add:["$price","$tax"]}}} |
| $match   | 通过使用query对象运算符来过滤文档集。                        |
| $limit   | 限定可以传递到聚合操作的下一个管道中的文档数量。例如{$limit:5} |
| $skip    | 指定处理聚合操作的下一个管道前跳过的一些文档                 |
| $unwind  | 指定一个数组字段用于分割，对每个值创建一个单独的文档。例如{$unwind:"$myArr"} |
| $group   | 把文档分成一组新的文档用于在管道中的下一级。新对象的字段必须在$group对象中定义。你还可以把表2中列出的分组表达式运算符应用到该组的多个文档中。例如，使用下面的语句汇总value字段：{$group:{set_id:"$0_id",total:{$sum:"$value"}}} |
| $sort    | 在把文档传递给处理聚合操作的下一个管道前对它们排序。排序指定一个带有field:<sort_order>属性的对象，其中<sort_order>为1表示升序，而-1表示降序 |

例子

```javascript
> db.mycol.aggregate([{$group : {_id : "$by_user", num_tutorial : {$sum : 1}}}])
```

**分组函数中必须含有累加器对象标识`_id`，这个标识对应的value值（这里是by_user的值）代表了分组的依据**
例子，$by_user引用了集合中的by_user属性，即根据by_user属性的值进行分组，num_tutorial是投影，即别名，组中每一项的权值为1，进行sum操作

```javascript
> db.mycol.aggregate([{$group : {_id : "$by_user", num_tutorial : {$sum : 1}}}])
```

类似sql语句：

```javascript
 select by_user, count(*) num_tutorial from mycol group by by_user
```

**实现聚合表达式的运算符**

    当你实现聚合运算符时，你建立将传递到聚合操作流水线的下一级的新文档。MongoDB的聚合框架提供了许多表达式运算符，它们有助于对新字段计算值或对文档中的现有字段进行比较。
    
    当在$group聚合管道上操作时，多个文档与创建的新文档中定义的字段匹配。MongoDB提供了一组你可以应用到这些文档的运算符，并用它在原来文档集的字段值的基础上计算新组文档中的字段值。下表列出了$group表达式运算符。

聚合$group表达式运算符

运算符		说明
$addToSet	返回一组文档中所有文档所选字段的全部唯一值的数组。例如:colors:{$addToSet:"color"}
$first		返回一组文档中一个字段的第一个值。例如：firstValue:{$first:"$value"}
$last		返回一组文档中一个字段的最后一个值。例如:lastValue:{$last:"$value"}
$max		返回一组文档中一个字段的最大值。例如:maxValue:{$max:"$value"}
$min		返回一组文档中一个字段的最小值。例如:minValue:{$min:"$value"}
$avg		返回一组文档中以个字段的平均值。例如:avgValue:{$avg:"$value"}
$push		返回一组文档中所有文档所选字段的全部值的数组。例如:username:{$push:"$username"}
$sum		返回一组文档中以个字段的全部值的总和。例如:total:{$sum:"$value"}

    此外，计算新的字段值时，可以应用一些字符串和算术运算符。下表列出了在聚合运算符中计算新字段值可以应用的最常用的一些运算符。

可用在聚合表达式的字符串和算术运算符

运算符		说明
$add		计算数值的总和。例如：valuePlus5:{$add:["$value",5]}
$divide		给定两个数值，用第一个数除以第二个数。例如：valueDividedBy5:{$divide:["$value",5]}
$mod		取模。例如:{$mod:["$value",5]}
$multiply		计算数值数组的乘积。例如:{$multiply:["$value",5]}
$subtract		给定两个数值，用第一个数减去第二个数。例如:{$subtract:["$value",5]}
$concat		连接两个字符串 例如：{$concat:["str1","str2"]}
$strcasecmp	比较两个字符串并返回一个整数来反应比较结果。例如 {$strcasecmp:["$value","$value"]}
$substr		返回字符串的一部分。例如:hasTest：{$substr:["$value","test"]}
$toLower		将字符串转化为小写。
$toUpper		将字符串转化为大写

🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊🖊😂😎

# 高级部分

## MapReduce

![map reduce](.\pic\map-reduce.bakedsvg.svg)mongoDB支持简单的mapReduce`（不需要解析文档生成key-value，不需要map()处理key-value生成多key一value的集合，不需要分区）`，因为输入的key-value对直接来源于document，实质上没有进行map操作，仅仅由emit()函数简单的对key-value进行combine操作，然后将combine后的key-values交给reduce函数。

```java
除了emit函数之外，所有都是标准的js语法,这个emit函数是非常重要的，可以这样理解，当所有需要计算的文档（因为在mapReduce时，可以对文档进行过滤，接下来会讲到）执行完了map函数，map函数会返回key_values对，key即是emit中的第一个参数key，values是对应同一key的emit的n个第二个参数组成的数组。这个key_values会作为参数传递给reduce，分别作为第1.2个参数。

reduce函数的任务就是将key-values变成key-value，也就是把values数组变成一个单一的值value。当key-values中的values数组过大时，会被再切分成很多个小的key-values块，然后分别执行Reduce函数，再将多个块的结果组合成一个新的数组，作为Reduce函数的第二个参数，继续Reducer操作。可以预见，如果我们初始的values非常大，可能还会对第一次分块计算后组成的集合再次Reduce。这就类似于多阶的归并排序了。具体会有多少重，就看数据量了。

reduce一定要能被反复调用，不论是映射环节还是前一个简化环节。所以reduce返回的文档必须能作为reduce的第二个参数(values数组)的一个元素(value元素)。

（当书写Map函数时，emit的第二个参数组成数组成了reduce函数的第二个参数，而Reduce函数的返回值，跟emit函数的第二个参数形式要一致，多个reduce函数的返回值可能会组成数组作为新的第二个输入参数再次执行Reduce操作。）
```

---

## 数据库引用

## 使用 DBRefs

DBRef的形式：

```java
{ $ref : collection, $id : oid, $db : database }
```

三个字段表示的意义为：

- $ref：集合名称
- $id：引用的id
- $db:数据库名称，可选参数
-  

以下实例中用户数据文档使用了 DBRef, 字段 address：

```java
{
   "_id":ObjectId("53402597d852426020000002"),
   "address": {
   "$ref": "address_home",
   "$id": ObjectId("534009e4d852427820000002"),
   "$db": "runoob"},
   "contact": "987654321",
   "dob": "01-01-1991",
   "name": "Tom Benzamin"
}
```

**address** DBRef 字段指定了引用的地址文档是在 runoob 数据库下的 address_home 集合，id 为 534009e4d852427820000002。

以下代码中，我们通过指定  $ref 参数（address_home 集合）来查找集合中指定id的用户地址信息：

```java
>var user = db.users.findOne({"name":"Tom Benzamin"})
>var dbRef = user.address
>db[dbRef.$ref].findOne({"_id":(dbRef.$id)})	//db[address_home].findOne({"_id":ObjectId("534009e4d852427820000002")})
```

以上实例返回了 address_home 集合中的地址数据：

```java
{
   "_id" : ObjectId("534009e4d852427820000002"),
   "building" : "22 A, Indiana Apt",
   "pincode" : 123456,
   "city" : "Los Angeles",
   "state" : "California"
}
```

```by Ftibw
The mapping framework does not handle cascading saves. If you change an Account object that is referenced by a Person object, you must save the Account object separately. Calling save on the Person object will not automatically save the Account objects in the property accounts.
引用关系的集合不会被级联操作，引用的集合不存在但是引用仍可以显示(类似主表不存在但是子表外键存在的bug).
In short,the best time to use DBRefs are when you’re storing heterogeneous references to documents in different collections.like when you want to take advantage of some additional DBRef-specific 
functionality in a driver or too。
只有当集合深度过深，结构复杂的时候才使用DBRefs，把过深的数据拆分成引用的集合。
```



# 数据库备份

tips：单-参数和参数值之间的空格可用省略，双--参数则不行

### 1.导出数据：

#### 1.MongoDB启动时

```
<将IP为192.168.1.132的服务器上mongodb中名为dmbd4的数据库以json格式导出到/opt/dump.mdb目录>
mongodump -h 192.168.1.132:27017 -d dmbd4 -o /opt/dump.mdb
```

则会在/opt中创建dump.mdb/dmbd4目录，其中存放了dmbd4库中每个集合的数据

#### 2.MongoDB或启动或关闭时

```
<将IP为192.168.1.132的服务器上mongodb中名为dmbd4的数据库中表terminalInfo导出为/opt/dump.mdb/dmbd4/terminalInfo.csv文件>
mongoexport -h192.168.1.132：27017 -ddmbd4 -cterminalInfo --type=csv -f_id,token,name -o /opt/dump.mdb/dmbd4/terminalInfo.csv
若不加--type=csv或者--type=json时，必须导出为json文件，导出json文件可用不用-f参数指定字段。带参数--type=csv时，必须用-f参数指定导出字段。
```

### 2.导入数据：

#### 1.MongoDB启动时

```
<将/opt/dump.mdb/dmbd4/terminalInfo.csv文件的数据导入到dmbd4库的terminalInfo表中>
mongoimport -h192.168.1.132：27017 -ddmbd4 -cterminalInfo--type=csv --file /opt/dump.mdb/dmbd4/terminalInfo.csv
```

#### 2.MongoDB或启动或关闭时

```
<将/opt/dump.mdb/dmbd4/目录中的数据库集合json数据，导入到IP为192.168.1.175的服务器上mongodb中，导入名为dmbd4的数据库数据（数据库不存在时会自动创建）>
mongorestore -h 192.168.1.175:27017 -d dmbd4 /opt/dump.mdb/dmbd4/
```

