(ns status-im.utils.db
  (:require [clojure.string :as string]
            [cljs.spec.alpha :as spec]
            [cljs.spec.gen.alpha :as gen]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.ethereum.core :as ethereum]))

(def hex-byte-gen (gen/fmap (fn [n] (-> n (.toString 16) (.padStart 2 0)))
                            (spec/gen (spec/int-in 0 256))))

(defn valid-public-key? [s]
  (boolean (re-matches #"0x04[0-9a-f]{128}" s)))

(spec/def :global/not-empty-string (spec/and string? (complement string/blank?)))
(spec/def :global/public-key (spec/with-gen (spec/and :global/not-empty-string valid-public-key?)
                               #(gen/fmap (fn [bytes] (->> bytes
                                                           string/join
                                                           (str "0x04")))
                                          (gen/vector hex-byte-gen 64))))
(spec/def :global/address (spec/with-gen ethereum/address?
                            #(gen/fmap (fn [bytes] (->> bytes
                                                        string/join
                                                        (str (-> (gen/elements [nil "0x"])
                                                                 (gen/sample 1)
                                                                 first))))
                                       (gen/vector hex-byte-gen 20))))
