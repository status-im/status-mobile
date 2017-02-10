(ns status-im.test.console
  (:require [clojure.test :refer :all]
            [status-im.accessibility-ids :as id]
            [status-im.test.appium :refer :all]))

(defaction send-command []
  (click id/chat-send-button))

(defaction respond-to-request [request value]
  (click (id/chat-request-message-button request))
  (write id/chat-message-input value)
  (send-command))

(defaction confirm-password [value]
  (write id/chat-message-input value)
  (send-command))

(appium-test sign-up-successfully
  (respond-to-request :password "password")
  (confirm-password "password")
  (respond-to-request :phone "2015550123")
  (respond-to-request :confirmation-code "1234")
  (contains-text "Done!")
  (click id/toolbar-back-button)
  (contains-text "Chats"))

(appium-test wrong-password
  (respond-to-request :password "abc")
  (contains-text "Password should be not less then 6 symbols.")
  (click id/chat-cancel-response-button)
  (respond-to-request :password "password")
  (confirm-password "abc")
  (contains-text "Password confirmation doesn't match password."))

(appium-test wrong-phone-number
  (respond-to-request :password "password")
  (confirm-password "password")
  (respond-to-request :phone "1234")
  (contains-text "Invalid phone number"))

(appium-test wrong-confirmation-code
  (respond-to-request :password "password")
  (confirm-password "password")
  (respond-to-request :phone "2015550123")
  (respond-to-request :confirmation-code "432")
  (contains-text "Wrong format")
  (click id/chat-cancel-response-button)
  (respond-to-request :confirmation-code "4321")
  (contains-text "Wrong code!"))
