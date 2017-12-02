########recode history
#/etc/profile
mkdir /usr/local/script
/usr/bin/script -qa /usr/local/script/log_record_script_$(date +%Y_%m_%d)
export HISTTIMEFORMAT="%Y-%m-%d:%H-%M-%S:`whoami`:"


#~/.bash_logout

history   >/usr/local/script/hist$(date +%Y%m%d%H%M%S)
