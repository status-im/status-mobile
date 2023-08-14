(ns status-im2.contexts.shell.activity-center.header.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.shell.activity-center.style :as style]
            [quo2.core :as quo]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [status-im2.contexts.shell.activity-center.tabs.view :as tabs]
            [status-im2.contexts.shell.activity-center.drawer.view :as drawer]))

(defn filter-selector-read-toggle
  []
  (let [customization-color    (rf/sub [:profile/customization-color])
        unread-filter-enabled? (rf/sub [:activity-center/filter-status-unread-enabled?])]
    [quo/filter
     {:pressed?            unread-filter-enabled?
      :customization-color customization-color
      :blur?               true
      :on-press-out        #(rf/dispatch [:activity-center.notifications/fetch-first-page
                                          {:filter-status (if unread-filter-enabled?
                                                            :all
                                                            :unread)}])}]))

(defn header
  []
  [rn/view
   [rn/view {:style style/header-container}
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :close-activity-center
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/close]
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :activity-center-open-more
      :on-press            #(rf/dispatch [:show-bottom-sheet
                                          {:content drawer/options
                                           :theme   :dark}])}
     :i/options]]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-heading}
    (i18n/label :t/notifications)]
   [rn/view {:style style/tabs-and-filter-container}
    [rn/view {:style style/tabs-container}
     [tabs/tabs]]
    [rn/view {:style style/filter-toggle-container}
     [filter-selector-read-toggle]]]])
