# NekoLevel 等级系统插件

一个为Minecraft服务器设计的玩家等级系统插件，支持通过指令管理玩家等级和经验。

## 功能特性

- 玩家等级系统（最高5000级）
- MySQL数据库存储玩家数据
- 指令管理系统
- 持久化数据存储

## 配置说明

### config.yml

```yaml
# 最大等级限制
max-level: 5000

# 数据库配置
database:
  host: localhost
  port: 3306
  database: neko_level
  username: root
  password: ""
```

## 指令说明

### 基本指令

- `/nekolevel` - 查看当前等级和经验
- `/nekolevel setlevel <等级>` - 设置玩家等级（需要权限）
- `/nekolevel addlevel <等级>` - 增加玩家等级（需要权限）
- `/nekolevel setexp <经验>` - 设置玩家经验（需要权限）
- `/nekolevel addexp <经验>` - 增加玩家经验（需要权限）
- `/nekolevel setcatfood <猫粮>` - 设置玩家猫粮（需要权限）
- `/nekolevel addcatfood <猫粮>` - 增加玩家猫粮（需要权限）
- `/nekolevel reload` - 重新加载配置文件（需要权限）

### 权限节点

- `nekolevel.admin` - 使用管理指令的权限

## 数据库结构

玩家数据存储在MySQL数据库中，表结构如下：

```sql
CREATE TABLE player_levels (
    uuid VARCHAR(36) PRIMARY KEY,
    name VARCHAR(16) NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    experience BIGINT NOT NULL DEFAULT 0,
    cat_food BIGINT NOT NULL DEFAULT 0,
    command_priority INTEGER NOT NULL DEFAULT 0
);
```

## 变量说明

在其他插件中使用此插件时，可以通过PlaceholderAPI使用以下变量：

- `%nekolevel_level%` - 玩家当前等级
- `%nekolevel_exp%` - 玩家当前经验
- `%nekolevel_next_exp%` - 升级到下一级所需的经验值
- `%nekolevel_catfood%` 或 `%nekolevel_cat_food%` - 玩家当前拥有的猫粮数量
- `%nekolevel_progress%` - 升级进度条（蓝色和灰色方块组成的10字符进度条）

## 开发信息

- 作者: 不穿胖次の小奶猫
- 网站: https://cnmsb.xin/