(ns status-im.test.i18n
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.i18n :as i18n]
            [clojure.set :as set]
            [cljs.spec.alpha :as spec]))

(deftest label-options
  (is (not (nil? (:key (i18n/label-options {:key nil}))))))

(deftest locales-only-have-existing-tran-ids
  (is (spec/valid? ::i18n/labels (i18n/labels-for-all-locales))
      (->> i18n/locales
           (remove #(spec/valid? ::i18n/labels (i18n/locale->labels %)))
           (map (fn [l]
                  (str "Extra translations in locale " l "\n"
                       (set/difference (i18n/locale->labels l) i18n/labels)
                       "\n\n")))
           (apply str))))

(deftest supported-locales-are-actually-supported
  (is (set/subset? i18n/supported-locales (i18n/actual-supported-locales))
      (->> i18n/supported-locales
           (remove i18n/locale-is-supported-based-on-translations?)
           (map (fn [l]
                  (str "Missing translations in supported locale " l "\n"
                       (set/difference (i18n/checkpoint->labels i18n/checkpoint-to-consider-locale-supported)
                                       (i18n/locale->labels l))
                       "\n\n")))
           (apply str))))
