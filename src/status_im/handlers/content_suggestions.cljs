(ns status-im.handlers.content-suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.db :as db]
            [status-im.utils.logging :as log]
            [clojure.string :as s]))

;; TODO stub data?
(def suggestions
  {:phone [{:header "Phone number formats"}
           {:value       "89171111111"
            :description "Number format 1"}
           {:value       "+79171111111"
            :description "Number format 2"}
           {:value       "9171111111"
            :description "Number format 3"}]})

(defn get-content-suggestions [db command text]
  (or (when command
        (when-let [command-suggestions ((:command command) suggestions)]
          (filterv (fn [s]
                     (or (:header s)
                         (and (.startsWith (:value s) (or text ""))
                              (not= (:value s) text))))
                   command-suggestions)))
      []))
