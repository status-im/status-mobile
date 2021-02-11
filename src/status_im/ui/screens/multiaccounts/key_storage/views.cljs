(ns status-im.ui.screens.multiaccounts.key-storage.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [re-frame.db]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.multiaccounts.key-storage.core :as multiaccounts.key-storage]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.accordion :as accordion]
            [status-im.ui.screens.multiaccounts.views :as multiaccounts.views]
            [status-im.ui.screens.multiaccounts.key-storage.styles :as styles]
            [status-im.utils.security]))

(defn local-topbar [subtitle]
  [topbar/topbar {:title   (i18n/label :t/key-managment)
                  :subtitle subtitle}])

(defonce accordian-data
  [{:id    :type
    :label (i18n/label :t/type)
    :value (i18n/label :t/master-account)}
   {:id    :back-up
    :label (i18n/label :t/back-up)
    :value (i18n/label :t/recovery-phrase)}
   {:id    :storage
    :label (i18n/label :t/storage)
    :value (i18n/label :t/key-on-device)}])

(defn accordion-content []
  [react/view {:padding-horizontal 16
               :flex-direction     :row}
   [react/view {:flex-shrink  0
                :margin-right 20}
    (for [{:keys [id label]} accordian-data]
      ^{:key (str "left-" id)}
      [react/text {:style {:color colors/gray
                           :padding-vertical 8}} label])]

   [react/view {:flex 1}
    (for [{:keys [id value]} accordian-data]
      ^{:key (str "right-" id)}
      [react/text {:flex      1
                   :flex-wrap :wrap
                   :style {:padding-vertical 8}}
       value])]])

;; Component to render Key and Storage management screen
(defview actions-base [{:keys [next-title next-event]}]
  (letsubs [{:keys [name] :as multiaccount} [:multiaccounts/login]
            {:keys [move-keystore-checked?]} [:multiaccounts/key-storage]]
    [react/view {:flex 1}
     [local-topbar (i18n/label :t/choose-actions)]
     [accordion/section {:title   name
                         :icon    [chat-icon.screen/contact-icon-contacts-tab
                                   (multiaccounts/displayed-photo multiaccount)]
                         :count   0
                         :content [accordion-content]}]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between}
      [react/view
       [quo/list-header (i18n/label :t/actions)]
       [quo/list-item {:title              (i18n/label :t/move-keystore-file)
                       :subtitle           (i18n/label :t/select-new-location-for-keys)
                       :subtitle-max-lines 4
                       :accessory          :checkbox
                       :active             move-keystore-checked?
                       :on-press           #(re-frame/dispatch [::multiaccounts.key-storage/move-keystore-checked (not move-keystore-checked?)])}]
       [quo/list-item {:title              (i18n/label :t/reset-database)
                       :subtitle           (i18n/label :t/reset-database-warning)
                       :subtitle-max-lines 4
                       :disabled           true
                       :active             move-keystore-checked?
                       :accessory          :checkbox}]]
      (when (and next-title next-event)
        [toolbar/toolbar {:show-border? true
                          :right        [quo/button
                                         {:type     :secondary
                                          :disabled (not move-keystore-checked?)
                                          :on-press #(re-frame/dispatch next-event)
                                          :after    :main-icons/next}
                                         next-title]}])]]))

(defn actions-not-logged-in
  "To be used when the flow is accessed before login, will enter seed phrase next"
  []
  [actions-base {:next-title (i18n/label :t/enter-seed-phrase)
                 :next-event [::multiaccounts.key-storage/enter-seed-pressed]}])

(defn actions-logged-in
  "To be used when the flow is accessed from profile, will choose storage next"
  []
  [actions-base {:next-title (i18n/label :t/choose-storage)
                 :next-event [::multiaccounts.key-storage/choose-storage-pressed]}])

(defview seed-phrase []
  (letsubs
    [{:keys [seed-word-count seed-shape-invalid?]} [:multiaccounts/key-storage]]
    [react/keyboard-avoiding-view {:flex 1}
     [local-topbar (i18n/label :t/enter-seed-phrase)]
     [multiaccounts.views/seed-phrase-input
      {:on-change-event     [::multiaccounts.key-storage/seed-phrase-input-changed]
       :seed-word-count     seed-word-count
       :seed-shape-invalid? seed-shape-invalid?}]
     [react/text {:style {:color         colors/gray
                          :font-size     14
                          :margin-bottom 8
                          :text-align    :center}}
      (i18n/label :t/multiaccounts-recover-enter-phrase-text)]
     [toolbar/toolbar {:show-border? true
                       :right        [quo/button
                                      {:type     :secondary
                                       :disabled (or seed-shape-invalid?
                                                     (nil? seed-shape-invalid?))
                                       :on-press #(re-frame/dispatch [::multiaccounts.key-storage/choose-storage-pressed])
                                       :after    :main-icons/next}
                                      (i18n/label :t/choose-storage)]}]]))

(defn keycard-subtitle []
  [react/view
   [react/text {:style {:color colors/gray}} (i18n/label :t/empty-keycard-required)]
   [react/view {:flex-direction :row
                :align-items    :center}
    [react/text {:style               {:color colors/blue}
                 :accessibility-label :learn-more
                 :on-press #(js/alert :press)}
     (i18n/label :learn-more)]
    [vector-icons/icon :main-icons/tiny-external {:color  colors/blue
                                                  :width  16
                                                  :height 16}]]])

(defn keycard-upsell-banner []
  [react/touchable-highlight {:on-press #(.openURL ^js react/linking "https://get-keycard.status.im/")}
   [react/view {:background-color (if (= :dark @colors/theme) "#2C5955" "#DDF8F4")
                :border-radius 16
                :margin 16
                :padding-horizontal 12
                :padding-vertical 8
                :flex-direction :row}
    [react/view
     [react/image {:source (resources/get-theme-image :keycard)
                   :resize-mode :contain
                   :style {:width 48
                           :height 48}}]]
    [react/view {:flex 1
                 :margin-left 12}
     [react/text {:style {:font-size 20
                          :font-weight "700"}}
      (i18n/label :t/get-a-keycard)]
     [react/text {:style {:color (colors/alpha colors/text 0.8)}}
      (i18n/label :t/keycard-upsell-subtitle)]]]])

(defview storage []
  (letsubs
    [{:keys [keycard-storage-selected?]} [:multiaccounts/key-storage]]
    [react/view {:flex 1}
     [local-topbar (i18n/label :t/choose-storage)]
     [react/view {:style styles/help-text-container}
      [react/text {:style styles/help-text}
       (i18n/label :t/choose-new-location-for-keystore)]]
     [react/view
      [quo/list-header (i18n/label :t/current)]
      [quo/list-item {:title     (i18n/label :t/this-device)
                      :text-size :base
                      :icon      :main-icons/mobile
                      :disabled  true}]
      [quo/list-header (i18n/label :t/new)]
      [quo/list-item {:title              (i18n/label :t/keycard)
                      :subtitle           (i18n/label :t/empty-keycard-required)
                      :subtitle-max-lines 4
                      :icon               :main-icons/keycard
                      :active             keycard-storage-selected?
                      :on-press           #(re-frame/dispatch [::multiaccounts.key-storage/keycard-storage-pressed (not keycard-storage-selected?)])
                      :accessory          :radio}]]
     [react/view {:flex            1
                  :justify-content :flex-end}
      (when-not keycard-storage-selected?
        [keycard-upsell-banner])
      [toolbar/toolbar {:show-border? true
                        :right        [quo/button
                                       {:type     :secondary
                                        :disabled (not keycard-storage-selected?)
                                        :on-press #(re-frame/dispatch [::multiaccounts.key-storage/show-transfer-warning-popup])}
                                       (i18n/label :t/confirm)]}]]]))

(defview seed-key-uid-mismatch-popover []
  (letsubs [{:keys [name]} [:multiaccounts/login]]
    [react/view {:margin-top        24
                 :margin-horizontal 24
                 :align-items       :center}
     [react/view {:width           32
                  :height          32
                  :border-radius   16
                  :align-items     :center
                  :justify-content :center}
      [vector-icons/icon :main-icons/warning {:color colors/blue}]]
     [react/text {:style {:typography    :title-bold
                          :margin-top    8
                          :margin-bottom 24}}
      (i18n/label :t/seed-key-uid-mismatch)]
     [react/view styles/popover-body-container
      [react/view
       [react/text {:style (into styles/popover-text
                                 {:margin-bottom 16})}
        (i18n/label :t/seed-key-uid-mismatch-desc-1 {:multiaccount-name name})]
       [react/text {:style styles/popover-text}
        (i18n/label :t/seed-key-uid-mismatch-desc-2)]]]
     [react/view {:margin-vertical 24
                  :align-items     :center}
      [quo/button {:on-press            #(re-frame/dispatch [:hide-popover])
                   :accessibility-label :cancel-custom-seed-phrase
                   :type                :secondary}
       (i18n/label :t/try-again)]]]))

(defview transfer-multiaccount-warning-popover []
  [react/view {:margin-top        24
               :margin-horizontal 24
               :align-items       :center}
   [react/view {:width           32
                :height          32
                :border-radius   16
                :align-items     :center
                :justify-content :center}
    [vector-icons/icon :main-icons/tiny-warning-background {:color colors/red}]]
   [react/text {:style styles/popover-title}
    (i18n/label :t/move-keystore-file-to-keycard)]
   [react/view styles/popover-body-container
    [react/text {:style styles/popover-text}
     (i18n/label :t/database-reset-warning)]]
   [react/view {:margin-vertical 24
                :align-items     :center}
    [quo/button {:on-press            #(re-frame/dispatch [::multiaccounts.key-storage/delete-multiaccount-and-init-keycard-onboarding])
                 :accessibility-label :cancel-custom-seed-phrase
                 :type                :primary
                 :theme               :negative}
     (i18n/label :t/move-and-reset)]
    [quo/button {:on-press            #(re-frame/dispatch [:hide-popover])
                 :accessibility-label :cancel-custom-seed-phrase
                 :type                :secondary}
     (i18n/label :t/cancel)]]])

(defview unknown-error-popover []
  [react/view {:margin-top        24
               :margin-horizontal 24
               :align-items       :center}
   [react/view {:width           32
                :height          32
                :border-radius   16
                :align-items     :center
                :justify-content :center}
    [vector-icons/icon :main-icons/close {:color colors/red}]]
   [react/text {:style {:typography    :title-bold
                        :margin-top    8
                        :margin-bottom 24}}
    (i18n/label :t/something-went-wrong)]
   [react/view styles/popover-body-container
    [react/view
     [react/text {:style (into styles/popover-text
                               {:margin-bottom 16})}
      (i18n/label :t/transfer-ma-unknown-error-desc-1)]
     [react/text {:style styles/popover-text}
      (i18n/label :t/transfer-ma-unknown-error-desc-2)]]]
   [react/view {:margin-vertical 24
                :align-items     :center}
    [quo/button {:on-press #(re-frame/dispatch [::multiaccounts.key-storage/hide-popover-and-goto-multiaccounts-screen])
                 :type     :secondary}
     (i18n/label :t/okay)]]])

(comment
  ;; UI flow
  (do
    ;; Goto key management actions screen
    (re-frame/dispatch [::multiaccounts.key-storage/key-and-storage-management-pressed])

    ;; Check move key store checkbox
    (re-frame/dispatch [::multiaccounts.key-storage/move-keystore-checked true])

    ;; Goto enter seed screen
    (re-frame/dispatch [::multiaccounts.key-storage/enter-seed-pressed])

    ;; Enter seed phrase

    ;; invalid seed shape
    #_(re-frame/dispatch [::multiaccounts.key-storage/seed-phrase-input-changed (status-im.utils.security/mask-data "h h h h h h h h h h h h")])

    ;; valid seed for Trusty Candid Bighornedsheep
    ;; If you try to select Dim Venerated Yaffle, but use this seed instead, validate-seed-against-key-uid will fail miserably
    #_(re-frame/dispatch [::multiaccounts.key-storage/seed-phrase-input-changed
                          (status-im.utils.security/mask-data "disease behave roof exile ghost head carry item tumble census rocket champion")])

    ;; valid seed for Swiffy Warlike Seagull
    #_(re-frame/dispatch [::multiaccounts.key-storage/seed-phrase-input-changed
                          (status-im.utils.security/mask-data "dirt agent garlic merge tuna leaf congress hedgehog absent dish pizza scrap")])

    ;; valid seed for Dim Venerated Yaffle (this is just a test account, okay to leak seed)
    (re-frame/dispatch [::multiaccounts.key-storage/seed-phrase-input-changed
                        (status-im.utils.security/mask-data "rocket mixed rebel affair umbrella legal resemble scene virus park deposit cargo")])

    ;; Click choose storage
    (re-frame/dispatch [::multiaccounts.key-storage/choose-storage-pressed])

    ;; Choose Keycard from storage options
    (re-frame/dispatch [::multiaccounts.key-storage/keycard-storage-pressed true])

    ;; Confirm migration popup
    (re-frame/dispatch [::multiaccounts.key-storage/show-transfer-warning-popup])

    ;; Delete multiaccount and init keycard onboarding
    (re-frame/dispatch [::multiaccounts.key-storage/delete-multiaccount-and-init-keycard-onboarding]))


  ;; Show error popup


  (re-frame/dispatch [::multiaccounts.key-storage/show-seed-key-uid-mismatch-error-popup])
  (re-frame/dispatch [::multiaccounts.key-storage/show-transfer-warning-popup])
  (re-frame/dispatch [::multiaccounts.key-storage/delete-multiaccount-error])
  (re-frame/dispatch [:hide-popover])

  ;; Flow to populate state after multiaccount is deleted
  (do
    ;; set seed phrase for Dim Venerated Yaffle
    (re-frame/dispatch [:set-in [:multiaccounts/key-storage :seed-phrase] "rocket mixed rebel affair umbrella legal resemble scene virus park deposit cargo"])

    ;; set seed for Trusty Candid Bighornedsheep
    #_(re-frame/dispatch [:set-in [:multiaccounts/key-storage :seed-phrase] "disease behave roof exile ghost head carry item tumble census rocket champion"])

    ;; simulate delete multiaccount success
    (re-frame/dispatch [::multiaccounts.key-storage/delete-multiaccount-success])))
