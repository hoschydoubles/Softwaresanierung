#!/bin/bash

# for OSX
function my_resolve() {
	if [ "${2:0:1}" == "/" ]; then
		printf "%s" "$2"
	elif [ "$2" == "." ]; then
		printf "%s" "$1"
	elif [ "$1" == "." ]; then
		printf "%s" "$2"
	else
		printf "%s" "$1/$2"
	fi
}
function my_readlink() {
	local TMP1=$(dirname -- "$1")
	local TMP2=$(readlink -- "$1")
	my_resolve "$TMP1" "$TMP2"
}
function my_build_cp() {
	local DIR="$1"
	if [ ! -d "$DIR" ]; then
		echo "Internal error. The following directory does not exist:" 1>&2
		echo "  \"$DIR\""1>&2
		return;
	fi

	local TMP_CP=$(find "$DIR" -name "*.jar" -print0 | tr "\0" "$SEP")
	# remove last $SEP 
	printf "%s" "${TMP_CP%?}"
}
function set_java_and_javac() {
	local VAR="$1"
	JAVA="${!VAR}/bin/java"
	JAVAC="${!VAR}/bin/javac"
	
	if [ ! -x "$JAVA" ]; then
		echo "The variable $VAR does not point to the root directory" 1>&2
		echo "of a JRE or JDK. The following file does not exist or" 1>&2
		echo "is not executable:" 1>&2
		echo "  \"$JAVA\"" 1>&2
	elif [ ! -x "$JAVAC" ]; then
		echo "The variable $VAR seems to point to the root directory" 1>&2
		echo "of a JRE. It should point to the root directory of a JDK." 1>&2
		echo "Otherwise, some tools might not work." 1>&2
	fi
}

EV3_COMMAND=$(basename -- "$0")
EV3_HOME="$0"
while [ -L "$EV3_HOME" ]; do
	EV3_HOME=$(my_readlink "$EV3_HOME")
done
EV3_HOME=$(dirname -- "$EV3_HOME")
EV3_HOME=$(my_resolve "$(pwd)" "$EV3_HOME")/..

if [ -n "$LEJOS_EV3_JAVA_HOME" ]; then
	set_java_and_javac LEJOS_EV3_JAVA_HOME
elif [ -n "$JAVA_HOME" ]; then
	set_java_and_javac JAVA_HOME
else
	JAVA="java"
	JAVAC="javac"
fi

SEP=":"
EV3_FORCE32=""

case $(uname -s) in
	CYGWIN*) SEP=";";;
	Darwin) EV3_FORCE32="-d32";;
esac

EV3_CP_PC=$(my_build_cp "$EV3_HOME/lib")
