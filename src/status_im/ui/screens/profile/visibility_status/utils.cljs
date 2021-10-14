(ns status-im.ui.screens.profile.visibility-status.utils
  (:require [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.profile.visibility-status.styles :as styles]
            [re-frame.core :as re-frame]
            [status-im.utils.datetime :as datetime]))

(def visibility-status-type-data
  {constants/visibility-status-unknown   {:color    styles/color-error
                                          :title    (i18n/label :t/error)}
   constants/visibility-status-online    {:color    styles/color-online
                                          :title    (i18n/label :t/status-online)}
   constants/visibility-status-dnd       {:color    styles/color-dnd
                                          :title    (i18n/label :t/status-dnd)
                                          :subtitle (i18n/label :t/subtitle-dnd)}
   constants/visibility-status-invisible {:color    styles/color-invisible
                                          :title    (i18n/label :t/status-invisible)
                                          :subtitle (i18n/label :t/subtitle-invisible)}})

;; Currently, Another user is broadcasting their status updates at the rate of 5 minutes.
;; So, we need to show that user online a little extra than that time. (broadcast receiving delay)
(defn dot-color [{:keys [public-key status-type clock]}]
  (let [status-lifespan    (datetime/minutes 5.1)
        status-expire-time (+ (datetime/to-mills clock) status-lifespan)
        time-left          (-  status-expire-time (datetime/timestamp))
        status-type        (if (or (nil? status-type)
                                   (and
                                    (= status-type constants/visibility-status-online)
                                    (neg? time-left)))
                             constants/visibility-status-invisible
                             status-type)]
    (when (= status-type constants/visibility-status-online)
      (re-frame/dispatch [:status-updates/countdown-for-online-user public-key clock time-left]))
    (:color (get visibility-status-type-data status-type))))

(defn icon-visibility-status-dot [public-key container-size identicon?]
  (let [status-update @(re-frame/subscribe [:status-updates/status-update public-key])
        size          (/ container-size 4)
        margin        (if identicon? (/ size 6) (/ size 7))
        dot-color     (dot-color status-update)]
    (merge (styles/visibility-status-dot dot-color size)
           {:bottom           margin
            :right            margin
            :position         :absolute})))
