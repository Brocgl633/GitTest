这里，以一个Client和一个Server举例说明：

1. 按照以下顺序，依次启动：
	(1) ClientInformation：
	运行结果如下：
	****** Client information is been maintained ******
   
	(2) Server：
	运行结果如下：
	****** SERVER STARTED ******

	(3) Client_1:
	运行结果如下：
	****** CLIENT STARTED ******
	Please input Server IP :

3. 在Clinet_1的Console中根据输出的提示内容填写：
   	(1) 输入 Server IP：(这里以本地主机为例)
   		****** CLIENT STARTED ******
   		Please input Server IP :
   		127.0.0.1
   		Set client id [ 1 ] as a backup node? (Y/N):

 	(2) 此时我们可以将Client_1设置为备份节点，这里不设置为备份节点，输入N(如果设置为备份节点，输入Y)
		Set client id [ 1 ] as a backup node? (Y/N):N

		****** Client-Server mode file downloading system ******
		1.Register files.
		2.Get files list.
		3.Search or download or print a file
		4.Unregister files of client [127.0.0.1]
		5.Print client log of client [127.0.0.1]
		6.Print server log of client [127.0.0.1]
		7.Print backup log of client [127.0.0.1]
		8.Exit.
		Please input number :

   	(3) 输入1，即为上传/注册文件：
   		Please input number : 1
		The path of the registered files :

		(a) 如果输入文件夹路径，则上传改文件夹中的所有文件。从下面的内容可以看出，该文件夹下的6个文件已经上传/注册成功:
		The path of the registered files : 
		C:\Users\c30035198\Pictures\Camera Roll
		6 files have registered. Take 4 ms.
   		(b) 如果输入文件的绝对路径，则上传该文件。从下面的内容可以看出，该文件上传/注册成功:
		The path of the registered files :
		C:/Users/c30035198/Pictures/b.txt
		1 files have registered. Take 2 ms.

   	(4) 输入2，可以获取所有Client节点中存储的文件信息。由于这里只启动了1个client，所以id=1，如果启动多个client，id以及ip是不一样的：
		Please input number : 2

		All file are as follows:
		Client id: 1, ip: 127.0.0.1, file name: a.txt
		Client id: 1, ip: 127.0.0.1, file name: a1.png
		Client id: 1, ip: 127.0.0.1, file name: a2.png
		Client id: 1, ip: 127.0.0.1, file name: a3.png
		Client id: 1, ip: 127.0.0.1, file name: a4.docx
		Client id: 1, ip: 127.0.0.1, file name: desktop.ini
		Client id: 1, ip: 127.0.0.1, file name: b.txt
		
	(5) 输入3，这个选项的功能是查找、下载、输出文件：
		Please input number : 3
		The file name you want to search : 

		(a) 输入要查找的文件名后，会显示查找所用时间、该文件所属哪一个client以及该client ip：
			The file name you want to search : 
			a.txt
			File found. Searching takes 5 ms.
			Client id : 1
			Client ip : 127.0.0.1
			Download(D) or print(P) this file? Input(D/P):

   		(b) 输入D，即为下载该文件。控制台打印下载时间以及提示下载的位置：
			Download(D) or print(P) this file? Input(D/P): D
			Downloading file: a.txt
			Requesting file ...
			Downloading file ...
			File downloaded successfully. Take 66 seconds.
			The file has downloaded in the current path's sub-directory '/downloads'.

   		(c) 输入P，即为打印该文件。控制台打印下载时间、提示下载的位置以及打印该文件的内容：
			Download(D) or print(P) this file? Input(D/P): P
			Downloading file: a.txt
			Requesting file ...
			Downloading file ...
			File downloaded successfully. Take 4 seconds.
			The file has downloaded in the current path's sub-directory '/downloads'.
			
			****** The content of the file is as follows: ******
			----------------------------------------------------
			hello world
			----------------------------------------------------

	(6) 输入4，删除/注销该client下的所有文件，并打印删除/注销时间：
		Please input number : 4
		Please confirm whether to un-register? (Y/N): Y
		Files in ip [ 127.0.0.1 ] have been unregistered. Take 0 ms.

 	(7) 输入5，打印client log：

   	(8) 输入6，打印client log：
	
 	(9) 输入7，打印client log：

   	(10) 输入8，退出程序：
   		Please input number : 8
		Thanks for using the system.

5. ClientInformation与Client通过端口12233进行通信，当Client中有操作的时候，会向12233端口发送流信息。那么ClientInformation能够接收Client发送过来的信息(clientRequest)，从而针对不同的requestType处理不同的逻辑，此程序实现了针对REGISTER、SEARCH、UNREGOISTER、GET_BACKUP_NODES、GET_FILES_LIST、DISCONNECT这6中请求类型的逻辑。ClientInformation监听到的信息会打印在控制台上。

6. Server与ClientInformation通过端口22233进行通信，当Client上传/注册和删除/注销文件时候，会触发两个备份操作。如果该Client为备份节点，会对维护的备份文件的映射表进行更新操作(添加/删除)，此时，会和Server通信，让Server创建备份目录(backupfiles/)。之后对于该Client的所有文件相关的添加和删除，备份目录都会同步更新。Server监听到的信息会打印在控制台上。

7. Client与Server通过端口22233进行通信，当Client下载文件时，首先会让FileUtil类创建创建下载目录(downloads/)，再通知Server通过维护的<Client ip, register directory>映射表查找要下载的文件，从而下载文件到本地。Server监听到的信息会打印在控制台上。

8. 启动测试类：
   	****** Client-Server mode file downloading system test program ******
	Please input server ip: 
	127.0.0.1
	Please input the absolute path file: 
	C:/Users/c30035198/Pictures/Camera Roll/a.txt
	
	输入server ip以及需要下载的文件的绝对路径后，程序会创建10个线程，并发执行下载任务，并在控制台输出平均下载时间。
   













   
   	
   
