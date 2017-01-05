(ns status-im.i18n
  (:require
    [status-im.translations.af :as af]
    [status-im.translations.ar :as ar]
    [status-im.translations.de :as de]
    [status-im.translations.de-ch :as de-ch]
    [status-im.translations.en :as en]
    [status-im.translations.es :as es]
    [status-im.translations.es-ar :as es-ar]
    [status-im.translations.fr :as fr]
    [status-im.translations.fr-ch :as fr-ch]
    [status-im.translations.hi :as hi]
    [status-im.translations.hu :as hu]
    [status-im.translations.it :as it]
    [status-im.translations.it-ch :as it-ch]
    [status-im.translations.ja :as ja]
    [status-im.translations.ko :as ko]
    [status-im.translations.nl :as nl]
    [status-im.translations.pl :as pl]
    [status-im.translations.pt-br :as pt-br]
    [status-im.translations.pt-pt :as pt-pt]
    [status-im.translations.ro :as ro]
    [status-im.translations.ru :as ru]
    [status-im.translations.sl :as sl]
    [status-im.translations.sv :as sv]
    [status-im.translations.sw :as sw]
    [status-im.translations.th :as th]
    [status-im.translations.tr :as tr]
    [status-im.translations.uk :as uk]
    [status-im.translations.ur :as ur]
    [status-im.translations.vi :as vi]
    [status-im.translations.zh-hans :as zh-hans]
    [status-im.translations.zh-hant :as zh-hant]
    [status-im.translations.zh-wuu :as zh-wuu]
    [status-im.translations.zh-yue :as zh-yue]
    [status-im.utils.utils :as u]
    [status-im.utils.js-resources :refer [default-contacts]]))

(def i18n (js/require "react-native-i18n"))
(set! (.-fallbacks i18n) true)
(set! (.-defaultSeparator i18n) "/")

(set! (.-translations i18n) (clj->js {:af      af/translations
                                      :ar      ar/translations
                                      :de      de/translations
                                      :de-ch   de-ch/translations
                                      :en      en/translations
                                      :es      es/translations
                                      :es-ar   es-ar/translations
                                      :fr      fr/translations
                                      :fr-ch   fr-ch/translations
                                      :hi      hi/translations
                                      :hu      hu/translations
                                      :it      it/translations
                                      :it-ch   it-ch/translations
                                      :ja      ja/translations
                                      :ko      ko/translations
                                      :nl      nl/translations
                                      :pl      pl/translations
                                      :pt-br   pt-br/translations
                                      :pt-pt   pt-pt/translations
                                      :ro      ro/translations
                                      :ru      ru/translations
                                      :sl      sl/translations
                                      :sv      sv/translations
                                      :sw      sw/translations
                                      :th      th/translations
                                      :tr      tr/translations
                                      :uk      uk/translations
                                      :ur      ur/translations
                                      :vi      vi/translations
                                      :zh-hans zh-hans/translations
                                      :zh-hant zh-hant/translations
                                      :zh-wuu  zh-wuu/translations
                                      :zh-yue  zh-yue/translations}))

(defn label
  ([path] (label path {}))
  ([path options]
   (if (exists? i18n.t)
     (.t i18n (name path) (clj->js options))
     (name path))))

(defn label-pluralize [count path & options]
  (if (exists? i18n.t)
    (.p i18n count (name path) (clj->js options))
    (name path)))

(defn message-status-label [status]
  (->> status
       (name)
       (str "t/status-")
       (keyword)
       (label)))

(def locale
  (.-locale i18n))

(defn get-contact-translated [contact-id key fallback]
  (let [translation #(get-in default-contacts [(keyword contact-id) key (keyword %)])]
    (or (translation locale)
        (translation (subs locale 0 2))
        fallback)))
