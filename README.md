# BigdataProject
记录大数据学习阶段学习过程
环境：虚拟机ubuntu18.04;hadoop-2.7.1;spark-2.1.0;scala-2.11.8;MySQL;IDEA-2017.3.5
将一个地震数据集(csv文件),用spark-core与spark-sql清洗后传入MySQL数据库中,学习基本的spark数据处理
----------earthquake.csv---------------
Date,Time,Latitude,Longitude,Type,Depth,Magnitude
01/02/1965,13:44:18,19.246,145.616,Earthquake,131.6,6
01/04/1965,11:29:49,1.863,127.352,Earthquake,80,5.8
01/05/1965,18:05:58,-20.579,-173.972,Earthquake,20,6.2
01/08/1965,18:49:43,-59.076,-23.557,Earthquake,15,5.8
01/09/1965,13:32:50,11.938,126.427,Earthquake,15,5.8
01/10/1965,13:36:32,-13.405,166.629,Earthquake,35,6.7
01/12/1965,13:32:25,27.357,87.867,Earthquake,20,5.9
01/15/1965,23:17:42,-13.309,166.212,Earthquake,35,6
01/16/1965,11:32:37,-56.452,-27.043,Earthquake,95,6
01/17/1965,10:43:17,-24.563,178.487,Earthquake,565,5.8
-----------------------------------------------------------

![Image](https://github.com/HanShuo123-maker/BigdataProject/image/earthquake_mysql.PNG)
