(ns status-im.contexts.syncing.utils
  (:require
    [clojure.string :as string]
    [status-im.constants :as constants]
    [utils.transforms :as transforms]))

(defn valid-connection-string?
  [connection-string]
  (when connection-string
    (string/starts-with?
     connection-string
     constants/local-pairing-connection-string-identifier)))

(defn extract-error
  [json-str]
  (-> json-str
      transforms/json->clj
      (get :error "")
      not-empty))
