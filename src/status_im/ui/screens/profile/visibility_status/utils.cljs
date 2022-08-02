(ns status-im.ui.screens.profile.visibility-status.utils
  (:require [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.profile.visibility-status.styles :as styles]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.utils.datetime :as datetime]
            [quo.design-system.colors :as colors]
            [clojure.string :as string]
            [quo2.foundations.colors :as quo2.colors]))

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
   {:color    quo2.colors/color-online
    :title    (i18n/label :t/status-automatic)
    :subtitle (i18n/label :t/status-automatic-subtitle)}
   constants/visibility-status-dnd
   {:color    colors/color-dnd
    :title    (i18n/label :t/status-dnd)
    :subtitle (i18n/label :t/status-dnd-subtitle)}
   constants/visibility-status-always-online
   {:color    quo2.colors/color-online
    :title    (i18n/label :t/status-always-online)}
   constants/visibility-status-inactive
   {:color    colors/color-inactive
    :title    (i18n/label :t/status-inactive)
    :subtitle (i18n/label :t/status-inactive-subtitle)}})

(def visibility-status-type-data-old
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

(defn dot-color [{:keys [status-type] :or {status-type constants/visibility-status-inactive}}]
  (:color (get visibility-status-type-data status-type)))

(defn dot-color-old [{:keys [status-type] :or {status-type constants/visibility-status-inactive}}]
  (:color (get visibility-status-type-data-old status-type)))

(defn my-icon? [public-key]
  (or (string/blank? public-key)
      (= public-key (<sub [:multiaccount/public-key]))))

(defn visibility-status-update [public-key my-icon?]
  (if my-icon?
    (<sub [:multiaccount/current-user-visibility-status])
    (<sub [:visibility-status-updates/visibility-status-update public-key])))

(defn icon-visibility-status-dot [public-key container-size _]
  (let [my-icon?                 (my-icon? public-key)
        visibility-status-update (visibility-status-update public-key my-icon?)
        size                     (/ container-size 2.4)
        margin                   -2
        dot-color                (dot-color visibility-status-update)
        accessibility-label      (if (= dot-color quo2.colors/color-online)
                                   :online-profile-photo-dot
                                   :offline-profile-photo-dot)]
    (merge (styles/visibility-status-dot dot-color size)
           {:bottom              margin
            :right               margin
            :position            :absolute
            :accessibility-label accessibility-label})))

(defn icon-visibility-status-dot-old [public-key container-size identicon?]
  (let [my-icon?                 (my-icon? public-key)
        visibility-status-update (visibility-status-update public-key my-icon?)
        size                     (/ container-size 4)
        margin                   (if identicon? (/ size 6) (/ size 7))
        dot-color                (dot-color-old visibility-status-update)
        accessibility-label      (if (= dot-color colors/color-online)
                                   :online-profile-photo-dot
                                   :offline-profile-photo-dot)]
    (merge (styles/visibility-status-dot-old dot-color size)
           {:bottom              margin
            :right               margin
            :position            :absolute
            :accessibility-label accessibility-label})))

(defn visibility-status-order [public-key]
  (let [my-icon?                 (my-icon? public-key)
        visibility-status-update (visibility-status-update public-key my-icon?)
        dot-color                (dot-color visibility-status-update)]
    (if (= dot-color colors/color-online) 0 1)))
