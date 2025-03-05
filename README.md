<section align="center">
    <img src="https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/68073009/e50e9fa7-3ddd-4198-be59-fc231f9b8986" alt="稀土掘金" width="260" />
</section>

<h1 align="center">HiFiNi - 音乐磁场签到助手</h1>

<p align="center">签到、推送、自动化工作流。</p>

## 通知

🔈 新增`telegram`推送(2024年4月28日15:06:25)

🔈 支持设置多cookie 通过`&`分割,如:cookie1&cookie2(2024年3月29日17:57:46)

🔈 更新新的签到校验方式(2024年3月21日09:06:51)

🔈 对论坛新的签到校验方式进行适配(2024年3月18日19:37:13)

🔈 过renji验证

## 如何使用

1.[Fork 仓库](https://github.com/anduinnn/HiFiNi-Auto-CheckIn)

2.仓库 -> Settings -> Secrets -> New repository secret, 添加Secrets变量如下:

| 变量名              | 信息                                  | 是否必须 |
|------------------|-------------------------------------| -------- |
| COOKIE           | HiFiNi的cookie信息                     | 是       |
| SERVER_CHAN      | [Service酱](https://sct.ftqq.com/)推送的key | 否       |
| DINGTALK_WEBHOOK | 钉钉机器人推送的token                       | 否       |
| WXWORK_WEBHOOK   | 企业微信机器人推送的token                     | 否       |
| TG_CHAT_ID       | Telegram Chat ID                           | 否       |
| TG_BOT_TOKEN     | Telegram Bot Token                           | 否       |

3.启动工作流程
![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/b89c7140-be7f-43aa-afaa-8554b4cab752)



## 如何拉取最新代码?

在自己的仓库里找到此项目
![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/68073009/46ab90db-b7fb-4097-9abe-fde8c2c3543e)





## 获取HifiNiCookie
访问`https://www.hifini.com/`
首页`F12`打开调试工具,在请求标头中找到并复制cookie的值
![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/97528823-4d31-4c72-bcca-e95bb5d75792)

## 获取Server酱的key(需要关注公众号)
访问 `https://sct.ftqq.com/`
![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/c70b4471-2933-4441-964c-5aa2873c3590)

## 配置钉钉机器人
点击查看[如何配置钉钉机器人？](READMES/DingTalkRobotConfigInfo.md)

## 配置企业微信机器人
点击查看[如何配置企业微信机器人？](READMES/WeChatWorkRobotConfigInfo.md)

## 配置 Gotify 推送
点击查看[如何配置 Gotify 推送？](READMES/GofityConfigInfo.md)
