(ns status-im.utils.utils
  (:require [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [clojure.set :as set]))

(def alert (.-Alert js-dependencies/react-native))

(defn show-popup
  ([title content]
   (show-popup title content nil))
  ([title content on-dismiss]
   (.alert alert
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
  [{:keys [title content confirm-button-text on-accept on-cancel cancel-button-text]}]
  (.alert alert
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
                    :accessibility-label :confirm-button})
           #js {:cancelable false})))

(defn show-question
  ([title content on-accept]
   (show-question title content on-accept nil))
  ([title content on-accept on-cancel]
   (.alert alert
           title
           content
           (clj->js
            (vector (merge {:text                (i18n/label :t/no)
                            :accessibility-label :no-button}
                           (when on-cancel {:onPress on-cancel}))
                    {:text                (i18n/label :t/yes)
                     :onPress             on-accept
                     :accessibility-label :yes-button})))))

(defn show-options-dialog
  [{:keys [title options]}]
  (.alert alert
          title
          ""
          (clj->js
           (vec (map #(set/rename-keys % {:label :text, :action :onPress}) options)))))

;; background-timer

(defn set-timeout [cb ms]
  (.setTimeout js-dependencies/background-timer cb ms))

;; same as re-frame dispatch-later but using background timer for long
;; running timeouts
(re-frame/reg-fx
 :utils/dispatch-later
 (fn [params]
   (doseq [{:keys [ms dispatch]} params]
     (set-timeout #(re-frame/dispatch dispatch) ms))))

(defn clear-timeout [id]
  (.clearTimeout js-dependencies/background-timer id))

(defn set-interval [cb ms]
  (.setInterval js-dependencies/background-timer cb ms))

(defn clear-interval [id]
  (.clearInterval js-dependencies/background-timer id))
