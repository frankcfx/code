#code source

Spark SQL笔记整理
http://blog.51cto.com/xpleaf/2113927

Hive数据仓库-Sqoop将数据从Mysql导入Hive中
https://blog.csdn.net/wangyang1354/article/details/52936400
query参数就可以让用户随意写sql语句来查询了。query和table参数是互斥的。
sqoop import --connect jdbc:mysql://localhost:3306/test --username root --password 123456 --delete-target-dir --target-dir person-mysql -m 1 --query "select * from person where name='003' and gender=0 and \$CONDITIONS"

https://www.jianshu.com/p/a19486f5a0ea

https://blog.csdn.net/YSC1123/article/details/78905073

scala kafka consumer
https://blog.csdn.net/u012965373/article/details/74548388
https://www.cnblogs.com/AK47Sonic/p/7260577.html
https://hk.saowen.com/a/63ba0bf52b73651f3d133e82829e4e817c03a9a0a24af42a3a5e74e3209483bf


https://blog.csdn.net/zhuiqiuuuu/article/details/72822570
https://www.tqcto.com/article/db/244077.html
http://www.aboutyun.com/forum-146-1.html