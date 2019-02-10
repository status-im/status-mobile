#!/usr/bin/env bash
Xvfb :1 -screen 0 1024x768x24 +extension GLX +render -noreset&
x11vnc --display :1 -forever&
icewm&
/bin/bash
