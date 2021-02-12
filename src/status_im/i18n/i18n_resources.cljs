(ns status-im.i18n.i18n-resources
  (:require [clojure.string :as string]
            ["i18n-js" :as i18n-js]
            ["react-native-languages" :default react-native-languages]))

(def default-device-language
  (keyword (.-language react-native-languages)))

(def languages #{:ar :bn :de :el :en :es :es_419 :fil :fr :id :in :it :ja :ko :ms :pt_BR :ru :tr :vi :zh :zh_Hant :zh_TW})

(defonce loaded-languages
  (atom
   (conj #{:en} default-device-language)))

(defn valid-language [lang]
  (if (contains? languages lang)
    (keyword lang)
    (let [parts (string/split (name lang) #"[\-\_]")
          short-lang (keyword (str (first parts) "_" (second parts)))
          shortest-lang (keyword (first parts))]
      (if (and (> (count parts) 2) (contains? languages short-lang))
        short-lang
        (when (contains? languages shortest-lang)
          shortest-lang)))))

(defn require-translation [lang-key]
  (when-let [lang (valid-language (keyword lang-key))]
    (case lang
      :ar         (js/require "../translations/ar.json")
      :bn         (js/require "../translations/bn.json")
      :de         (js/require "../translations/de.json")
      :el         (js/require "../translations/el.json")
      :en         (js/require "../translations/en.json")
      :es         (js/require "../translations/es.json")
      :es_419     (js/require "../translations/es_419.json")
      :fil        (js/require "../translations/fil.json")
      :fr         (js/require "../translations/fr.json")
      :id         (js/require "../translations/id.json")
      :in         (js/require "../translations/id.json")
      :it         (js/require "../translations/it.json")
      :ja         (js/require "../translations/ja.json")
      :ko         (js/require "../translations/ko.json")
      :ms         (js/require "../translations/ms.json")
      :pt_BR      (js/require "../translations/pt_BR.json")
      :ru         (js/require "../translations/ru.json")
      :tr         (js/require "../translations/tr.json")
      :vi         (js/require "../translations/vi.json")
      :zh         (js/require "../translations/zh.json")
      :zh_Hant    (js/require "../translations/zh_hant.json")
      :zh_TW      (js/require "../translations/zh_TW.json"))))

;; translations
(def translations-by-locale
  (cond-> {:en (require-translation :en)}
    (not= :en default-device-language)
    (assoc default-device-language
           (require-translation (-> (name default-device-language)
                                    (string/replace "-" "_")
                                    keyword)))))

(defn load-language [lang]
  (when-let [lang-key (valid-language (keyword lang))]
    (when-not (contains? @loaded-languages lang-key)
      (aset (.-translations i18n-js)
            lang
            (require-translation lang-key))
      (swap! loaded-languages conj lang-key))))
