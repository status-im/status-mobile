(ns status-im.console
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]))

(def command-request-icon
  (str
    "//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]"
    "/android.widget.FrameLayout[1]/android.view.ViewGroup[1]"
    "/android.widget.ScrollView[1]/android.view.ViewGroup[1]"
    "/android.view.ViewGroup[1]/android.view.ViewGroup[1]"
    "/android.view.ViewGroup[2]/android.widget.ImageView[1]"))

(def input
  (str
    "//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]"
    "/android.widget.FrameLayout[1]/android.view.ViewGroup[1]"
    "/android.view.ViewGroup[2]/android.view.ViewGroup[1]"
    "/android.widget.EditText[1]"))

(def send-button1
  (str "//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]"
       "/android.widget.FrameLayout[1]/android.view.ViewGroup[1]"
       "/android.view.ViewGroup[2]/android.view.ViewGroup[1]"
       "/android.view.ViewGroup[2]/android.view.ViewGroup[1]"))

(def send-button2
  (str "//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]"
       "/android.widget.FrameLayout[1]/android.view.ViewGroup[1]"
       "/android.view.ViewGroup[2]/android.view.ViewGroup[2]"
       "/android.view.ViewGroup[2]/android.view.ViewGroup[1]"))

(def message-text
  (str "Your phone number is also required to use the app. Type"
       " the exclamation mark or hit the icon to open the command "
       "list and choose the !phone command"))

(deftest console-test
  (let [driver (init)]
    (click driver command-request-icon)
    (write driver input "123")
    (click driver send-button1)
    (click driver send-button2)
    (contains-text driver message-text)
    (quit driver)))
