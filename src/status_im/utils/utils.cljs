(ns status-im.utils.utils
  (:require [status-im.constants :as const]
            [status-im.i18n :refer [label]]
            [reagent.core :as r]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn show-popup [title content]
  (.alert (.-Alert rn-dependencies/react-native)
          title
          content))

(defn show-confirmation
  ([title content on-accept]
   (show-confirmation title content nil on-accept))
  ([title content s on-accept]
   (show-confirmation title content s on-accept nil))
  ([title content s on-accept on-cancel]
   (.alert (.-Alert rn-dependencies/react-native)
           title
           content
           ;; Styles are only relevant on iOS. On Android first button is 'neutral' and second is 'positive'
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
                (log/debug response)
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
                (log/debug response)
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

(defn truncate-str
  "Given string and max threshold, trims the string to threshold length with `...`
  appended to end if length of the string exceeds max threshold, returns the same
  string if threshold is not exceeded"
  [s threshold]
  (if (and s (< threshold (count s)))
    (str (subs s 0 (- threshold 3)) "...")
    s))

(defn clean-text [s]
  (-> s
      (str/replace #"\n" "")
      (str/replace #"\r" "")
      (str/trim)))

(defn first-index
  "Returns first index in coll where predicate on coll element is truthy"
  [pred coll]
  (->> coll
       (keep-indexed (fn [idx e]
                       (when (pred e)
                         idx)))
       first))

(defn hash-tag? [s]
  (= \# (first s)))

(defn wrap-call-once!
  "Returns a version of provided function that will be called only the first time wrapping function is called. Returns nil."
  [f]
  (let [called? (volatile! false)]
    (fn [& args]
      (when-not @called?
        (vreset! called? true)
        (apply f args)
        nil))))

(defn update-if-present
  "Like regular `clojure.core/update` but returns original map if update key is not present"
  [m k f & args]
  (if (contains? m k)
    (apply update m k f args)
    m))

