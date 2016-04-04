(ns syng-im.handlers.sign-up
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.models.commands :as commands]
            [syng-im.utils.utils :refer [log on-error http-post toast]]
            [syng-im.utils.logging :as log]
            [syng-im.utils.random :as random]
            [syng-im.constants :refer [text-content-type
                                       content-type-command]]))

(defn intro [db]
  (dispatch [:received-msg {:msg-id "1"
                            :content "Hello there! It's Syng, a Dapp browser in your phone."
                            :content-type text-content-type
                            :outgoing false
                            :from "console"
                            :to "me"}])
  (dispatch [:received-msg {:msg-id "2"
                            :content "Syng uses  a highly secure key-pair authentication type to provide you a reliable way to access your account"
                            :content-type text-content-type
                            :outgoing false
                            :from "console"
                            :to "me"}])
  (dispatch [:received-msg {:msg-id "3"
                            :content "A key pair has been generated and saved to your device. Create a password to secure your key"
                            :content-type text-content-type
                            :outgoing false
                            :from "console"
                            :to "me"}])
  (dispatch [:set-input-command :keypair-password])
  db)

(defn send-console-msg [text]
  {:msg-id       (random/id)
   :from         "me"
   :to           "console"
   :content      text
   :content-type text-content-type
   :outgoing     true})

;; TODO store command key in a separate field
(defn send-console-command [command content]
  {:msg-id       (random/id)
   :from         "me"
   :to           "console"
   :content      (commands/format-command-msg-content command content)
   :content-type content-type-command
   :outgoing     true})
