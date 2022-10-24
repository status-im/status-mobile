(ns quo2.components.navigation.top-nav
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.counter.counter :as counter]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.avatars.user-avatar :as user-avatar]))

(defn- get-button-common-props [type]
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

(defn- base-button [icon on-press accessibility-label button-common-props]
  [quo2.button/button
   (merge
    {:on-press            on-press
     :accessibility-label accessibility-label}
    button-common-props)
   icon])

(defn top-nav
  "[top-nav opts]
  opts
  {:type                   :default/:blurred/:shell
   :new-notifications?     true/false
   :notification-indicator :unread-dot/:counter
   :open-profile           fn
   :open-search            fn
   :open-scanner           fn
   :show-qr                fn
   :open-activity-center   fn
   :style                  override-style
   :avatar                 user-avatar
   :counter-label          number}
  "
  [{:keys [type new-notifications? notification-indicator open-profile open-search
           open-scanner show-qr open-activity-center style avatar counter-label]}]
  (let [button-common-props (get-button-common-props type)]
    [rn/view {:style (merge
                      {:height 56}
                      style)}
   ;; Left Section
     [rn/touchable-without-feedback {:on-press open-profile}
      [rn/view {:style {:position :absolute
                        :left     20
                        :top      12}}
       [user-avatar/user-avatar
        (merge
         {:ring?             true
          :status-indicator? true
          :size              :small}
         avatar)]]]
   ;; Right Section
     [rn/view {:style {:position :absolute
                       :right    20
                       :top      12
                       :flex-direction :row}}
      [base-button :main-icons2/search open-search :open-search-button button-common-props]
      [base-button :main-icons2/scan open-scanner :open-scanner-button button-common-props]
      [base-button :main-icons2/qr-code show-qr :show-qr-button button-common-props]
      [rn/view ;; Keep view instead of "[:<>" to make sure relative
               ;; position is calculated from this view instead of its parent
       [rn/hole-view {:key    new-notifications? ;; Key is required to force removal of holes
                      :holes  (cond
                                (not new-notifications?) ;; No new notifications, remove holes
                                []

                                (= notification-indicator :unread-dot)
                                [{:x 37 :y -3 :width 10 :height 10 :borderRadius 5}]

                                :else
                                [{:x 33 :y -7 :width 18 :height 18 :borderRadius 7}])}
        [base-button :main-icons2/activity-center open-activity-center
         :open-activity-center-button button-common-props]]
       (when new-notifications?
         (if (= notification-indicator :counter)
           [counter/counter {:outline             false
                             :override-text-color colors/white
                             :override-bg-color   colors/primary-50
                             :style               {:position :absolute
                                                   :left     34
                                                   :top      -6}}
            counter-label]
           [rn/view {:style {:width            8
                             :height           8
                             :border-radius    4
                             :top             -2
                             :left             38
                             :position         :absolute
                             :background-color colors/primary-50}}]))]]]))
