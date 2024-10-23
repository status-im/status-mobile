(ns legacy.status-im.utils.utils
  (:require
    ["react-native" :as react-native]
    ["react-native-background-timer" :default background-timer]
    [clojure.string :as string]
    [goog.string :as gstring]
    [re-frame.core :as re-frame]
    [utils.address :as address]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.i18n :as i18n]))

;;TODO (14/11/22 flexsurfer) .-Alert usage code has been moved to the status-im2 namespace, we keep this
;;only for old (status 1.0) code,
;; can be removed with old code later
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

(defn show-confirmation
  [{:keys [title content confirm-button-text on-accept on-cancel cancel-button-text
           extra-options]}]
  (.alert (.-Alert react-native)
          title
          content
          ;; Styles are only relevant on iOS. On Android first button is 'neutral' and second is
          ;; 'positive'
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

;;TODO (14/11/22 flexsurfer) background-timer usage code has been moved to the status-im2 namespace, we
;;keep this only for old (status 1.0) code,
;; can be removed with old code later

(defn set-timeout
  [cb ms]
  (.setTimeout background-timer cb ms))

(defn clear-timeout
  [id]
  (.clearTimeout background-timer id))

(defn set-interval
  [cb ms]
  (.setInterval background-timer cb ms))

(defn clear-interval
  [id]
  (.clearInterval background-timer id))

(re-frame/reg-fx
 :utils/dispatch-later
 (fn [params]
   (doseq [{:keys [ms dispatch]} params]
     (when (and ms dispatch)
       (set-timeout #(re-frame/dispatch dispatch) ms)))))

(re-frame/reg-fx
 ::clear-timeouts
 (fn [ids]
   (doseq [id ids]
     (when id
       (clear-timeout id)))))

(defn get-shortened-address
  "Takes first and last 4 digits from address including leading 0x
  and adds unicode ellipsis in between"
  [address]
  (when address
    (str (subs address 0 6) "\u2026" (subs address (- (count address) 3) (count address)))))

(defn get-shortened-checksum-address
  [address]
  (when address
    (get-shortened-address (eip55/address->checksum (address/normalized-hex address)))))

;;TODO (14/11/22 flexsurfer) haven't moved yet
(defn format-decimals
  [amount places]
  (let [decimal-part (get (string/split (str amount) ".") 1)]
    (if (> (count decimal-part) places)
      (gstring/format (str "%." places "f") amount)
      (or (str amount) 0))))

(defn safe-nth
  [coll index]
  (when (number? index)
    (nth coll index)))

(defn svg?
  [some-string]
  (string/ends-with? some-string ".svg"))

(defn exclude-svg-resources
  [lst]
  (remove svg? lst))
