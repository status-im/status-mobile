(ns status-im.utils.utils
  (:require [status-im.constants :as const]
            [status-im.i18n :refer [label]]
            [reagent.core :as r]
            [clojure.string :as str]))

(defn log [obj]
  (.log js/console obj))

(def react-native (js/require "react-native"))

(defn show-popup [title content]
  (.alert (.-Alert react-native)
          title
          content))

(defn show-confirmation
  ([title content on-accept]
   (show-confirmation title content nil on-accept))
  ([title content s on-accept]
   (show-confirmation title content s on-accept nil))
  ([title content s on-accept on-cancel]
   (.alert (.-Alert react-native)
           title
           content
           ; Styles are only relevant on iOS. On Android first button is 'neutral' and second is 'positive'
           (clj->js
             (vector (merge {:text (label :t/cancel) :style "cancel"}
                            (when on-cancel {:onPress on-cancel}))
                     {:text (or s "OK") :onPress on-accept :style "destructive"})))))

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
    (http-get url nil on-success on-error))
  ([url valid-response? on-success on-error]
   (-> (.fetch js/window url (clj->js {:method  "GET"
                                       :headers {"Cache-Control" "no-cache"}}))
       (.then (fn [response]
                (log response)
                (let [ok?  (.-ok response)
                      ok?' (if valid-response?
                             (and ok? (valid-response? response))
                             ok?)]
                  [(.-_bodyText response) ok?'])))
       (.then (fn [[response ok?]]
                (cond
                  ok? (on-success response)

                  (and on-error (not ok?))
                  (on-error response)

                  :else false)))
       (.catch (or on-error
                   (fn [error]
                     (show-popup "Error" (str error))))))))

(defn truncate-str [s max]
  (if (and (< max (count s)) s)
    (str (subs s 0 (- max 3)) "...")
    s))

(defn clean-text [s]
  (-> s
      (str/replace #"\n" "")
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

(defn hash-tag? [s]
  (= \# (first s)))
