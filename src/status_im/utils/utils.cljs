(ns status-im.utils.utils
  (:require [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [re-frame.core :as re-frame]))

(defn show-popup
  ([title content]
   (show-popup title content nil))
  ([title content on-dismiss]
   (.alert (.-Alert rn-dependencies/react-native)
           title
           content
           (clj->js
            (vector (merge {:text                "OK"
                            :style               "cancel"
                            :accessibility-label :cancel-button}
                           (when on-dismiss {:onPress on-dismiss}))))
           (when on-dismiss
             (clj->js {:cancelable false})))))

(re-frame/reg-fx
 :utils/show-popup
 (fn [{:keys [title content on-dismiss]}]
   (show-popup title content on-dismiss)))

(defn show-confirmation
  [{:keys [title content confirm-button-text on-dismiss on-accept on-cancel cancel-button-text]}]
  (.alert (.-Alert rn-dependencies/react-native)
          title
          content
          ;; Styles are only relevant on iOS. On Android first button is 'neutral' and second is 'positive'
          (clj->js
           (vector (merge {:text                (or cancel-button-text (i18n/label :t/cancel))
                           :style               "cancel"
                           :accessibility-label :cancel-button}
                          (when on-cancel {:onPress on-cancel}))
                   {:text                (or confirm-button-text (i18n/label :t/ok))
                    :onPress             on-accept
                    :style               "default"
                    :accessibility-label :confirm-button}))
          #js {:cancelable false}))

(re-frame/reg-fx
 :utils/show-confirmation
 (fn [{:keys [title content confirm-button-text on-accept on-cancel cancel-button-text]}]
   (show-confirmation {:title title
                       :content content
                       :confirm-button-text confirm-button-text
                       :cancel-button-text cancel-button-text
                       :on-accept on-accept
                       :on-cancel on-cancel})))

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

(defn unread-messages-count
  "display actual # if less than 1K, round to the lowest thousand if between 1 and 10K, otherwise 10K+ for anything larger"
  [messages-count]
  (let [round-to-lowest-single-thousand #(-> %
                                             (/ 1000)
                                             (Math/floor)
                                             (str "K+"))]
    (cond
      (< messages-count 1000)        (str messages-count)
      (<= 1000 messages-count 10000) (round-to-lowest-single-thousand messages-count)
      (> messages-count 10000)       "10K+")))

;; same as re-frame dispatch-later but using background timer for long
;; running timeouts
(re-frame/reg-fx
 :utils/dispatch-later
 (fn [params]
   (doseq [{:keys [ms dispatch]} params]
     (set-timeout #(re-frame/dispatch dispatch) ms))))

(defn clear-timeout [id]
  (.clearTimeout rn-dependencies/background-timer id))

(defn set-interval [cb ms]
  (.setInterval rn-dependencies/background-timer cb ms))

(defn clear-interval [id]
  (.clearInterval rn-dependencies/background-timer id))
