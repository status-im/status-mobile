(ns syng-im.handlers.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(def commands [{:command :phone
                :text "!phone"
                :description "Send phone number"}
               {:command :send
                :text "!send"
                :description "Send location"}
               {:command :request
                :text "!request"
                :description "Send request"}
               {:command :help
                :text "!help"
                :description "Help"}])

(defn get-suggestions [text]
  (if (= (get text 0) "!")
    (filterv #(.startsWith (:text %) text) commands)
    []))

(defn generate-suggestions [db text]
  (assoc-in db db/input-suggestions-path (get-suggestions text)))
