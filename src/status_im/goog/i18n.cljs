(ns status-im.goog.i18n
  (:require [clojure.string :as string]
            goog.i18n.DateTimeSymbols
            goog.i18n.DateTimeSymbolsType
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
            goog.i18n.MessageFormat
            goog.i18n.NumberFormat
            goog.i18n.NumberFormat.CurrencyStyle
            goog.i18n.NumberFormat.Format
            goog.i18n.ordinalRules
            goog.i18n.pluralRules
            goog.i18n.TimeZone))

(def locales
  {"af"      goog/i18n.DateTimeSymbols_af
   "am"      goog/i18n.DateTimeSymbols_am
   "ar"      goog/i18n.DateTimeSymbols_ar
   "ar_DZ"   goog/i18n.DateTimeSymbols_ar_DZ
   "ar_EG"   goog/i18n.DateTimeSymbols_ar_EG
   "az"      goog/i18n.DateTimeSymbols_az
   "be"      goog/i18n.DateTimeSymbols_be
   "bg"      goog/i18n.DateTimeSymbols_bg
   "bn"      goog/i18n.DateTimeSymbols_bn
   "br"      goog/i18n.DateTimeSymbols_br
   "bs"      goog/i18n.DateTimeSymbols_bs
   "ca"      goog/i18n.DateTimeSymbols_ca
   "chr"     goog/i18n.DateTimeSymbols_chr
   "cs"      goog/i18n.DateTimeSymbols_cs
   "cy"      goog/i18n.DateTimeSymbols_cy
   "da"      goog/i18n.DateTimeSymbols_da
   "de"      goog/i18n.DateTimeSymbols_de
   "de_AT"   goog/i18n.DateTimeSymbols_de_AT
   "de_CH"   goog/i18n.DateTimeSymbols_de_CH
   "el"      goog/i18n.DateTimeSymbols_el
   "en"      goog/i18n.DateTimeSymbols_en
   "en_AU"   goog/i18n.DateTimeSymbols_en_AU
   "en_CA"   goog/i18n.DateTimeSymbols_en_CA
   "en_GB"   goog/i18n.DateTimeSymbols_en_GB
   "en_IE"   goog/i18n.DateTimeSymbols_en_IE
   "en_IN"   goog/i18n.DateTimeSymbols_en_IN
   "en_ISO"  goog/i18n.DateTimeSymbols_en_ISO
   "en_SG"   goog/i18n.DateTimeSymbols_en_SG
   "en_US"   goog/i18n.DateTimeSymbols_en_US
   "en_ZA"   goog/i18n.DateTimeSymbols_en_ZA
   "es"      goog/i18n.DateTimeSymbols_es
   "es_419"  goog/i18n.DateTimeSymbols_es_419
   "es_ES"   goog/i18n.DateTimeSymbols_es_ES
   "es_MX"   goog/i18n.DateTimeSymbols_es_MX
   "es_US"   goog/i18n.DateTimeSymbols_es_US
   "et"      goog/i18n.DateTimeSymbols_et
   "eu"      goog/i18n.DateTimeSymbols_eu
   "fa"      goog/i18n.DateTimeSymbols_fa
   "fi"      goog/i18n.DateTimeSymbols_fi
   "fil"     goog/i18n.DateTimeSymbols_fil
   "fr"      goog/i18n.DateTimeSymbols_fr
   "fr_CA"   goog/i18n.DateTimeSymbols_fr_CA
   "ga"      goog/i18n.DateTimeSymbols_ga
   "gl"      goog/i18n.DateTimeSymbols_gl
   "gsw"     goog/i18n.DateTimeSymbols_gsw
   "gu"      goog/i18n.DateTimeSymbols_gu
   "haw"     goog/i18n.DateTimeSymbols_haw
   "he"      goog/i18n.DateTimeSymbols_he
   "hi"      goog/i18n.DateTimeSymbols_hi
   "hr"      goog/i18n.DateTimeSymbols_hr
   "hu"      goog/i18n.DateTimeSymbols_hu
   "hy"      goog/i18n.DateTimeSymbols_hy
   "id"      goog/i18n.DateTimeSymbols_id
   "in"      goog/i18n.DateTimeSymbols_in
   "is"      goog/i18n.DateTimeSymbols_is
   "it"      goog/i18n.DateTimeSymbols_it
   "iw"      goog/i18n.DateTimeSymbols_iw
   "ja"      goog/i18n.DateTimeSymbols_ja
   "ka"      goog/i18n.DateTimeSymbols_ka
   "kk"      goog/i18n.DateTimeSymbols_kk
   "km"      goog/i18n.DateTimeSymbols_km
   "kn"      goog/i18n.DateTimeSymbols_kn
   "ko"      goog/i18n.DateTimeSymbols_ko
   "ky"      goog/i18n.DateTimeSymbols_ky
   "ln"      goog/i18n.DateTimeSymbols_ln
   "lo"      goog/i18n.DateTimeSymbols_lo
   "lt"      goog/i18n.DateTimeSymbols_lt
   "lv"      goog/i18n.DateTimeSymbols_lv
   "mk"      goog/i18n.DateTimeSymbols_mk
   "ml"      goog/i18n.DateTimeSymbols_ml
   "mn"      goog/i18n.DateTimeSymbols_mn
   "mo"      goog/i18n.DateTimeSymbols_mo
   "mr"      goog/i18n.DateTimeSymbols_mr
   "ms"      goog/i18n.DateTimeSymbols_ms
   "mt"      goog/i18n.DateTimeSymbols_mt
   "my"      goog/i18n.DateTimeSymbols_my
   "nb"      goog/i18n.DateTimeSymbols_nb
   "ne"      goog/i18n.DateTimeSymbols_ne
   "nl"      goog/i18n.DateTimeSymbols_nl
   "no"      goog/i18n.DateTimeSymbols_no
   "no_NO"   goog/i18n.DateTimeSymbols_no_NO
   "or"      goog/i18n.DateTimeSymbols_or
   "pa"      goog/i18n.DateTimeSymbols_pa
   "pl"      goog/i18n.DateTimeSymbols_pl
   "pt"      goog/i18n.DateTimeSymbols_pt
   "pt_BR"   goog/i18n.DateTimeSymbols_pt_BR
   "pt_PT"   goog/i18n.DateTimeSymbols_pt_PT
   "ro"      goog/i18n.DateTimeSymbols_ro
   "ru"      goog/i18n.DateTimeSymbols_ru
   "sh"      goog/i18n.DateTimeSymbols_sh
   "si"      goog/i18n.DateTimeSymbols_si
   "sk"      goog/i18n.DateTimeSymbols_sk
   "sl"      goog/i18n.DateTimeSymbols_sl
   "sq"      goog/i18n.DateTimeSymbols_sq
   "sr"      goog/i18n.DateTimeSymbols_sr
   "sr_Latn" goog/i18n.DateTimeSymbols_sr_Latn
   "sv"      goog/i18n.DateTimeSymbols_sv
   "sw"      goog/i18n.DateTimeSymbols_sw
   "ta"      goog/i18n.DateTimeSymbols_ta
   "te"      goog/i18n.DateTimeSymbols_te
   "th"      goog/i18n.DateTimeSymbols_th
   "tl"      goog/i18n.DateTimeSymbols_tl
   "tr"      goog/i18n.DateTimeSymbols_tr
   "uk"      goog/i18n.DateTimeSymbols_uk
   "ur"      goog/i18n.DateTimeSymbols_ur
   "uz"      goog/i18n.DateTimeSymbols_uz
   "vi"      goog/i18n.DateTimeSymbols_vi
   "zh"      goog/i18n.DateTimeSymbols_zh
   "zh_CN"   goog/i18n.DateTimeSymbols_zh_CN
   "zh_HK"   goog/i18n.DateTimeSymbols_zh_HK
   "zh_TW"   goog/i18n.DateTimeSymbols_zh_TW
   "zu"      goog/i18n.DateTimeSymbols_zu})

;; xx-YY locale, xx locale or en fallback
(defn locale-symbols [locale-name]
  (if-let [loc (get locales locale-name)]
    loc
    (let [name-first (string/replace (or locale-name "") #"-.*$" "")
          loc        (get locales name-first)]
      (or loc goog/i18n.DateTimeSymbols_en))))

;; get formatter for current locale symbols and format function
(defn mk-fmt [locale format-fn]
  (let [locsym (locale-symbols locale)]
    (goog/i18n.DateTimeFormat. (format-fn locsym) locsym)))

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
