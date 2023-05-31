(ns utils.i18n
  (:require ["i18n-js" :as i18n]
            [clojure.string :as string]
            [utils.i18n-goog :as i18n-goog]))

(defn setup
  [default-device-language translations-by-locale]
  (println "dsdsdasda")
  (set! (.-fallbacks i18n) true)
  (set! (.-defaultSeparator i18n) "/")
  (set! (.-locale i18n) default-device-language)
  (set! (.-translations i18n) translations-by-locale))

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

(def format-currency i18n-goog/format-currency)
