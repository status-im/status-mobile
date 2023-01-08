(ns status-im2.setup.i18n-resources
  (:require [clojure.string :as string]
            [i18n.i18n :as i18n]
            [react-native.languages :as react-native-languages]))

(def default-device-language (react-native-languages/get-lang-keyword))

(def languages
  #{:ar :bn :de :el :en :es :es_419 :es_AR :fil :fr :hi :id :in :it :ja :ko :ms :nl :pl :pt :pt_BR :ru
    :tr :vi :zh :zh_Hant :zh_TW})

(defonce loaded-languages
         (atom
          (conj #{:en} default-device-language)))

(defn valid-language
  [lang]
  (if (contains? languages lang)
    (keyword lang)
    (let [parts         (string/split (name lang) #"[\-\_]")
          short-lang    (keyword (str (first parts) "_" (second parts)))
          shortest-lang (keyword (first parts))]
      (if (and (> (count parts) 2) (contains? languages short-lang))
        short-lang
        (when (contains? languages shortest-lang)
          shortest-lang)))))

(defn require-translation
  [lang-key]
  (when-let [lang (valid-language (keyword lang-key))]
    (case lang
      :ar      (js/require "../translations/ar.json")
      :bn      (js/require "../translations/bn.json")
      :de      (js/require "../translations/de.json")
      :el      (js/require "../translations/el.json")
      :en      (js/require "../translations/en.json")
      :es      (js/require "../translations/es.json")
      :es_419  (js/require "../translations/es_419.json")
      :es_AR   (js/require "../translations/es_AR.json")
      :fil     (js/require "../translations/fil.json")
      :fr      (js/require "../translations/fr.json")
      :hi      (js/require "../translations/hi.json")
      :id      (js/require "../translations/id.json")
      :in      (js/require "../translations/id.json")
      :it      (js/require "../translations/it.json")
      :ja      (js/require "../translations/ja.json")
      :ko      (js/require "../translations/ko.json")
      :ms      (js/require "../translations/ms.json")
      :nl      (js/require "../translations/nl.json")
      :pl      (js/require "../translations/pl.json")
      :pt      (js/require "../translations/pt.json")
      :pt_BR   (js/require "../translations/pt_BR.json")
      :ru      (js/require "../translations/ru.json")
      :tr      (js/require "../translations/tr.json")
      :vi      (js/require "../translations/vi.json")
      :zh      (js/require "../translations/zh.json")
      :zh_Hant (js/require "../translations/zh_hant.json")
      :zh_TW   (js/require "../translations/zh_TW.json"))))

;; translations
(def translations-by-locale
  (cond-> {:en (require-translation :en)}
    (not= :en default-device-language)
    (assoc default-device-language
           (require-translation (-> (name default-device-language)
                                    (string/replace "-" "_")
                                    keyword)))))

(i18n/setup (name default-device-language) (clj->js translations-by-locale))

(defn load-language
  [lang]
  (when-let [lang-key (valid-language (keyword lang))]
    (when-not (contains? @loaded-languages lang-key)
      (aset (i18n/get-translations)
            lang
            (require-translation lang-key))
      (swap! loaded-languages conj lang-key))))
