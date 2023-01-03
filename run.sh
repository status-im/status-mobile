#!/bin/bash

#osascript -e 'tell app "Terminal"
#    do script "cd status-dev/status-mobile; make run-clojure"
#end tell'
#
#osascript -e 'tell app "Terminal"
#    do script "cd status-dev/status-mobile; make run-metro"
#end tell'

osascript -e 'tell application "iTerm2"
set newWindow to (create window with default profile)
tell current session of newWindow
    write text "echo Hello Terminal 1"
end tell

tell current window
create tab with default profile
tell current session
    write text "echo Hello Terminal 2"
end tell
end tell

tell current window
create tab with default profile
tell current session
    write text "echo Hello Terminal 3"
end tell
end tell

end tell'
