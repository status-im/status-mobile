(ns syng-im.models.commands
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.utils :refer [log toast]]
            [syng-im.persistence.realm :as realm]))

(def commands [{:command :money
                :text "!money"
                :description "Send money"
                :color "#48ba30"
                :suggestion true}
               {:command :location
                :text "!location"
                :description "Send location"
                :color "#9a5dcf"
                :suggestion true}
               {:command :phone
                :text "!phone"
                :description "Send phone number"
                :color "#48ba30"
                :suggestion true}
               {:command :send
                :text "!send"
                :description "Send location"
                :color "#9a5dcf"
                :suggestion true}
               {:command :request
                :text "!request"
                :description "Send request"
                :color "#48ba30"
                :suggestion true}
               {:command :keypair-password
                :text "!keypairPassword"
                :description ""
                :color "#019af0"
                :suggestion false}
               {:command :help
                :text "!help"
                :description "Help"
                :color "#9a5dcf"
                :suggestion true}])

(def suggestions (filterv :suggestion commands))

(defn get-command [command-key]
  (first (filter #(= command-key (:command %)) commands)))
