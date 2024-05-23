从上传的文档中，我们可以提取到 WideCast 公司的系统需求，包含多种actors（参与者）和用例。下面将详细描述这些actors和用例，并阐述每个actors与用例之间的关系。

### Actors（参与者）
1. **Manager（经理）**
2. **Account Specialist（客户支持专员）**
3. **Technical Support Specialist（技术支持专员）**
4. **Customer（客户）**

### Use Cases（用例）
1. **Create Customer Account（创建客户账户）**
2. **Update Customer Account（更新客户账户）**
3. **Create Incident Ticket（创建事件票）**
4. **Assign Incident Ticket（分配事件票）**
5. **Place PPV Order（下单点播订单）**
6. **Update Record（更新记录）**
7. **Pay Monthly Bill（支付月账单）**
8. **Rent Online Game（租用在线游戏）**
9. **Order PPV Event（订购点播事件）**
10. **Cancel PPV Event（取消点播事件）**
11. **Change/Cancel/Add TV or Internet Plan（更改/取消/添加电视或互联网计划）**
12. **Schedule Service Appointment（安排服务预约）**
13. **Reschedule Service Appointment（重新安排服务预约）**
14. **Cancel Service Appointment（取消服务预约）**
15. **Rate/Review Service or Event（评价/评论服务或事件）**
16. **Share Service Link（分享服务链接）**
17. **Forecast Sales Revenue（预测销售收入）**
18. **Perform Demand Analysis（进行需求分析）**
19. **Identify Super Connectors（识别超级连接者）**
20. **Update/Delete/Cancel Order（更新/删除/取消订单）**

### Actors 与 Use Cases 的关系

#### Manager（经理）
- **Update/Delete/Cancel Order**: 经理可以更新、删除或取消任何订单。
- **Forecast Sales Revenue**: 经理使用客户交易数据预测销售收入。
- **Perform Demand Analysis**: 经理进行服务/事件/产品需求分析。

#### Account Specialist（客户支持专员）
- **Create Customer Account**: 负责创建新客户账户。
- **Update Customer Account**: 更新客户账户信息。
- **Create Incident Ticket**: 创建事件票，并将其分配给技术支持专员。
- **Assign Incident Ticket**: 将事件票分配给技术支持专员。
- **Place PPV Order**: 帮助客户下单点播订单。

#### Technical Support Specialist（技术支持专员）
- **Schedule Service Appointment**: 安排服务预约。
- **Reschedule Service Appointment**: 重新安排服务预约。
- **Cancel Service Appointment**: 取消服务预约。
- **Close Incident Ticket**: 关闭事件票。
- **Cancel Incident Ticket**: 取消事件票。

#### Customer（客户）
- **Update Record**: 更新客户的个人信息和信用卡信息。
- **Pay Monthly Bill**: 支付月账单。
- **Rent Online Game**: 租用在线游戏。
- **Order PPV Event**: 订购点播事件。
- **Cancel PPV Event**: 取消已订购的点播事件（需在点播日期前24小时取消）。
- **Change/Cancel/Add TV or Internet Plan**: 更改、取消或添加电视或互联网计划。
- **Rate/Review Service or Event**: 对服务或事件进行评价或评论。
- **Share Service Link**: 分享服务、电影或点播事件的在线链接。
- **Schedule Service Appointment**: 安排服务预约（创建事件票）。

### UML 用例图
基于上述描述，一个详细的用例图可以展示出每个actor和相关用例之间的关系。由于我不能绘制图形，但可以通过以下文本描述来理解用例图的设计：

1. **Manager** 通过关联线连接到 **Update/Delete/Cancel Order**、**Forecast Sales Revenue** 和 **Perform Demand Analysis**。
2. **Account Specialist** 通过关联线连接到 **Create Customer Account**、**Update Customer Account**、**Create Incident Ticket**、**Assign Incident Ticket** 和 **Place PPV Order**。
3. **Technical Support Specialist** 通过关联线连接到 **Schedule Service Appointment**、**Reschedule Service Appointment**、**Cancel Service Appointment**、**Close Incident Ticket** 和 **Cancel Incident Ticket**。
4. **Customer** 通过关联线连接到 **Update Record**、**Pay Monthly Bill**、**Rent Online Game**、**Order PPV Event**、**Cancel PPV Event**、**Change/Cancel/Add TV or Internet Plan**、**Rate/Review Service or Event**、**Share Service Link** 和 **Schedule Service Appointment**。

通过这个描述，可以更清晰地理解 WideCast 系统中的参与者和用例之间的交互。希望这些信息对你有帮助！

1. Update Record
2. Change/Cancel/Add TV or Internet Plan
3. Pay Monthly Bill
4. Order PPV Event
5. Rent Online Game
6. Cancel PPV Event
7. Check Order Date
8. Rate/Review Service or Event
9. Share Service Link
10. Schedule Service Appointment
11. Reschedule Service Appointment
12. Cancel Service Appointment
13. Update Order
14. Delete Order
15. Cancel Order
16. Place PPV Order
17. If Custormer Agree
18. Forecast Sales Revenue
19. Perform Demand Analysis
20. Identify Super Connectors
21. Create Customer Account
22. Check If Customer Account Exist
23. Update Customer Account
24. Create Incident Ticket
25. Assign Incident Ticket
26. Schedule incident ticket
27. Close incident ticket
28. Cancel incident ticket
