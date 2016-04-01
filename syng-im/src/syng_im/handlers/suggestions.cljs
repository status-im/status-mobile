(ns syng-im.handlers.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(def commands [{:command :money
                :text "!money"
                :description "Send money"
                :color "#48ba30"}
               {:command :location
                :text "!location"
                :description "Send location"
                :color "#9a5dcf"}
               {:command :phone
                :text "!phone"
                :description "Send phone number"
                :color "#48ba30"}
               {:command :send
                :text "!send"
                :description "Send location"
                :color "#9a5dcf"}
               {:command :request
                :text "!request"
                :description "Send request"
                :color "#48ba30"}
               {:command :help
                :text "!help"
                :description "Help"
                :color "#9a5dcf"}])

(defn get-suggestions [text]
  (if (= (get text 0) "!")
    (filterv #(.startsWith (:text %) text) commands)
    []))

(defn generate-suggestions [db text]
  (assoc-in db db/input-suggestions-path (get-suggestions text)))
