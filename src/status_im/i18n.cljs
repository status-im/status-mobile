(ns status-im.i18n
  (:require
   [status-im.react-native.js-dependencies :as rn-dependencies]
   [clojure.string :as string]
   [status-im.i18n-resources :as i18n-resources]))

(set! (.-locale rn-dependencies/i18n) (name i18n-resources/default-device-language))
(set! (.-fallbacks rn-dependencies/i18n) true)
(set! (.-defaultSeparator rn-dependencies/i18n) "/")

(set! (.-translations rn-dependencies/i18n)
      (clj->js i18n-resources/translations-by-locale))

(defn set-language [lang]
  (i18n-resources/load-language lang)
  (set! (.-locale rn-dependencies/i18n) lang))

;;:zh, :zh-hans-xx, :zh-hant-xx have been added until this bug will be fixed https://github.com/fnando/i18n-js/issues/460

(def delimeters
  "This function is a hack: mobile Safari doesn't support toLocaleString(), so we need to pass
  this map to WKWebView to make number formatting work."
  (let [n          (.toLocaleString (js/Number 1000.1))
        delimiter? (= (count n) 7)]
    (if delimiter?
      {:delimiter (subs n 1 2)
       :separator (subs n 5 6)}
      {:delimiter ""
       :separator (subs n 4 5)})))

(defn label-number [number]
  (when number
    (let [{:keys [delimiter separator]} delimeters]
      (.toNumber rn-dependencies/i18n
                 (string/replace number #"," ".")
                 (clj->js {:precision                 10
                           :strip_insignificant_zeros true
                           :delimiter                 delimiter
                           :separator                 separator})))))

(def default-option-value "<no value>")

(defn label-options [options]
  ;; i18n ignores nil value, leading to misleading messages
  (into {} (for [[k v] options] [k (or v default-option-value)])))

(defn label
  ([path] (label path {}))
  ([path options]
   (if (exists? rn-dependencies/i18n.t)
     (let [options (update options :amount label-number)]
       (.t rn-dependencies/i18n (name path) (clj->js (label-options options))))
     (name path))))

(defn label-pluralize [count path & options]
  (if (exists? rn-dependencies/i18n.t)
    (.p rn-dependencies/i18n count (name path) (clj->js options))
    (name path)))

(defn message-status-label [status]
  (->> status
       (name)
       (str "t/status-")
       (keyword)
       (label)))

(def locale
  (.-locale rn-dependencies/i18n))

(defn format-currency
  ([value currency-code]
   (format-currency value currency-code true))
  ([value currency-code currency-symbol?]
   (.addTier2Support goog/i18n.currency)
   (let [currency-code-to-nfs-map {"ZAR" goog/i18n.NumberFormatSymbols_af
                                   "ETB" goog/i18n.NumberFormatSymbols_am
                                   "EGP" goog/i18n.NumberFormatSymbols_ar
                                   "DZD" goog/i18n.NumberFormatSymbols_ar_DZ
                                   "AZN" goog/i18n.NumberFormatSymbols_az
                                   "BYN" goog/i18n.NumberFormatSymbols_be
                                   "BGN" goog/i18n.NumberFormatSymbols_bg
                                   "BDT" goog/i18n.NumberFormatSymbols_bn
                                   "EUR" goog/i18n.NumberFormatSymbols_br
                                   "BAM" goog/i18n.NumberFormatSymbols_bs
                                   "USD" goog/i18n.NumberFormatSymbols_en
                                   "CZK" goog/i18n.NumberFormatSymbols_cs
                                   "GBP" goog/i18n.NumberFormatSymbols_cy
                                   "DKK" goog/i18n.NumberFormatSymbols_da
                                   "CHF" goog/i18n.NumberFormatSymbols_de_CH
                                   "AUD" goog/i18n.NumberFormatSymbols_en_AU
                                   "CAD" goog/i18n.NumberFormatSymbols_en_CA
                                   "INR" goog/i18n.NumberFormatSymbols_en_IN
                                   "SGD" goog/i18n.NumberFormatSymbols_en_SG
                                   "MXN" goog/i18n.NumberFormatSymbols_es_419
                                   "IRR" goog/i18n.NumberFormatSymbols_fa
                                   "PHP" goog/i18n.NumberFormatSymbols_fil
                                   "ILS" goog/i18n.NumberFormatSymbols_he
                                   "HRK" goog/i18n.NumberFormatSymbols_hr
                                   "HUF" goog/i18n.NumberFormatSymbols_hu
                                   "AMD" goog/i18n.NumberFormatSymbols_hy
                                   "IDR" goog/i18n.NumberFormatSymbols_id
                                   "ISK" goog/i18n.NumberFormatSymbols_is
                                   "JPY" goog/i18n.NumberFormatSymbols_ja
                                   "GEL" goog/i18n.NumberFormatSymbols_ka
                                   "KZT" goog/i18n.NumberFormatSymbols_kk
                                   "KHR" goog/i18n.NumberFormatSymbols_km
                                   "KRW" goog/i18n.NumberFormatSymbols_ko
                                   "KGS" goog/i18n.NumberFormatSymbols_ky
                                   "CDF" goog/i18n.NumberFormatSymbols_ln
                                   "LAK" goog/i18n.NumberFormatSymbols_lo
                                   "MKD" goog/i18n.NumberFormatSymbols_mk
                                   "MNT" goog/i18n.NumberFormatSymbols_mn
                                   "MDL" goog/i18n.NumberFormatSymbols_mo
                                   "MYR" goog/i18n.NumberFormatSymbols_ms
                                   "MMK" goog/i18n.NumberFormatSymbols_my
                                   "NOK" goog/i18n.NumberFormatSymbols_nb
                                   "NPR" goog/i18n.NumberFormatSymbols_ne
                                   "PLN" goog/i18n.NumberFormatSymbols_pl
                                   "BRL" goog/i18n.NumberFormatSymbols_pt
                                   "RON" goog/i18n.NumberFormatSymbols_ro
                                   "RUB" goog/i18n.NumberFormatSymbols_ru
                                   "RSD" goog/i18n.NumberFormatSymbols_sh
                                   "LKR" goog/i18n.NumberFormatSymbols_si
                                   "ALL" goog/i18n.NumberFormatSymbols_sq
                                   "SEK" goog/i18n.NumberFormatSymbols_sv
                                   "TZS" goog/i18n.NumberFormatSymbols_sw
                                   "THB" goog/i18n.NumberFormatSymbols_th
                                   "TRY" goog/i18n.NumberFormatSymbols_tr
                                   "UAH" goog/i18n.NumberFormatSymbols_uk
                                   "PKR" goog/i18n.NumberFormatSymbols_ur
                                   "UZS" goog/i18n.NumberFormatSymbols_uz
                                   "VND" goog/i18n.NumberFormatSymbols_vi
                                   "CNY" goog/i18n.NumberFormatSymbols_zh
                                   "HKD" goog/i18n.NumberFormatSymbols_zh_HK
                                   "TWD" goog/i18n.NumberFormatSymbols_zh_TW}
         nfs                      (or (get currency-code-to-nfs-map currency-code)
                                      goog/i18n.NumberFormatSymbols_en)]
     (set! goog/i18n.NumberFormatSymbols
           (if currency-symbol?
             nfs
             (-> nfs
                 (js->clj :keywordize-keys true)
                 ;; Remove any currency symbol placeholders in the pattern
                 (update :CURRENCY_PATTERN (fn [pat]
                                             (string/replace pat #"\s*¤\s*" "")))
                 clj->js)))
     (.format
      (new goog/i18n.NumberFormat goog/i18n.NumberFormat.Format.CURRENCY currency-code)
      value))))

