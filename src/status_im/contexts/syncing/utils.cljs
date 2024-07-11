(ns status-im.contexts.syncing.utils
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [utils.transforms :as transforms]))

(defn validate-connection-string
  [connection-string]
  (native-module/validate-connection-string
   connection-string))

(defn valid-connection-string?
  [connection-string]
  (some-> connection-string
          validate-connection-string
          string/blank?))

(defn extract-error
  [json-str]
  (-> json-str
      transforms/json->clj
      (get :error "")
      not-empty))
