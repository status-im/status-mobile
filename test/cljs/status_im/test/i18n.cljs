(ns status-im.test.i18n
  (:require-macros [status-im.test.i18n :refer [translations]])
  (:require [cljs.test :refer-macros [deftest is]]
            [clojure.data :as data]
            [status-im.i18n :as i18n]
            [status-im.translations.af :as af]
            [status-im.translations.ar :as ar]
            [status-im.translations.bel :as be]
            [status-im.translations.cs :as cs]
            [status-im.translations.da :as da]
            [status-im.translations.de :as de]
            [status-im.translations.de-ch :as de-ch]
            [status-im.translations.en :as en]
            [status-im.translations.es :as es]
            [status-im.translations.es-ar :as es-ar]
            [status-im.translations.es-mx :as es-mx]
            [status-im.translations.fi :as fi]
            [status-im.translations.fr :as fr]
            [status-im.translations.fr-ch :as fr-ch]
            [status-im.translations.fy :as fy]
            [status-im.translations.he :as he]
            [status-im.translations.hi :as hi]
            [status-im.translations.hu :as hu]
            [status-im.translations.id :as id]
            [status-im.translations.it :as it]
            [status-im.translations.it-ch :as it-ch]
            [status-im.translations.ja :as ja]
            [status-im.translations.ko :as ko]
            [status-im.translations.la :as la]
            [status-im.translations.lt :as lt]
            [status-im.translations.lv :as lv]
            [status-im.translations.ms :as ms]
            [status-im.translations.nb :as nb]
            [status-im.translations.ne :as ne]
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
            [cljs.reader :as reader]))

;; developpers: don't forget to add keys to these list when you add a key to the english translations
(def missing-keys-to-ignore [:status-sending])
;; developpers: don't forget to add keys to these list when you remove a key to the english translations
(def keys-to-remove-to-ignore [])

(deftest label-options
  (is (not (nil? (:key (i18n/label-options {:key nil}))))))

(defn to-keyset [translation]
  (into #{} (keys translation)))

(defn compare-to-en [translations]
  (let [[to-remove missing _] (data/diff (to-keyset translations)
                                         (to-keyset en/translations))
        to-remove (apply disj to-remove keys-to-remove-to-ignore)
        missing (apply disj missing missing-keys-to-ignore)]
    [to-remove missing]))

(defn list-translations [translations]
  (str "\n- " (apply str (interpose "\n- " (into [] translations)))))

(deftest test-translations
  (doall (for [[language translation] (translations)]
           (let [[to-remove missing _] (compare-to-en translation)]
             (is (empty? to-remove) (str "The following keys need to be removed for " language " translation: " (list-translations to-remove)))
             (is (empty? missing) (str "The following keys are missing for " language " translation: " (list-translations missing)))))))
