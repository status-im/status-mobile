(ns status-im.utils.utils
  (:require [status-im.constants :as const]
            [reagent.core :as r]
            [clojure.string :as str]))

(defn require [module]
  (if (exists? js/window)
    (js/require module)
    #js {}))

(defn log [obj]
  (.log js/console obj))

(def react-native (js/require "react-native"))

(defn show-popup [title content]
  (.alert (.-Alert react-native)
          title
          content))

(defn http-post
  ([action data on-success]
   (http-post action data on-success nil))
  ([action data on-success on-error]
   (-> (.fetch js/window
               (str const/server-address action)
               (clj->js {:method "POST"
                         :headers {:accept "application/json"
                                   :content-type "application/json"}
                         :body (.stringify js/JSON (clj->js data))}))
       (.then (fn [response]
                (log response)
                (.text response)))
       (.then (fn [text]
                (let [json (.parse js/JSON text)
                      obj (js->clj json :keywordize-keys true)]
                  (on-success obj))))
       (.catch (or on-error
                   (fn [error]
                     (show-popup "Error" (str error))))))))

(defn http-get
  ([url on-success on-error]
   (-> (.fetch js/window url (clj->js {:method "GET"}))
       (.then (fn [response]
                (log response)
                (.text response)))
       (.then on-success)
       (.catch (or on-error
                   (fn [error]
                     (show-popup "Error" (str error))))))))

(defn truncate-str [s max]
  (if (and (< max (count s)) s)
    (str (subs s 0 (- max 3)) "...")
    s))

(defn clean-text [s]
  (-> s
      (str/replace #"\n" " ")
      (str/replace #"\r" "")
      (str/trim)))

(defn first-index
  [cond coll]
  (loop [index 0
         cond cond
         coll coll]
    (when (seq coll)
      (if (cond (first coll))
        index
        (recur (inc index) cond (next coll))))))

(defn get-react-property [name]
  (aget react-native name))

(defn adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-react-property name)))
