(ns legacy.status-im.ui.screens.keycard.settings.views
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]])
  (:require
    [legacy.status-im.react-native.resources :as resources]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.keycard.views :as keycard.views]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]))

(defn- activity-indicator
  [loading?]
  (when loading?
    [react/view {:margin-top 35}
     [react/activity-indicator
      {:animating true
       :size      :large}]]))

(defn- reset-card-next-button
  [disabled?]
  [react/view
   {:margin-right  6
    :margin-bottom 8}
   [quo/button
    ;; TODO: Should have label?:
    {:on-press #(re-frame/dispatch [:keycard-settings.ui/reset-card-next-button-pressed])
     :disabled disabled?
     :type     :secondary
     :after    :main-icon/next}]])

(defview reset-card
  []
  (letsubs [disabled? [:keycard-reset-card-disabled?]]
    [:<>
     [react/view
      {:margin-top  71
       :flex        1
       :align-items :center}
      [react/image
       {:source (resources/get-image :warning-sign)
        :style  {:width  160
                 :height 160}}]]
     [react/view
      {:flex               1
       :padding-horizontal 30}
      [react/text
       {:style {:typography :header
                :text-align :center}}
       (i18n/label :t/reset-card-description)]
      [activity-indicator disabled?]]
     [react/view
      {:flex-direction   :row
       :justify-content  :space-between
       :align-items      :center
       :width            "100%"
       :height           68
       :border-top-width 1
       :border-color     colors/black-transparent}
      [react/view {:flex 1}]
      [reset-card-next-button disabled?]]]))

(defn- card-blocked
  []
  [react/view
   [react/text
    {:style {:font-size          20
             :text-align         :center
             :padding-horizontal 40}}
    (i18n/label :t/keycard-blocked)]])

(defview keycard-settings
  []
  (letsubs [paired-on         [:keycard-paired-on]
            puk-retry-counter [:keycard/puk-retry-counter]
            pairing           [:keycard-multiaccount-pairing]]
    [react/scroll-view {:flex 1}
     [react/view
      {:margin-top  47
       :align-items :center}
      [react/image
       {:source (resources/get-image :keycard-card)
        :style  {:width  255
                 :height 160}}]
      (when paired-on
        [react/view {:margin-top 27}
         [react/text
          (i18n/label :t/linked-on {:date paired-on})]])]
     [react/view {:padding-vertical 16}
      (if (zero? puk-retry-counter)
        [card-blocked]
        [:<>
         [list.item/list-item
          {:icon     :main-icons/help
           :size     :small
           :title    (i18n/label :t/help-capitalized)
           :on-press #(.openURL ^js react/linking
                                constants/faq-keycard)}]
         (when pairing
           [:<>
            [list.item/list-item
             {:icon     :main-icons/add
              :size     :small
              :title    (i18n/label :t/change-pin)
              :on-press #(re-frame/dispatch [:keycard-settings.ui/change-credentials-pressed :pin])}]
            [list.item/list-item
             {:icon                :main-icons/security
              :size                :small
              :title               (i18n/label :t/change-puk)
              :accessibility-label "change-puk"
              :on-press            #(re-frame/dispatch [:keycard-settings.ui/change-credentials-pressed
                                                        :puk])}]
            [list.item/list-item
             {:icon                :main-icons/password
              :size                :small
              :title               (i18n/label :t/change-pairing)
              :accessibility-label "change-pairing"
              :on-press            #(re-frame/dispatch [:keycard-settings.ui/change-credentials-pressed
                                                        :pairing])}]
            [list.item/list-item
             {:icon                :main-icons/keycard
              :size                :small
              :title               (i18n/label :t/keycard-backup)
              :accessibility-label "create-backup-keycard"
              :on-press            #(re-frame/dispatch [:keycard-settings.ui/backup-card-pressed
                                                        :backup-card])}]
            ;; TODO(rasom): uncomment this when unpairing will be enabled
            ;; https://github.com/status-im/status-mobile/issues/9227
            #_[list/list-item
               {:icon     :main-icons/close
                :size     :small
                :title    (i18n/label :t/unpair-card)
                :on-press #(re-frame/dispatch [:keycard-settings.ui/unpair-card-pressed])}]])])]]))

(defn reset-pin
  []
  [keycard.views/login-pin
   {:back-button-handler
    :navigate-back
    :hide-login-actions? true
    :default-enter-step :reset}])
