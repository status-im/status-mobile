(ns status-im.ui.screens.profile.visibility-status.utils
  (:require [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.profile.visibility-status.styles :as styles]
            [re-frame.core :as re-frame]
            [status-im.utils.datetime :as datetime]))

;; Specs:
;; :visibility-status-automatic
;;        To Send - "visibility-status-automatic" status ping every 5 minutes
;;        Display - Online for up to 5 minutes from the last clock, after that Offline
;; :visibility-status-always-online
;;        To Send - "visibility-status-always-online" status ping every 5 minutes
;;        Display - Online for up to 2 weeks from the last clock, after that Offline
;; :visibility-status-inactive
;;        To Send - A single "visibility-status-inactive" status ping
;;        Display - Offline forever
;; Note: Only send pings if the user interacted with the app in the last x minutes.
(def visibility-status-type-data
  {constants/visibility-status-unknown       {:color    styles/color-error
                                              :title    (i18n/label :t/error)}
   constants/visibility-status-automatic     {:color    styles/color-online
                                              :title    (i18n/label :t/status-automatic)
                                              :subtitle (i18n/label :t/status-automatic-subtitle)}
   constants/visibility-status-dnd           {:color    styles/color-dnd
                                              :title    (i18n/label :t/status-dnd)
                                              :subtitle (i18n/label :t/status-dnd-subtitle)}
   constants/visibility-status-always-online {:color    styles/color-online
                                              :title    (i18n/label :t/status-always-online)
                                              :alias    (i18n/label :t/status-always-online-alias)}
   constants/visibility-status-inactive      {:color    styles/color-inactive
                                              :title    (i18n/label :t/status-inactive)
                                              :subtitle (i18n/label :t/status-inactive-subtitle)}})

;; Currently, Another user is broadcasting their status updates at the rate of 5 minutes.
;; So for status-type automatic, we need to show that user online a little longer than that time. (broadcast receiving delay)
(defn dot-color [{:keys [public-key status-type clock]}]
  (let [status-lifespan    (if (= status-type constants/visibility-status-automatic)
                             (datetime/minutes 5.05)
                             (datetime/weeks 2))
        status-expire-time (+ (datetime/to-mills clock) status-lifespan)
        time-left          (-  status-expire-time (datetime/timestamp))
        status-type        (if (or (nil? status-type)
                                   (and
                                    (not= status-type constants/visibility-status-inactive)
                                    (neg? time-left)))
                             constants/visibility-status-inactive
                             status-type)]
    (when (= status-type constants/visibility-status-automatic)
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
