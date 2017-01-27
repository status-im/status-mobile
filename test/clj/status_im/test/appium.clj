(ns status-im.test.appium
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all])
  (:import (org.openqa.selenium.remote DesiredCapabilities)
           (org.openqa.selenium By)
           (io.appium.java_client.android AndroidDriver)
           (java.net URL)
           (java.util.concurrent TimeUnit)))


(defn init []
  (let [dir          (io/file (str (System/getProperty "user.dir")
                                   "/android/app/build/outputs/apk"))
        app          (io/file dir "app-debug.apk")
        capabilities (doto (DesiredCapabilities.)
                       (.setCapability "deviceName" "device")
                       (.setCapability "app" (.getAbsolutePath app))
                       (.setCapability "appPackage" "im.status.ethereum")
                       (.setCapability "appActivity" ".MainActivity"))
        driver       (AndroidDriver. (URL. "http://127.0.0.1:4723/wd/hub") capabilities)]
    (-> driver
        .manage
        .timeouts
        (.implicitlyWait 25 TimeUnit/SECONDS))
    driver))

(defn by-xpath [driver xpath]
  (.findElement driver (By/xpath xpath)))

(defn elements-by-xpath [driver xpath]
  (.findElements driver (By/xpath xpath)))

(defn by-id [driver id]
  (.findElementByAccessibilityId driver (name id)))

(defn get-element [driver id]
  (if (keyword? id)
    (by-id driver id)
    (by-xpath driver id)))

(defn click [driver id]
  (.click (get-element driver id)))

(defn write [driver input-xpath text]
  (.sendKeys (get-element driver input-xpath) (into-array [text])))

(defn get-text [driver xpath]
  (.getText (by-xpath driver xpath)))

(defn xpath-by-text [text]
  (str ".//*[@text=\"" text "\"]"))

(defn click-by-text [driver text]
  (let [elements (->> (xpath-by-text text)
                      (elements-by-xpath driver))]
    (when (pos? (.size elements))
      (let [element (.get elements 0)]
        (.click element)))))

(defn contains-text [driver text]
  (is (pos? (->> (xpath-by-text text)
                 (elements-by-xpath driver)
                 (.size)))
      (format "Text \"%s\" was not found on screen." text)))

(defn quit [driver]
  (.quit driver))

(defmacro appium-test
  "Defines test which will create new appium session and will pass that
  session as first argument to each command inside it's body. After execution
  of all commands, the session will be closed.
  Also, at the start of every test, the 'Continue' button from the 'Your phone
  appears to be ROOTED' message will be pressed.

  For instance,

  (appium-test my-test
    (click :button)
    (write :input \"oops\"))

  will be expanded to

  (deftest my-test
    (let [session (init)]
      (click session :button)
      (write session :input \"oops\")
      (quit session)))"
  [name & body]
  (let [sym (gensym)]
    `(deftest ~name
       (let [~sym (init)]
         (click-by-text ~sym "Continue")
         ~@(for [[f & rest] body]
             `(~f ~sym ~@rest))
         (quit ~sym)))))

(defmacro defaction
  [name parameters & body]
  (let [session (gensym)]
    `(defn ~name [~@(concat [session] parameters)]
       ~@(for [[f & rest] body]
           `(~f ~session ~@rest)))))
