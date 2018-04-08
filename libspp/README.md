# 说明

暂时放到io.nukkit包里面
@iTXTech

# 数据报定义（草稿）

## 数据报结构

| 字节开始 | 类型 | 定义为 |
|---------|-----|-------|
| 0 | u8/byte | 版本号 |
| 2 | u8/byte | id |
| 4 | [u8]/byte[] | 数据 |

## 版本号

目前的版本为0x01。

## id

| id范围 | 定义为 |
|---------|-------|
| 0x00~0x0f | spp协议操作 |
| 0x10~0x1f | 浏览器操作和静态资源操作 |
| 0x20~0x2f | 动态地图操作 |
| 0x30~0x3f | 游戏操作 |

| id | 方向 | 参数 | 作用 |
|------|-----|------|-----|
| 0x10 | S->B | map[url] data | 刷新静态资源[1] |
| 0x11 | B->S | client_id, url | 访问页面 |
| 0x12 | S->B | client_id, url | 重定向 |
| 0x20 | B->S | pos（位置）, int（视距） | 按视距请求刷新地图 |
| 0x21 | S->B | map[pos] block_data | 小范围修改地图 |
| 0x22 | S->B | [chunks] | 大范围修改地图或发送初始地图 |
| 0x30 | B->S | <无> | 获得feature列表[2] |
| 0x30 | S->B | map[int] string | 返回feature列表 |
| 0x31 | S->B | int, byte[] | 发送feature |

[1] 和一般意义上的浏览器不同，spp的刷新是服务器请求到浏览器的。
[2] feature包括所有的游戏操作

## 连接流程

C代表客户端，B代表Browser，S代表Server。

| 处理 | 内容 | 数据报id |
|-----|------|-------|
| B->S spp协议 | 我要最新的静态资源 | 0x11 |
| S->B spp协议 | 拿着 | 0x10 |
| B->S spp协议 | 我要地图spp://localhost/static/example_world | 0x20 |
| S->B spp协议 | 打开管道，开始传输视界内地图区块 | 0x22 |

