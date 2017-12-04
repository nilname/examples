
CDH 的Kerberos认证配置

    博客分类：  Hadoop

 
http://xubo8118.blog.163.com/blog/static/1855523322013918103857226/
关于：hadoop的安全机制 
hadoop kerberos的安全机制
 
参考Cloudera官方文档：Configuring Hadoop Security in CDH3
 

一、部署无kerberos认证的Hadoop环境
参考另一篇笔记：hadoop集群部署
或者按照Cloudera的官方文档：CDH3 Installation Guide.
 
二、环境说明
1、主机名
之前部署hadoop集群时，没有使用节点的hostname，而是在hosts文件里添加了ip要域名的解析，部署后的hadoop没有问题，但是在为集群添加kerberos认证时因为这一点，遇到很多的问题。所以，建议还是使用节点的hostname来做解析。
 
集群中包含一个NameNode/JobTracker，两个DataNode/TaskTracker。
 
hosts文件

    172.18.6.152 nn.hadoop.local
    172.18.6.143 dn143.hadoop.local
    172.18.6.145 dn145.hadoop.local

注意：hosts文件中不要包含127.0.0.1的解析。
 
2、hadoop安装部署相关
hadoop 和kerberos的部署需要hadoop-sbin和hadoop-native。
如果使用的是rpm部署的hadoop，需要安装上面的两个rpm包。
我的集群使用的是tar包部署的，所以默认是包含这两部分文件的，可以检查一下：
hadoop-sbin对应的文件是：
/usr/local/hadoop/sbin/Linux-amd64-64
文件夹中包含两个文件：jsvc、task-controller
 
hadoop-native对应的目录是：
/usr/local/hadoop/lib/native
 
3、AES-256加密
我的系统使用的是centos6.2和centos5.7，对于使用centos5.6及以上的系统，默认使用AES-256来加密的。这就需要集群中的所有节点和hadoop user machine上安装 Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy File
打开上面的链接，在页面的下方，下载jdk对应的文件，jdk1.6.0_22下载下面的文件：
注：如果后面出现login failed的错误，应先检查是否是从官方网站下载的JCE。
下载的文件是一个zip包，解开后，将里面的两个文件放到下面的目录中：
/usr/java/jdk1.6.0_22/jre/lib/security
注：也可以不使用AED-256加密，方法见官方文档对应的部分。
 
三、部署KDC
1、安装kdc server
只需要在kdc中安装
yum install krb5-server.x86_64  krb5-devel.x86_64
 
2、配置文件
kdc服务器涉及到三个配置文件：

    /etc/krb5.conf、
    /var/kerberos/krb5kdc/kdc.conf、
    /var/kerberos/krb5kdc/kadm5.acl

 
hadoop集群中其他服务器涉及到的kerberos配置文件：/etc/krb5.conf。
将kdc中的/etc/krb5.conf拷贝到集群中其他服务器即可。
集群如果开启selinux了，拷贝后可能需要执行restorecon -R -v /etc/krb5.conf
 
/etc/krb5.conf

    [logging]
    default = FILE:/var/log/krb5libs.log
    kdc = FILE:/var/log/krb5kdc.log
    admin_server = FILE:/var/log/kadmind.log
    [libdefaults]
    default_realm = for_hadoop
    dns_lookup_realm = false
    dns_lookup_kdc = false
    ticket_lifetime = 24h
    renew_lifetime = 2d
    forwardable = true
    renewable = true
    [realms]
    for_hadoop = {
    kdc = 172.18.6.152:88
    admin_server = 172.18.6.152:749
    }
    [domain_realm]
    [kdc]
    profile=/var/kerberos/krb5kdc/kdc.conf

/var/kerberos/krb5kdc/kdc.conf

    [kdcdefaults]
    kdc_ports = 88
    kdc_tcp_ports = 88
    [realms]
    for_hadoop = {
    master_key_type = aes256-cts
    max_life = 25h
    max_renewable_life = 4w
    acl_file = /var/kerberos/krb5kdc/kadm5.acl
    dict_file = /usr/share/dict/words
    admin_keytab = /var/kerberos/krb5kdc/kadm5.keytab
    supported_enctypes = aes256-cts:normal aes128-cts:normal des3-hmac-sha1:normal arcfour-hmac:normal des-hmac-sha1:normal des-cbc-md
    5:normal des-cbc-crc:normal
    }

/var/kerberos/krb5kdc/kadm5.acl

     */admin@for_hadoop *

3、创建数据库

 

    #kdb5_util create -r for_hadoop -s

 
该命令会在/var/kerberos/krb5kdc/目录下创建principal数据库。
 
4、关于kerberos的管理
可以使用kadmin.local或kadmin,至于使用哪个，取决于账户和访问权限：
kadmin.local（on the KDC machine）or kadmin （from any machine）
如果有访问kdc服务器的root权限，但是没有kerberos admin账户，使用kadmin.local
如果没有访问kdc服务器的root权限，但是用kerberos admin账户，使用kadmin
 
5、创建远程管理的管理员

    #kadmin.local
    addprinc root/admin@for_hadoop

密码不能为空，且需妥善保存。
 
6、创建测试用户

    #kadmin.local
    addprinc test

 
7、常用kerberos管理命令

    #kadmin.local
    列出所有用户 listprincs
    查看某个用户属性，如 getprinc hdfs/nn.hadoop.local@for_hadoop
    注意，是getprinc，没有's'
    添加用户 addprinc
    更多，查看帮助

8、添加kerberos自启动及重启服务

    chkconfig --level 35 krb5kdc on
    chkconfig --level 35 kadmin on
    service krb5kdc restart
    service kadmin restart

9、测试
使用之前创建的test用户

    # kinit test
    Password for test@for_hadoop:
    #

输入密码后，没有报错即可。

    # klist -e
    Ticket cache: FILE:/tmp/krb5cc_0
    Default principal: test@for_hadoop
    Valid starting Expires Service principal
    06/14/12 15:42:33 06/15/12 15:42:33 krbtgt/for_hadoop@for_hadoop
    renew until 06/21/12 15:42:33, Etype (skey, tkt): AES-256 CTS mode with 96-bit SHA-1 HMAC, AES-256 CTS mode with 96-bit SHA-1 HMAC
    Kerberos 4 ticket cache: /tmp/tkt0
    klist: You have no tickets cached

可以看到，已经以test@for_hadoop登陆成功。
 
四、为hadoop创建认证规则（Principals）和keytab
1、一些概念
Kerberos principal用于在kerberos加密系统中标记一个唯一的身份。
kerberos为kerberos principal分配tickets使其可以访问由kerberos加密的hadoop服务。
对于hadoop，principals的格式为username/fully.qualified.domain.name@YOUR-REALM.COM.
 
keytab是包含principals和加密principal key的文件。
keytab文件对于每个host是唯一的，因为key中包含hostname。keytab文件用于不需要人工交互和保存纯文本密码，实现到kerberos上验证一个主机上的principal。
因为服务器上可以访问keytab文件即可以以principal的身份通过kerberos的认证，所以，keytab文件应该被妥善保存，应该只有少数的用户可以访问。
 
按照Cloudrea的文档，我们也使用两个用户hdfs和mapred，之前已经在linux上创建了相应的用户。
 
2、为集群中每个服务器节点添加三个principals，分别是hdfs、mapred和host。

    创建hdfs principal
    kadmin: addprinc -randkey hdfs/nn.hadoop.local@for_hadoop
    kadmin: addprinc -randkey hdfs/dn143.hadoop.local@for_hadoop
    kadmin: addprinc -randkey hdfs/dn145.hadoop.local@for_hadoop
    创建mapred principal
    kadmin: addprinc -randkey mapred/nn.hadoop.local@for_hadoop
    kadmin: addprinc -randkey mapred/dn143.hadoop.local@for_hadoop
    kadmin: addprinc -randkey mapred/dn145.hadoop.local@for_hadoop
    创建host principal
    kadmin: addprinc -randkey host/nn.hadoop.local@for_hadoop
    kadmin: addprinc -randkey host/dn143.hadoop.local@for_hadoop
    kadmin: addprinc -randkey host/dn145.hadoop.local@for_hadoop
    创建完成后，查看：
    kadmin: listprincs

3、创建keytab文件
创建包含hdfs principal和host principal的hdfs keytab

    kadmin: xst -norandkey -k hdfs.keytab hdfs/fully.qualified.domain.name host/fully.qualified.domain.name

创建包含mapred principal和host principal的mapred keytab

    kadmin: xst -norandkey -k mapred.keytab mapred/fully.qualified.domain.name host/fully.qualified.domain.name

 
 
注：上面的方法使用了xst的norandkey参数，有些kerberos不支持该参数，我在Centos6.2上即不支持该参数。
当不支持该参数时有这样的提示：Principal -norandkey does not exist.
需要使用下面的方法来生成keytab文件。
 
生成独立key

    # cd /var/kerberos/krb5kdc
    #kadmin
    kadmin: xst -k hdfs-unmerged.keytab hdfs/nn.hadoop.local@for_hadoop
    kadmin: xst -k hdfs-unmerged.keytab hdfs/dn143.hadoop.local@for_hadoop
    kadmin: xst -k hdfs-unmerged.keytab hdfs/dn145.hadoop.local@for_hadoop
     
    kadmin: xst -k mapred-unmerged.keytab mapred/nn.hadoop.local@for_hadoop
    kadmin: xst -k mapred-unmerged.keytab mapred/dn143.hadoop.local@for_hadoop
    kadmin: xst -k mapred-unmerged.keytab mapred/dn145.hadoop.local@for_hadoop

    kadmin: xst -k host.keytab host/nn.hadoop.local@for_hadoop
    kadmin: xst -k host.keytab host/dn143.hadoop.local@for_hadoop
    kadmin: xst -k host.keytab host/dn145.hadoop.local@for_hadoop

合并key
使用ktutil 合并前面创建的keytab

    # cd /var/kerberos/krb5kdc
    #ktutil
    ktutil: rkt hdfs-unmerged.keytab
    ktutil: rkt host.keytab
    ktutil: wkt hdfs.keytab
    ktutil: clear
    ktutil: rkt mapred-unmerged.keytab
    ktutil: rkt host.keytab
    ktutil: wkt mapred.keytab

 
这个过程创建了两个文件，hdfs.keytab和mapred.keytab，分别包含hdfs和host的principals，mapred和host的principals。
 
使用klist显示keytab文件列表，一个正确的hdfs keytab文件看起来类似于：

    #cd /var/kerberos/krb5kdc
    #klist -e -k -t hdfs.keytab
    Keytab name: WRFILE:hdfs.keytab
    slot KVNO Principal
    ---- ---- ---------------------------------------------------------------------
    1 7 host/fully.qualified.domain.name@YOUR-REALM.COM (DES cbc mode with CRC-32)
    2 7 host/fully.qualified.domain.name@YOUR-REALM.COM (Triple DES cbc mode with HMAC/sha1)
    3 7 hdfs/fully.qualified.domain.name@YOUR-REALM.COM (DES cbc mode with CRC-32)
    4 7 hdfs/fully.qualified.domain.name@YOUR-REALM.COM (Triple DES cbc mode with HMAC/sha1)

验证是否正确合并了key，使用合并后的keytab，分别使用hdfs和host principals来获取证书。

    # kinit -k -t hdfs.keytab hdfs/fully.qualified.domain.name@YOUR-REALM.COM
    # kinit -k -t hdfs.keytab host/fully.qualified.domain.name@YOUR-REALM.COM

如果出现错误：
 "kinit: Key table entry not found while getting initial credentials",
则上面的合并有问题，重新执行前面的操作。
 
4、部署kerberos keytab文件
在集群中所有节点，执行下面的操作来部署hdfs.keytab和mapred.keytab文件
 
拷贝hdfs.keytab和mapred.keytab文件到hadoop可以访问的目录。

    scp hdfs.keytab mapred.keytab host:/usr/local/hadoop/conf

确保hdfs.keytab对hdfs用户可读
确报mapred.keytab对mapred用户可读
后面经常会遇到使用keytab login失败的问题，首先需要检查的就是文件的权限。
 
五、停止hadoop集群
 
六、Enable Hadoop Security
在集群中所有节点的core-site.xml文件中添加下面的配置

    <property>
      <name>hadoop.security.authentication</name>
      <value>kerberos</value> <!-- A value of "simple" would disable security. -->
    </property>
     
    <property>
      <name>hadoop.security.authorization</name>
      <value>true</value>
    </property>

七、Configure Secure HDFS
1、在集群中所有节点的hdfs-site.xml文件中添加下面的配置 

    <!-- General HDFS security config -->
    <property>
      <name>dfs.block.access.token.enable</name>
      <value>true</value>
    </property>
     
    <!-- NameNode security config -->
    <property>
      <name>dfs.https.address</name>
      <value><fully qualified domain name of NN>:50470</value>
    </property>
    <property>
      <name>dfs.https.port</name>
      <value>50470</value>
    </property>
    <property>
      <name>dfs.namenode.keytab.file</name>
      <value>/usr/local/hadoop/conf/hdfs.keytab</value> <!-- path to the HDFS keytab -->
    </property>
    <property>
      <name>dfs.namenode.kerberos.principal</name>
      <value>hdfs/_HOST@YOUR-REALM.COM</value>
    </property>
    <property>
      <name>dfs.namenode.kerberos.https.principal</name>
      <value>host/_HOST@YOUR-REALM.COM</value>
    </property>
     
    <!-- Secondary NameNode security config -->
    <property>
      <name>dfs.secondary.https.address</name>
      <value><fully qualified domain name of 2NN>:50495</value>
    </property>
    <property>
      <name>dfs.secondary.https.port</name>
      <value>50495</value>
    </property>
    <property>
      <name>dfs.secondary.namenode.keytab.file</name>
      <value>/usr/local/hadoop/conf/hdfs.keytab</value> <!-- path to the HDFS keytab -->
    </property>
    <property>
      <name>dfs.secondary.namenode.kerberos.principal</name>
      <value>hdfs/_HOST@YOUR-REALM.COM</value>
    </property>
    <property>
      <name>dfs.secondary.namenode.kerberos.https.principal</name>
      <value>host/_HOST@YOUR-REALM.COM</value>
    </property>
     
    <!-- DataNode security config -->
    <property>
      <name>dfs.datanode.data.dir.perm</name>
      <value>700</value>
    </property>
    <property>
      <name>dfs.datanode.address</name>
      <value>0.0.0.0:1004</value>
    </property>
    <property>
      <name>dfs.datanode.http.address</name>
      <value>0.0.0.0:1006</value>
    </property>
    <property>
      <name>dfs.datanode.keytab.file</name>
      <value>/usr/local/hadoop/conf/hdfs.keytab</value> <!-- path to the HDFS keytab -->
    </property>
    <property>
      <name>dfs.datanode.kerberos.principal</name>
      <value>hdfs/_HOST@YOUR-REALM.COM</value>
    </property>
    <property>
      <name>dfs.datanode.kerberos.https.principal</name>
      <value>host/_HOST@YOUR-REALM.COM</value>
    </property>

2、启动namenode

    # sudo -u hdfs /usr/local/hadoop/bin/hadoop namenode

启动后可以看到下面的信息

    10/10/25 17:01:46 INFO security.UserGroupInformation:
    Login successful for user hdfs/fully.qualified.domain.name@YOUR-REALM.COM using keytab file /etc/hadoop/hdfs.keytab

    10/10/25 17:01:52 INFO security.UserGroupInformation: Login successful for user host/fully.qualified.domain.name@YOUR-REALM.COM using keytab file /etc/hadoop/hdfs.keytab
    10/10/25 17:01:52 INFO http.HttpServer: Added global filtersafety (class=org.apache.hadoop.http.HttpServer$QuotingInputFilter)
    10/10/25 17:01:57 INFO http.HttpServer: Adding Kerberos filter to getDelegationToken
    10/10/25 17:01:57 INFO http.HttpServer: Adding Kerberos filter to renewDelegationToken
    10/10/25 17:01:57 INFO http.HttpServer: Adding Kerberos filter to cancelDelegationToken
    10/10/25 17:01:57 INFO http.HttpServer: Adding Kerberos filter to fsck
    10/10/25 17:01:57 INFO http.HttpServer: Adding Kerberos filter to getimage

关于错误：

    12/06/13 13:24:43 WARN ipc.Server: Auth failed for 127.0.0.1:63202:null
    12/06/13 13:24:43 WARN ipc.Server: Auth failed for 127.0.0.1:63202:null
    12/06/13 13:24:43 INFO ipc.Server: IPC Server listener on 9000: readAndProcess threw exception javax.security.sasl.SaslException: GSS initiate failed [Caused by GS***ception: Failure unspecified at GSS-API level (Mechanism level: Encryption type AES256 CTS mode with HMAC SHA1-96 is not supported/enabled)] from client 127.0.0.1. Count of bytes read: 0
    javax.security.sasl.SaslException: GSS initiate failed [Caused by GS***ception: Failure unspecified at GSS-API level (Mechanism level: Encryption type AES256 CTS mode with HMAC SHA1-96 is not supported/enabled)]

    12/06/13 13:23:21 WARN security.UserGroupInformation: Not attempting to re-login since the last re-login was attempted less than 600 seconds before.

这两个错误与前面提到的JCE jar包有关，确保已经下载并替换了相应的jar包。
 
如果出现Login failed，应首先使用kinit的方式登陆，如果可以登陆，检查是否使用了正确的JCE jar包。然后就是检查keytab的路径及权限。
 
另外，第二个错误，也有可能与SELINUX有关，在所有配置不变的情况下，关闭selinux可以解决问题。但是在/var/log/audit/audit.log里没有看到相关的错误。之后不知何故，开启selinux也不会造成上面的那个问题了。
 
3、验证namenode是否正确启动
两种方法：
（1）访问http://machine:50070
（2）

    #hadoop fs -ls

注：如果在你的凭据缓存中没有有效的kerberos ticket，执行hadoop fs -ls将会失败。
可以使用klist来查看是否有有有效的ticket。
可以通过kinit来获取ticket.
kinit -k -t /usr/local/hadoop/conf/hdfs.ketab hdfs/nn.hadoop.local@for_hadoop
如果没有有效的ticket，将会出现下面的错误：

    11/01/04 12:08:12 WARN ipc.Client: Exception encountered while connecting to the server : javax.security.sasl.SaslException:
    GSS initiate failed [Caused by GS***ception: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
    Bad connection to FS. command aborted. exception: Call to nn-host/10.0.0.2:8020 failed on local exception: java.io.IOException:
    javax.security.sasl.SaslException: GSS initiate failed [Caused by GS***ception: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]

注：如果使用的MIT kerberos 1.8.1或更高版本，ORACLE JDK6 UPDATE 26和更早的版本存在一个bug：
即使成功的使用kinit获取了ticket，java仍然无法读取kerberos 票据缓存。
解决的办法是在使用kinit获取ticket之后使用kinit -R 来renew ticket。这样，将重写票据缓存中的ticket为java可读的格式。
但是，在使用kinit -R 时遇到一个问题，就是无法renew ticket

    kinit: Ticket expired while renewing credentials

在官方文档中也有描述：Java is unable to read the Kerberos credentials cache created by versions of MIT Kerberos 1.8.1 or higher.
关于是否以获取renew的ticket，取决于KDC的设置。
是否是可以获取renew的ticket，可以通过klist来查看：
如果不可以获取renw的ticket，”valid starting" and "renew until"的值是相同的时间。
我为了获取renw的ticket，做了以下的尝试：
<1>、在kdc.conf中添加默认flag
default_principal_flags = +forwardable,+renewable
但是实际没有起作用，因为查看资料，默认的principal_flags就包含了renewable，所以问题不是出在这里。
另外需要说明一点，default_principal_flags 只对这个flags生效以后创建的principal生效，之前创建的不生效，需要使用modprinc来使之前的principal生效。
 
<2>、在kdc.conf中添加：

    max_renewable_life = 10d

重启kdc， 重新kinit -k -t .....，重新执行kinit -R可以正常renw了。
再次验证，修改为：

    max_renewable_life = 0s

重启kdc，重新kinit -k -t ......，重新执行 kinit -R在此不能renew ticket了。
所以，是否可以获取renew的ticket是这样设置的：
默认是可以获取renew的ticket的，但是，可以renw的最长时间是0s，所以造成无法renew，解决的办法是在kdc.conf中增大该参数。
 
另外关于krb5.conf中的renew_lifetime = 7d参数，该参数设置该服务器上的使用kinit -R时renew的时间。
 
另外，也可以通过modprinc来修改max_renewable_life的值，使用modprinc修改的值比kdc.conf中的配置有更高的优先级，例如，使用modprinc设置了为7天，kdc.conf中设置了为10天，使用getprinc可以看出，实际生效的是7天。需要注意的是，即要修改krbtgt/for_hadoop@for_hadoop，也要修改类似于hdfs/dn145.hadoop.local@for_hadoop这样的prinicials，通过klist可以看出来：

    # klist
    Ticket cache: FILE:/tmp/krb5cc_0
    Default principal: hdfs/dn145.hadoop.local@for_hadoop
    Valid starting Expires Service principal
    06/14/12 17:15:05 06/15/12 17:15:05 krbtgt/for_hadoop@for_hadoop
    renew until 06/21/12 17:15:04
    Kerberos 4 ticket cache: /tmp/tkt0
    klist: You have no tickets cached

如何使用modprinc来修改max_renewable_life

    #kadmin.local
    modprinc -maxrenewlife 7days krbtgt/for_hadoop@for_hadoop
    getprinc krbtgt/for_hadoop@for_hadoop
    Principal: krbtgt/for_hadoop@for_hadoop
    Expiration date: [never]
    Last password change: [never]
    Password expiration date: [none]
    Maximum ticket life: 1 day 00:00:00
    Maximum renewable life: 7 days 00:00:00
    Last modified: Thu Jun 14 11:25:15 CST 2012 (hdfs/admin@for_hadoop)
    Last successful authentication: [never]
    Last failed authentication: [never]
    Failed password attempts: 0
    Number of keys: 7
    Key: vno 1, aes256-cts-hmac-sha1-96, no salt
    Key: vno 1, aes128-cts-hmac-sha1-96, no salt
    Key: vno 1, des3-cbc-sha1, no salt
    Key: vno 1, arcfour-hmac, no salt
    Key: vno 1, des-hmac-sha1, no salt
    Key: vno 1, des-cbc-md5, no salt
    Key: vno 1, des-cbc-crc, no salt

到这里，kinit -R的问题解决，可以成功的执行hadoop fs -ls了。
 
4、启动datanode
正确的启动方法应该是使用root账号

    HADOOP_DATANODE_USER=hdfs sudo -E /usr/local/hadoop/bin/hadoop datanode

如果使用其他用户，直接执行hadoop datanode，则会报错：

    11/03/21 12:46:57 ERROR datanode.DataNode: java.lang.RuntimeException: Cannot start secure cluster without privileged resources. In a secure cluster, the DataNode must
    be started from within jsvc. If using Cloudera packages, please install the hadoop-0.20-sbin package.
    For development purposes ONLY you may override this check by setting dfs.datanode.require.secure.ports to false. *** THIS WILL OPEN A SECURITY HOLE AND MUST NOT BE
    USED FOR A REAL CLUSTER ***.
    at org.apache.hadoop.hdfs.server.datanode.DataNode.startDataNode(DataNode.java:306)
    at org.apache.hadoop.hdfs.server.datanode.DataNode.<init>(DataNode.java:280)
    at org.apache.hadoop.hdfs.server.datanode.DataNode.makeInstance(DataNode.java:1533)
    at org.apache.hadoop.hdfs.server.datanode.DataNode.instantiateDataNode(DataNode.java:1473)
    at org.apache.hadoop.hdfs.server.datanode.DataNode.createDataNode(DataNode.java:1491)
    at org.apache.hadoop.hdfs.server.datanode.DataNode.secureMain(DataNode.java:1616)
    at org.apache.hadoop.hdfs.server.datanode.DataNode.main(DataNode.java:1626)

官方文档中提到了这个问题：
Cannot start secure cluster without privileged resources.
官方的解释是和jsvc有关，确实，与jsvc有关.
（1）、有可能没有安装hadoop-sbin。
 （2）、确保jsv对于HADOOP_DATANODE_USER=hdfs有可执行的权限。
（3）、通过查看hadoop这个启动脚本，可以看到这样的代码：

    if [ "$EUID" = "0" ] ; then
    if [ "$COMMAND" == "datanode" ] && [ -x "$_JSVC_PATH" ]; then
    _HADOOP_RUN_MODE="jsvc"
    elif [ -x /bin/su ]; then
    _HADOOP_RUN_MODE="su"
    else

检查执行hadoop命令的用户的EUID是否为0，即root，只有root用户才去执行jsvc相关的命令。
关于EUID：linux系统中每个进程都有2个ID，分别为用户ID（uid）和有效用户ID（euid），UID一般表示进程的创建者（属于哪个用户创建），而EUID表示进程对于文件和资源的访问权限（具备等同于哪个用户的权限）。一般情况下2个ID是相同的。
 
5、 Set the Sticky Bit on HDFS Directories.
可以针对hdfs上的目录设置sticky bit，用于防止除superuser，owner以外的用户删除文件夹中的文件。对一个文件设置sticky bit是无效的。
 
八、Start up the Secondary NameNode
跳过
 
九、Configure Secure MapReduce
在mapred-site.xml中添加

    <!-- JobTracker security configs -->
    <property>
      <name>mapreduce.jobtracker.kerberos.principal</name>
      <value>mapred/_HOST@YOUR-REALM.COM</value>
    </property>
    <property>
      <name>mapreduce.jobtracker.kerberos.https.principal</name>
      <value>host/_HOST@YOUR-REALM.COM</value>
    </property>
    <property>
      <name>mapreduce.jobtracker.keytab.file</name>
      <value>/usr/local/hadoop/conf/mapred.keytab</value> <!-- path to the MapReduce keytab -->
    </property>
     
    <!-- TaskTracker security configs -->
    <property>
      <name>mapreduce.tasktracker.kerberos.principal</name>
      <value>mapred/_HOST@YOUR-REALM.COM</value>
    </property>
    <property>
      <name>mapreduce.tasktracker.kerberos.https.principal</name>
      <value>host/_HOST@YOUR-REALM.COM</value>
    </property>
    <property>
      <name>mapreduce.tasktracker.keytab.file</name>
      <value>/usr/local/hadoop/conf/mapred.keytab</value> <!-- path to the MapReduce keytab -->
    </property>
     
    <!-- TaskController settings -->
    <property>
      <name>mapred.task.tracker.task-controller</name>
      <value>org.apache.hadoop.mapred.LinuxTaskController</value>
    </property>
    <property>
      <name>mapreduce.tasktracker.group</name>
      <value>mapred</value>
    </property>

 
创建一个taskcontroller.cfg文件，路径为
<path of task-controller binary>/../../conf/taskcontroller.cfg
即/usr/local/hadoop/sbin/Linux-amd64-64/../../conf/taskcontroller.cfg
即conf目录，和site文件相同的目录

    mapred.local.dir=/hadoop_data/tmp/mapred/local
    hadoop.log.dir=/usr/local/hadoop/logs
    mapreduce.tasktracker.group=hadoop
    banned.users=hadoop,hdfs,bin
    min.user.id=500

其中：
mapred.local.dir需要和mapred-site.xml中指定的相同，否则this error message 
hadoop.log.dir要和hadoop所使用的目录相同，可以在core-site.xml中指定，不同的话会报错：this error message
另外mapred.local.dir的属主为mapred用户:

    chown -R mapred.mapred  /hadoop_data/tmp/mapred/local

Note
In the taskcontroller.cfg file, the default setting for the banned.users property is mapred, hdfs, and bin to prevent jobs from being submitted via those user accounts. The default setting for themin.user.id property is 1000 to prevent jobs from being submitted with a user ID less than 1000, which are conventionally Unix super users. Note that some operating systems such as CentOS 5 use a default value of 500 and above for user IDs, not 1000. If this is the case on your system, change the default setting for the min.user.id property to 500. If there are user accounts on your cluster that have a user ID less than the value specified for the min.user.id property, the TaskTracker returns an error code of 255.
 
修改task-controller文件的权限：
More Information about the hadoop-0.20-sbin Binary Programs.

    chown root:mapred /usr/local/hadoop/sbin/Linux-amd64-64/task-controller
    chmod 4754 /usr/local/hadoop/sbin/Linux-amd64-64/task-controller

 
启动JOBTRACKER

    sudo -u mapred /usr/local/hadoop/bin/hadoop jobtracker

错误：

    FATAL mapred.JobTracker: org.apache.hadoop.security.AccessControlException: The systemdir hdfs://nn.hadoop.local:9000/hadoop_data/tmp/mapred/system is not owned by mapred

修改hdfs上对应目录的属性

    hadoop fs -chown -R mapred /hadoop_data/tmp/mapred

注意，是mapred而不是mapred.mapred，否则会变成 mapred.mapred supergroup          0 2012-06-08 11:41 /hadoop_data/tmp/mapred/system
 
重新启动JobTracker。
 
到这里JobTracker启动完成，最后一步，启动TaskTracker
修改taskcontroller.cfg文件属性，启动tasktracker时会检查（jobtracker不需要？待验证）

    chown root.mapred taskcontroller.cfg
    chmod 600 taskcontroller.cfg

同样的，也需要修改task-controler的属性

    chown root:mapred  /usr/local/hadoop/sbin/Linux-amd64-64/task-controller
    chmod 4754 /usr/local/hadoop/sbin/Linux-amd64-64/task-controller

启动

    sudo -u mapred /usr/local/hadoop/bin/hadoop tasktracker

错误：

    ERROR mapred.TaskTracker: Can not start task tracker because java.io.IOException: Login failure for mapred/srv143.madeforchina.co@for_hadoop from keytab /usr/local/hadoop/mapred.keytab

使用kinit可以登陆？确保key对于mapred用户可读。
 
另外，可以还需要修改log目录的权限

    chown -R mapred.hadoop /usr/local/hadoop/logs/

到这里，hadoop + kerberos基本完后。
 
后面需要做的工作包括修改启动hadoop的脚本，部署kerberos slave服务器

大数据平台架构参考文档
http://database.51cto.com/art/201407/446416.htm


////TODO HADOOP KERBEROS

HDFS配置Kerberos认证
 junjie_m |  发布于 2015-01-30 22:04:33 |  阅读量 1128 |  Kerberos
本文主要记录 CDH Hadoop 集群上配置 HDFS 集成 Kerberos 的过程，包括 Kerberos 的安装和 Hadoop 相关配置修改说明。

注意：

下面第一、二部分内容，摘抄自《Hadoop的kerberos的实践部署》，主要是为了对 Hadoop 的认证机制和 Kerberos 认证协议做个简单介绍。在此，对原作者表示感谢。
1. Hadoop 的认证机制
简单来说，没有做 kerberos 认证的 Hadoop，只要有 client 端就能够连接上。而且，通过一个有 root 的权限的内网机器，通过创建对应的 linux 用户，就能够得到 Hadoop 集群上对应的权限。

而实行 Kerberos 后，任意机器的任意用户都必须现在 Kerberos 的 KDC 中有记录，才允许和集群中其它的模块进行通信。

详细介绍请参考 Hadoop安全机制研究

2. Kerberos 认证协议
Kerberos 是一种网络认证协议，其设计目标是通过密钥系统为客户机/服务器应用程序提供强大的认证服务。

使用 Kerberos 时，一个客户端需要经过三个步骤来获取服务:

认证：客户端向认证服务器发送一条报文，并获取一个含时间戳的 Ticket-Granting Ticket（TGT）。
授权：客户端使用 TGT 向 Ticket-Granting Server（TGS）请求一个服务 Ticket。
服务请求：客户端向服务器出示服务 Ticket ，以证实自己的合法性。
为此，Kerberos 需要 The Key Distribution Centers（KDC）来进行认证。KDC 只有一个 Master，可以带多个 slaves 机器。slaves 机器仅进行普通验证。Mater 上做的修改需要自动同步到 slaves。

另外，KDC 需要一个 admin，来进行日常的管理操作。这个 admin 可以通过远程或者本地方式登录。

3. 搭建 Kerberos
3.1 环境
我们在三个节点的服务器上安装 Kerberos，这三个节点上安装了 hadoop 集群，安装 hadoop 过程见：使用yum安装CDH Hadoop集群。这三个节点机器分布为：cdh1、cdh2、cdh3。

操作系统：CentOs 6.6
运行用户：root
3.2 安装过程
3.2.1 准备工作

确认添加主机名解析到 
/etc/hosts 文件中。

$ cat /etc/hosts
127.0.0.1       localhost
 
192.168.56.121 cdh1
192.168.56.122 cdh2
192.168.56.123 cdh3
注意：hostname 请使用小写，要不然在集成 kerberos 时会出现一些错误。
3.2.2 安装 kdc server

在 KDC (这里是 cdh1 ) 上安装包 krb5、krb5-server 和 krb5-client。

$ yum install krb5-server krb5-libs krb5-auth-dialog krb5-workstation  -y
在其他节点（cdh1、cdh2、cdh3）安装 krb5-devel、krb5-workstation ：

$ ssh cdh1 "yum install krb5-devel krb5-workstation -y"
$ ssh cdh2 "yum install krb5-devel krb5-workstation -y"
$ ssh cdh3 "yum install krb5-devel krb5-workstation -y"
3.2.3 修改配置文件

kdc 服务器涉及到三个配置文件：

/etc/krb5.conf
/var/kerberos/krb5kdc/kdc.conf
/var/kerberos/krb5kdc/kadm5.acl
配置 Kerberos 的一种方法是编辑配置文件 /etc/krb5.conf。默认安装的文件中包含多个示例项。

$ cat /etc/krb5.conf
  [logging]
   default = FILE:/var/log/krb5libs.log
   kdc = FILE:/var/log/krb5kdc.log
   admin_server = FILE:/var/log/kadmind.log
 
  [libdefaults]
   default_realm = JAVACHEN.COM
   dns_lookup_realm = false
   dns_lookup_kdc = false
   clockskew = 120
   ticket_lifetime = 24h
   renew_lifetime = 7d
   forwardable = true
   renewable = true
   udp_preference_limit = 1
   default_tgs_enctypes = arcfour-hmac
   default_tkt_enctypes = arcfour-hmac
 
  [realms]
   JAVACHEN.COM = {
    kdc = cdh1:88
    admin_server = cdh1:749
   }
 
  [domain_realm]
    .javachen.com = JAVACHEN.COM
    www.javachen.com = JAVACHEN.COM
 
  [kdc]
  profile=/var/kerberos/krb5kdc/kdc.conf
说明：

[logging]：表示 server 端的日志的打印位置
[libdefaults]：每种连接的默认配置，需要注意以下几个关键的小配置
default_realm = JAVACHEN.COM：设置 Kerberos 应用程序的默认领域。如果您有多个领域，只需向 [realms] 节添加其他的语句。
udp_preference_limit= 1：禁止使用 udp 可以防止一个Hadoop中的错误
clockskew：时钟偏差是不完全符合主机系统时钟的票据时戳的容差，超过此容差将不接受此票据。通常，将时钟扭斜设置为 300 秒（5 分钟）。这意味着从服务器的角度看，票证的时间戳与它的偏差可以是在前后 5 分钟内。
ticket_lifetime： 表明凭证生效的时限，一般为24小时。
renew_lifetime： 表明凭证最长可以被延期的时限，一般为一个礼拜。当凭证过期之后，对安全认证的服务的后续访问则会失败。
[realms]：列举使用的 realm。
kdc：代表要 kdc 的位置。格式是 
机器:端口
admin_server：代表 admin 的位置。格式是 
机器:端口
default_domain：代表默认的域名
[appdefaults]：可以设定一些针对特定应用的配置，覆盖默认配置。
修改 
/var/kerberos/krb5kdc/kdc.conf ，该文件包含 Kerberos 的配置信息。例如，KDC 的位置，Kerbero 的 admin 的realms 等。需要所有使用的 Kerberos 的机器上的配置文件都同步。这里仅列举需要的基本配置。详细介绍参考：krb5conf

$ cat /var/kerberos/krb5kdc/kdc.conf
[kdcdefaults]
 v4_mode = nopreauth
 kdc_ports = 88
 kdc_tcp_ports = 88
 
[realms]
 JAVACHEN.COM = {
  #master_key_type = aes256-cts
  acl_file = /var/kerberos/krb5kdc/kadm5.acl
  dict_file = /usr/share/dict/words
  admin_keytab = /var/kerberos/krb5kdc/kadm5.keytab
  supported_enctypes =  des3-hmac-sha1:normal arcfour-hmac:normal des-hmac-sha1:normal des-cbc-md5:normal des-cbc-crc:normal des-cbc-crc:v4 des-cbc-crc:afs3
  max_life = 24h
  max_renewable_life = 10d
  default_principal_flags = +renewable, +forwardable
 }
说明：

JAVACHEN.COM： 是设定的 realms。名字随意。Kerberos 可以支持多个 realms，会增加复杂度。大小写敏感，一般为了识别使用全部大写。这个 realms 跟机器的 host 没有大关系。
master_key_type：和 
supported_enctypes 默认使用 
aes256-cts。由于，JAVA 使用 
aes256-cts 验证方式需要安装额外的 jar 包（后面再做说明）。推荐不使用，并且删除 aes256-cts。
acl_file：标注了 admin 的用户权限，需要用户自己创建。文件格式是：
Kerberos_principal permissions [target_principal] [restrictions]
supported_enctypes：支持的校验方式。
admin_keytab：KDC 进行校验的 keytab。
关于AES-256加密：

对于使用 centos5. 6及以上的系统，默认使用 AES-256 来加密的。这就需要集群中的所有节点上安装 Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy File。

下载的文件是一个 zip 包，解开后，将里面的两个文件放到下面的目录中：
$JAVA_HOME/jre/lib/security
为了能够不直接访问 KDC 控制台而从 Kerberos 数据库添加和删除主体，请对 Kerberos 管理服务器指示允许哪些主体执行哪些操作。通过编辑文件 /var/lib/kerberos/krb5kdc/kadm5.acl 完成此操作。ACL（访问控制列表）允许您精确指定特权。

$ cat /var/kerberos/krb5kdc/kadm5.acl
  */admin@JAVACHEN.COM *
3.2.4 同步配置文件

将 kdc 中的 
/etc/krb5.conf 拷贝到集群中其他服务器即可。

$ scp /etc/krb5.conf cdh2:/etc/krb5.conf
$ scp /etc/krb5.conf cdh3:/etc/krb5.conf
请确认集群如果关闭了 selinux。

3.2.5 创建数据库

在 cdh1 上运行初始化数据库命令。其中 
-r 指定对应 realm。

$ kdb5_util create -r JAVACHEN.COM -s
出现 
Loading random data 的时候另开个终端执行点消耗CPU的命令如 
cat /dev/sda > /dev/urandom 可以加快随机数采集。

该命令会在 
/var/kerberos/krb5kdc/ 目录下创建 principal 数据库。

如果遇到数据库已经存在的提示，可以把 
/var/kerberos/krb5kdc/ 目录下的 principal 的相关文件都删除掉。默认的数据库名字都是 principal。可以使用 
-d 指定数据库名字。

3.2.6 启动服务

在 cdh1 节点上运行：

$ chkconfig --level 35 krb5kdc on
$ chkconfig --level 35 kadmin on
$ service krb5kdc start
$ service kadmin start
3.2.7 创建 kerberos 管理员

关于 kerberos 的管理，可以使用 
kadmin.local 或 
kadmin，至于使用哪个，取决于账户和访问权限：

如果有访问 kdc 服务器的 root 权限，但是没有 kerberos admin 账户，使用 
kadmin.local
如果没有访问 kdc 服务器的 root 权限，但是用 kerberos admin 账户，使用 
kadmin
在 cdh1 上创建远程管理的管理员：

#手动输入两次密码，这里密码为 root
$ kadmin.local -q "addprinc root/admin"
 
# 也可以不用手动输入密码
$ echo -e "root\nroot" | kadmin.local -q "addprinc root/admin"
系统会提示输入密码，密码不能为空，且需妥善保存。

3.2.8 测试 kerberos

以下内容仅仅是为了测试，你可以直接跳到下部分内容。
查看当前的认证用户：

# 查看principals
$ kadmin: list_principals
 
  # 添加一个新的 principal
  kadmin:  addprinc user1
    WARNING: no policy specified for user1@JAVACHEN.COM; defaulting to no policy
    Enter password for principal "user1@JAVACHEN.COM":
    Re-enter password for principal "user1@JAVACHEN.COM":
    Principal "user1@JAVACHEN.COM" created.
 
  # 删除 principal
  kadmin:  delprinc user1
    Are you sure you want to delete the principal "user1@JAVACHEN.COM"? (yes/no): yes
    Principal "user1@JAVACHEN.COM" deleted.
    Make sure that you have removed this principal from all ACLs before reusing.
 
  kadmin: exit
也可以直接通过下面的命令来执行：

# 提示需要输入密码
$ kadmin -p root/admin -q "list_principals"
$ kadmin -p root/admin -q "addprinc user2"
$ kadmin -p root/admin -q "delprinc user2"
 
# 不用输入密码
$ kadmin.local -q "list_principals"
$ kadmin.local -q "addprinc user2"
$ kadmin.local -q "delprinc user2"
创建一个测试用户 test，密码设置为 test：

$ echo -e "test\ntest" | kadmin.local -q "addprinc test"
获取 test 用户的 ticket：

# 通过用户名和密码进行登录
$ kinit test
Password for test@JAVACHEN.COM:
 
$ klist  -e
Ticket cache: FILE:/tmp/krb5cc_0
Default principal: test@JAVACHEN.COM
 
Valid starting     Expires            Service principal
11/07/14 15:29:02  11/08/14 15:29:02  krbtgt/JAVACHEN.COM@JAVACHEN.COM
  renew until 11/17/14 15:29:02, Etype (skey, tkt): AES-128 CTS mode with 96-bit SHA-1 HMAC, AES-128 CTS mode with 96-bit SHA-1 HMAC
 
 
Kerberos 4 ticket cache: /tmp/tkt0
klist: You have no tickets cached
销毁该 test 用户的 ticket：

$ kdestroy
 
$ klist
klist: No credentials cache found (ticket cache FILE:/tmp/krb5cc_0)
 
 
Kerberos 4 ticket cache: /tmp/tkt0
klist: You have no tickets cached
更新 ticket：

$ kinit root/admin
  Password for root/admin@JAVACHEN.COM:
 
$  klist
  Ticket cache: FILE:/tmp/krb5cc_0
  Default principal: root/admin@JAVACHEN.COM
 
  Valid starting     Expires            Service principal
  11/07/14 15:33:57  11/08/14 15:33:57  krbtgt/JAVACHEN.COM@JAVACHEN.COM
    renew until 11/17/14 15:33:57
 
  Kerberos 4 ticket cache: /tmp/tkt0
  klist: You have no tickets cached
 
$ kinit -R
 
$ klist
  Ticket cache: FILE:/tmp/krb5cc_0
  Default principal: root/admin@JAVACHEN.COM
 
  Valid starting     Expires            Service principal
  11/07/14 15:34:05  11/08/14 15:34:05  krbtgt/JAVACHEN.COM@JAVACHEN.COM
    renew until 11/17/14 15:33:57
 
  Kerberos 4 ticket cache: /tmp/tkt0
  klist: You have no tickets cached
抽取密钥并将其储存在本地 keytab 文件 /etc/krb5.keytab 中。这个文件由超级用户拥有，所以您必须是 root 用户才能在 kadmin shell 中执行以下命令：

$ kadmin.local -q "ktadd kadmin/admin"
 
$ klist -k /etc/krb5.keytab
  Keytab name: FILE:/etc/krb5.keytab
  KVNO Principal
  ---- --------------------------------------------------------------------------
     3 kadmin/admin@LASHOU-INC.COM
     3 kadmin/admin@LASHOU-INC.COM
     3 kadmin/admin@LASHOU-INC.COM
     3 kadmin/admin@LASHOU-INC.COM
     3 kadmin/admin@LASHOU-INC.COM
4. HDFS 上配置 kerberos
4.1 创建认证规则
在 Kerberos 安全机制里，一个 principal 就是 realm 里的一个对象，一个 principal 总是和一个密钥（secret key）成对出现的。

这个 principal 的对应物可以是 service，可以是 host，也可以是 user，对于 Kerberos 来说，都没有区别。

Kdc(Key distribute center) 知道所有 principal 的 secret key，但每个 principal 对应的对象只知道自己的那个 secret key 。这也是“共享密钥“的由来。

对于 hadoop，principals 的格式为 
username/fully.qualified.domain.name@YOUR-REALM.COM。

通过 yum 源安装的 cdh 集群中，NameNode 和 DataNode 是通过 hdfs 启动的，故为集群中每个服务器节点添加两个principals：hdfs、HTTP。

在 KCD server 上（这里是 cdh1）创建 hdfs principal：

kadmin.local -q "addprinc -randkey hdfs/cdh1@JAVACHEN.COM"
kadmin.local -q "addprinc -randkey hdfs/cdh2@JAVACHEN.COM"
kadmin.local -q "addprinc -randkey hdfs/cdh3@JAVACHEN.COM"
-randkey 标志没有为新 principal 设置密码，而是指示 kadmin 生成一个随机密钥。之所以在这里使用这个标志，是因为此 principal 不需要用户交互。它是计算机的一个服务器帐户。

创建 HTTP principal：

kadmin.local -q "addprinc -randkey HTTP/cdh1@JAVACHEN.COM"
kadmin.local -q "addprinc -randkey HTTP/cdh2@JAVACHEN.COM"
kadmin.local -q "addprinc -randkey HTTP/cdh3@JAVACHEN.COM"
创建完成后，查看：

$ kadmin.local -q "listprincs"
4.2 创建keytab文件
keytab 是包含 principals 和加密 principal key 的文件。

keytab 文件对于每个 host 是唯一的，因为 key 中包含 hostname。keytab 文件用于不需要人工交互和保存纯文本密码，实现到 kerberos 上验证一个主机上的 principal。

因为服务器上可以访问 keytab 文件即可以以 principal 的身份通过 kerberos 的认证，所以，keytab 文件应该被妥善保存，应该只有少数的用户可以访问

创建包含 hdfs principal 和 host principal 的 hdfs keytab：

xst -norandkey -k hdfs.keytab hdfs/fully.qualified.domain.name host/fully.qualified.domain.name
创建包含 mapred principal 和 host principal 的 mapred keytab：

xst -norandkey -k mapred.keytab mapred/fully.qualified.domain.name host/fully.qualified.domain.name
注意：
上面的方法使用了xst的norandkey参数，有些kerberos不支持该参数。
当不支持该参数时有这样的提示：
Principal -norandkey does not exist.，需要使用下面的方法来生成keytab文件。
在 cdh1 节点，即 KDC server 节点上执行下面命令：

$ cd /var/kerberos/krb5kdc/
 
$ kadmin.local -q "xst  -k hdfs-unmerged.keytab  hdfs/cdh1@JAVACHEN.COM"
$ kadmin.local -q "xst  -k hdfs-unmerged.keytab  hdfs/cdh2@JAVACHEN.COM"
$ kadmin.local -q "xst  -k hdfs-unmerged.keytab  hdfs/cdh3@JAVACHEN.COM"
 
$ kadmin.local -q "xst  -k HTTP.keytab  HTTP/cdh1@JAVACHEN.COM"
$ kadmin.local -q "xst  -k HTTP.keytab  HTTP/cdh2@JAVACHEN.COM"
$ kadmin.local -q "xst  -k HTTP.keytab  HTTP/cdh3@JAVACHEN.COM"
这样，就会在 
/var/kerberos/krb5kdc/ 目录下生成 
hdfs-unmerged.keytab 和 
HTTP.keytab 两个文件，接下来使用 
ktutil 合并者两个文件为 
hdfs.keytab。

$ cd /var/kerberos/krb5kdc/
 
$ ktutil
ktutil: rkt hdfs-unmerged.keytab
ktutil: rkt HTTP.keytab
ktutil: wkt hdfs.keytab
使用 klist 显示 hdfs.keytab 文件列表：

$ klist -ket  hdfs.keytab
Keytab name: FILE:hdfs.keytab
KVNO Timestamp         Principal
---- ----------------- --------------------------------------------------------
   2 11/13/14 10:40:18 hdfs/cdh1@JAVACHEN.COM (des3-cbc-sha1)
   2 11/13/14 10:40:18 hdfs/cdh1@JAVACHEN.COM (arcfour-hmac)
   2 11/13/14 10:40:18 hdfs/cdh1@JAVACHEN.COM (des-hmac-sha1)
   2 11/13/14 10:40:18 hdfs/cdh1@JAVACHEN.COM (des-cbc-md5)
   4 11/13/14 10:40:18 hdfs/cdh2@JAVACHEN.COM (des3-cbc-sha1)
   4 11/13/14 10:40:18 hdfs/cdh2@JAVACHEN.COM (arcfour-hmac)
   4 11/13/14 10:40:18 hdfs/cdh2@JAVACHEN.COM (des-hmac-sha1)
   4 11/13/14 10:40:18 hdfs/cdh2@JAVACHEN.COM (des-cbc-md5)
   4 11/13/14 10:40:18 hdfs/cdh3@JAVACHEN.COM (des3-cbc-sha1)
   4 11/13/14 10:40:18 hdfs/cdh3@JAVACHEN.COM (arcfour-hmac)
   4 11/13/14 10:40:18 hdfs/cdh3@JAVACHEN.COM (des-hmac-sha1)
   4 11/13/14 10:40:18 hdfs/cdh3@JAVACHEN.COM (des-cbc-md5)
   3 11/13/14 10:40:18 HTTP/cdh1@JAVACHEN.COM (des3-cbc-sha1)
   3 11/13/14 10:40:18 HTTP/cdh1@JAVACHEN.COM (arcfour-hmac)
   3 11/13/14 10:40:18 HTTP/cdh1@JAVACHEN.COM (des-hmac-sha1)
   3 11/13/14 10:40:18 HTTP/cdh1@JAVACHEN.COM (des-cbc-md5)
   3 11/13/14 10:40:18 HTTP/cdh2@JAVACHEN.COM (des3-cbc-sha1)
   3 11/13/14 10:40:18 HTTP/cdh2@JAVACHEN.COM (arcfour-hmac)
   3 11/13/14 10:40:18 HTTP/cdh2@JAVACHEN.COM (des-hmac-sha1)
   3 11/13/14 10:40:18 HTTP/cdh2@JAVACHEN.COM (des-cbc-md5)
   3 11/13/14 10:40:18 HTTP/cdh3@JAVACHEN.COM (des3-cbc-sha1)
   3 11/13/14 10:40:18 HTTP/cdh3@JAVACHEN.COM (arcfour-hmac)
   3 11/13/14 10:40:18 HTTP/cdh3@JAVACHEN.COM (des-hmac-sha1)
   3 11/13/14 10:40:18 HTTP/cdh3@JAVACHEN.COM (des-cbc-md5)
验证是否正确合并了key，使用合并后的keytab，分别使用hdfs和host principals来获取证书。

$ kinit -k -t hdfs.keytab hdfs/cdh1@JAVACHEN.COM
$ kinit -k -t hdfs.keytab HTTP/cdh1@JAVACHEN.COM
如果出现错误：
kinit: Key table entry not found while getting initial credentials，
则上面的合并有问题，重新执行前面的操作。

4.3 部署kerberos keytab文件
拷贝 hdfs.keytab 文件到其他节点的 /etc/hadoop/conf 目录

$ cd /var/kerberos/krb5kdc/
 
$ scp hdfs.keytab cdh1:/etc/hadoop/conf
$ scp hdfs.keytab cdh2:/etc/hadoop/conf
$ scp hdfs.keytab cdh3:/etc/hadoop/conf
并设置权限，分别在 cdh1、cdh2、cdh3 上执行：

$ ssh cdh1 "chown hdfs:hadoop /etc/hadoop/conf/hdfs.keytab ;chmod 400 /etc/hadoop/conf/hdfs.keytab"
$ ssh cdh2 "chown hdfs:hadoop /etc/hadoop/conf/hdfs.keytab ;chmod 400 /etc/hadoop/conf/hdfs.keytab"
$ ssh cdh3 "chown hdfs:hadoop /etc/hadoop/conf/hdfs.keytab ;chmod 400 /etc/hadoop/conf/hdfs.keytab"
由于 keytab 相当于有了永久凭证，不需要提供密码(如果修改kdc中的principal的密码，则该keytab就会失效)，所以其他用户如果对该文件有读权限，就可以冒充 keytab 中指定的用户身份访问 hadoop，所以 keytab 文件需要确保只对 owner 有读权限(0400)

4.4 修改 hdfs 配置文件
先停止集群：

$ for x in `cd /etc/init.d ; ls hive-*` ; do sudo service $x stop ; done
$ for x in `cd /etc/init.d ; ls impala-*` ; do sudo service $x stop ; done
$ for x in `cd /etc/init.d ; ls hadoop-*` ; do sudo service $x stop ; done
$ for x in `cd /etc/init.d ; ls zookeeper-*` ; do sudo service $x stop ; done
在集群中所有节点的 core-site.xml 文件中添加下面的配置:

<property>
  <name>hadoop.security.authentication</name>
  <value>kerberos</value>
</property>
 
<property>
  <name>hadoop.security.authorization</name>
  <value>true</value>
</property>
在集群中所有节点的 hdfs-site.xml 文件中添加下面的配置：

<property>
  <name>dfs.block.access.token.enable</name>
  <value>true</value>
</property>
<property>  
  <name>dfs.datanode.data.dir.perm</name>  
  <value>700</value>  
</property>
<property>
  <name>dfs.namenode.keytab.file</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.namenode.kerberos.principal</name>
  <value>hdfs/_HOST@JAVACHEN.COM</value>
</property>
<property>
  <name>dfs.namenode.kerberos.https.principal</name>
  <value>HTTP/_HOST@JAVACHEN.COM</value>
</property>
<property>
  <name>dfs.datanode.address</name>
  <value>0.0.0.0:1004</value>
</property>
<property>
  <name>dfs.datanode.http.address</name>
  <value>0.0.0.0:1006</value>
</property>
<property>
  <name>dfs.datanode.keytab.file</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.datanode.kerberos.principal</name>
  <value>hdfs/_HOST@JAVACHEN.COM</value>
</property>
<property>
  <name>dfs.datanode.kerberos.https.principal</name>
  <value>HTTP/_HOST@JAVACHEN.COM</value>
</property>
如果想开启 SSL，请添加（本文不对这部分做说明）：

<property>
  <name>dfs.http.policy</name>
  <value>HTTPS_ONLY</value>
</property>
如果 HDFS 配置了 QJM HA，则需要添加（另外，你还要在 zookeeper 上配置 kerberos）：

<property>
  <name>dfs.journalnode.keytab.file</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.journalnode.kerberos.principal</name>
  <value>hdfs/_HOST@JAVACHEN.COM</value>
</property>
<property>
  <name>dfs.journalnode.kerberos.internal.spnego.principal</name>
  <value>HTTP/_HOST@JAVACHEN.COM</value>
</property>
如果配置了 WebHDFS，则添加：

<property>
  <name>dfs.webhdfs.enabled</name>
  <value>true</value>
</property>
 
<property>
  <name>dfs.web.authentication.kerberos.principal</name>
  <value>HTTP/_HOST@JAVACHEN.COM</value>
</property>
 
<property>
  <name>dfs.web.authentication.kerberos.keytab</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
配置中有几点要注意的：

1. 
dfs.datanode.address表示 data transceiver RPC server 所绑定的 hostname 或 IP 地址，如果开启 security，端口号必须小于 
1024(privileged port)，否则的话启动 datanode 时候会报 
Cannot start secure cluster without privileged resources 错误
2. principal 中的 instance 部分可以使用 
_HOST 标记，系统会自动替换它为全称域名
3. 如果开启了 security, hadoop 会对 hdfs block data(由 
dfs.data.dir 指定)做 permission check，方式用户的代码不是调用hdfs api而是直接本地读block data，这样就绕过了kerberos和文件权限验证，管理员可以通过设置 
dfs.datanode.data.dir.perm 来修改 datanode 文件权限，这里我们设置为700
4.5 检查集群上的 HDFS 和本地文件的权限
请参考 Verify User Accounts and Groups in CDH 5 Due to Security 或者 Hadoop in Secure Mode。

4.6 启动 NameNode
启动之前，请确认 JCE jar 已经替换，请参考前面的说明。

在每个节点上获取 root 用户的 ticket，这里 root 为之前创建的 root/admin 的密码。

$ ssh cdh1 "echo root|kinit root/admin"
$ ssh cdh1 "echo root|kinit root/admin"
$ ssh cdh1 "echo root|kinit root/admin"
获取 cdh1的 ticket：

$ kinit -k -t /etc/hadoop/conf/hdfs.keytab hdfs/cdh1@JAVACHEN.COM
如果出现下面异常 
kinit: Password incorrect while getting initial credentials
，则重新导出 keytab 再试试。

然后启动服务，观察日志：

$ /etc/init.d/hadoop-hdfs-namenode start
验证 NameNode 是否启动，一是打开 web 界面查看启动状态，一是运行下面命令查看 hdfs：

$ hadoop fs -ls /
Found 4 items
drwxrwxrwx   - yarn hadoop          0 2014-06-26 15:24 /logroot
drwxrwxrwt   - hdfs hadoop          0 2014-11-04 10:44 /tmp
drwxr-xr-x   - hdfs hadoop          0 2014-08-10 10:53 /user
drwxr-xr-x   - hdfs hadoop          0 2013-05-20 22:52 /var
如果在你的凭据缓存中没有有效的 kerberos ticket，执行上面命令将会失败，将会出现下面的错误：

14/11/04 12:08:12 WARN ipc.Client: Exception encountered while connecting to the server : javax.security.sasl.SaslException:
GSS initiate failed [Caused by GS***ception: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
Bad connection to FS. command aborted. exception: Call to cdh1/192.168.56.121:8020 failed on local exception: java.io.IOException:
javax.security.sasl.SaslException: GSS initiate failed [Caused by GS***ception: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
4.6 启动DataNode
DataNode 需要通过 JSVC 启动。首先检查是否安装了 JSVC 命令，然后配置环境变量。

在 cdh1 节点查看是否安装了 JSVC：

$ ls /usr/lib/bigtop-utils/
bigtop-detect-classpath  bigtop-detect-javahome  bigtop-detect-javalibs  jsvc
然后编辑 
/etc/default/hadoop-hdfs-datanode，取消对下面的注释并添加一行设置 
JSVC_HOME，修改如下：

export HADOOP_SECURE_DN_USER=hdfs
export HADOOP_SECURE_DN_PID_DIR=/var/run/hadoop-hdfs
export HADOOP_SECURE_DN_LOG_DIR=/var/log/hadoop-hdfs
 
export JSVC_HOME=/usr/lib/bigtop-utils
将该文件同步到其他节点：

$ scp /etc/default/hadoop-hdfs-datanode cdh2:/etc/default/hadoop-hdfs-datanode
$ scp /etc/default/hadoop-hdfs-datanode cdh3:/etc/default/hadoop-hdfs-datanode
分别在 cdh2、cdh3 获取 ticket 然后启动服务：

#root 为 root/admin 的密码
$ ssh cdh1 "kinit -k -t /etc/hadoop/conf/hdfs.keytab hdfs/cdh1@JAVACHEN.COM; service hadoop-hdfs-datanode start"
$ ssh cdh2 "kinit -k -t /etc/hadoop/conf/hdfs.keytab hdfs/cdh2@JAVACHEN.COM; service hadoop-hdfs-datanode start"
$ ssh cdh3 "kinit -k -t /etc/hadoop/conf/hdfs.keytab hdfs/cdh3@JAVACHEN.COM; service hadoop-hdfs-datanode start"
观看 cdh1 上 NameNode 日志，出现下面日志表示 DataNode 启动成功：

14/11/04 17:21:41 INFO security.UserGroupInformation:
Login successful for user hdfs/cdh2@JAVACHEN.COM using keytab file /etc/hadoop/conf/hdfs.keytab
5. 其他
5.1 批量生成 keytab
为了方便批量生成 keytab，写了一个脚本，如下：

#!/bin/bash
 
DNS=LASHOU.COM
hostname=`hostname -i`
 
yum install krb5-server krb5-libs krb5-auth-dialog krb5-workstation  -y
rm -rf /var/kerberos/krb5kdc/*.keytab /var/kerberos/krb5kdc/prin*
 
kdb5_util create -r LASHOU.COM -s
 
chkconfig --level 35 krb5kdc on
chkconfig --level 35 kadmin on
service krb5kdc restart
service kadmin restart
 
echo -e "root\nroot" | kadmin.local -q "addprinc root/admin"
 
for host in  `cat /etc/hosts|grep 10|grep -v $hostname|awk '{print $2}'` ;do
  for user in hdfs hive; do
    kadmin.local -q "addprinc -randkey $user/$host@$DNS"
    kadmin.local -q "xst -k /var/kerberos/krb5kdc/$user-un.keytab $user/$host@$DNS"
  done
  for user in HTTP lashou yarn mapred impala zookeeper sentry llama zkcli ; do
    kadmin.local -q "addprinc -randkey $user/$host@$DNS"
    kadmin.local -q "xst -k /var/kerberos/krb5kdc/$user.keytab $user/$host@$DNS"
  done
done
 
cd /var/kerberos/krb5kdc/
echo -e "rkt lashou.keytab\nrkt hdfs-un.keytab\nrkt HTTP.keytab\nwkt hdfs.keytab" | ktutil
echo -e "rkt lashou.keytab\nrkt hive-un.keytab\nwkt hive.keytab" | ktutil
 
#kerberos 重新初始化之后，还需要添加下面代码用于集成 ldap
 
kadmin.local -q "addprinc ldapadmin@JAVACHEN.COM"
kadmin.local -q "addprinc -randkey ldap/cdh1@JAVACHEN.COM"
kadmin.local -q "ktadd -k /etc/openldap/ldap.keytab ldap/cdh1@JAVACHEN.COM"
 
#如果安装了 openldap ，重启 slapd
/etc/init.d/slapd restart
 
#测试 ldap 是否可以正常使用
ldapsearch -x -b 'dc=javachen,dc=com'
以下脚本用于在每个客户端上获得 root/admin 的 ticket，其密码为 root：

#!/bin/sh
 
for node in 56.121 56.122 56.123 ;do
  echo "========10.168.$node========"
  ssh 192.168.$node 'echo root|kinit root/admin'
done
5.2 管理集群脚本
另外，为了方便管理集群，在 cdh1 上创建一个 shell 脚本用于批量管理集群，脚本如下（保存为 
manager_cluster.sh）：

#!/bin/bash
 
role=$1
command=$2
 
dir=$role
 
if [ X"$role" == X"hdfs" ];then
  dir=hadoop
fi
 
if [ X"$role" == X"yarn" ];then
        dir=hadoop
fi
 
if [ X"$role" == X"mapred" ];then
        dir=hadoop
fi
 
for node in 56.121 56.122 56.123 ;do
  echo "========192.168.$node========"
  ssh 192.168.$node '
    #先获取 root/admin 的凭证
    echo root|kinit root/admin
    host=`hostname -f| tr "[:upper:]" "[:lower:]"`
    path="'$role'/$host"
    #echo $path
    principal=`klist -k /etc/'$dir'/conf/'$role'.keytab | grep $path | head -n1 | cut -d " " -f5`
    #echo $principal
    if [ X"$principal" == X ]; then
      principal=`klist -k /etc/'$dir'/conf/'$role'.keytab | grep $path | head -n1 | cut -d " " -f4`
      if [ X"$principal" == X ]; then
            echo "Failed to get hdfs Kerberos principal"
            exit 1
      fi
    fi
    kinit -r 24l -kt /etc/'$dir'/conf/'$role'.keytab $principal
    if [ $? -ne 0 ]; then
        echo "Failed to login as hdfs by kinit command"
        exit 1
    fi
    kinit -R
    for src in `ls /etc/init.d|grep '$role'`;do service $src '$command'; done
  '
done
使用方法为：

$ sh manager_cluster.sh hdfs start #启动 hdfs 用户管理的服务
$ sh manager_cluster.sh yarn start #启动 yarn 用户管理的服务
$ sh manager_cluster.sh mapred start #启动 mapred 用户管理的服务
 
$ sh manager_cluster.sh hdfs status # 在每个节点上获取 hdfs 的 ticket，然后可以执行其他操作，如批量启动 datanode 等等
5.3 使用 java 代码测试 kerberos
在 hdfs 中集成 kerberos 之前，可以先使用下面代码(Krb.java)进行测试：

import com.sun.security.auth.module.Krb5LoginModule;
 
import javax.security.auth.Subject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
 
public class Krb {
    private void loginImpl(final String propertiesFileName) throws Exception {
        System.out.println("NB: system property to specify the krb5 config: [java.security.krb5.conf]");
        //System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
 
        System.out.println(System.getProperty("java.version"));
 
        System.setProperty("sun.security.krb5.debug", "true");
 
        final Subject subject = new Subject();
 
        final Krb5LoginModule krb5LoginModule = new Krb5LoginModule();
        final Map<String,String> optionMap = new HashMap<String,String>();
 
        if (propertiesFileName == null) {
            //optionMap.put("ticketCache", "/tmp/krb5cc_1000");
            optionMap.put("keyTab", "/etc/krb5.keytab");
            optionMap.put("principal", "foo"); // default realm
 
            optionMap.put("doNotPrompt", "true");
            optionMap.put("refreshKrb5Config", "true");
            optionMap.put("useTicketCache", "true");
            optionMap.put("renewTGT", "true");
            optionMap.put("useKeyTab", "true");
            optionMap.put("storeKey", "true");
            optionMap.put("isInitiator", "true");
        } else {
            File f = new File(propertiesFileName);
            System.out.println("======= loading property file ["+f.getAbsolutePath()+"]");
            Properties p = new Properties();
            InputStream is = new FileInputStream(f);
            try {
                p.load(is);
            } finally {
                is.close();
            }
            optionMap.putAll((Map)p);
        }
        optionMap.put("debug", "true"); // switch on debug of the Java implementation
 
        krb5LoginModule.initialize(subject, null, new HashMap<String,String>(), optionMap);
 
        boolean loginOk = krb5LoginModule.login();
        System.out.println("======= login:  " + loginOk);
 
        boolean commitOk = krb5LoginModule.commit();
        System.out.println("======= commit: " + commitOk);
        System.out.println("======= Subject: " + subject);
    }
 
    public static void main(String[] args) throws Exception {
        System.out.println("A property file with the login context can be specified as the 1st and the only paramater.");
        final Krb krb = new Krb();
        krb.loginImpl(args.length == 0 ? null : args[0]);
    }
}
创建一个配置文件krb5.properties：

keyTab=/etc/hadoop/conf/hdfs.keytab
principal=hdfs/cdh1@JAVACHEN.COM
 
doNotPrompt=true
refreshKrb5Config=true
useTicketCache=true
renewTGT=true
useKeyTab=true
storeKey=true
isInitiator=true
编译 java 代码并运行：

# 先销毁当前 ticket
 
$ kdestroy
 
$ javac Krb.java
 
$ java -cp . Krb ./krb5.properties
6. 总结
本文介绍了 CDH Hadoop 集成 kerberos 认证的过程，其中主要需要注意以下几点：

1. 配置 hosts，
hostname 请使用小写。
2. 确保 kerberos 客户端和服务端连通
3. 替换 JRE 自带的 JCE jar 包
4. 为 DataNode 设置运行用户并配置 
JSVC_HOME
5. 启动服务前，先获取 ticket 再运行相关命令
接下来就是配置 Hadoop 集群中其他服务以集成 kerberos 认证，由于篇幅原因，后面再做说明。

7. 参考文章
Hadoop的kerberos的实践部署
hadoop 添加kerberos认证
YARN & HDFS2 安装和配置Kerberos
Kerberos basics and installing a KDC
Hadoop, Hbase, Zookeeper安全实践
内容更新于： 2015-01-30 22:02:47 
链接地址： http://junjiem.leanote.com/post/Untitled-532460444fadc07bea00002e-7