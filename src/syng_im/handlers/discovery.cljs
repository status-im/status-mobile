(ns syng-im.handlers.discovery
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [syng-im.utils.debug :refer [log]]
            [syng-im.protocol.api :as api]
            [syng-im.models.discoveries :refer [save-discoveries
                                                discovery-list
                                                signal-discovery-updated
                                                discovery-updated?]]))


;; -- Discovery --------------------------------------------------------------

(register-handler :discovery-response-received
                  (fn [db [_ from payload]]
                    (let [{:keys [status hashtags location]} payload
                          location (if location location "")]
                      (save-discoveries [{:name         from
                                                      :status       status
                                                      :whisper-id   from
                                                      :photo        ""
                                                      :location     location
                                                      :tags         hashtags
                                                      :last-updated (js/Date.)}])
                      (signal-discovery-updated db)
                      db)))

(register-handler :updated-discoveries
                  (fn [db _]
                    (signal-discovery-updated db)
                    db))

(register-handler :broadcast-status
                  (fn [db [action status hashtags]]
                    (let [_ (log "Status: " status)
                          _ (log "Hashtags: " hashtags)
                          name (:name db)]
                      (api/broadcast-discover-status name status hashtags)
                      db)))