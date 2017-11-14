(ns status-im.test.i18n)

;; translator: add your language to this list
(def languages-to-test ["ko"])

(defmacro translations []
  (mapv (fn [language]
          (vector language (symbol language "translations")))
        languages-to-test))
