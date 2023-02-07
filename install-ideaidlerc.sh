#!/bin/sh

# shellcheck disable=SC2164
SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
cd "$SCRIPTPATH" || exit 1

pullbin() {
    echo "pulling ideaidle"
    curl "https://sg-support-bot-public.s3.amazonaws.com/ideaidle/ideaidle_plugin.zip" > tmp/ideaidle_plugin.zip
    mkdir -p bin
    unzip tmp/ideaidle_plugin.zip
    chmod +x bin/RoslynBin/CompCollector.dll
    echo "You can now install the plugin with install from disk... bin/IdeaIdle-0.0.1.zip"
}

[ "$1" = "--pull" ] && rm -r "bin"

[ -d "bin" ] || pullbin

mydir="$HOME/.ideaidle.d"
mkdir -p "$mydir"
myfile="$mydir/config.edn"


[ "$1" != "-f" ] && [ -e "$myfile" ] && printf "%s already exists.\nUse ./install-ideaidlerc.sh -f to override back to defaults.\n" "$myfile" && exit 1

printf "Writing defaults to %s\n" "$myfile"

cp -f "$SCRIPTPATH/src/main/resources/default_config.edn" "$myfile"
sed --in-place "$myfile" --expression "s|/fix/the/path|$SCRIPTPATH|"
sed --in-place "$myfile" --expression "s|/\([a-z]\)/|\U\1:/|"
