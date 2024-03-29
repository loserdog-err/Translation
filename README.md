<img src="https://github.com/Chenantao/Translation/blob/master/pic/1.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/2.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/3.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/4.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/5.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/6.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/7.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/8.jpg" width="300" height="500">
<img src="https://github.com/Chenantao/Translation/blob/master/pic/9.jpg" width="300" height="500">



# Translation

众包翻译是网络社会的社会化翻译生产力，是一种新型的翻译工作方法模式，即通过移动互联网技术海选译员，再由多个人以最短时间合作工作的模式。 
翻译人员在翻译过程中遇到难翻译的句子或术语时，往往是通过搜索引擎、论坛或社交网络发问等方式解决，这过程比较费时、费力。如果译员能够通过某种途径使用语音、移动设备等将这类问题快速众包，利用专业群体智慧，以任务实时发布和领取的方式（类似目前的打车软件）去解决问题，那么从一定程度上将会提高翻译效率，降低翻译难度。在这基础上，一方面通过不断积累收集分析这些问题，形成在翻译领域方面的专家机器人；另一方面，通过这种方式汇聚翻译专业人才，构建翻译社交网络。 

所用技术
--
- 使用Xutils进行网络请求以及数据缓存以实现离线模式。
- 使用Universal-Image-Loader进行图片缓存和加载。
- 通过本地生成values文件进行百分比布局，以达到屏幕适配的目的。
- 通过自定义View实现了可一键删除的EditText、圆形图片、过渡动画splash view等。
- 界面部分遵循Material Design的设计原则，以带来更好的界面设计。
- MVC设计原则，达到解耦的目的。
- 使用Jmessage实现实时推送以及实时聊天的功能。
- 使用ShareSDK提供的社交分享功能进行分享。


核心功能
--
需求商注册以后，可以在应用发布一个问题，并且通过相应的金额数，进行悬赏。<br><br>
服务商注册以后，可以在应用内找到有需求的用户，并且可以利用自己的专业知识进行回复，一经需求商采纳为最佳答案后，改服务商便可以获得相应的报酬。<br><br>
服务商与需求商货币流通的形式是通过 积分—人民币<br><br> 这条途径的，即，需求商可以使用软件支付一定数额的人民币获得相应的积分，发布问题时通过积分进行悬赏。服务商可以在被采纳为最佳答案后获得相应积分，并使用一定数量的积分兑换相应的人民币。<br><br>
服务商也可以构建一个属于自己的团体，一个服务商只能有一个团体，用户可以通过选择自己喜欢的团队加入。查看团队排名或者使用系统推荐的团队加入。用户可以在团队内部进行类似qq微信的实时聊天，支持文本以及语音的形式。<br><br>
服务商开启推送后，可以收到问题被采纳，回答过的问题已经结贴等的推送，或者是邀请你解答的推送。<br><br>
需求商开启推送后，可以收到问题有回复等的推送。<br><br>
需求商与服务商都可以在问题页面将问题分享到新浪微博，微信，qq，朋友圈，邀请好友一起来。<br><br>
需求商发布问题时也可以根据查看名人榜，查看解答问题被采纳率的服务商，然后进行邀请回答，服务器便会推送到该需求商。<br><br>
需求商可以在系统内购买积分，并且查看 自己的消费记录、流水账、资产分析等，支持柱状分析图。<br><br>
注册用户可以创建自己的团队，团队成员回答问题被采纳有助于团队排名的提升。<br><br>

其他功能
--
注册用户可以在app内更改一些个人信息，便于他人了解你。<br><br>
注册用户可以查看一下自己的动态，看一下自己干过了什么事，提问，回答等。<br><br>
注册用户可以查看提醒，看是否有人回答过你的问题，自己的回复是否被采纳等。<br><br>
注册用户可以进行一些系统设置，是否开启自动登录，是否接收推送等。

编译环境
---
Window10+AndroidStudio+jdk7+SdkVersion21+buildToolsVersion21.1.2

注意
--
一定使用AndroidStudio编译，不然带来其他麻烦自己负责。<br>
本项目是一个网络客户端，需要服务器，请自行下载另一个项目Translation_Server对服务器进行部署。
用户需要在HttpUtils类中修改常量 BASE_URL、BASE_FILE_PATH的值，将ip改为你服务器的ip。
