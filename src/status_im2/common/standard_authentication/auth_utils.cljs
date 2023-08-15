(ns status-im2.common.standard-authentication.auth-utils
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]))

(defn get-error-message
  [error]
  (if (and (some? error)
           (or (= error "file is not a database")
               (string/starts-with? error "failed to set ")
               (string/starts-with? error "Failed")))
    (i18n/label :t/oops-wrong-password)
    error))
