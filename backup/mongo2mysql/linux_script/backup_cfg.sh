basepath=$(cd `dirname $0`;pwd)
. $basepath/../utils

replace_content $basepath backup.sql \
"EXISTS .* default" \
"EXISTS "$1" default"

replace_content $basepath backup.sql \
"use .*;" \
"use "$1";"

replace_content $basepath backup.sql \
"infile '.*'" \
"infile '"$2"'"

replace_content $basepath backup.sql \
"table \`.*\`" \
"table \`"$3"\`"