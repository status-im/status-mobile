(ns status-im.utils.utils
  (:require [status-im.constants :as const]
            [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn show-popup [title content]
  (.alert (.-Alert rn-dependencies/react-native)
          title
          content))

(defn show-confirmation
  ([title content on-accept]
   (show-confirmation title content nil on-accept))
  ([title content confirm-button-text on-accept]
   (show-confirmation title content confirm-button-text on-accept nil))
  ([title content confirm-button-text on-accept on-cancel]
   (.alert (.-Alert rn-dependencies/react-native)
           title
           content
           ;; Styles are only relevant on iOS. On Android first button is 'neutral' and second is 'positive'
           (clj->js
            (vector (merge {:text (i18n/label :t/cancel) :style "cancel"}
                           (when on-cancel {:onPress on-cancel}))
                    {:text    (or confirm-button-text "OK")
                     :onPress on-accept
                     :style   "destructive"})))))

(defn show-question
  ([title content on-accept]
   (show-question title content on-accept nil))
  ([title content on-accept on-cancel]
   (.alert (.-Alert rn-dependencies/react-native)
           title
           content
           (clj->js
            (vector (merge {:text (i18n/label :t/no)}
                           (when on-cancel {:onPress on-cancel}))
                    {:text (i18n/label :t/yes) :onPress on-accept})))))

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

;; background-timer

(defn set-timeout [cb ms]
  (.setTimeout rn-dependencies/background-timer cb ms))

(defn clear-timeout [id]
  (.clearTimeout rn-dependencies/background-timer id))

(defn set-interval [cb ms]
  (.setInterval rn-dependencies/background-timer cb ms))

(defn clear-interval [id]
  (.clearInterval rn-dependencies/background-timer id))
