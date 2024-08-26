(ns utils.hex
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [schema.core :as schema]))

(defn normalize-hex
  [hex]
  (when hex
    (string/lower-case (if (string/starts-with? hex "0x")
                         (subs hex 2)
                         hex))))

(schema/=> normalize-hex
  [:=>
   [:cat [:maybe :string]]
   [:maybe :string]])

(defn prefix-hex
  [hex]
  (if (string/starts-with? hex "0x")
    hex
    (str "0x" hex)))

(schema/=> prefix-hex
  [:=>
   [:cat :string]
   :string])

(defn number-to-hex
  [value]
  (->> value
       native-module/number-to-hex
       prefix-hex))

(schema/=> number-to-hex
  [:=>
   [:cat [:or :string :int]]
   :string])
