(ns syng-im.handlers.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(def commands [{:text "!phone"}
               {:text "!send"}
               {:text "!request"}
               {:text "!help"}])

(defn get-suggestions [text]
  (when (= (get text 0) "!")
    (filterv #(.startsWith (:text %) text) commands)))

(defn generate-suggestions [db text]
  (assoc-in db db/input-suggestions-path (get-suggestions text)))
