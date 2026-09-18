#!/bin/bash

# set -e

source define_colors.sh
source _confirm.sh

# P A R A M S

# stage
from="$1"
shift

# tag
tag="$1"
shift
if [ -z "${tag}" ]; then
	echo -e "${R}No tag${Z}"
	exit 1
fi

# release params
RELEASE_NAME="OEWN_Server-${tag}"
RELEASE_TITLE="OEWN JSON Server ${tag}"
RELEASE_NOTES="version ${tag}"

# assets
assets="
target/oewn-server-${tag}-uber.jar
run_server.sh
run_server_yaml.sh
run_server.bat
run_server_yaml.bat
run_curl_lemma.sh
run_curl_lex.sh
run_curl_sense.sh
run_curl_synset.sh
"

# M A I N

echo -e "${Y}Make GitHub release${Z}"
echo -e "Github assets:
${C}${assets}${Z}"
if confirm 'Github' "Proceed $from with release ${RELEASE_NAME}?" 'proceeding...'; then

case "$from" in
       initial) echo -e "${bY}${K}initial${Z}"
                ;&
       auth) echo -e "${bY}${K}auth${Z}"
                gh auth status
                #gh auth logout
                #gh auth login
                ;&

       create) echo -e "${bY}${K}create${Z}"
                gh release create "${RELEASE_NAME}" --title "${RELEASE_TITLE}" --notes "${RELEASE_NOTES}"
                ;&

       upload) echo -e "${bY}${K}upload${Z}"
                gh release upload "${RELEASE_NAME}" ${assets}
                ;&
                
       list) echo -e "${bY}${K}list${Z}"
                gh release list
                ;&
               
       view) echo -e "${bY}${K}view${Z}"
                gh release view "${RELEASE_NAME}"
                ;&
                
       end) echo -e "${bY}${K}end${Z}"
                ;;
                
       flush) echo -e "${bY}${K}end${Z}"
                for a in ${assets}; do
                  an=$(basename ${a})
                  echo "delete ${an}"
                  gh release delete-asset "${RELEASE_NAME}" ${an}
                done
                ;;

       logged) echo -e "${bY}${K}logged${Z}"
                gh auth status
                ;;

       logout) echo -e "${bY}${K}logout${Z}"
                gh auth logout
                ;;

       login) echo -e "${bY}${K}login${Z}"
                gh auth login
                ;;
esac

fi
