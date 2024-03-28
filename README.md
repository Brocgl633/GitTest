设计思想：
取消了 Indexing Server 的引用就必须要引入其他方式来维护节点之间的关系和文件存储记录，由于项目要求10个 super peer 和 30个 weak peer（每1个 super peer 对应的3个weak peer），所以通过数据库的方式来记录文件存储的详细信息，用yaml文件的层级关系来体现 super peer 和 weak peer 之间的关联关系。
启动所有的 super peer 节点，持续监听各自创建的 socket 链接，任意选择一个 weak peer 节点，启动后则会获取其所属的 super peer 代理对象，通过远程调用的方式执行 super peer 节点实现的具体代码，最后将结果返回给 weak peer。



TopologyGeneration：
1. 根据题意，要求生成两种拓扑结构：树形结构结构和网状结构。启动该类，根据提示选择要生成的拓扑结构，就会生成对应的yaml配置文件。该配置文件展示了每一个peer之间的对应关系，包括ip、serverPort、clientPort、linkNodes，在linkNodes中，以类似的结构展示了与当前peer邻接的peer信息。
2. 这里生成的拓扑结构是双向邻接图，也就是peer1指向peer2的同时peer2也指向peer1。
3. 根据测试要求，拓扑图中有10个super peer，每一个 super peer 对应3个weak peer，例如，SuperPeer#V1拥有3个weak peer，分别是WeakPeer#V1、WeakPeer#V2、WeakPeer#V3，SuperPeer#V2、SuperPeer#V3……以此类推。

数据库相关(src/java/com/cao/pa3/database/…)：
由于取消了 pa2 中的 Indexing Server，所以要将文件信息记录在Super Peer中。由于有10个SuperPeer和30个WeakPeer，并且任何两个节点之间都能进行通信，通过哈希表来存储信息比较复杂且数量很多。所以通过数据库记录代替哈希表，能够很大程度降低项目的复杂程度。
该项目创建了两张表：peer和server：
1. peer：记录哪一个peer节点作为客户端向服务端发出请求，包括该节点注册的文件名(file_name)、路径(file_path)和大小(file_size)，所属哪一个服务peer节点(peer_name)，文件类型(type)，文件状态(state)以及文件的生命周期(life_cycle)。
2. server：记录服务端节点，在本次项目中，首先会记录10个SuperPeer作为服务段节点，随后当启动了一台客户端节点并上传了文件后，如果该客户端节点要下载，则系统会将该客户端是做服务端，因为在peer表中存储了该文件的上传路径，所以下载的时候就需要将它看作服务端，为客户端提供下载服务。server表记录了服务端的名称(name)、ip、port和代理类对象(service)。

YamlUtil：
这个类用来读取并解析TopologyGeneration生成的yaml文件内容。
首先通过静态代码块初始化哈希表，这里存储两个哈希变量，一个是拓扑结构的哈希表，另一个是数据库连接的相关信息的哈希表。编写getInstance()方法，其中通过单例模式初始化该类对象，在业务代码中就可以通过getInstance()方法获取该对象，通过yaml中的key获取对应的value。

JdbcUtil：
这个类用来获取连接数据库的静态对象，方便在业务代码中随时随地调用该对象，实现对数据库的增删改查。

FileUtil：
这个类用来生成用户选择的拓扑结构所对应的yaml文件层级格式。

RegistrationCenter：
这个类用来注册服务和订阅已注册服务，其中有两个方法：
1. registerServer(…): 通过入参来创建服务端对象，首先去数据库查询该对象，检查是否已经注册，如果没有注册则将该服务端信息新增到数据库中；
2. subscribe(…): 通过两个入参 serverName 和 代理类对象名称 来获取数据库中已经注册的服务。

ServerThread：
这个类主要功能是记录服务端的哈希表和启动服务：
1. 维护了一个哈希表 serverObjectMap，记录每一个服务端名称与代理对象的映射关系；
2. 维护了一个RegistrationCenter的静态变量，用于对新增进来服务端做入库处理；
3. 重写方法 run()，通过服务端的 serverPort 来创建 socket 链接，用来监听 Skeleton 对象来接收到的数据，从而获取代理对象，调用该对象的方法，返回结果。

Skeleton：
该类是服务端的组件，负责接收客户端发来的请求，并调用相应的服务方法进行处理。可以将它看作是服务的框架或者入口点，将客户端的请求转发给实际的服务对象来执行。Skeleton 负责将客户端传递过来的请求信息解析，并将解析后的信息传递给具体的服务对象，然后将服务对象的执行结果返回给客户端。
在本项目中，class Skeleton 重写方法 run()，通过 socket 获取输入流和输出流，从输入流中获取封装好的 Invocation 对象，其中包含了代理对象类、调用方法名、该方法所需要的参数和参数类型等信息，再通过代理对象获得实例去调用对应的方法，通过输出流将该结果返回给客户端的 Stub。

ServerApplication：
初始化服务端的10个 super peer 节点的相关属性，包括ip、serverPort、clientPort等信息，新增到 superPeerList 中。将这10个 super peer 节点插入数据库的 server 表中，每一个服务端就是一个 ServerThread 对象，它继承了 Thread 类，每一个服务端启动一个线程，开始为客户端提供服务。 

ClientApplication：
与 ServerApplication 同理，首先初始化客户端的30个 weak peer 节点的相关属性，并新增到 weakPeerList 中，用户输入作为客户端的 weak peer name，系统则会创建 WeakPeerCLient 对象，该对象集成了 Thread 类，重写方法 run()。通过调用 Stub 中的 方法 getStub() 来获取远程调用对象 X。那么在后面的一系列操作中，都会通过调用 X 中的方法，实现远程调用的服务。

Stub：
这个类是客户端的代理对象，用于代表服务端的远程对象。客户端通过 Stub 对象来调用远程服务，而不必了解远程服务的具体实现细节。Stub 负责将客户端调用的方法、参数等信息封装成远程调用的请求，并通过网络发送给服务端。然后等待服务端的响应，将响应结果返回给客户端。
在本项目中，首先获取当前 weak peer 所属的 super peer，调用上述 RegistrationCenter 类中的方法 subscribe() 获取该 super peer 的相关信息。通过它的 ip 和 port 创建 socket 链接。接着，封装远程调用的入参，包括代理对象类名，方法名，方法入参以及类型，将它们封装成 Invocation 对象，通过输出流发送给 Skeleton 对象，经过 Skeleton 调用对应的代理类中的方法后，Stub 通过输入流接收调用的结果，最后返回该代理对象。

Message：
该类用于封装广播消息体，当用户需要在当前的拓扑网络中搜索文件的时候，则会以发送该消息体的形式，将必要的参数传递至下一个节点。
其中有一个特殊的属性 ttl，这里默认设置为 16，每经过一个节点，ttl 就会减一，直至减为0，目的是为了防止消息体在网络中无限制的转发，保证网络的稳定性和可靠性。

InvalidationMessage：
与 Message 类似，其中新增了一个属性version，它表示文件的版本号。每一个文件都有自己的版本，多个用户对同一个文件发出下载请求后，会在本地的 /download/ 目录下创建复制的版本，这些复制版本与源文件的版本号是相同的。如果源文件的内容发生了改变，则这些复制版本的文件均已失效，为了能在网络中广播删除失效版本的文件内容，通过 InvalidationMessage 作为消息实例，将文件名、序列号以及版本号等信息封装在消息实例中，删除数据库中所有具有相同 version 的数据行，同时在下载了该文件复制版本的 weak peer 节点的目录中删除该文件。

注：
无论是文件搜索的广播，还是删除失效文件的广播，都需要用哈希表记录已访问节点与消息实例的映射关系。因为默认的 ttl 值是16，而拓扑网络中的 super peer 节点数量是10，那么，针对 all-to-all 类型的拓扑结构而言，广播算法的时间复杂度会达到10的16次方，所以需要用哈希表记录节点是否被访问过，如果已经被同一个消息实例访问（这里使用消息实例的 messageId 作为标识），则不需要从该节点发出广播请求，从而减小算法的时间复杂度。

Invocation：
这个类作为远程调用之间的消息传递的载体，封装了代理对象名称、方法名、调用方法的入参以及这些参数的类型。调用方封装好后发送给代理对象，代理对象解析参数后调用对应的方法，并将执行结果返回给调用方。






meta events
Activate()
Start()
Paycredit()
Paycash()
Approved()
Ceject()
Cancel()
StartPump()
StopPump()
BelowCurrentPrice()
AboveCurrentPrice()
SelectGas()
Receipt()
NoReceipt()

meta actions
StoreData()
PayMsg()
RejectMsg()
CancelMsg()
EjectCard()
DisplayMenu()
StoreCash()
StorePrice()
StorePumpData()
PrintReceipt()
ReturnCash()
