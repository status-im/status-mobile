(ns status-im.utils.utils
  (:require [status-im.constants :as const]
            [status-im.i18n :as i18n]
            [clojure.string :as str]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

;; Default HTTP request timeout ms
(def http-request-default-timeout-ms 3000)

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
            (vector (merge {:text                (i18n/label :t/cancel)
                            :style               "cancel"
                            :accessibility-label :cancel-button}
                           (when on-cancel {:onPress on-cancel}))
                    {:text                (or confirm-button-text "OK")
                     :onPress             on-accept
                     :style               "destructive"
                     :accessibility-label :confirm-button})))))

(defn show-question
  ([title content on-accept]
   (show-question title content on-accept nil))
  ([title content on-accept on-cancel]
   (.alert (.-Alert rn-dependencies/react-native)
           title
           content
           (clj->js
            (vector (merge {:text                (i18n/label :t/no)
                            :accessibility-label :no-button}
                           (when on-cancel {:onPress on-cancel}))
                    {:text                (i18n/label :t/yes)
                     :onPress             on-accept
                     :accessibility-label :yes-button})))))

(defn http-post
  "Performs an HTTP POST request"
  ([action data on-success]
   (http-post action data on-success nil))
  ([action data on-success on-error]
   (http-post action data on-success on-error nil))
  ([action data on-success on-error {:keys [timeout-ms] :as opts}]
   (-> (rn-dependencies/fetch (str const/server-address action)
                              (clj->js {:method  "POST"
                                        :headers {:accept       "application/json"
                                                  :content-type "application/json"}
                                        :body    (.stringify js/JSON (clj->js data))
                                        :timeout (or timeout-ms http-request-default-timeout-ms)}))
       (.then (fn [response]
                (.text response)))
       (.then (fn [text]
                (let [json (.parse js/JSON text)
                      obj  (js->clj json :keywordize-keys true)]
                  (on-success obj))))
       (.catch (or on-error
                   (fn [error]
                     (show-popup "Error" (str error))))))))

(defn http-get
  "Performs an HTTP GET request"
  ([url on-success on-error]
   (http-get url on-success on-error nil))
  ([url on-success on-error {:keys [valid-response? timeout-ms] :as opts}]
   (-> (rn-dependencies/fetch url
                              (clj->js {:method  "GET"
                                        :headers {"Cache-Control" "no-cache"}
                                        :timeout (or timeout-ms http-request-default-timeout-ms)}))
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
