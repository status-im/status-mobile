(ns legacy.status-im.utils.label
  (:require
    [utils.i18n :as i18n]))

(defn stringify
  [keyword-or-number]
  (cond
    (string? keyword-or-number)
    keyword-or-number

    (and (qualified-keyword? keyword-or-number)
         (= "t" (namespace keyword-or-number)))
    (i18n/label keyword-or-number)

    (and (qualified-keyword? keyword-or-number)
         (not= "t" (namespace keyword-or-number)))
    (str (namespace keyword-or-number) "/" (name keyword-or-number))

    (simple-keyword? keyword-or-number)
    (name keyword-or-number)

    (number? keyword-or-number)
    (str keyword-or-number)

    :else nil))
