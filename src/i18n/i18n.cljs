(ns i18n.i18n
  (:require ["i18n-js" :as i18n]
            [clojure.string :as string]
            [status-im.goog.i18n :as goog.i18n]
            [react-native.languages :as react-native-languages]))

(def default-device-language (react-native-languages/get-lang-keyword))

(def languages
  #{:ar :bn :de :el :en :es :es_419 :es_AR :fil :fr :hi :id :in :it :ja :ko :ms :nl :pl :pt :pt_BR :ru
    :tr :vi :zh :zh_Hant :zh_TW})

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

(def translations-by-locale
  (cond-> {:en (require-translation :en)}
    (not= :en default-device-language)
    (assoc default-device-language
           (require-translation (-> (name default-device-language)
                                    (string/replace "-" "_")
                                    keyword)))))

(set! (.-fallbacks i18n) true)
(set! (.-defaultSeparator i18n) "/")
(set! (.-locale i18n) (name default-device-language))
(set! (.-translations i18n) (clj->js translations-by-locale))

(defn init
  []
  (set! (.-fallbacks i18n) true)
  (set! (.-defaultSeparator i18n) "/")
  (set! (.-locale i18n) (name default-device-language))
  (set! (.-translations i18n) (clj->js translations-by-locale)))

(defn get-translations
  []
  (.-translations i18n))

(defn set-language
  [lang]
  (set! (.-locale i18n) lang))

;;:zh, :zh-hans-xx, :zh-hant-xx have been added until this bug will be fixed
;;https://github.com/fnando/i18n-js/issues/460

(def delimeters
  "This function is a hack: mobile Safari doesn't support toLocaleString(), so we need to pass
  this map to WKWebView to make number formatting work."
  (let [n          (.toLocaleString ^js (js/Number 1000.1))
        delimiter? (= (count n) 7)]
    (if delimiter?
      {:delimiter (subs n 1 2)
       :separator (subs n 5 6)}
      {:delimiter ""
       :separator (subs n 4 5)})))

(defn label-number
  [number]
  (when number
    (let [{:keys [delimiter separator]} delimeters]
      (.toNumber i18n
                 (string/replace number #"," ".")
                 (clj->js {:precision                 10
                           :strip_insignificant_zeros true
                           :delimiter                 delimiter
                           :separator                 separator})))))

(def default-option-value "<no value>")

(defn label-options
  [options]
  ;; i18n ignores nil value, leading to misleading messages
  (into {} (for [[k v] options] [k (or v default-option-value)])))

(defn label-fn
  ([path] (label-fn path {}))
  ([path options]
   (if (exists? (.t i18n))
     (let [options (update options :amount label-number)]
       (.t i18n (name path) (clj->js (label-options options))))
     (name path))))

(def label (memoize label-fn))

(defn label-pluralize
  [count path & options]
  (if (exists? (.t i18n))
    (.p i18n count (name path) (clj->js options))
    (name path)))

(def locale
  (.-locale i18n))

(def format-currency goog.i18n/format-currency)

(defn load-language
  [lang loaded-languages]
  (when-let [lang-key (valid-language (keyword lang))]
    (when-not (contains? @loaded-languages lang-key)
      (aset (i18n/get-translations)
            lang
            (require-translation lang-key))
      (swap! loaded-languages conj lang-key))))
