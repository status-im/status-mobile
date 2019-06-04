(ns status-im.goog.i18n-module
  (:require-macros [status-im.modules :as modules]))

(modules/defmodule i18n
  {:mk-fmt          'status-im.goog.i18n/mk-fmt
   :format-currency 'status-im.goog.i18n/format-currency})

(defn mk-fmt [locale format-fn]
  ((get-symbol :mk-fmt) locale format-fn))

(defn format-currency
  ([value currency-code]
   ((get-symbol :format-currency) value currency-code true))
  ([value currency-code currency-symbol?]
   ((get-symbol :format-currency) value currency-code currency-symbol?)))
