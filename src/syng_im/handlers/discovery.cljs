(ns syng-im.handlers.discovery
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [syng-im.models.discoveries :refer [save-discoveries
                                                discovery-list
                                                signal-discovery-updated
                                                discovery-updated?]]))


;; -- Discovery --------------------------------------------------------------

(register-handler :discovery-response-received
                  (fn [db [_ from payload]]
                    (let [{:keys [status hashtags location]} payload]
                      (save-discoveries [{:name         from
                                                      :status       status
                                                      :whisper-id   from
                                                      :photo        ""
                                                      :location     location
                                                      :tags         hashtags
                                                      :last-updated (js/Date.)}])
                      )))

(register-handler :updated-discoveries
                  (fn [db _]
                    (signal-discovery-updated db)))