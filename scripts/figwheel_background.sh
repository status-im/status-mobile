#!/usr/bin/expect -f
set timeout 600
exec cd ..
spawn -ignore HUP lein figwheel android ios
expect -ex "Prompt will show when Figwheel connects to your application"
expect_background
send_user "Figwheel Initialized\n"
exit 0