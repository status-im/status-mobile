(ns status-im.console
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]))

(def message-text
  (str "Your phone number is also required to use the app. Type"
       " the exclamation mark or hit the icon to open the command "
       "list and choose the !phone command"))

(deftest console-test
  (let [driver (init)]
    (click driver :request-keypair-password)
    (write driver :command-input "123")
    (click driver :stage-command)
    (click driver :send-message)
    (contains-text driver message-text)
    (quit driver)))
