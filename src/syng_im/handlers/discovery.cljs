(ns syng-im.handlers.discovery
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [syng-im.utils.logging :as log]
            [syng-im.protocol.api :as api]
            [syng-im.navigation :refer [nav-push
                                        nav-replace
                                        nav-pop]]
            [syng-im.models.discoveries :refer [save-discoveries
                                                set-current-tag
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

(register-handler :show-discovery-tag
                  (fn [db [action tag navigator nav-type]]
                    (log/debug action "setting current tag: " tag)
                    (let [db (set-current-tag db tag)]
                      (dispatch [:navigate-to navigator {:view-id :discovery-tag} nav-type])
                      db)))