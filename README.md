# 房屋租赁系统

### 简介

代码来自于b站-程序员辰星，本人在原来代码基础上添加了3D看房功能，作为课程作业。

【【原创精品】房屋租赁系统，基于SpringBoot+Vue实现，手把手带敲，功能强大，界面美观，集成协同过滤算法及评论敏感词替换算法】https://www.bilibili.com/video/BV1n4gHz6E1X?vd_source=80a298d359396b53633403edf7839b3b

### 本地部署方式

#### 前端

① vscode打开view文件夹

② 如果没有安装依赖，运行

```bash
npm install
```

③ 启动前端

```bash
npm run dev
```

④ 复制网址至浏览器

⑤ 关闭前端

终端处Ctrl+C即可结束前端运行

#### 数据库

Navicat新建连接（MySQL)

新建数据库house-rental

运行SQL文件

#### 后端

① 修改文件连接MySQL处的用户密码

application.yml文件resource -> application.yml

```
username: root
password: 123456
```

改为本地MySQL的用户和密码

② 连接数据库后：

数据库->新建->数据源选择MySQL

填写密码和所需要的数据库名，测试连接，成功连接后，点“应用”、“确定”

③ 运行`HouseRentalApplication`，开启后端





