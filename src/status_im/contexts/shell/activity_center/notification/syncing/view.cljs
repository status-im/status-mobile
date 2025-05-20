(ns status-im.contexts.shell.activity-center.notification.syncing.view
  (:require
    quo.context
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.new-device-sheet.view :as new-device-sheet]
    [status-im.contexts.shell.activity-center.notification.common.view :as common]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- swipeable
  [{:keys [extra-fn]} child]
  [common/swipeable
   {:left-button    common/swipe-button-read-or-unread
    :left-on-press  common/swipe-on-press-toggle-read
    :right-button   common/swipe-button-delete
    :right-on-press common/swipe-on-press-delete
    :extra-fn       extra-fn}
   child])

(defn installation-created-view
  [{:keys [notification extra-fn]}]
  (let [{:keys [installation-id read timestamp]} notification
        customization-color (rf/sub [:profile/customization-color])
        theme (quo.context/use-theme)
        more-details (rn/use-callback
                      (fn []
                        (rf/dispatch [:show-bottom-sheet
                                      {:content
                                       (fn []
                                         [new-device-sheet/installation-request-creator-view
                                          installation-id])}]))
                      [installation-id])]
    [swipeable {:extra-fn extra-fn}
     [quo/activity-log
      {:title               (i18n/label :t/sync-your-profile)
       :customization-color customization-color
       :icon                :i/mobile
       :timestamp           (datetime/timestamp->relative timestamp)
       :unread?             (not read)
       :context             [(i18n/label :t/check-other-device-for-pairing)]
       :items               [{:type     :button
                              :subtype  :positive
                              :key      :review-pairing-request
                              :blur?    true
                              :label    (i18n/label :t/more-details)
                              :theme    theme
                              :on-press more-details}]}]]))

(defn installation-received-view
  [{:keys [notification extra-fn]}]
  (let [{:keys [installation-id read timestamp]} notification
        customization-color (rf/sub [:profile/customization-color])
        theme (quo.context/use-theme)
        review-pairing-request (rn/use-callback
                                (fn []
                                  (rf/dispatch [:show-bottom-sheet
                                                {:content
                                                 (fn []
                                                   [new-device-sheet/installation-request-receiver-view
                                                    installation-id])}]))
                                [installation-id])]
    [swipeable {:extra-fn extra-fn}
     [quo/activity-log
      {:title               (i18n/label :t/new-device-detected)
       :customization-color customization-color
       :icon                :i/mobile
       :timestamp           (datetime/timestamp->relative timestamp)
       :unread?             (not read)
       :context             [(i18n/label :t/new-device-detected)]
       :items               [{:type     :button
                              :subtype  :positive
                              :key      :review-pairing-request
                              :blur?    true
                              :label    (i18n/label :t/review-pairing-request)
                              :theme    theme
                              :on-press review-pairing-request}]}]]))
