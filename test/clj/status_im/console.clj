(ns status-im.console
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]))

(def message-text
  (str "Your phone number is also required to use the app. Type"
       " the exclamation mark or hit the icon to open the command "
       "list and choose the !phone command"))

(defaction send-sommand []
  (click :send-message)
  (click :send-message))

(defaction respond-to-request
  [request value]
  (click (keyword (str "request-" (name request))))
  (write :input value)
  (send-sommand))

(appium-test happy-case
  (click :create-account)
  (respond-to-request :keypair "123")
  (contains-text message-text)
  (respond-to-request :phone "+380671111111")
  (respond-to-request :confirmation-code "1234")
  (click :navigate-back)
  (contains-text "Switch users"))

(appium-test wrong-confirmation-code
  (click :create-account)
  (respond-to-request :keypair "123")
  (respond-to-request :phone "+380671111111")
  (respond-to-request :confirmation-code "432")
  (contains-text "Wrong format")
  (respond-to-request :confirmation-code "4321")
  (contains-text "Wrong code!")
  (write :input "1234")
  (send-sommand)
  (click :navigate-back)
  (contains-text "Switch users"))

(appium-test wrong-phone-number
  (click :create-account)
  (respond-to-request :keypair "123")
  (respond-to-request :phone "+380671111111")
  (write :input "+380671111112")
  (send-sommand)
  (write :input "1234")
  (send-sommand)
  (click :navigate-back)
  (contains-text "Switch users"))
