# 配置钉钉机器人推送
`以下方法使用PC端为例`
1. 创建群聊

   打开钉钉，页面右上角➕，发起群聊。

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/e1e0e9d0-fc6c-4ebf-bdca-2ee6ecae2388)

   选择普通群，随便选两个人。(由于钉钉限制，必须选中额外的两个人，~~然后将他们踢出群聊就行，不然TA就能看到你的签到信息。~~💦)

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/2a843acb-ef78-40ea-9248-3adfd025f509)
   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/df7adb79-34c8-4d6b-969b-9c2edfea8ce0)

   `群聊名称可以随便修改`

2. 添加机器人

   点击右上角群设置。

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/89ed565a-c26c-4ae9-954e-e2559c861540)

   点击机器人

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/66c0fbbe-da38-404d-80a3-9982f792c036)

   点击添加机器人x2

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/eae8f91f-6bf8-4571-803c-06d05978f326)
   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/9d2aa7a9-3adc-4b7a-a2b2-6c8a940b41be)

   选择自定义，点击添加

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/fbfca0bc-b981-415e-8362-7e37bf708db1)

   填写机器人名字（随便）、**安全设置关键词（必须有`HiFiNi`或者`HiFiNi签到`，也可以两个都添加）** --> 勾选同意免责条款 --> 点击完成

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/c303beb5-beb0-4dbd-8c24-d8106b83e585)

   出现以下页面代表设置完成

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/dcdda610-7045-4bc0-9e27-0f6f0f4c36bc)

   **❗❗❗复制Webhook❗❗❗**
   ```
   复制之后你会得到如下：
   https://oapi.dingtalk.com/robot/send?access_token=12345678910xxxxxxxxxx
   
   我们不需要等号前面的信息，只需要等号后面的信息，如：`12345678910xxxxxxxxxx`，然后将这段数据设置到对应的环境变量中去。
   ```

   如果不小心将该窗口关闭，可以点击`群设置-->机器人-->选择刚刚添加的机器人`就可找到信息了，在这里面还可以设置关键词、对机器人改名、删除机器人等操作。

   成功案例：

   ![image](https://github.com/anduinnn/HiFiNi-Auto-CheckIn/assets/115618748/5973ff0d-d8f9-4e1c-87b9-7e2c841793f5)
   
