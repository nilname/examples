#!/bin/bash
execute(){
    url=$1@$2
    shift
    shift
    ssh $url $*
}
rscp(){
    src=$1
    shift
    url=$1@$2
    shift
    shift
    scp $src $url:$*
}

help(){
    echo "execute commands in remote host:"
    echo "  bash -x utils.sh  exec  root ls  /home"
    echo "copy file to remote host"
    echo "  bash -x utils.sh  cp  slaves root  /home"

}
FILENAME=$(basename $0)
RALETIVEDIR=$(dirname $0)
ABSPATH=$(pwd)

if [ ${RALETIVEDIR:0:1} == "/" ];then
    WORKDIR=${RALETIVEDIR}
else
    WORKDIR=${ABSPATH}
fi

cd ${WORKDIR}

if [  $# -eq 0 ]
then
    help
    exit
fi

option=$1
shift
hosts=$(cat slaves)
if [ $option == "exec" ];then
    user=$1
    shift
    cmd=$@
    for host in $hosts
    do
    #echo $host
    execute $user $host $cmd
    done

efif [ $option == "cp" ]

    src=$1
    shift
    user=$1
    shift
    cmd=$@
    for host in $hosts
    do
    rscp $src $user $host $cmd
    done

else
    help
fi
