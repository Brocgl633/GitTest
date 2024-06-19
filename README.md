各位好，今天有我向大家讲解我和wuxiang一起完成的主题为GameStore的项目设计。
The Game Store project is an online platform which aims to create a digital platform similar to Steam or Xbox Game Center, designed to enable users to browse / search / subscript / like(collect) / buy the video games. We Name it as GameOne Store.
This platform caters to a wide range of users including gamers, developers, and administrators. The goal is to provide a seamless, user-friendly experience where users can browse games, make purchases, and leave reviews. At the same time, we’d like to enrich the user experience on our platform and enhance the user stickiness of users. Such as post / comment / share game experiences or insights.
For Game Developers, they can upload and manage their games, while administrators oversee platform activities to ensure smooth operations. The objective is to offer a seamless and engaging user experience for gamers while ensuring robust backend support for developers / publishers to manage their game listings.

接下来，展示的是功能性需求，这部分需求与业务紧密相关，比如每个角色具有哪些功能，需要用到哪些模块等等：
【功能性需求表】

下面是非功能性需求，这部分需求与整个系统紧密相关，与系统内部逻辑没有直接关联，和系统与外部交互直接相关，比如系统的扩展性、安全性、性能等：
【非功能性需求表】

该项目中有四种角色，普通用户、已注册的用户、游戏开发者以及系统管理员，我们来一一介绍：
普通用户仅仅具登录、登出和浏览的功能：
【user actor diagram】

已注册用户的功能会更加全面，如将游戏添加到愿望单、购买游戏、发布评论、发布帖子、分享游戏以及接收游戏通知等：
【registered user actor diagram】

作为开发者，在已注册用户的基础上，还增加了发布游戏、修改漏洞、查看自己所发布的游戏的收益情况等：
【developer actor diagram】

系统管理员是权限最高的角色，可以管理其他用户、同意或拒绝游戏的发布、查看整体收益情况等：
【administrator ator diagram】

这是项目的用例图：
【Use case diagram】

这是项目的设计类图：
【Design Diagram】

为了更好的理解设计类图，接下来结合一个例子的时序图来讲解：submitGame(game:Game)。这个方法标识开发者发布一个Game对象。
【interaction sequence diagram】

首先Developer调用这个方法，首先会检查这个游戏是否已经上传过，也就是在维护的已发布游戏列表中查询是否存在，如果不存在则可以向管理员申请提交。
调用管理员的approveGame(game)方法。如果管理员选择同意，那么会将该游戏添加到维护的该管理员曾经同意发布的游戏列表中，并返回true给Developer。
Developer接收到状态后，则会将该游戏添加到维护的自己曾经发布的游戏列表中。
至此，整个流程结束。

这是这个例子对应的System Sequence Diagram：
【System Sequence Diagram】

这是这个例子对应的Activity Diagram：
【Activity Diagram】

对系统有了基本了解后，下面展示的是每个实体对象之间的对应关系，通过这张图，可以容易地设计表结构。
【object-relational model】

最后，来分析一下项目中所用到的设计模式。让我们回到设计类图：
【Design Diagram】

第一种设计模式是Observer Pattern。由RegisteredUser、Developer、Administrator、User、Notification、NotificationImpl构成。Each time a user subscribes to the Game Update Service, the user is registered to the NotificationImpl, which is stored in the subscriberList. When the game is updated, the new game is pushed to every user who subscribes to the game, including regular users, developers, and administrators.


第二种设计模式是 Factory Method Pattern。由RegisteredUser、Developer、Administrator、User、SalesData、RegisteredUserSalesData、DeveloperSalesData、AdministratorSalesData构成。User can be seen as an abstract factory and its sub-classes as concrete factories. SalesData can be viewed as an abstract product and its sub-classes as concrete products. When a concrete user is created and a game is purchased, the purchaseGame() method implemented by the concrete user is invoked, where a concrete SalesData subclass is created, which can then perform different logical processing such as the amount of merchandise sold or maintenance costs, and so on.


第三种设计模式是 Composite Pattern。由User、Review、SingleReview、MultiReview构成。Users can create a single comment, while other users can attach their own comments to the comment. This is a self-associative process that is typical of the Composite design pattern.





