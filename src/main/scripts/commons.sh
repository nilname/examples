########recode history
#/etc/profile
mkdir /usr/local/script
/usr/bin/script -qa /usr/local/script/log_record_script_$(date +%Y_%m_%d)
export HISTTIMEFORMAT="%Y-%m-%d:%H-%M-%S:`whoami`:"


#~/.bash_logout

history   >/usr/local/script/hist$(date +%Y%m%d%H%M%S)



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

//////////////////

以下脚本用于在每个客户端上获得 root/admin 的 ticket，其密码为 root：

#!/bin/sh

for node in 56.121 56.122 56.123 ;do
  echo "========10.168.$node========"
  ssh 192.168.$node 'echo root|kinit root/admin'
done


///////////////


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