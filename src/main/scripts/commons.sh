#!/usr/bin/env bash
########recode history
#/etc/profile
mkdir /usr/local/script
/usr/bin/script -qa /usr/local/script/log_record_script_$(date +%Y_%m_%d)
export HISTTIMEFORMAT="%Y-%m-%d:%H-%M-%S:`whoami`:"


#~/.bash_logout

history   >/usr/local/script/hist$(date +%Y%m%d%H%M%S)





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



#!/bin/sh

for node in 56.121 56.122 56.123 ;do
  echo "========10.168.$node========"
  ssh 192.168.$node 'echo root|kinit root/admin'
done





