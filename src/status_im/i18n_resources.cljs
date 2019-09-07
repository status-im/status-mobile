(ns status-im.i18n-resources
  (:require-macros [status-im.i18n :as i18n])
  (:require [status-im.utils.types :as types]
            [clojure.string :as string]
            ["react-native-languages" :default react-native-languages]))

(def default-device-language
  (keyword (.-language react-native-languages)))

;; translations
(def translations-by-locale
  (->> (i18n/translations [:ar :en :es :es_419 :fil :fr :it :ko :ru :in :id :tr :zh :zh_Hant :zh_TW])
       (map (fn [[k t]]
              (let [k' (-> (name k)
                           (string/replace "_" "-")
                           keyword)]
                [k' (types/json->clj t)])))
       (into {})))

;; API compatibility
(defn load-language [lang])
