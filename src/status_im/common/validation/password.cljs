(ns status-im.common.validation.password
  (:require
    [status-im.constants :as constants]
    [utils.string :as utils.string]))

(defn validate-short-enough?
  [password]
  (utils.string/at-least-n-chars? password
                                  constants/new-password-min-length))

(defn validate-long-enough?
  [password]
  (and (validate-short-enough? password)
       (utils.string/at-most-n-chars? password
                                      constants/new-password-max-length)))

(defn validate
  [password]
  (let [validations (juxt
                     utils.string/has-lower-case?
                     utils.string/has-upper-case?
                     utils.string/has-numbers?
                     utils.string/has-symbols?
                     validate-short-enough?
                     validate-long-enough?)]
    (->> password
         validations
         (zipmap constants/password-tips))))
