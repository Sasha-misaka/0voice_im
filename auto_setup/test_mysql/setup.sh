#!/bin/bash
# this is a setup scripts for mysql
# author: luoning
# date: 08/30/2014

# setup mysql

IM_SQL=ttopen.sql
MYSQL_PASSWORD=123456


print_hello(){
	echo "==========================================="
	echo "$1 mysql for TeamTalk"
	echo "==========================================="
}

check_user() {
	if [ $(id -u) != "0" ]; then
    	echo "Error: You must be root to run this script, please use root to install mysql"
    	exit 1
	fi
}

create_database() {
	cd ./conf/
	if [ -f "$IM_SQL" ]; then
		echo "$IM_SQL existed, begin to run $IM_SQL"
	else
		echo "Error: $IM_SQL not existed."
		cd ..
		return 1
	fi

	mysql -u root -p$MYSQL_PASSWORD < $IM_SQL
	if [ $? -eq 0 ]; then
		echo "run sql successed."
		cd ..
	else
		echo "Error: run sql failed."
		cd ..
		return 1
	fi
}

build_all() {
	create_database
	if [ $? -eq 0 ]; then
		echo "create database successed."
	else
		echo "Error: create database failed."
		exit 1
	fi	
}


print_help() {
	echo "Usage: "
	echo "  $0 check --- check environment"
	echo "  $0 install --- check & run scripts to install"
}

case $1 in
	check)
		print_hello $1
		;;
	install)
		print_hello $1
		build_all
		;;
	*)
		print_help
		;;
esac



