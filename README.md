# nukkit-2.0
下一个nukkit版本，代号blackbird

# 架构
## 综述
nukkit 2.0分为browser和server两部分。browser将不同的客户端协议（bedrock和java）转码为类似于websocket的spp协议，和server沟通；server处理spp协议，实现游戏逻辑。spp协议基于url，实现动态资源（地图运行时图层）的调配。另外，browser还通过spp协议，从静态资源服务器上下载toml格式的静态资源表（类似于html网页），实现静态资源（资源包材质包、静态地图）的调配；spp协议的通讯地址通过静态资源表传输。

## repo代码组成
repo代码由libmc、libspp、libticker、blackbird等部分组成，每个部分又分为不同的模块。具体部署如下：

| 模块 | 功能 | 开发状态 |
|------|------|----------|
| libmc-core | 包含*平台无关的*游戏基础内容，包括方块、物品、药水、食物、实体、世界、实体效果等 | 筹划中 |
| libmc-attack | 处理游戏伤害，包括环境与环境、玩家与环境（pve）、玩家与玩家（pvp）等 | 筹划中 |
| libmc-vanilla | 包含游戏功能，包括所有方块、物品、实体等 | 筹划中 |
| libmc-world-gen | 世界生成器，包括mc-bedrock和mc-java的normal，包括超平坦生成器 | 筹划中 |
| libmc-world-fmt | 世界的二进制转储格式，包括anvil、mcregion等 | 筹划中 |
| libmc-net | 网络层协议，包括mc-bedrock和mc-java及其多版本实现 | 筹划中 | 
| libspp | 服务器组网协议、server与browser通讯协议 | 筹划中 |
| libticker | 小游戏房间系统和定时器 | 筹划中 |
| blackbird-browser | 一个nukkit browser实现，实现libspp客户端 | 筹划中 |
| blackbird-server | 一个nukkit server实现，打包了libmc，实现libspp服务端，不包含插件接口 | 筹划中 |
| blackbird-app | 打包server和browser，实现基础listener逻辑，包含插件接口 | 筹划中 |
| blackbird-ui | 面向用户，实现图形化用户接口，便于使用 | 筹划中 |

## 登录流程
C代表客户端，B代表Browser，S代表Server

| 处理 | 内容 |
|------|------|
| C->B 多种协议 | 我要登录play.nukkit.io:19132 |
| B | 读取设置，发现play.nukkit.io:19132对应spp://localhost |
| B | 访问缓存，尝试找spp://localhost/index.toml，比如说没找到 |
| B->S spp协议 | 我要最新的index.toml |
| S->B spp协议 | 拿着 |
| B | 读取index.toml，发现需要从spp://localhost/static/example_world下载一个地图 |
| B | 访问缓存，比如说，发现没有这个地图 |
| B->S spp协议 | 我要地图spp://localhost/static/example_world |
| S->B spp协议 | 打开管道，开始传输视界内地图区块 |
| B->C 多种协议 | 打开管道传输地图，同时B缓存地图 |
| B->C 多种协议 | 传输完成，游戏开始 |
| B | 读取index.toml，发现在on_join时需要发送一个提示信息 |
| B->C 多种协议 | 发送提示信息：欢迎进入游戏！|

# 开发进程
近期正在筹划，2018-06-10开始代码工作，预计2018-09结束非游戏功能的架构开发
