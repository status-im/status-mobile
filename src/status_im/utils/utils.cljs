(ns status-im.utils.utils
  (:require [status-im.i18n :as i18n]
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

;; background-timer

(defn set-timeout [cb ms]
  (.setTimeout rn-dependencies/background-timer cb ms))

(defn clear-timeout [id]
  (.clearTimeout rn-dependencies/background-timer id))

(defn set-interval [cb ms]
  (.setInterval rn-dependencies/background-timer cb ms))

(defn clear-interval [id]
  (.clearInterval rn-dependencies/background-timer id))
