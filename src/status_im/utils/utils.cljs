(ns status-im.utils.utils
  (:require [clojure.string :as string]
            [goog.string :as gstring]
            [status-im.i18n.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.core :as ethereum]
            ["react-native" :as react-native]
            ["react-native-background-timer" :default background-timer]))

(defn show-popup
  ([title content]
   (show-popup title content nil))
  ([title content on-dismiss]
   (.alert (.-Alert react-native)
           title
           content
           (clj->js
            (vector (merge {:text                "OK"
                            :style               "cancel"
                            :accessibility-label :cancel-button}
                           (when on-dismiss {:onPress on-dismiss}))))
           (when on-dismiss
             (clj->js {:cancelable false})))))

(defn vibrate []
  #_(.vibrate (.-Vibration react-native)))

(re-frame/reg-fx
 :utils/show-popup
 (fn [{:keys [title content on-dismiss]}]
   (show-popup title content on-dismiss)))

(defn show-confirmation
  [{:keys [title content confirm-button-text on-accept on-cancel cancel-button-text
           extra-options]}]
  (.alert (.-Alert react-native)
          title
          content
          ;; Styles are only relevant on iOS. On Android first button is 'neutral' and second is 'positive'
          (clj->js
           (concat
            (vector (merge {:text                (or cancel-button-text (i18n/label :t/cancel))
                            :style               "cancel"
                            :accessibility-label :cancel-button}
                           (when on-cancel {:onPress on-cancel}))
                    {:text                (or confirm-button-text (i18n/label :t/ok))
                     :onPress             on-accept
                     :style               "default"
                     :accessibility-label :confirm-button})
            (or extra-options nil)))
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
   (.alert (.-Alert react-native)
           title
           content
           (clj->js
            (vector (merge {:text                (i18n/label :t/no)
                            :accessibility-label :no-button}
                           (when on-cancel {:onPress on-cancel}))
                    {:text                (i18n/label :t/yes)
                     :onPress             on-accept
                     :accessibility-label :yes-button})))))

(defn get-shortened-address
  "Takes first and last 4 digits from address including leading 0x
  and adds unicode ellipsis in between"
  [address]
  (when address
    (str (subs address 0 6) "\u2026" (subs address (- (count address) 4) (count address)))))

(defn get-shortened-checksum-address [address]
  (when address
    (get-shortened-address (eip55/address->checksum (ethereum/normalized-hex address)))))

;; background-timer

(defn set-timeout [cb ms]
  (.setTimeout background-timer cb ms))

;; same as re-frame dispatch-later but using background timer for long
;; running timeouts
(re-frame/reg-fx
 :utils/dispatch-later
 (fn [params]
   (doseq [{:keys [ms dispatch]} params]
     (set-timeout #(re-frame/dispatch dispatch) ms))))

(defn clear-timeout [id]
  (.clearTimeout background-timer id))

(re-frame/reg-fx
 ::clear-timeouts
 (fn [ids]
   (doseq [id ids]
     (when id
       (clear-timeout id)))))

(defn set-interval [cb ms]
  (.setInterval background-timer cb ms))

(defn clear-interval [id]
  (.clearInterval background-timer id))

(defn format-decimals [amount places]
  (let [decimal-part (get (string/split (str amount) ".") 1)]
    (if (> (count decimal-part) places)
      (gstring/format (str "%." places "f") amount)
      (or (str amount) 0))))

(defn safe-trim [s]
  (when (string? s)
    (string/trim s)))

(defn safe-replace [s m r]
  (when (string? s)
    (string/replace s m r)))
