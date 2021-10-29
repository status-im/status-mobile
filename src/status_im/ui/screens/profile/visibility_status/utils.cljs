(ns status-im.ui.screens.profile.visibility-status.utils
  (:require [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.profile.visibility-status.styles :as styles]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.utils.datetime :as datetime]
            [quo.design-system.colors :as colors]
            [clojure.string :as string]))

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
  {constants/visibility-status-unknown
   {:color    colors/red
    :title    (i18n/label :t/error)}
   constants/visibility-status-automatic
   {:color    colors/color-online
    :title    (i18n/label :t/status-automatic)
    :subtitle (i18n/label :t/status-automatic-subtitle)}
   constants/visibility-status-dnd
   {:color    colors/color-dnd
    :title    (i18n/label :t/status-dnd)
    :subtitle (i18n/label :t/status-dnd-subtitle)}
   constants/visibility-status-always-online
   {:color    colors/color-online
    :title    (i18n/label :t/status-always-online)}
   constants/visibility-status-inactive
   {:color    colors/color-inactive
    :title    (i18n/label :t/status-inactive)
    :subtitle (i18n/label :t/status-inactive-subtitle)}})

;; Currently, Another user is broadcasting their status updates at the rate of 5 minutes.
;; So for status-type automatic, we need to show
;; that user online a little longer than that time. (broadcast receiving delay)
(defn calculate-real-status-type-and-time-left
  [{:keys [status-type clock]}]
  (let [status-lifespan    (if (= status-type
                                  constants/visibility-status-automatic)
                             (datetime/minutes 5.05)
                             (datetime/weeks 2))
        status-expire-time (+ (datetime/to-ms clock) status-lifespan)
        time-left          (-  status-expire-time (datetime/timestamp))
        status-type        (if (or (nil? status-type)
                                   (and
                                    (not= status-type
                                          constants/visibility-status-inactive)
                                    (neg? time-left)))
                             constants/visibility-status-inactive
                             status-type)]
    {:real-status-type status-type
     :time-left        time-left}))

(defn dot-color
  [{:keys [status-type] :as visibility-status-update} my-icon?]
  (if my-icon?
    (if (= status-type constants/visibility-status-inactive)
      colors/color-inactive colors/color-online)
    (let [{:keys [real-status-type]}
          (calculate-real-status-type-and-time-left visibility-status-update)]
      (:color (get visibility-status-type-data real-status-type)))))

(defn my-icon? [public-key]
  (or (string/blank? public-key)
      (= public-key (<sub [:multiaccount/public-key]))))

(defn visibility-status-update [public-key my-icon?]
  (if my-icon?
    (<sub [:multiaccount/current-user-visibility-status])
    (<sub [:visibility-status-updates/visibility-status-update public-key])))

(defn icon-visibility-status-dot [public-key container-size identicon?]
  (let [my-icon?                 (my-icon? public-key)
        visibility-status-update (visibility-status-update public-key my-icon?)
        size                     (/ container-size 4)
        margin                   (if identicon? (/ size 6) (/ size 7))
        dot-color                (dot-color visibility-status-update my-icon?)]
    (merge (styles/visibility-status-dot dot-color size)
           {:bottom           margin
            :right            margin
            :position         :absolute})))

(defn visibility-status-order [public-key]
  (let [my-icon?                 (my-icon? public-key)
        visibility-status-update (visibility-status-update public-key my-icon?)
        dot-color                (dot-color visibility-status-update my-icon?)]
    (if (= dot-color colors/color-online) 0 1)))
