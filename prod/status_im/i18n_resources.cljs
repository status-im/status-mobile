(ns status-im.i18n-resources
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def default-device-language
  (keyword (.-language rn-dependencies/react-native-languages)))

(def languages [:en :es_419 :fa :ko :ms :pl :ru :zh_Hans_CN])

(defonce loaded-languages
  (atom
   (conj #{:en} default-device-language)))

(def prod-translations
  {:en         (js/require "status-modules/translations/en.json")
   :es_419     (js/require "status-modules/translations/es_419.json")
   :fa         (js/require "status-modules/translations/fa.json")
   :ko         (js/require "status-modules/translations/ko.json")
   :ms         (js/require "status-modules/translations/ms.json")
   :pl         (js/require "status-modules/translations/pl.json")
   :ru         (js/require "status-modules/translations/ru.json")
   :zh_Hans_CN (js/require "status-modules/translations/zh_Hans_CN.json")})

(defn valid-language [lang]
  (if (contains? prod-translations lang)
    lang
    (let [short-lang (keyword (subs (name lang) 0 2))]
      (when (contains? prod-translations short-lang)
        short-lang))))

(defn require-translation [lang-key]
  (when-let [lang (valid-language lang-key)]
    (get prod-translations lang)))

;; translations
(def translations-by-locale
  (cond->
    {:en (require-translation :en)}
    (not= :en default-device-language)
    (assoc default-device-language
           (require-translation default-device-language))))

(defn load-language [lang]
  (let [lang-key (valid-language (keyword lang))]
    (when-not (contains? @loaded-languages lang-key)
      (aset (.-translations rn-dependencies/i18n)
            lang
            (require-translation lang-key))
      (swap! loaded-languages conj lang-key))))
