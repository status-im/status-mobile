(ns syng-im.handlers.discovery
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [syng-im.utils.logging :as log]
            [syng-im.protocol.api :as api]
            [syng-im.models.discoveries :refer [save-discoveries
                                                signal-discoveries-updated]]))


;; -- Discovery --------------------------------------------------------------

(register-handler :discovery-response-received
                  (fn [db [_ from payload]]
                    (let [{:keys [name status hashtags location]} payload
                          location (if location location "")]
                      (save-discoveries [{:name         name
                                          :status       status
                                          :whisper-id   from
                                          :photo        ""
                                          :location     location
                                          :tags         hashtags
                                          :last-updated (js/Date.)}])
                      (signal-discoveries-updated db))))

(register-handler :updated-discoveries
                  (fn [db _]
                    (signal-discoveries-updated db)))

(register-handler :broadcast-status
                  (fn [db [action status hashtags]]
                    (let [name (:name db)]
                      (log/debug "Status: " status ", Hashtags: " hashtags)
                      (api/broadcast-discover-status name status hashtags)
                      db)))