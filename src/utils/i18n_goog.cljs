(ns utils.i18n-goog
  (:require
    [clojure.string :as string]
    goog.i18n.CompactNumberFormatSymbols
    goog.i18n.CompactNumberFormatSymbols_af
    goog.i18n.CompactNumberFormatSymbols_am
    goog.i18n.CompactNumberFormatSymbols_ar
    goog.i18n.CompactNumberFormatSymbols_ar_DZ
    goog.i18n.CompactNumberFormatSymbols_ar_EG
    goog.i18n.CompactNumberFormatSymbols_az
    goog.i18n.CompactNumberFormatSymbols_be
    goog.i18n.CompactNumberFormatSymbols_bg
    goog.i18n.CompactNumberFormatSymbols_bn
    goog.i18n.CompactNumberFormatSymbols_br
    goog.i18n.CompactNumberFormatSymbols_bs
    goog.i18n.CompactNumberFormatSymbols_ca
    goog.i18n.CompactNumberFormatSymbols_chr
    goog.i18n.CompactNumberFormatSymbols_cs
    goog.i18n.CompactNumberFormatSymbols_cy
    goog.i18n.CompactNumberFormatSymbols_da
    goog.i18n.CompactNumberFormatSymbols_de
    goog.i18n.CompactNumberFormatSymbols_de_AT
    goog.i18n.CompactNumberFormatSymbols_de_CH
    goog.i18n.CompactNumberFormatSymbols_el
    goog.i18n.CompactNumberFormatSymbols_en
    goog.i18n.CompactNumberFormatSymbols_en_AU
    goog.i18n.CompactNumberFormatSymbols_en_CA
    goog.i18n.CompactNumberFormatSymbols_en_GB
    goog.i18n.CompactNumberFormatSymbols_en_IE
    goog.i18n.CompactNumberFormatSymbols_en_IN
    goog.i18n.CompactNumberFormatSymbols_en_SG
    goog.i18n.CompactNumberFormatSymbols_en_US
    goog.i18n.CompactNumberFormatSymbols_en_ZA
    goog.i18n.CompactNumberFormatSymbols_es
    goog.i18n.CompactNumberFormatSymbols_es_419
    goog.i18n.CompactNumberFormatSymbols_es_ES
    goog.i18n.CompactNumberFormatSymbols_es_MX
    goog.i18n.CompactNumberFormatSymbols_es_US
    goog.i18n.CompactNumberFormatSymbols_et
    goog.i18n.CompactNumberFormatSymbols_eu
    goog.i18n.CompactNumberFormatSymbols_fa
    goog.i18n.CompactNumberFormatSymbols_fi
    goog.i18n.CompactNumberFormatSymbols_fil
    goog.i18n.CompactNumberFormatSymbols_fr
    goog.i18n.CompactNumberFormatSymbols_fr_CA
    goog.i18n.CompactNumberFormatSymbols_ga
    goog.i18n.CompactNumberFormatSymbols_gl
    goog.i18n.CompactNumberFormatSymbols_gsw
    goog.i18n.CompactNumberFormatSymbols_gu
    goog.i18n.CompactNumberFormatSymbols_haw
    goog.i18n.CompactNumberFormatSymbols_he
    goog.i18n.CompactNumberFormatSymbols_hi
    goog.i18n.CompactNumberFormatSymbols_hr
    goog.i18n.CompactNumberFormatSymbols_hu
    goog.i18n.CompactNumberFormatSymbols_hy
    goog.i18n.CompactNumberFormatSymbols_id
    goog.i18n.CompactNumberFormatSymbols_in
    goog.i18n.CompactNumberFormatSymbols_is
    goog.i18n.CompactNumberFormatSymbols_it
    goog.i18n.CompactNumberFormatSymbols_iw
    goog.i18n.CompactNumberFormatSymbols_ja
    goog.i18n.CompactNumberFormatSymbols_ka
    goog.i18n.CompactNumberFormatSymbols_kk
    goog.i18n.CompactNumberFormatSymbols_km
    goog.i18n.CompactNumberFormatSymbols_kn
    goog.i18n.CompactNumberFormatSymbols_ko
    goog.i18n.CompactNumberFormatSymbols_ky
    goog.i18n.CompactNumberFormatSymbols_ln
    goog.i18n.CompactNumberFormatSymbols_lo
    goog.i18n.CompactNumberFormatSymbols_lt
    goog.i18n.CompactNumberFormatSymbols_lv
    goog.i18n.CompactNumberFormatSymbols_mk
    goog.i18n.CompactNumberFormatSymbols_ml
    goog.i18n.CompactNumberFormatSymbols_mn
    goog.i18n.CompactNumberFormatSymbols_mo
    goog.i18n.CompactNumberFormatSymbols_mr
    goog.i18n.CompactNumberFormatSymbols_ms
    goog.i18n.CompactNumberFormatSymbols_mt
    goog.i18n.CompactNumberFormatSymbols_my
    goog.i18n.CompactNumberFormatSymbols_nb
    goog.i18n.CompactNumberFormatSymbols_ne
    goog.i18n.CompactNumberFormatSymbols_nl
    goog.i18n.CompactNumberFormatSymbols_no
    goog.i18n.CompactNumberFormatSymbols_no_NO
    goog.i18n.CompactNumberFormatSymbols_or
    goog.i18n.CompactNumberFormatSymbols_pa
    goog.i18n.CompactNumberFormatSymbols_pl
    goog.i18n.CompactNumberFormatSymbols_pt
    goog.i18n.CompactNumberFormatSymbols_pt_BR
    goog.i18n.CompactNumberFormatSymbols_pt_PT
    goog.i18n.CompactNumberFormatSymbols_ro
    goog.i18n.CompactNumberFormatSymbols_ru
    goog.i18n.CompactNumberFormatSymbols_sh
    goog.i18n.CompactNumberFormatSymbols_si
    goog.i18n.CompactNumberFormatSymbols_sk
    goog.i18n.CompactNumberFormatSymbols_sl
    goog.i18n.CompactNumberFormatSymbols_sq
    goog.i18n.CompactNumberFormatSymbols_sr
    goog.i18n.CompactNumberFormatSymbols_sr_Latn
    goog.i18n.CompactNumberFormatSymbols_sv
    goog.i18n.CompactNumberFormatSymbols_sw
    goog.i18n.CompactNumberFormatSymbols_ta
    goog.i18n.CompactNumberFormatSymbols_te
    goog.i18n.CompactNumberFormatSymbols_th
    goog.i18n.CompactNumberFormatSymbols_tl
    goog.i18n.CompactNumberFormatSymbols_tr
    goog.i18n.CompactNumberFormatSymbols_uk
    goog.i18n.CompactNumberFormatSymbols_ur
    goog.i18n.CompactNumberFormatSymbols_uz
    goog.i18n.CompactNumberFormatSymbols_vi
    goog.i18n.CompactNumberFormatSymbols_zh
    goog.i18n.CompactNumberFormatSymbols_zh_CN
    goog.i18n.CompactNumberFormatSymbols_zh_HK
    goog.i18n.CompactNumberFormatSymbols_zh_TW
    goog.i18n.CompactNumberFormatSymbols_zu
    goog.i18n.currency
    goog.i18n.currency.CurrencyInfo
    goog.i18n.currency.CurrencyInfoTier2
    goog.i18n.DateTimeFormat
    goog.i18n.DateTimeFormat.Format
    goog.i18n.DateTimeSymbols
    goog.i18n.DateTimeSymbols_af
    goog.i18n.DateTimeSymbols_am
    goog.i18n.DateTimeSymbols_ar
    goog.i18n.DateTimeSymbols_ar_DZ
    goog.i18n.DateTimeSymbols_ar_EG
    goog.i18n.DateTimeSymbols_az
    goog.i18n.DateTimeSymbols_be
    goog.i18n.DateTimeSymbols_bg
    goog.i18n.DateTimeSymbols_bn
    goog.i18n.DateTimeSymbols_br
    goog.i18n.DateTimeSymbols_bs
    goog.i18n.DateTimeSymbols_ca
    goog.i18n.DateTimeSymbols_chr
    goog.i18n.DateTimeSymbols_cs
    goog.i18n.DateTimeSymbols_cy
    goog.i18n.DateTimeSymbols_da
    goog.i18n.DateTimeSymbols_de
    goog.i18n.DateTimeSymbols_de_AT
    goog.i18n.DateTimeSymbols_de_CH
    goog.i18n.DateTimeSymbols_el
    goog.i18n.DateTimeSymbols_en
    goog.i18n.DateTimeSymbols_en_AU
    goog.i18n.DateTimeSymbols_en_CA
    goog.i18n.DateTimeSymbols_en_GB
    goog.i18n.DateTimeSymbols_en_IE
    goog.i18n.DateTimeSymbols_en_IN
    goog.i18n.DateTimeSymbols_en_ISO
    goog.i18n.DateTimeSymbols_en_SG
    goog.i18n.DateTimeSymbols_en_US
    goog.i18n.DateTimeSymbols_en_ZA
    goog.i18n.DateTimeSymbols_es
    goog.i18n.DateTimeSymbols_es_419
    goog.i18n.DateTimeSymbols_es_ES
    goog.i18n.DateTimeSymbols_es_MX
    goog.i18n.DateTimeSymbols_es_US
    goog.i18n.DateTimeSymbols_et
    goog.i18n.DateTimeSymbols_eu
    goog.i18n.DateTimeSymbols_fa
    goog.i18n.DateTimeSymbols_fi
    goog.i18n.DateTimeSymbols_fil
    goog.i18n.DateTimeSymbols_fr
    goog.i18n.DateTimeSymbols_fr_CA
    goog.i18n.DateTimeSymbols_ga
    goog.i18n.DateTimeSymbols_gl
    goog.i18n.DateTimeSymbols_gsw
    goog.i18n.DateTimeSymbols_gu
    goog.i18n.DateTimeSymbols_haw
    goog.i18n.DateTimeSymbols_he
    goog.i18n.DateTimeSymbols_hi
    goog.i18n.DateTimeSymbols_hr
    goog.i18n.DateTimeSymbols_hu
    goog.i18n.DateTimeSymbols_hy
    goog.i18n.DateTimeSymbols_id
    goog.i18n.DateTimeSymbols_in
    goog.i18n.DateTimeSymbols_is
    goog.i18n.DateTimeSymbols_it
    goog.i18n.DateTimeSymbols_iw
    goog.i18n.DateTimeSymbols_ja
    goog.i18n.DateTimeSymbols_ka
    goog.i18n.DateTimeSymbols_kk
    goog.i18n.DateTimeSymbols_km
    goog.i18n.DateTimeSymbols_kn
    goog.i18n.DateTimeSymbols_ko
    goog.i18n.DateTimeSymbols_ky
    goog.i18n.DateTimeSymbols_ln
    goog.i18n.DateTimeSymbols_lo
    goog.i18n.DateTimeSymbols_lt
    goog.i18n.DateTimeSymbols_lv
    goog.i18n.DateTimeSymbols_mk
    goog.i18n.DateTimeSymbols_ml
    goog.i18n.DateTimeSymbols_mn
    goog.i18n.DateTimeSymbols_mo
    goog.i18n.DateTimeSymbols_mr
    goog.i18n.DateTimeSymbols_ms
    goog.i18n.DateTimeSymbols_mt
    goog.i18n.DateTimeSymbols_my
    goog.i18n.DateTimeSymbols_nb
    goog.i18n.DateTimeSymbols_ne
    goog.i18n.DateTimeSymbols_nl
    goog.i18n.DateTimeSymbols_no
    goog.i18n.DateTimeSymbols_no_NO
    goog.i18n.DateTimeSymbols_or
    goog.i18n.DateTimeSymbols_pa
    goog.i18n.DateTimeSymbols_pl
    goog.i18n.DateTimeSymbols_pt
    goog.i18n.DateTimeSymbols_pt_BR
    goog.i18n.DateTimeSymbols_pt_PT
    goog.i18n.DateTimeSymbols_ro
    goog.i18n.DateTimeSymbols_ru
    goog.i18n.DateTimeSymbols_sh
    goog.i18n.DateTimeSymbols_si
    goog.i18n.DateTimeSymbols_sk
    goog.i18n.DateTimeSymbols_sl
    goog.i18n.DateTimeSymbols_sq
    goog.i18n.DateTimeSymbols_sr
    goog.i18n.DateTimeSymbols_sr_Latn
    goog.i18n.DateTimeSymbols_sv
    goog.i18n.DateTimeSymbols_sw
    goog.i18n.DateTimeSymbols_ta
    goog.i18n.DateTimeSymbols_te
    goog.i18n.DateTimeSymbols_th
    goog.i18n.DateTimeSymbols_tl
    goog.i18n.DateTimeSymbols_tr
    goog.i18n.DateTimeSymbols_uk
    goog.i18n.DateTimeSymbols_ur
    goog.i18n.DateTimeSymbols_uz
    goog.i18n.DateTimeSymbols_vi
    goog.i18n.DateTimeSymbols_zh
    goog.i18n.DateTimeSymbols_zh_CN
    goog.i18n.DateTimeSymbols_zh_HK
    goog.i18n.DateTimeSymbols_zh_TW
    goog.i18n.DateTimeSymbols_zu
    goog.i18n.DateTimeSymbolsType
    goog.i18n.MessageFormat
    goog.i18n.NumberFormat
    goog.i18n.NumberFormat.CurrencyStyle
    goog.i18n.NumberFormat.Format
    goog.i18n.ordinalRules
    goog.i18n.pluralRules
    goog.i18n.TimeZone))

(def locales
  {"af"      ^js goog.i18n.DateTimeSymbols_af
   "am"      ^js goog.i18n.DateTimeSymbols_am
   "ar"      ^js goog.i18n.DateTimeSymbols_ar
   "ar_DZ"   ^js goog.i18n.DateTimeSymbols_ar_DZ
   "ar_EG"   ^js goog.i18n.DateTimeSymbols_ar_EG
   "az"      ^js goog.i18n.DateTimeSymbols_az
   "be"      ^js goog.i18n.DateTimeSymbols_be
   "bg"      ^js goog.i18n.DateTimeSymbols_bg
   "bn"      ^js goog.i18n.DateTimeSymbols_bn
   "br"      ^js goog.i18n.DateTimeSymbols_br
   "bs"      ^js goog.i18n.DateTimeSymbols_bs
   "ca"      ^js goog.i18n.DateTimeSymbols_ca
   "chr"     ^js goog.i18n.DateTimeSymbols_chr
   "cs"      ^js goog.i18n.DateTimeSymbols_cs
   "cy"      ^js goog.i18n.DateTimeSymbols_cy
   "da"      ^js goog.i18n.DateTimeSymbols_da
   "de"      ^js goog.i18n.DateTimeSymbols_de
   "de_AT"   ^js goog.i18n.DateTimeSymbols_de_AT
   "de_CH"   ^js goog.i18n.DateTimeSymbols_de_CH
   "el"      ^js goog.i18n.DateTimeSymbols_el
   "en"      ^js goog.i18n.DateTimeSymbols_en
   "en_AU"   ^js goog.i18n.DateTimeSymbols_en_AU
   "en_CA"   ^js goog.i18n.DateTimeSymbols_en_CA
   "en_GB"   ^js goog.i18n.DateTimeSymbols_en_GB
   "en_IE"   ^js goog.i18n.DateTimeSymbols_en_IE
   "en_IN"   ^js goog.i18n.DateTimeSymbols_en_IN
   "en_ISO"  ^js goog.i18n.DateTimeSymbols_en_ISO
   "en_SG"   ^js goog.i18n.DateTimeSymbols_en_SG
   "en_US"   ^js goog.i18n.DateTimeSymbols_en_US
   "en_ZA"   ^js goog.i18n.DateTimeSymbols_en_ZA
   "es"      ^js goog.i18n.DateTimeSymbols_es
   "es_419"  ^js goog.i18n.DateTimeSymbols_es_419
   "es_ES"   ^js goog.i18n.DateTimeSymbols_es_ES
   "es_MX"   ^js goog.i18n.DateTimeSymbols_es_MX
   "es_US"   ^js goog.i18n.DateTimeSymbols_es_US
   "et"      ^js goog.i18n.DateTimeSymbols_et
   "eu"      ^js goog.i18n.DateTimeSymbols_eu
   "fa"      ^js goog.i18n.DateTimeSymbols_fa
   "fi"      ^js goog.i18n.DateTimeSymbols_fi
   "fil"     ^js goog.i18n.DateTimeSymbols_fil
   "fr"      ^js goog.i18n.DateTimeSymbols_fr
   "fr_CA"   ^js goog.i18n.DateTimeSymbols_fr_CA
   "ga"      ^js goog.i18n.DateTimeSymbols_ga
   "gl"      ^js goog.i18n.DateTimeSymbols_gl
   "gsw"     ^js goog.i18n.DateTimeSymbols_gsw
   "gu"      ^js goog.i18n.DateTimeSymbols_gu
   "haw"     ^js goog.i18n.DateTimeSymbols_haw
   "he"      ^js goog.i18n.DateTimeSymbols_he
   "hi"      ^js goog.i18n.DateTimeSymbols_hi
   "hr"      ^js goog.i18n.DateTimeSymbols_hr
   "hu"      ^js goog.i18n.DateTimeSymbols_hu
   "hy"      ^js goog.i18n.DateTimeSymbols_hy
   "id"      ^js goog.i18n.DateTimeSymbols_id
   "in"      ^js goog.i18n.DateTimeSymbols_in
   "is"      ^js goog.i18n.DateTimeSymbols_is
   "it"      ^js goog.i18n.DateTimeSymbols_it
   "iw"      ^js goog.i18n.DateTimeSymbols_iw
   "ja"      ^js goog.i18n.DateTimeSymbols_ja
   "ka"      ^js goog.i18n.DateTimeSymbols_ka
   "kk"      ^js goog.i18n.DateTimeSymbols_kk
   "km"      ^js goog.i18n.DateTimeSymbols_km
   "kn"      ^js goog.i18n.DateTimeSymbols_kn
   "ko"      ^js goog.i18n.DateTimeSymbols_ko
   "ky"      ^js goog.i18n.DateTimeSymbols_ky
   "ln"      ^js goog.i18n.DateTimeSymbols_ln
   "lo"      ^js goog.i18n.DateTimeSymbols_lo
   "lt"      ^js goog.i18n.DateTimeSymbols_lt
   "lv"      ^js goog.i18n.DateTimeSymbols_lv
   "mk"      ^js goog.i18n.DateTimeSymbols_mk
   "ml"      ^js goog.i18n.DateTimeSymbols_ml
   "mn"      ^js goog.i18n.DateTimeSymbols_mn
   "mo"      ^js goog.i18n.DateTimeSymbols_mo
   "mr"      ^js goog.i18n.DateTimeSymbols_mr
   "ms"      ^js goog.i18n.DateTimeSymbols_ms
   "mt"      ^js goog.i18n.DateTimeSymbols_mt
   "my"      ^js goog.i18n.DateTimeSymbols_my
   "nb"      ^js goog.i18n.DateTimeSymbols_nb
   "ne"      ^js goog.i18n.DateTimeSymbols_ne
   "nl"      ^js goog.i18n.DateTimeSymbols_nl
   "no"      ^js goog.i18n.DateTimeSymbols_no
   "no_NO"   ^js goog.i18n.DateTimeSymbols_no_NO
   "or"      ^js goog.i18n.DateTimeSymbols_or
   "pa"      ^js goog.i18n.DateTimeSymbols_pa
   "pl"      ^js goog.i18n.DateTimeSymbols_pl
   "pt"      ^js goog.i18n.DateTimeSymbols_pt
   "pt_BR"   ^js goog.i18n.DateTimeSymbols_pt_BR
   "pt_PT"   ^js goog.i18n.DateTimeSymbols_pt_PT
   "ro"      ^js goog.i18n.DateTimeSymbols_ro
   "ru"      ^js goog.i18n.DateTimeSymbols_ru
   "sh"      ^js goog.i18n.DateTimeSymbols_sh
   "si"      ^js goog.i18n.DateTimeSymbols_si
   "sk"      ^js goog.i18n.DateTimeSymbols_sk
   "sl"      ^js goog.i18n.DateTimeSymbols_sl
   "sq"      ^js goog.i18n.DateTimeSymbols_sq
   "sr"      ^js goog.i18n.DateTimeSymbols_sr
   "sr_Latn" ^js goog.i18n.DateTimeSymbols_sr_Latn
   "sv"      ^js goog.i18n.DateTimeSymbols_sv
   "sw"      ^js goog.i18n.DateTimeSymbols_sw
   "ta"      ^js goog.i18n.DateTimeSymbols_ta
   "te"      ^js goog.i18n.DateTimeSymbols_te
   "th"      ^js goog.i18n.DateTimeSymbols_th
   "tl"      ^js goog.i18n.DateTimeSymbols_tl
   "tr"      ^js goog.i18n.DateTimeSymbols_tr
   "uk"      ^js goog.i18n.DateTimeSymbols_uk
   "ur"      ^js goog.i18n.DateTimeSymbols_ur
   "uz"      ^js goog.i18n.DateTimeSymbols_uz
   "vi"      ^js goog.i18n.DateTimeSymbols_vi
   "zh"      ^js goog.i18n.DateTimeSymbols_zh
   "zh_CN"   ^js goog.i18n.DateTimeSymbols_zh_CN
   "zh_HK"   ^js goog.i18n.DateTimeSymbols_zh_HK
   "zh_TW"   ^js goog.i18n.DateTimeSymbols_zh_TW
   "zu"      ^js goog.i18n.DateTimeSymbols_zu})

;; xx-YY locale, xx locale or en fallback
(defn locale-symbols
  [locale-name]
  (if-let [loc (get locales locale-name)]
    loc
    (let [name-first (string/replace (or locale-name "") #"-.*$" "")
          loc        (get locales name-first)]
      (or loc goog.i18n.DateTimeSymbols_en))))

;; get formatter for current locale symbols and format function
(defn mk-fmt
  [locale format-fn]
  (let [locsym (locale-symbols locale)]
    (goog.i18n.DateTimeFormat. (format-fn locsym) locsym)))

(defn format-currency
  "Formats an amount of a currency based on the currency pattern
  If currency-symbol? is false, the currency symbol is excluded from the
  formatting"
  [value currency-code]
  (.addTier2Support ^js goog.i18n.currency)
  (let [currency-code-to-nfs-map {"ZAR" ^js goog.i18n.NumberFormatSymbols_af
                                  "ETB" ^js goog.i18n.NumberFormatSymbols_am
                                  "EGP" ^js goog.i18n.NumberFormatSymbols_ar
                                  "DZD" ^js goog.i18n.NumberFormatSymbols_ar_DZ
                                  "AZN" ^js goog.i18n.NumberFormatSymbols_az
                                  "BYN" ^js goog.i18n.NumberFormatSymbols_be
                                  "BGN" ^js goog.i18n.NumberFormatSymbols_bg
                                  "BDT" ^js goog.i18n.NumberFormatSymbols_bn
                                  "EUR" ^js goog.i18n.NumberFormatSymbols_br
                                  "BAM" ^js goog.i18n.NumberFormatSymbols_bs
                                  "USD" ^js goog.i18n.NumberFormatSymbols_en
                                  "CZK" ^js goog.i18n.NumberFormatSymbols_cs
                                  "GBP" ^js goog.i18n.NumberFormatSymbols_cy
                                  "DKK" ^js goog.i18n.NumberFormatSymbols_da
                                  "CHF" ^js goog.i18n.NumberFormatSymbols_de_CH
                                  "AUD" ^js goog.i18n.NumberFormatSymbols_en_AU
                                  "CAD" ^js goog.i18n.NumberFormatSymbols_en_CA
                                  "INR" ^js goog.i18n.NumberFormatSymbols_en_IN
                                  "SGD" ^js goog.i18n.NumberFormatSymbols_en_SG
                                  "MXN" ^js goog.i18n.NumberFormatSymbols_es_419
                                  "IRR" ^js goog.i18n.NumberFormatSymbols_fa
                                  "PHP" ^js goog.i18n.NumberFormatSymbols_fil
                                  "ILS" ^js goog.i18n.NumberFormatSymbols_he
                                  "HRK" ^js goog.i18n.NumberFormatSymbols_hr
                                  "HUF" ^js goog.i18n.NumberFormatSymbols_hu
                                  "AMD" ^js goog.i18n.NumberFormatSymbols_hy
                                  "IDR" ^js goog.i18n.NumberFormatSymbols_id
                                  "ISK" ^js goog.i18n.NumberFormatSymbols_is
                                  "JPY" ^js goog.i18n.NumberFormatSymbols_ja
                                  "GEL" ^js goog.i18n.NumberFormatSymbols_ka
                                  "KZT" ^js goog.i18n.NumberFormatSymbols_kk
                                  "KHR" ^js goog.i18n.NumberFormatSymbols_km
                                  "KRW" ^js goog.i18n.NumberFormatSymbols_ko
                                  "KGS" ^js goog.i18n.NumberFormatSymbols_ky
                                  "CDF" ^js goog.i18n.NumberFormatSymbols_ln
                                  "LAK" ^js goog.i18n.NumberFormatSymbols_lo
                                  "MKD" ^js goog.i18n.NumberFormatSymbols_mk
                                  "MNT" ^js goog.i18n.NumberFormatSymbols_mn
                                  "MDL" ^js goog.i18n.NumberFormatSymbols_mo
                                  "MYR" ^js goog.i18n.NumberFormatSymbols_ms
                                  "MMK" ^js goog.i18n.NumberFormatSymbols_my
                                  "NOK" ^js goog.i18n.NumberFormatSymbols_nb
                                  "NPR" ^js goog.i18n.NumberFormatSymbols_ne
                                  "PLN" ^js goog.i18n.NumberFormatSymbols_pl
                                  "BRL" ^js goog.i18n.NumberFormatSymbols_pt
                                  "RON" ^js goog.i18n.NumberFormatSymbols_ro
                                  "RUB" ^js goog.i18n.NumberFormatSymbols_ru
                                  "RSD" ^js goog.i18n.NumberFormatSymbols_sh
                                  "LKR" ^js goog.i18n.NumberFormatSymbols_si
                                  "ALL" ^js goog.i18n.NumberFormatSymbols_sq
                                  "SEK" ^js goog.i18n.NumberFormatSymbols_sv
                                  "TZS" ^js goog.i18n.NumberFormatSymbols_sw
                                  "THB" ^js goog.i18n.NumberFormatSymbols_th
                                  "TRY" ^js goog.i18n.NumberFormatSymbols_tr
                                  "UAH" ^js goog.i18n.NumberFormatSymbols_uk
                                  "PKR" ^js goog.i18n.NumberFormatSymbols_ur
                                  "UZS" ^js goog.i18n.NumberFormatSymbols_uz
                                  "VND" ^js goog.i18n.NumberFormatSymbols_vi
                                  "CNY" ^js goog.i18n.NumberFormatSymbols_zh
                                  "HKD" ^js goog.i18n.NumberFormatSymbols_zh_HK
                                  "TWD" ^js goog.i18n.NumberFormatSymbols_zh_TW}
        nfs                      (or (get currency-code-to-nfs-map currency-code)
                                     ^js goog.i18n.NumberFormatSymbols_en)]
    (.format
     ^js
     (new ^js goog.i18n.NumberFormat
          (let [pattern (.-CURRENCY_PATTERN ^js nfs)]
            (string/replace pattern #"\s*¤\s*" "")))
     value)))
