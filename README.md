# 当前问题: 2024-03-05 更新了新的签到校验方式,待处理

**每天00.00执行签到任务(根据github当前时段的任务数量,可能会有延迟)**
# 使用方法

1. fork此仓库
   ![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/c0a5a7b3-b921-4b13-bd68-adce964701ad)

2. 设置环境变量
   ![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/7bc3df71-6a8c-466f-9854-33d21ae45f94)

3. 启动工作流程
   ![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/b89c7140-be7f-43aa-afaa-8554b4cab752)

   在工作流启动成功后可以手动触发工作流
   ![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/a1855da6-2dd4-47c3-be8c-86108527e841)


# 环境变量

| 变量名      | 信息               |
| ----------- | ------------------ |
| COOKIE`*`   | HiFiNi的cookie信息 |
| SERVER_CHAN | Service酱推送的key |

`*`:表示必选。



## 获取HifiNiCookie
访问`https://www.hifini.com/`
首页`F12`打开调试工具,在请求标头中找到并复制cookie的值
![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/97528823-4d31-4c72-bcca-e95bb5d75792)

## 获取Server酱的key(需要关注公众号)
访问 `https://sct.ftqq.com/`
![image](https://github.com/anduinnn/HifiNiAutoCheckIn/assets/68073009/c70b4471-2933-4441-964c-5aa2873c3590)

## 如何拉取最新代码?
在自己的仓库里找到此项目
![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/68073009/46ab90db-b7fb-4097-9abe-fde8c2c3543e)


