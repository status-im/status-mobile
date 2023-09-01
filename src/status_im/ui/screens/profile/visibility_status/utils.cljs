(ns status-im.ui.screens.profile.visibility-status.utils
  (:require [quo.design-system.colors :as colors]
            [quo2.foundations.colors :as quo2.colors]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [status-im.ui.screens.profile.visibility-status.styles :as styles]
            [utils.datetime :as datetime]
            [utils.re-frame :as rf]))

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
   {:color colors/red
    :title (i18n/label :t/error)}
   constants/visibility-status-automatic
   {:color    quo2.colors/success-50
    :title    (i18n/label :t/status-automatic)
    :subtitle (i18n/label :t/status-automatic-subtitle)}
   constants/visibility-status-dnd
   {:color    colors/color-dnd
    :title    (i18n/label :t/status-dnd)
    :subtitle (i18n/label :t/status-dnd-subtitle)}
   constants/visibility-status-always-online
   {:color quo2.colors/success-50
    :title (i18n/label :t/status-always-online)}
   constants/visibility-status-inactive
   {:color    colors/color-inactive
    :title    (i18n/label :t/status-inactive)
    :subtitle (i18n/label :t/status-inactive-subtitle)}})

(def visibility-status-type-data-old
  {constants/visibility-status-unknown
   {:color colors/red
    :title (i18n/label :t/error)}
   constants/visibility-status-automatic
   {:color    colors/color-online
    :title    (i18n/label :t/status-automatic)
    :subtitle (i18n/label :t/status-automatic-subtitle)}
   constants/visibility-status-dnd
   {:color    colors/color-dnd
    :title    (i18n/label :t/status-dnd)
    :subtitle (i18n/label :t/status-dnd-subtitle)}
   constants/visibility-status-always-online
   {:color colors/color-online
    :title (i18n/label :t/status-always-online)}
   constants/visibility-status-inactive
   {:color    colors/color-inactive
    :title    (i18n/label :t/status-inactive)
    :subtitle (i18n/label :t/status-inactive-subtitle)}})

(defn calculate-real-status-type
  [{:keys [status-type clock]}]
  (let [status-lifespan    (if (= status-type constants/visibility-status-automatic)
                             (datetime/minutes 5)
                             (datetime/weeks 2))
        status-expire-time (+ (datetime/to-ms clock) status-lifespan)
        time-left          (- status-expire-time (datetime/timestamp))]
    (if (or (nil? status-type)
            (and
             (not= status-type constants/visibility-status-inactive)
             (neg? time-left)))
      constants/visibility-status-inactive
      status-type)))

(defn icon-dot-color
  [{:keys [status-type] :or {status-type constants/visibility-status-inactive}}]
  (:color (get visibility-status-type-data status-type)))

(defn icon-dot-accessibility-label
  [dot-color]
  (if (= dot-color quo2.colors/success-50)
    :online-profile-photo-dot
    :offline-profile-photo-dot))

(defn icon-dot-size
  [container-size]
  (/ container-size 2.4))

(defn icon-visibility-status-dot
  [public-key container-size]
  (let [status    (rf/sub [:visibility-status-updates/visibility-status-update public-key])
        size      (icon-dot-size container-size)
        margin    -2
        dot-color (icon-dot-color status)
        new-ui?   true]
    (merge (styles/visibility-status-dot {:color   dot-color
                                          :size    size
                                          :new-ui? new-ui?})
           {:bottom              margin
            :right               margin
            :position            :absolute
            :accessibility-label (icon-dot-accessibility-label dot-color)})))

(defn visibility-status-order
  [public-key]
  (let [status    (rf/sub [:visibility-status-updates/visibility-status-update public-key])
        dot-color (icon-dot-color status)]
    (if (= dot-color colors/color-online) 0 1)))
