(ns status-im.contexts.shell.activity-center.notification.common.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.contexts.shell.activity-center.context :as ac.context]
    [status-im.contexts.shell.activity-center.notification.common.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn user-avatar-tag
  [user-id]
  (let [profile (rf/sub [:contacts/contact-by-identity user-id])]
    [rn/view
     {:on-start-should-set-responder
      (fn []
        (rf/dispatch [:navigate-back])
        (rf/dispatch [:chat.ui/show-profile user-id])
        true)}
     [quo/context-tag
      {:blur?           true
       :size            24
       :full-name       (profile.utils/displayed-name profile)
       :profile-picture (profile.utils/photo profile)}]]))

(defn- render-swipe-action
  [{:keys [active-swipeable
           extra-fn
           interpolation-opacity
           interpolation-translate-x
           on-press
           swipe-button
           swipeable-ref
           style]}]
  (fn [_ drag-x]
    (let [extra       (extra-fn)
          opacity     (oops/ocall drag-x :interpolate interpolation-opacity)
          translate-x (oops/ocall drag-x :interpolate interpolation-translate-x)]
      [gesture/rect-button
       {:style               (merge {:border-radius style/swipe-button-border-radius} style)
        :accessibility-label :notification-swipe-action-button
        :on-press            (fn []
                               (when @swipeable-ref
                                 (oops/ocall @swipeable-ref :close)
                                 (reset! active-swipeable nil))
                               (on-press extra))}
       [swipe-button
        {:style {:opacity   opacity
                 :transform [{:translateX translate-x}]
                 :flex      1
                 :width     style/swipe-action-width}}
        extra]])))

(defn swipe-button-container
  [{:keys [style icon text]}]
  [rn/animated-view
   {:accessibility-label :notification-swipe
    :style               style}
   [rn/view {:style style/swipe-text-wrapper}
    [quo/icon icon
     {:color colors/white}]
    [quo/text
     {:style  style/swipe-text
      :size   :paragraph-2
      :weight :medium}
     text]]])

(defn swipe-button-read-or-unread
  [{:keys [style]} {:keys [notification]}]
  [swipe-button-container
   {:style (style/swipe-primary-container style)
    :icon  (if (:read notification)
             :i/notifications
             :i/check)
    :text  (if (:read notification)
             (i18n/label :t/unread)
             (i18n/label :t/read))}])

(defn swipe-button-delete
  [{:keys [style]}]
  [swipe-button-container
   {:style (style/swipe-danger-container style)
    :icon  :i/delete
    :text  (i18n/label :t/delete)}])

(defn swipe-on-press-toggle-read
  [{:keys [notification]}]
  (if (:read notification)
    (rf/dispatch [:activity-center.notifications/mark-as-unread (:id notification)])
    (rf/dispatch [:activity-center.notifications/mark-as-read (:id notification)])))

(defn swipe-on-press-delete
  [{:keys [notification]}]
  (rf/dispatch [:activity-center.notifications/delete (:id notification)]))

(defn swipeable
  [{:keys [extra-fn
           left-button
           left-on-press
           right-button
           right-on-press]}
   child]
  (let [{:keys [active-swipeable]} (ac.context/use-context)
        this-swipeable             (rn/use-ref-atom nil)
        set-this-swipeable         (rn/use-callback #(reset! this-swipeable %)
                                                    [this-swipeable])
        on-swipeable-will-open     (rn/use-callback
                                    (fn []
                                      (when (and @active-swipeable
                                                 (not= @active-swipeable @this-swipeable))
                                        (oops/ocall @active-swipeable :close))
                                      (reset! active-swipeable @this-swipeable))
                                    [@active-swipeable @this-swipeable])]
    [gesture/swipeable
     (cond-> {:ref                      set-this-swipeable
              :accessibility-label      :notification-swipeable
              :friction                 2
              :on-swipeable-will-open   on-swipeable-will-open
              :children-container-style {:padding-horizontal 20}}
       left-button
       (assoc :overshoot-left      false
              :left-threshold      style/swipe-action-width
              :render-left-actions (render-swipe-action
                                    {:active-swipeable active-swipeable
                                     :extra-fn extra-fn
                                     :interpolation-opacity style/left-swipe-opacity-interpolation-js
                                     :interpolation-translate-x
                                     style/left-swipe-translate-x-interpolation-js
                                     :on-press left-on-press
                                     :swipe-button left-button
                                     :swipeable-ref this-swipeable
                                     :style {:left style/swipe-button-margin}}))

       right-button
       (assoc :overshoot-right      false
              :right-threshold      style/swipe-action-width
              :render-right-actions (render-swipe-action
                                     {:active-swipeable active-swipeable
                                      :extra-fn extra-fn
                                      :interpolation-opacity style/right-swipe-opacity-interpolation-js
                                      :interpolation-translate-x
                                      style/right-swipe-translate-x-interpolation-js
                                      :on-press right-on-press
                                      :swipe-button right-button
                                      :swipeable-ref this-swipeable
                                      :style {:right (- style/swipe-button-margin)}})))
     child]))
