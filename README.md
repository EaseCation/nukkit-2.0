# nukkit-2.0
下一个nukkit版本，代号blackbird

# 架构
## 综述
nukkit 2.0分为browser和server两部分。browser将不同的客户端协议（bedrock和java）转码为类似于websocket的spp协议，和server沟通；server处理spp协议，实现游戏逻辑。spp协议基于url，实现动态资源（地图运行时图层）的调配。另外，browser还通过spp协议，从静态资源服务器上下载toml格式的静态资源表（类似于html网页），实现静态资源（资源包材质包、静态地图）的调配；spp协议的通讯地址通过静态资源表传输。

## 登录流程
C代表客户端，B代表Browser，S代表Server。
事实上表格内顺序无关的消息是并发处理的。

### 预处理
| 时间顺序 | 处理         | 内容                               |
| ---- | ---------- | -------------------------------- |
| 1    | B->S spp协议 | 我要所有的静态页面和地图，我已有的资源url有这些，哈希是这样的 |
| 2    | S->B spp协议 | 传输哈希有变化的或者浏览器没有的所有资源             |

### 客户端登录
| 时间顺序 | 处理         | 内容                                       |
| ---- | ---------- | ---------------------------------------- |
| 1    | C->B 多种协议  | 我要登录play.nukkit.io:19132                 |
| 2    | B          | 读取设置，发现play.nukkit.io:19132对应spp://localhost |
| 3    | B          | 访问缓存，找spp://localhost/index.toml         |
| 4    | B          | 读取index.toml，发现需要连接到动态地图spp://localhost/static/example_world |
| 5    | B->S spp协议 | 我要连接到地图spp://localhost/static/example_world |
| 6    | S->B spp协议 | 打开管道，开始传输视界内地图区块                         |
| 6    | B->C 多种协议  | 打开管道传输地图，同时B缓存地图                         |
| 7    | B->C 多种协议  | 传输完成，游戏开始                                |
| 7    | B          | 读取index.toml，发现在on_join时需要发送一个提示信息       |
| 8    | B->C 多种协议  | 发送提示信息：欢迎进入游戏！                           |

### 放置并点燃TNT

| 时间顺序 | 处理         | 内容                           |
| ---- | ---------- | ---------------------------- |
| 1    | C->B 多种协议  | 控制器尝试在pos位置放置方块              |
| 2    | B->S spp协议 | 客户端client尝试放置方块到某位置          |
| 3    | S          | 反作弊、权限等系统计算并处理，发现客户端可以放置这个方块 |
| 4    | S->B spp协议 | 发送方块放置数据报                    |
| 5    | B->C 多种协议  | 给可以看见方块[1]的玩家发送方块放置数据报       |

| 时间顺序 | 处理         | 内容                                    |
| ---- | ---------- | ------------------------------------- |
| 1    | C->B 多种协议  | 控制器与pos位置的方块互动                        |
| 2    | B->S spp协议 | 客户端client尝试与pos位置方块互动                 |
| 3    | S          | 处理计算，发现client手中拿的是打火石，pos位置方块是TNT     |
| 4    | S->B spp协议 | 对可以看见方块的玩家，发送生成TNT实体数据报，并更新实体过期（消失）时间 |
| 5    | B->C 多种协议  | 分发TNT实体生成数据报到客户端                      |
| 6    | S          | 时间到之后，计算TNT需要破坏哪些方块                   |
| 7    | S->B spp协议 | 发送对应的方块破坏和物品掉落                        |
| 8    | B->C 多种协议  | 分发TNT实体消失数据报到客户端                      |
| 8    | B->C 多种协议  | 分发方块和物品数据报到客户端                        |

[1] 由browser计算玩家视距，决定是否发送

## 小游戏服务器架构设计（半成品）

客户端`C`使用bedrock协议，进入一个小游戏服务器群组`{S}`，该群组在外部的地址为，比如说，play.nukkit.io:19132。群组中服务器可分配如下：

| 编号   | 域名（可做cdn动态解析）                 | 功能                           |
| ---- | ----------------------------- | ---------------------------- |
| `Sg` | spp://gate.play.nukkit.io/    | 入口服务器，负责查询玩家上次所在服务器，比如在某个游戏内 |
| `Ss` | spp://static.play.nukkit.io/  | 静态资源服务器                      |
| `Sm` | spp://gate.play.nukkit.io/mg/ | 游戏逻辑处理服务器，下属多个分页面            |
| `S1` | spp://s1.play.nukkit.io/      | 具体的服务器地址，这里只举一个S1为例          |

### 页面布置

#### 入口服务器Sg与Sm

地址为`spp://gate.play.nukkit.io/`。根目录开一个spp Server应用，负责转发玩家。

在下方开设一个`spp://gate.play.nukkit.io/mg/`内有小游戏逻辑若干。

#### 静态资源服务器Ss
地址为`spp://static.play.nukkit.io/`。

##### Ss：游戏crystal_wars，地图undersea_world
地址为`spp://static.play.nukkit.io/mg/crystal_wars/undersea_world.toml`。
```toml
[spp]
world = "/world/undersea_world"
connect = "spp://gate.play.nukkit.io/mg/crystal_wars?room_id=${room_id}"

[spp.effects] # 可以设置世界玩家effect
water_breathing = "inf" 
```

### 抓包记录

| 时间顺序 | 方向      | 协议      | 内容                                       |
| ---- | ------- | ------- | ---------------------------------------- |
| 1    | C->B    | bedrock | 客户端C登录服务器play.nukkit.io:19132            |
| 2    | B->Sg   | spp     | 客户端C登录服务器群组                              |
| 3    | Sg->B   | spp     | 查询到客户端C上次在服务器玩游戏crystal_wars，房间号是12345，地图为undersea_world，服务器为s1，返回以下toml内容：`spp.location="spp://static.play.nukkit.io/crystal_wars/undersea_world.toml?room_id=12345"` |
| 4    | B(查询缓存) | -       | 访问缓存中`spp://static.play.nukkit.io/crystal_wars/undersea_world.toml`页面 |
| 5    | B->Ss   | spp     | 下载并缓存地图undersea_world                    |
| 6    | Ss->B   | spp     | 你要的地图                                    |
| 5    | B->Sm   | spp     | 连接到`spp://gate.play.nukkit.io/mg/crystal_wars/undersea_world.toml?room_id=12345`。 |
| 6    | Sm->B   | spp     | 转发到S1，`spp://s1.play.nukkit.io/mg/crystal_wars/undersea_world.toml?room_id=12345`。 |
| 7    | B->S1   | spp     | 连接到S1                                    |
| 8    | S1->B   | spp     | 连接完成。以feature形式发送信息，开始加载地图，加载完开始游戏       |
| 9    | B->C    | bedrock | 传输地图，开始游戏                                |

## repo代码组成
repo代码由libmc、libspp、libticker、blackbird等部分组成，每个部分又分为不同的模块。具体部署如下：

| 模块                                       | 功能                                       | 开发状态 |
| ---------------------------------------- | ---------------------------------------- | ---- |
| libmc-core                               | 包含*平台无关的*游戏基础内容，包括方块、物品、药水、食物、实体、世界、实体效果等 | 筹划中  |
| libmc-algorithm                          | 包含平台无关的算法，包括玩家攻击、钓鱼获得物品、炼药与熔炉时间等         | 筹划中  |
| libmc-vanilla                            | 包含游戏功能，包括所有方块、物品、实体等                     | 筹划中  |
| libmc-world-gen                          | 世界生成器，包括mc-bedrock和mc-java的normal，包括超平坦生成器 | 筹划中  |
| libmc-world-fmt                          | 世界的二进制转储格式，包括anvil、mcregion等             | 筹划中  |
| libmc-net                                | 网络层协议，包括mc-bedrock和mc-java及其多版本实现        | 筹划中  |
| [libspp](https://github.com/EaseCation/nukkit-2.0/tree/2.0/libspp) | 服务器组网协议、server与browser通讯协议               | 开发中  |
| libticker                                | 小游戏房间系统和定时器                              | 筹划中  |
| [blackbird-core](https://github.com/EaseCation/nukkit-2.0/tree/2.0/blackbird-core) | 基本的client-browser-server架构代码             | 开发中  |
| blackbird-impl                           | 一个nukkit实现，包括：1、browser实现，实现libspp客户端；2、server实现，打包了libmc，实现libspp服务端，不包含插件接口 | 筹划中  |
| blackbird-app                            | 打包impl，实现基础listener逻辑，包含插件接口             | 筹划中  |
| blackbird-ui                             | 面向用户，实现图形化用户接口，便于使用                      | 筹划中  |

## 示例代码
```kotlin
// 启动一个简单的服务器
fun launch() {
    val world = World.ofPath(Path("/usr/local/worlds/my_world/")) // 从文件读取，读写分离world
    // 启动一个server
    Server()
        .route("/survival", LibMc.survival(world))      // 生存逻辑开放到 spp://<服务器地址>/survival
        .route("/world-survival", world)                // 地图开放到 spp://<服务器地址>/world-survival
        .route("/",
            Page().connectWorld("/world-survival")
                .redirect("/survival"))                 // 发送页面
        .listen("localhost")                            // 服务器地址为 localhost
        .start()
    
    // 启动一个browser
    Browser()
        .adapt(Adapter.bedrock(), "localhost:19132")    // 兼容基岩版，开放到 localhost:19132
        .adapt(Adapter.mcJava(), "localhost:25565")     // 兼容Java版，开放到 localhost:25565
        .setHomepage("spp://localhost")                 // 默认是about:blank
        .start()
}
```

# 贡献指南

## 环境准备

你需要安装Java、Gradle以及适合开发Kotlin的IDE。
你可以在 https://gradle.org/install/ 安装Gradle。
我们建议你使用 IntelliJ IDEA 作为IDE。

# 开发进程
近期正在筹划，2018-06-10开始代码工作，预计2018-09结束非游戏功能的架构开发
