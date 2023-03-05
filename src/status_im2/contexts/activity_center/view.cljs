(ns status-im2.contexts.activity-center.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.activity-center.notification-types :as types]
            [status-im2.contexts.activity-center.notification.admin.view :as admin]
            [status-im2.contexts.activity-center.notification.contact-requests.view :as contact-requests]
            [status-im2.contexts.activity-center.notification.contact-verification.view :as
             contact-verification]
            [status-im2.contexts.activity-center.notification.membership.view :as membership]
            [status-im2.contexts.activity-center.notification.mentions.view :as mentions]
            [status-im2.contexts.activity-center.notification.reply.view :as reply]
            [status-im2.contexts.activity-center.style :as style]
            [utils.i18n :as i18n]
            [react-native.blur :as blur]
            [utils.re-frame :as rf]))

(defn filter-selector-read-toggle
  []
  (let [unread-filter-enabled? (rf/sub [:activity-center/filter-status-unread-enabled?])]
    [rn/view]
    ;[quo/filter
    ; {:pressed?       unread-filter-enabled?
    ;  :blur?          true
    ;  :override-theme :dark
    ;  :on-press-out   #(rf/dispatch [:activity-center.notifications/fetch-first-page
    ;                                 {:filter-status (if unread-filter-enabled?
    ;                                                   :all
    ;                                                   :unread)}])}]
    ))

(defn options-bottom-sheet-content
  []
  (let [unread-count (rf/sub [:activity-center/unread-count])]
    [quo/action-drawer
     [[{:icon           :i/check
        :override-theme :dark
        :label          (i18n/label :t/mark-all-notifications-as-read)
        :on-press       (fn []
                          (if (pos? unread-count)
                            (rf/dispatch [:activity-center.notifications/mark-all-as-read-locally
                                          (fn []
                                            {:icon           :up-to-date
                                             :icon-color     colors/success-50
                                             :text           (i18n/label :t/notifications-marked-as-read
                                                                         {:count unread-count})
                                             :override-theme :dark})])
                            ;; Need design improvements if there is NO unread
                            ;; notifications to mark as read
                            ;; https://github.com/status-im/status-mobile/issues/14983
                            (js/alert "No unread notifications to mark as read"))
                          (rf/dispatch [:bottom-sheet/hide]))}]]]))

(defn empty-tab
  [close]
  (let [filter-status (rf/sub [:activity-center/filter-status])]
    [rn/touchable-opacity
     {:style               style/empty-container
      :on-press close
      :accessibility-label :empty-notifications}
     [rn/view {:style style/empty-rectangle-placeholder}]
     [quo/text
      {:size   :paragraph-1
       :style  style/empty-title
       :weight :semi-bold}
      (i18n/label (if (= :unread filter-status)
                    :t/empty-notifications-title-unread
                    :t/empty-notifications-title-read))]
     [quo/text
      {:size  :paragraph-2
       :style style/empty-subtitle}
      (i18n/label (if (= :unread filter-status)
                    :t/empty-notifications-subtitle-unread
                    :t/empty-notifications-subtitle-read))]]))

(defn tabs
  []
  (let [filter-type                   (rf/sub [:activity-center/filter-type])
        types-with-unread             (rf/sub [:activity-center/notification-types-with-unread])
        is-mark-all-as-read-undoable? (boolean (rf/sub
                                                 [:activity-center/mark-all-as-read-undoable-till]))]
    [quo/tabs
     {:size                32
      :scrollable?         true
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
                            {:id                  types/admin
                             :label               (i18n/label :t/admin)
                             :accessibility-label :tab-admin
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/admin))}
                            {:id                  types/mention
                             :label               (i18n/label :t/mentions)
                             :accessibility-label :tab-mention
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/mention))}
                            {:id                  types/reply
                             :label               (i18n/label :t/replies)
                             :accessibility-label :tab-reply
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/reply))}
                            {:id                  types/contact-request
                             :label               (i18n/label :t/contact-requests)
                             :accessibility-label :tab-contact-request
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/contact-request))}
                            {:id                  types/contact-verification
                             :label               (i18n/label :t/identity-verification)
                             :accessibility-label :tab-contact-verification
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread
                                                               types/contact-verification))}
                            {:id                  types/tx
                             :label               (i18n/label :t/transactions)
                             :accessibility-label :tab-tx
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/tx))}
                            {:id                  types/membership
                             :label               (i18n/label :t/membership)
                             :accessibility-label :tab-membership
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/membership))}
                            {:id                  types/system
                             :label               (i18n/label :t/system)
                             :accessibility-label :tab-system
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/system))}]}]))

(defn header
  [request-close]
  [rn/touchable-opacity
   [rn/view {:style style/header-container}
    [quo/button
     {:icon                true
      :type                :blur-bg
      :size                32
      :accessibility-label :close-activity-center
      :override-theme      :dark
      :on-press            request-close}
     :i/close]
    [quo/button
     {:icon                true
      :type                :blur-bg
      :size                32
      :accessibility-label :activity-center-open-more
      :override-theme      :dark
      :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                          {:content        options-bottom-sheet-content
                                           :override-theme :dark}])}
     :i/options]]
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

(defn notification-component
  []
  (let [height               (atom 0)
        set-swipeable-height #(reset! height (oops/oget % "nativeEvent.layout.height"))]
    (fn [{:keys [type] :as notification} index _ active-swipeable]
      (let [swipeable-args {:height           height
                            :active-swipeable active-swipeable
                            :notification     notification}]
        [rn/view {:style (style/notification-container index)}
         (cond
           (= type types/contact-verification)
           [contact-verification/view notification {}]

           (= type types/contact-request)
           [contact-requests/swipeable swipeable-args
            [contact-requests/view notification set-swipeable-height]]

           (= type types/mention)
           [mentions/swipeable swipeable-args
            [mentions/view notification set-swipeable-height]]

           (= type types/reply)
           [reply/swipeable swipeable-args
            [reply/view notification set-swipeable-height]]

           (= type types/admin)
           [admin/swipeable swipeable-args
            [admin/view notification set-swipeable-height]]

           (some types/membership [type])
           [membership/view notification]

           :else
           nil)]))))

(defn view
  [request-close]
  (let [active-swipeable (atom nil)]
    [:f>
     (fn []
       (rn/use-effect-once #(rf/dispatch [:activity-center.notifications/fetch-first-page]))
       [safe-area/consumer
        (fn [{:keys [top bottom]}]
          (let [notifications (rf/sub [:activity-center/notifications])
                window-width  (rf/sub [:dimensions/window-width])
                window-height  (rf/sub [:dimensions/window-height])]
            [blur/view {:style        {:flex 1}
                        :blurAmount   32
                        ;:blurType     :light
                        ;:overlayColor colors/neutral-100
                        }
             [rn/view {:style (style/screen-container window-width window-height top bottom)}
              ;[rn/view {:style {:flex 1}}
              [header request-close]
              [rn/flat-list
               {:data                      notifications
                :render-data               active-swipeable
                :content-container-style   {:flex-grow 1}
                :empty-component           [empty-tab request-close]
                :key-fn                    :id
                :on-scroll-to-index-failed identity
                :on-end-reached            #(rf/dispatch [:activity-center.notifications/fetch-next-page])
                :render-fn                 notification-component}]]]))])]))
