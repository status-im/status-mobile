(ns status-im.common.validation.password
  (:require
    [status-im.constants :as constants]
    [utils.string :as utils.string]))

(defn validate-short-enough?
  [password]
  (utils.string/at-most-n-chars? password
                                 constants/new-password-max-length))

(defn validate-long-enough?
  [password]
  (utils.string/at-least-n-chars? password
                                  constants/new-password-min-length))

(defn validate
  [password]
  (let [validations (juxt
                     utils.string/has-lower-case?
                     utils.string/has-upper-case?
                     utils.string/has-numbers?
                     utils.string/has-symbols?
                     validate-long-enough?
                     validate-short-enough?)]
    (->> password
         validations
         (zipmap (conj constants/password-tips
                       :long-enough?
                       :short-enough?)))))

(def password-tip? (set constants/password-tips))

(defn strength
  [validations]
  (->> validations
       (filter #(and (password-tip? (key %)) (val %)))
       (count)))
