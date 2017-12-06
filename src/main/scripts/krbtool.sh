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

for node in $(cat slaves) ;do
  echo "========$node========"
  ssh $node
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
done