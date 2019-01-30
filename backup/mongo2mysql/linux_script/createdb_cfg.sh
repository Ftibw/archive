basepath=$(cd `dirname $0`;pwd)
. $basepath/../utils

replace_content $basepath createdb.sql \
"EXISTS .* default" \
"EXISTS "$1" default"