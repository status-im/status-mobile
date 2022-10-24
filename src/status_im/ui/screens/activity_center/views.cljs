(ns status-im.ui.screens.activity-center.views
  (:require [quo.components.safe-area :as safe-area]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [quo2.components.scrollable-view :as scrollable-view]
            [status-im.activity-center.notification-types :as types]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.activity-center.notification.contact-request.view :as contact-request]
            [status-im.ui.screens.activity-center.notification.contact-verification.view :as contact-verification]
            [status-im.ui.screens.activity-center.notification.mentions.view :as mentions]
            [status-im.ui.screens.activity-center.style :as style]
            [utils.re-frame :as rf]))

(defn filter-selector-read-toggle
  []
  (let [unread-filter-enabled? (rf/sub [:activity-center/filter-status-unread-enabled?])]
    ;; TODO(@ilmotta): Replace the button by a Filter Selector.
    ;; https://github.com/status-im/status-mobile/issues/14355
    [quo2/button {:icon           true
                  :type           (if unread-filter-enabled? :primary :blur-bg-outline)
                  :size           32
                  :override-theme :dark
                  :on-press       #(rf/dispatch [:activity-center.notifications/fetch-first-page
                                                 {:filter-status (if unread-filter-enabled?
                                                                   :all
                                                                   :unread)}])}
     :i/unread]))

;; TODO(@ilmotta,2022-10-07): The empty state is still under design analysis, so we
;; shouldn't even care about translations at this point. A placeholder box is
;; used instead of an image.
(defn empty-tab
  []
  [rn/view {:style {:align-items      :center
                    :flex             1
                    :justify-content  :center
                    :padding-vertical 12}}
   [rn/view {:style {:background-color colors/neutral-80
                     :height           120
                     :margin-bottom    20
                     :width            120}}]
   [quo2/text {:size   :paragraph-1
               :style  {:padding-bottom 2}
               :weight :semi-bold}
    "No notifications"]
   [quo2/text {:size :paragraph-2}
    "Your notifications will be here"]])

(defn tabs
  []
  (let [filter-type (rf/sub [:activity-center/filter-type])]
    [scrollable-view/view {:size                32
                           :blur?               true
                           :override-theme      :dark
                           :style               style/tabs
                           :fade-end-percentage 0.79
                           :scroll-on-press?    true
                           :fade-end?           true
                           :on-change           #(rf/dispatch [:activity-center.notifications/fetch-first-page {:filter-type %}])
                           :default-active      filter-type
                           :data                [{:id    types/no-type
                                                  :label (i18n/label :t/all)}
                                                 {:id    types/admin
                                                  :label (i18n/label :t/admin)}
                                                 {:id    types/mention
                                                  :label (i18n/label :t/mentions)}
                                                 {:id    types/reply
                                                  :label (i18n/label :t/replies)}
                                                 {:id    types/contact-request
                                                  :label (i18n/label :t/contact-requests)}
                                                 {:id    types/contact-verification
                                                  :label (i18n/label :t/identity-verification)}
                                                 {:id    types/tx
                                                  :label (i18n/label :t/transactions)}
                                                 {:id    types/membership
                                                  :label (i18n/label :t/membership)}
                                                 {:id    types/system
                                                  :label (i18n/label :t/system)}]}]))

(defn header
  []
  (let [screen-padding 20]
    [rn/view
     [quo2/button {:icon                true
                   :type                :blur-bg
                   :size                32
                   :accessibility-label :close-activity-center
                   :override-theme      :dark
                   :style               style/header-button
                   :on-press            #(rf/dispatch [:hide-popover])}
      :i/close]
     [quo2/text {:size   :heading-1
                 :weight :semi-bold
                 :style  style/header-heading}
      (i18n/label :t/notifications)]
     [rn/view {:flex-direction   :row
               :padding-vertical 12}
      [rn/view {:flex       1
                :align-self :stretch}
       [tabs]]
      [rn/view {:flex-grow     0
                :margin-left   16
                :padding-right screen-padding}
       [filter-selector-read-toggle]]]]))

(defn render-notification
  [notification index]
  [rn/view {:style (style/notification-container index)}
   (case (:type notification)
     types/contact-verification
     [contact-verification/view notification {}]

     types/contact-request
     [contact-request/view notification]

     types/mention
     [mentions/view notification]

     nil)])

(defn activity-center
  []
  [:f>
   (fn []
     (let [notifications        (rf/sub [:activity-center/filtered-notifications])
           window-width         (rf/sub [:dimensions/window-width])
           {:keys [top bottom]} (safe-area/use-safe-area)]
       (react/effect! #(rf/dispatch [:activity-center.notifications/fetch-first-page]))
       [rn/view {:style (style/screen-container window-width top bottom)}
        [header]
        [rn/flat-list {:data                      notifications
                       :empty-component           [empty-tab]
                       :key-fn                    :id
                       :on-scroll-to-index-failed identity
                       :on-end-reached            #(rf/dispatch [:activity-center.notifications/fetch-next-page])
                       :render-fn                 render-notification}]]))])
