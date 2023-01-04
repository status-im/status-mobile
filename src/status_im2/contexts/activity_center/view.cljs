(ns status-im2.contexts.activity-center.view
  (:require [i18n.i18n :as i18n]
            [quo.react :as react]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.activity-center.notification-types :as types]
            [status-im2.contexts.activity-center.notification.contact-request.view :as contact-request]
            [status-im2.contexts.activity-center.notification.contact-verification.view :as
             contact-verification]
            [status-im2.contexts.activity-center.notification.mentions.view :as mentions]
            [status-im2.contexts.activity-center.style :as style]
            [utils.re-frame :as rf]))

(defn filter-selector-read-toggle
  []
  (let [unread-filter-enabled? (rf/sub [:activity-center/filter-status-unread-enabled?])]
    [quo/filter
     {:pressed?       unread-filter-enabled?
      :blur?          true
      :override-theme :dark
      :on-press-out   #(rf/dispatch [:activity-center.notifications/fetch-first-page
                                     {:filter-status (if unread-filter-enabled?
                                                       :all
                                                       :unread)}])}]))

(defn empty-tab
  []
  [rn/view
   {:style               style/empty-container
    :accessibility-label :empty-notifications}
   [quo/icon :i/placeholder]
   [quo/text
    {:size   :paragraph-1
     :style  style/empty-title
     :weight :semi-bold}
    (i18n/label :t/empty-notifications-title)]
   [quo/text {:size :paragraph-2}
    (i18n/label :t/empty-notifications-subtitle)]])

(defn tabs
  []
  (let [filter-type (rf/sub [:activity-center/filter-type])]
    [quo/scrollable-tabs
     {:size                32
      :blur?               true
      :override-theme      :dark
      :style               style/tabs
      :fade-end-percentage 0.79
      :scroll-on-press?    true
      :fade-end?           true
      :on-change           #(rf/dispatch [:activity-center.notifications/fetch-first-page
                                          {:filter-type %}])
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
  [rn/view
   [quo/button
    {:icon                true
     :type                :blur-bg
     :size                32
     :accessibility-label :close-activity-center
     :override-theme      :dark
     :style               style/header-button
     :on-press            #(rf/dispatch [:hide-popover])}
    :i/close]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-heading}
    (i18n/label :t/notifications)]
   [rn/view {:style style/tabs-and-filter-container}
    [rn/view {:style style/tabs-container}
     [tabs]]
    [rn/view {:style style/filter-toggle-container}
     [filter-selector-read-toggle]]]])

(defn render-notification
  [notification index]
  [reanimated/view
   {:style    (style/notification-container index)
    ;; The `:layout` property is only necessary when not rendering notifications
    ;; in a FlatList.
    :layout   reanimated/linear-transition
    :entering (-> ^js reanimated/fade-in-left-animation (.duration 250) (.delay (* index 100)))
    :exiting  (-> ^js reanimated/fade-out-right-animation (.duration 150))}
   (case (:type notification)
     types/contact-verification
     [contact-verification/view notification {}]

     types/contact-request
     [contact-request/view notification]

     types/mention
     [mentions/view notification]

     nil)])

(defn view
  []
  [:f>
   (fn []
     (react/effect! #(rf/dispatch [:activity-center.notifications/fetch-first-page]))
     [safe-area/consumer
      (fn [{:keys [top bottom]}]
        (let [notifications (rf/sub [:activity-center/filtered-notifications])
              window-width  (rf/sub [:dimensions/window-width])]
          [rn/view {:style (style/screen-container window-width top bottom)}
           [header]
           ;; Set to true to see insertions/removals causing the layout to
           ;; animate correctly. When false, the FlatList is not animated at all
           ;; on layout changes.
           ;;
           ;; Issue:
           ;; https://github.com/software-mansion/react-native-reanimated/issues/2737
           (if true
             (if (seq notifications)
               (map-indexed (fn [index notification]
                              ^{:key (:id notification)}
                              [render-notification notification index])
                            notifications)
               [empty-tab])
             [rn/flat-list-animated
              {:data                      notifications
               ;; This property seems to be necessary to tell Reanimated which
               ;; transition to use to animate layout changes. Unfortunately it
               ;; doesn't work.
               ;;
               ;; Comment:
               ;; https://github.com/software-mansion/react-native-reanimated/issues/2737#issuecomment-1077956272
               :item-layout-animation     reanimated/linear-transition
               :content-container-style   {:flex-grow 1}
               :empty-component           [empty-tab]
               :key-fn                    :id
               :on-scroll-to-index-failed identity
               :on-end-reached            #(rf/dispatch [:activity-center.notifications/fetch-next-page])
               :render-fn                 render-notification}])]))])])
