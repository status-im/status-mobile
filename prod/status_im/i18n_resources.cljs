(ns status-im.i18n-resources
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]))

(def default-device-language
  (keyword (.-language rn-dependencies/react-native-languages)))

(def languages [:ar :en :es :es_419 :fil :fr :id :in :it :ko :ru :zh :zh_Hant :zh_TW])

(defonce loaded-languages
  (atom
   (conj #{:en} default-device-language)))

   (def prod-translations
     {
      :ar         (js/require "status-modules/translations/ar.json")
      :en         (js/require "status-modules/translations/en.json")
      :es         (js/require "status-modules/translations/es.json")
      :es_419     (js/require "status-modules/translations/es_419.json")
      :fil        (js/require "status-modules/translations/fil.json")
      :fr         (js/require "status-modules/translations/fr.json")
      :id         (js/require "status-modules/translations/id.json")
      :in         (js/require "status-modules/translations/id.json")
      :it         (js/require "status-modules/translations/it.json")
      :ko         (js/require "status-modules/translations/ko.json")
      :ru         (js/require "status-modules/translations/ru.json")
      :zh         (js/require "status-modules/translations/zh.json")
      :zh_Hant    (js/require "status-modules/translations/zh_TW.json")
      :zh_TW      (js/require "status-modules/translations/zh_TW.json")})

(defn valid-language [lang]
  (if (contains? prod-translations lang)
    lang
    (let [parts (string/split (name lang) #"_")
          short-lang (keyword (str (first parts) "_" (second parts)))
          shortest-lang (keyword (first parts))]
      (if (and (> (count parts) 2) (contains? prod-translations short-lang))
        short-lang
        (when (contains? prod-translations shortest-lang)
          shortest-lang)))))

(defn require-translation [lang-key]
  (when-let [lang (valid-language lang-key)]
    (get prod-translations lang)))

;; translations
(def translations-by-locale
  (cond->
    {:en (require-translation :en)}
    (not= :en default-device-language)
    (assoc default-device-language
           (require-translation (-> (name default-device-language)
                                    (string/replace "-" "_")
                                    keyword)))))

(defn load-language [lang]
  (let [lang-key (valid-language (keyword lang))]
    (when-not (contains? @loaded-languages lang-key)
      (aset (.-translations rn-dependencies/i18n)
            lang
            (require-translation lang-key))
      (swap! loaded-languages conj lang-key))))
