
<section align="center">
  <img src="https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/68073009/e50e9fa7-3ddd-4198-be59-fc231f9b8986" alt="hifini" width="260" />
</section>

<h1 align="center">HiFiNi - 音乐磁场签到助手</h1>

<p align="center">自动签到、消息推送、自动化工作流。</p>

---

## 🚀 快速开始

### 1. Fork 项目

点击 [Fork](https://github.com/anduinnn/HiFiNi-Auto-CheckIn) 到自己的仓库。

### 2. 配置 Secrets

进入你的仓库：

- `Settings` → `Secrets and variables` → `Actions` → `New repository secret`
- 添加以下变量：

| 名称               | 说明                                                      | 必填  |
|:-----------------|:--------------------------------------------------------|:----|
| COOKIE           | HiFiNi 的 Cookie 信息（支持多账号，使用 `&` 分隔，如 `cookie1&cookie2`） | ✅ 是 |
| SERVER_CHAN      | [Server酱](https://sct.ftqq.com/)推送 Key（推荐）              | 否   |
| DINGTALK_WEBHOOK | 钉钉机器人推送 Token                                           | 否   |
| WXWORK_WEBHOOK   | 企业微信机器人推送 Token                                         | 否   |
| TG_CHAT_ID       | Telegram 聊天 ID                                          | 否   |
| TG_BOT_TOKEN     | Telegram Bot Token                                      | 否   |
| GOTIFY_URL       | 部署的地址：`https://gotify.example.com`                      | 否   |
| GOTIFY_APP_TOKEN | 创建应用获取的token                                            | 否   |

### 3. 启动工作流程

在仓库 `Actions` 页面，手动运行工作流。

![启动工作流示例](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/b89c7140-be7f-43aa-afaa-8554b4cab752)

---

## 🧩 如何获取必要信息？

### 获取 HiFiNi Cookie

1. 访问 [https://www.hifini.com/](https://www.hifini.com/)
2. 打开浏览器开发者工具（`F12`）。
3. 在 `请求头` 中找到并复制你的 Cookie。

![获取 Cookie 示例](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/97528823-4d31-4c72-bcca-e95bb5d75792)

---

### 获取 Server酱 Key

访问 [Server酱官网](https://sct.ftqq.com/)，注册并获取推送 Key。

![Server酱示例](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/c70b4471-2933-4441-964c-5aa2873c3590)

---

### 配置钉钉机器人

👉 [查看钉钉机器人配置教程](READMES/DingTalkRobotConfigInfo.md)

---

### 配置企业微信机器人

👉 [查看企业微信机器人配置教程](READMES/WeChatWorkRobotConfigInfo.md)

---

### 配置 Gotify 推送

👉 [查看 Gotify 配置教程](READMES/GofityConfigInfo.md)

---

## 🔄 如何同步最新代码？

如果 Fork 后需要拉取主仓库更新：

1. 打开你的项目仓库。
2. 点击 `Sync fork` 或使用 Git 命令行同步更新。

![同步仓库示例](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/68073009/46ab90db-b7fb-4097-9abe-fde8c2c3543e)

---

## ⭐ Star 趋势

[![Star History Chart](https://api.star-history.com/svg?repos=anduinnn/HiFiNi-Auto-CheckIn&type=Date)](https://www.star-history.com/#anduinnn/HiFiNi-Auto-CheckIn&Date)

---

# ✨ Thanks for using HiFiNi-Auto-CheckIn！

如果觉得项目有帮助，欢迎 ⭐️Star 支持！
