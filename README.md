业务背景：电商业务中，需要给电商app设计一个用户钱包，用户可以往钱包中充值，购买商品时用户可以使用钱包中的钱消费，商品申请退款成功后钱会退回钱包中，用户也可以申请提现把钱提到银行卡中
用程序实现如下api接口
 1.  查询用户钱包余额
2. 用户消费100元的接口
3. 用户退款20元接口
4. 查询用户钱包金额变动明细的接口
请给出建表语句和对应的代码（只要能实现上面api接口要求即可，不相关的表和代码不用写）

思路：以下以MySQL为例
关于建表
1.需要一张用户表且有余额（余额可不用）有注册时间，id等关键信息
2.需要一张交易拉链表，记录每次交易的时间，金额，以及余额，交易类型，开始时间和结束时间等，每次的最后一条记录只有有开始时间，等下次交易的时候更新上条记录的结束时间

关于java代码
1.需要一个类，里面含有充值，交易，查询历史交易记录，查询余额，退款的方法
2.主类查询信息需要有id，交易要有类型和id和金额，根据以上进行计算