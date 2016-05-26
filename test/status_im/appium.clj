(ns status-im.appium
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
                       (.setCapability "platformVersion" "6.0.0")
                       (.setCapability "app" (.getAbsolutePath app))
                       (.setCapability "appPackage" "com.statusim")
                       (.setCapability "appActivity" ".MainActivity"))
        driver       (AndroidDriver. (URL. "http://127.0.0.1:4723/wd/hub") capabilities)]
    (-> driver
        .manage
        .timeouts
        (.implicitlyWait 100 TimeUnit/MILLISECONDS))
    (Thread/sleep 9000)
    driver))

(defn by-xpath [driver xpath]
  (.findElement driver (By/xpath xpath)))

(defn elements-by-xpath [driver xpath]
  (.findElements driver (By/xpath xpath)))

(defn click [driver xpath]
  (.click (by-xpath driver xpath)))

(defn write [driver input-xpath text]
  (.sendKeys (by-xpath driver input-xpath) (into-array [text])))

(defn get-text [driver xpath]
  (.getText (by-xpath driver xpath)))

(defn xpath-by-text [text]
  (str ".//*[@text='" text "']" ))

(defn contains-text [driver text]
  (is (= 1 (->> (xpath-by-text text)
                (elements-by-xpath driver)
                (.size)))))

(defn quit [driver]
  (.quit driver))
