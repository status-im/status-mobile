(ns status-im.console
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]))

(def message-text
  (str "Your phone number is also required to use the app. Type"
       " the exclamation mark or hit the icon to open the command "
       "list and choose the !phone command"))

(appium-test console-test
  (click :request-keypair-password)
  (write :command-input "123")
  (click :stage-command)
  (click :send-message)
  (contains-text message-text))
