(ns status-im2.contexts.activity-center.notification.common.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [quo2.foundations.colors :as colors]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im2.contexts.activity-center.notification.common.style :as style]
            [status-im2.contexts.activity-center.utils :as activity-center.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn user-avatar-tag
  [user-id]
  (let [contact (rf/sub [:contacts/contact-by-identity user-id])]
    [quo/user-avatar-tag
     {:color          :purple
      :override-theme :dark
      :size           :small
      :style          style/user-avatar-tag
      :text-style     style/user-avatar-tag-text}
     (activity-center.utils/contact-name contact)
     (multiaccounts/displayed-photo contact)]))

(defn- render-swipe-action
  [{:keys [active-swipeable
           extra-fn
           interpolation-opacity
           interpolation-translate-x
           on-press
           swipe-button
           swipeable-ref]}]
  (fn [_ ^js drag-x]
    (let [{:keys [height] :as extra} (extra-fn)
          opacity                    (.interpolate drag-x interpolation-opacity)
          translate-x                (.interpolate drag-x interpolation-translate-x)]
      [gesture/rect-button
       {:style               {:border-radius style/swipe-button-border-radius}
        :accessibility-label :notification-swipe-action-button
        :on-press            (fn []
                               (when @swipeable-ref
                                 (.close ^js @swipeable-ref)
                                 (reset! active-swipeable nil))
                               (on-press extra))}
       [swipe-button
        {:style {:opacity   opacity
                 :transform [{:translateX translate-x}]
                 :height    height}}
        extra]])))

(defn- close-active-swipeable
  [active-swipeable swipeable]
  (fn [_]
    (when (and @active-swipeable
               (not= @active-swipeable @swipeable))
      (.close ^js @active-swipeable))
    (reset! active-swipeable @swipeable)))

(defn left-swipe-button
  [{:keys [style]} {:keys [notification]}]
  [rn/animated-view
   {:accessibility-label :notification-left-swipe
    :style               (style/left-swipe-container style)}
   [rn/view {:style style/swipe-text-wrapper}
    [quo/icon
     (if (:read notification)
       :i/notifications
       :i/check)
     {:color colors/white}]
    [quo/text {:style style/swipe-text}
     (if (:read notification)
       (i18n/label :t/unread)
       (i18n/label :t/read))]]])

(defn right-swipe-button
  [{:keys [style]}]
  [rn/animated-view
   {:accessibility-label :notification-right-swipe
    :style               (style/right-swipe-container style)}
   [rn/view {:style style/swipe-text-wrapper}
    [quo/icon :i/delete {:color colors/white}]
    [quo/text {:style style/swipe-text}
     (i18n/label :t/delete)]]])

(defn left-swipe-on-press
  [{:keys [notification]}]
  (if (:read notification)
    (rf/dispatch [:activity-center.notifications/mark-as-unread (:id notification)])
    (rf/dispatch [:activity-center.notifications/mark-as-read (:id notification)])))

(defn right-swipe-on-press
  [{:keys [notification]}]
  (rf/dispatch [:activity-center.notifications/delete (:id notification)]))

(defn swipeable
  [_]
  (let [swipeable-ref (atom nil)]
    (fn [{:keys [active-swipeable
                 extra-fn
                 left-button
                 left-on-press
                 right-button
                 right-on-press]}
         & children]
      (into
       [gesture/swipeable
        {:ref                    #(reset! swipeable-ref %)
         :accessibility-label    :notification-swipeable
         :friction               2
         :left-threshold         style/swipe-action-width
         :right-threshold        style/swipe-action-width
         :overshoot-left         false
         :overshoot-right        false
         :on-swipeable-will-open (close-active-swipeable active-swipeable swipeable-ref)
         :render-left-actions    (render-swipe-action
                                  {:active-swipeable active-swipeable
                                   :extra-fn extra-fn
                                   :interpolation-opacity style/left-swipe-opacity-interpolation-js
                                   :interpolation-translate-x
                                   style/left-swipe-translate-x-interpolation-js
                                   :on-press left-on-press
                                   :swipe-button left-button
                                   :swipeable-ref swipeable-ref})
         :render-right-actions   (render-swipe-action
                                  {:active-swipeable active-swipeable
                                   :extra-fn extra-fn
                                   :interpolation-opacity style/right-swipe-opacity-interpolation-js
                                   :interpolation-translate-x
                                   style/right-swipe-translate-x-interpolation-js
                                   :on-press right-on-press
                                   :swipe-button right-button
                                   :swipeable-ref swipeable-ref})}]
       children))))
