(ns status-im.multiaccounts.dev
  (:require [status-im.utils.types :as types])
  (:require-macros [status-im.utils.slurp :as slurp]))

;; Predefined accounts can be added to .dev-multiaccounts.json to simplify
;; multiacc creation, restoring, and unlocking during development.
;; Example JSON:
;; ```
;; {
;;   "multiaccounts": {
;;                      "account-name1": "seed-word#1 seed-word#2 ...",
;;                      "account-name2": "seed-word#1 seed-word#2 ..."
;;                    },
;;   "password": "123123"
;; }
;; ```
;; A single password is used for all account, it allows to avoid mapping
;; multiacc -> password mapping during unlocking and in most cases we use a
;; single password during development anyway.

(defn data []
  (when js/goog.DEBUG
    (->
     (slurp/dev-slurp ".dev-multiaccounts.json")
     types/json->clj)))

(defn dev-multiaccounts [] (:multiaccounts (data)))
(defn dev-password [] (:password (data)))
