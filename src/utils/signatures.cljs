(ns utils.signatures
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [utils.hex :as hex]))

(defn signature->rsv
  [hex-signature]
  (let [signature (hex/normalize-hex hex-signature)]
    {:r (subs signature 0 64)
     :s (subs signature 64 128)
     :v (subs signature 128 130)}))

(defn rsv->signature
  [rsv]
  (->> rsv
       vals
       (string/join "")
       hex/prefix-hex))

(defn adjust-v-value
  [rsv]
  (let [v-int (js/parseInt (:v rsv) 16)]
    (if (or (= v-int 0) (= v-int 1))
      (assoc rsv
             :v
             (-> (+ v-int 27)
                 native-module/number-to-hex))
      rsv)))

(defn adjust-legacy-ecdsa-signature
  "Due to legacy reasons, ensures the `v` value in an Ethereum personal_sign
  signature should be set to 27 or 28, which comes from legacy ECDSA signature
  recovery mechanisms."
  [signature]
  (-> signature
      signature->rsv
      adjust-v-value
      rsv->signature))
