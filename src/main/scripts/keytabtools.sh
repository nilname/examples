#!/bin/bash

DNS=YUTU.COM
hostname=`hostname -i`

#yum install krb5-server krb5-libs krb5-auth-dialog krb5-workstation  -y
#rm -rf /var/kerberos/krb5kdc/*.keytab /var/kerberos/krb5kdc/prin*
#kdb5_util create -r $DNS -s
#chkconfig --level 35 krb5kdc on
#chkconfig --level 35 kadmin on
#service krb5kdc restart
#service kadmin restart
#echo -e "root\nroot" | kadmin.local -q "addprinc root/admin"
#192.168.1.106 yutu001.com yutu001
for host in  `cat /etc/hosts|grep 192|grep -v $hostname|awk '{print $2}'` ;do
  for user in hdfs hive; do
    kadmin.local -q "addprinc -randkey $user/$host@$DNS"
    kadmin.local -q "xst -k /var/kerberos/krb5kdc/$user-un.keytab $user/$host@$DNS"
  done
  for user in HTTP  yarn mapred spark zookeeper  ; do
    kadmin.local -q "addprinc -randkey $user/$host@$DNS"
    kadmin.local -q "xst -k /var/kerberos/krb5kdc/$user.keytab $user/$host@$DNS"
  done
done

cd /var/kerberos/krb5kdc/
echo -e "rkt yutu.keytab\nrkt hdfs-un.keytab\nrkt HTTP.keytab\nwkt hdfs.keytab" | ktutil
echo -e "rkt yutu.keytab\nrkt hive-un.keytab\nwkt hive.keytab" | ktutil