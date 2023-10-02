(ns react-native.touch-id
  (:require ["react-native-touch-id" :default touchid]))

;; currently, for android, react-native-touch-id
;; is not returning supported biometric type
;; defaulting to :fingerprint
(def android-default-support :fingerprint)

(defn get-supported-type
  [callback]
  (-> (.isSupported ^js touchid)
      (.then #(callback (or (keyword %) android-default-support)))
      (.catch #(callback nil))))

(defn authenticate
  [{:keys [on-success on-fail reason options]}]
  (-> (.authenticate ^js touchid reason (clj->js options))
      (.then #(when on-success (on-success %)))
      (.catch #(when on-fail (on-fail (aget % "code"))))))
