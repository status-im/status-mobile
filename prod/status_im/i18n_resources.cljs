(ns status-im.i18n-resources
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def default-device-language
  (keyword (.-language rn-dependencies/react-native-languages)))

(def languages [:af :ar :bel :cs :da :de :de_ch :el :en :es :es_419 :es_ar :es_mx :fa :fi :fr :fr_ch :fy :he :hi :hu :id :it_ch :ja :ko :la :lt :lv :ms :nb :ne :nl :pl :pt_br :pt_pt :ro :ru :sl :sr_rs_cyrl :sr_rs :latn :sv :sw :th :tr :uk :ur :vi :zh_Hans_CN :zh_hans :zh_hant :zh_hant_hk :zh_hant_sg :zh_hant_tw :zh_wuu :zh_yue])

(defonce loaded-languages
  (atom
   (conj #{:en} default-device-language)))

   (def prod-translations
     {:af         (js/require "status-modules/translations/af.json")
      :ar         (js/require "status-modules/translations/ar.json")
      :bel        (js/require "status-modules/translations/bel.json")
      :cs         (js/require "status-modules/translations/cs.json")
      :da         (js/require "status-modules/translations/da.json")
      :de         (js/require "status-modules/translations/de.json")
      :de_ch      (js/require "status-modules/translations/de_ch.json")
      :el         (js/require "status-modules/translations/el.json")
      :en         (js/require "status-modules/translations/en.json")
      :es         (js/require "status-modules/translations/es.json")
      :es_419     (js/require "status-modules/translations/es_419.json")
      :es_ar      (js/require "status-modules/translations/es_ar.json")
      :es_mx      (js/require "status-modules/translations/es_mx.json")
      :fa         (js/require "status-modules/translations/fa.json")
      :fi         (js/require "status-modules/translations/fi.json")
      :fr         (js/require "status-modules/translations/fr.json")
      :fr_ch      (js/require "status-modules/translations/fr_ch.json")
      :fy         (js/require "status-modules/translations/fy.json")
      :he         (js/require "status-modules/translations/he.json")
      :hi         (js/require "status-modules/translations/hi.json")
      :hu         (js/require "status-modules/translations/hu.json")
      :id         (js/require "status-modules/translations/id.json")
      :it         (js/require "status-modules/translations/it.json")
      :it_ch      (js/require "status-modules/translations/it_ch.json")
      :ja         (js/require "status-modules/translations/ja.json")
      :ko         (js/require "status-modules/translations/ko.json")
      :la         (js/require "status-modules/translations/la.json")
      :lt         (js/require "status-modules/translations/lt.json")
      :lv         (js/require "status-modules/translations/lv.json")
      :ms         (js/require "status-modules/translations/ms.json")
      :nb         (js/require "status-modules/translations/nb.json")
      :ne         (js/require "status-modules/translations/ne.json")
      :nl         (js/require "status-modules/translations/nl.json")
      :pl         (js/require "status-modules/translations/pl.json")
      :pt_br      (js/require "status-modules/translations/pt_br.json")
      :pt_pt      (js/require "status-modules/translations/pt_pt.json")
      :ro         (js/require "status-modules/translations/ro.json")
      :ru         (js/require "status-modules/translations/ru.json")
      :sl         (js/require "status-modules/translations/sl.json")
      :sr_rs_cyrl (js/require "status-modules/translations/sr_rs_cyrl.json")
      :sr_rs_latn (js/require "status-modules/translations/sr_rs_latn.json")
      :sv         (js/require "status-modules/translations/sv.json")
      :sw         (js/require "status-modules/translations/sw.json")
      :th         (js/require "status-modules/translations/th.json")
      :tr         (js/require "status-modules/translations/tr.json")
      :uk         (js/require "status-modules/translations/uk.json")
      :ur         (js/require "status-modules/translations/ur.json")
      :vi         (js/require "status-modules/translations/vi.json")
      :zh_Hans_CN (js/require "status-modules/translations/zh_Hans_CN.json")
      :zh_hans    (js/require "status-modules/translations/zh_hans.json")
      :zh_hant    (js/require "status-modules/translations/zh_hant.json")
      :zh_hant_hk (js/require "status-modules/translations/zh_hant_hk.json")
      :zh_hant_sg (js/require "status-modules/translations/zh_hant_sg.json")
      :zh_hant_tw (js/require "status-modules/translations/zh_hant_tw.json")
      :zh_wuu     (js/require "status-modules/translations/zh_wuu.json")
      :zh_yue     (js/require "status-modules/translations/zh_yue.json")})

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
