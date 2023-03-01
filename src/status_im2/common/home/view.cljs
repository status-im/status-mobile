(ns status-im2.common.home.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [status-im2.common.home.style :as style]
            [status-im2.common.plus-button.view :as components.plus-button]
            [utils.re-frame :as rf]))

(defn title-column
  [{:keys [label handler accessibility-label]}]
  [rn/view style/title-column
   [rn/view {:flex 1}
    [quo/text style/title-column-text
     label]]
   [components.plus-button/plus-button
    {:on-press            handler
     :accessibility-label accessibility-label}]])

(defn- get-button-common-props
  [type]
  (let [default? (= type :default)
        dark?    (colors/dark?)]
    {:icon                      true
     :size                      32
     :style                     {:margin-left 12}
     :type                      (if default?
                                  (if dark? :grey :dark-grey)
                                  type)
     :override-background-color (when (and dark? default?)
                                  colors/neutral-90)}))

(defn- base-button
  [icon on-press accessibility-label button-common-props]
  [quo/button
   (merge
    {:on-press            on-press
     :accessibility-label accessibility-label}
    button-common-props)
   icon])

(defn top-nav
  "[top-nav opts]
  opts
  {:type                   :default/:blurred/:shell
   :style                  override-style
   :avatar                 user-avatar}
  "
  [{:keys [type style avatar hide-search]}]
  (let [button-common-props    (get-button-common-props type)
        notif-count            (rf/sub [:activity-center/unread-count])
        new-notifications?     (pos? notif-count)
        notification-indicator :unread-dot
        counter-label          "0"]
    [rn/view
     {:style (merge
              {:height 56}
              style)}
     ;; Left Section
     [rn/touchable-without-feedback {:on-press #(rf/dispatch [:navigate-to :my-profile])}
      [rn/view
       {:style {:accessibility-label :open-profile
                :position            :absolute
                :left                20
                :top                 12}}
       [quo/user-avatar
        (merge
         {:ring?             true
          :status-indicator? true
          :size              :small}
         avatar)]]]
     ;; Right Section
     [rn/view
      {:style {:position       :absolute
               :right          20
               :top            12
               :flex-direction :row}}
      (when-not hide-search
        [base-button :i/search #() :open-search-button button-common-props])
      [base-button :i/scan #() :open-scanner-button button-common-props]
      [base-button :i/qr-code #() :show-qr-button button-common-props]
      [rn/view                     ;; Keep view instead of "[:<>" to make sure relative
       ;; position is calculated from this view instead of its parent
       [hole-view/hole-view
        {:key   new-notifications? ;; Key is required to force removal of holes
         :holes (cond
                  (not new-notifications?) ;; No new notifications, remove holes
                  []

                  (= notification-indicator :unread-dot)
                  [{:x 37 :y -3 :width 10 :height 10 :borderRadius 5}]

                  :else
                  [{:x 33 :y -7 :width 18 :height 18 :borderRadius 7}])}
        [base-button :i/activity-center #(rf/dispatch [:activity-center/open])
         :open-activity-center-button button-common-props]]
       (when new-notifications?
         (if (= notification-indicator :counter)
           [quo/counter
            {:accessibility-label :notifications-unread-badge
             :outline             false
             :override-text-color colors/white
             :override-bg-color   colors/primary-50
             :style               {:position :absolute
                                   :left     34
                                   :top      -6}}
            counter-label]
           [rn/view
            {:accessible          true
             :accessibility-label :notifications-unread-badge
             :style               {:width            8
                                   :height           8
                                   :border-radius    4
                                   :top              -2
                                   :left             38
                                   :position         :absolute
                                   :background-color colors/primary-50}}]))]]]))
