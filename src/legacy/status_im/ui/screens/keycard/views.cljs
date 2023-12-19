(ns legacy.status-im.ui.screens.keycard.views
  (:require
    [clojure.string :as string]
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    [legacy.status-im.keycard.login :as keycard.login]
    [legacy.status-im.react-native.resources :as resources]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.chat.photos :as photos]
    [legacy.status-im.ui.screens.keycard.frozen-card.view :as frozen-card.view]
    [legacy.status-im.ui.screens.keycard.pin.views :as pin.views]
    [legacy.status-im.ui.screens.keycard.styles :as styles]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.profile.utils :as profile.utils]
    [status-im2.navigation.events :as navigation]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

;; NOTE(Ferossgp): Seems like it should be in popover
(defn blank
  []
  [react/view
   {:flex             1
    :justify-content  :center
    :align-items      :center
    :background-color colors/gray-transparent-40}
   [react/view
    {:background-color colors/white
     :height           433
     :width            "85%"
     :border-radius    16
     :flex-direction   :column
     :justify-content  :space-between
     :align-items      :center}
    [react/view
     {:margin-top         32
      :padding-horizontal 34}
     [react/text
      {:style {:typography :title-bold
               :text-align :center}}
      (i18n/label :t/blank-keycard-title)]
     [react/view {:margin-top 16}
      [react/text
       {:style {:color       colors/gray
                :line-height 22
                :text-align  :center}}
       (i18n/label :t/blank-keycard-text)]]]
    [react/view
     [react/image
      {:source      (resources/get-image :keycard)
       :resize-mode :center
       :style       {:width  144
                     :height 114}}]]
    [react/view {:margin-bottom 32}
     [quo/button {:on-press #(re-frame/dispatch [:navigate-back])}
      (i18n/label :t/ok-got-it)]]]])

;; NOTE(Ferossgp): Seems like it should be in popover
(defn wrong
  []
  [react/view
   {:flex             1
    :justify-content  :center
    :align-items      :center
    :background-color colors/gray-transparent-40}
   [react/view
    {:background-color colors/white
     :height           413
     :width            "85%"
     :border-radius    16
     :flex-direction   :column
     :justify-content  :space-between
     :align-items      :center}
    [react/view
     {:margin-top         32
      :padding-horizontal 34}
     [react/text
      {:style {:typography :title-bold
               :text-align :center}}
      (i18n/label :t/wrong-keycard-title)]
     [react/view {:margin-top 16}
      [react/text
       {:style {:color       colors/gray
                :line-height 22
                :text-align  :center}}
       (i18n/label :t/wrong-keycard-text)]]]
    [react/view
     [react/image
      {:source (resources/get-image :keycard-wrong)
       :style  {:width  255
                :height 124}}]]
    [react/view {:margin-bottom 32}
     [quo/button {:on-press #(re-frame/dispatch [:navigate-back])}
      (i18n/label :t/ok-got-it)]]]])

(defn unpaired
  []
  [react/view
   {:flex             1
    :justify-content  :center
    :align-items      :center
    :background-color colors/gray-transparent-40}
   [react/view
    {:background-color colors/white
     :height           433
     :width            "85%"
     :border-radius    16
     :flex-direction   :column
     :justify-content  :space-between
     :align-items      :center}
    [react/view
     {:margin-top         32
      :padding-horizontal 34}
     [react/text
      {:style {:typography :title-bold
               :text-align :center}}
      (i18n/label :t/unpaired-keycard-title)]
     [react/view {:margin-top 16}
      [react/text
       {:style {:color       colors/gray
                :line-height 22
                :text-align  :center}}
       (i18n/label :t/unpaired-keycard-text)]]]
    [react/view
     [react/image
      {:source (resources/get-image :keycard-wrong)
       :style  {:width  255
                :height 124}}]]
    [react/view
     {:margin-bottom  32
      :flex-direction :column
      :align-items    :center}
     [quo/button
      {:on-press #(re-frame/dispatch [:keycard.login.ui/pair-card-pressed])}
      (i18n/label :t/pair-this-card)]
     [react/view {:margin-top 27}
      [quo/button
       {:type     :secondary
        :on-press #(re-frame/dispatch [:navigate-back])}
       (i18n/label :t/dismiss)]]]]])

;; NOTE(Ferossgp): Seems like it should be in popover
(defn not-keycard
  []
  [react/view
   {:flex             1
    :justify-content  :center
    :align-items      :center
    :background-color colors/gray-transparent-40}
   [react/view
    {:background-color colors/white
     :height           453
     :width            "85%"
     :border-radius    16
     :flex-direction   :column
     :justify-content  :space-between
     :align-items      :center}
    [react/view {:margin-top 32}
     [react/text
      {:style {:typography :title-bold
               :text-align :center}}
      (i18n/label :t/not-keycard-title)]
     [react/view
      {:margin-top         16
       :padding-horizontal 38}
      [react/text
       {:style {:color       colors/gray
                :line-height 22
                :text-align  :center}}
       (i18n/label :t/not-keycard-text)]]]
    [react/view
     {:margin-top  16
      :align-items :center}
     [react/image
      {:source (resources/get-image :not-keycard)
       :style  {:width  144
                :height 120}}]
     [react/view {:margin-top 40}
      [react/touchable-highlight
       {:on-press #(.openURL ^js react/linking
                             constants/keycard-integration-link)}
       [react/view
        {:flex-direction  :row
         :align-items     :center
         :justify-content :center}
        [react/text
         {:style {:text-align :center
                  :color      colors/blue}}
         (i18n/label :t/learn-more-about-keycard)]
        [icons/tiny-icon :tiny-icons/tiny-external
         {:color           colors/blue
          :container-style {:margin-left 5}}]]]]]
    [react/view {:margin-bottom 32}
     [quo/button {:on-press #(re-frame/dispatch [:navigate-back])}
      (i18n/label :t/ok-got-it)]]]])

(defn photo
  [_ _]
  (reagent/create-class
   {:should-component-update
    (fn [_ [_ _] [_ new-account]]
      (not (nil? new-account)))

    :reagent-render
    (fn [account small-screen?]
      ;;TODO this should be done in a subscription
      [photos/photo (profile.utils/photo account)
       {:size (if small-screen? 45 61)}])}))

(defn access-is-reset
  [{:keys [hide-login-actions?]}]
  [react/view
   {:style {:flex        1
            :align-items :center}}
   [react/view
    {:style {:flex            1
             :align-items     :center
             :justify-content :center}}
    [react/view
     {:style
      {:background-color colors/green-transparent-10
       :margin-bottom    32
       :width            40
       :height           40
       :align-items      :center
       :justify-content  :center
       :border-radius    20}}
     [icons/icon
      :main-icons/check
      {:color colors/green}]]
    [react/text {:style {:typography :header}}
     (i18n/label :t/keycard-access-reset)]
    [react/text (i18n/label :t/keycard-can-use-with-new-passcode)]]
   (when-not hide-login-actions?
     [react/view
      {:style {:width         260
               :margin-bottom 15}}
      [react/view
       {:align-items        :center
        :padding-horizontal 32}
       [quo/button
        {:on-press #(re-frame/dispatch
                     [::keycard.login/login-after-reset])}
        (i18n/label :t/open)]]])])

(defn frozen-card
  []
  [frozen-card.view/frozen-card
   {:show-dismiss-button? false}])

(defn blocked-card
  [{:keys [show-dismiss-button?]}]
  [react/view
   {:style (when-not show-dismiss-button?
             {:flex 1})}
   [react/view
    {:margin-top        24
     :margin-horizontal 24
     :align-items       :center}
    [react/view
     {:background-color colors/red-transparent-10
      :width            32
      :height           32
      :border-radius    16
      :align-items      :center
      :justify-content  :center}
     [icons/icon
      :main-icons/cancel
      {:color  colors/red
       :width  20
       :height 20}]]
    [react/text
     {:style {:typography    :title-bold
              :margin-top    16
              :margin-bottom 8}}
     (i18n/label :t/keycard-is-blocked-title)]
    [react/text
     {:style {:color      colors/gray
              :text-align :center}}
     (i18n/label :t/keycard-is-blocked-details)]
    [react/text "\n"]
    [react/nested-text
     {:style {:color      colors/gray
              :text-align :center}}
     (i18n/label :t/keycard-is-blocked-instructions)]
    [react/view {:style {:margin-top 24}}
     [quo/button
      {:on-press #(re-frame/dispatch [:keycard-settings.ui/recovery-card-pressed false])}
      (i18n/label :t/keycard-is-frozen-factory-reset)]]
    (when show-dismiss-button?
      [react/view
       {:margin-top    24
        :margin-bottom 24}
       [quo/button
        {:on-press #(re-frame/dispatch [::keycard.login/frozen-keycard-popover-dismissed])
         :type     :secondary}
        (i18n/label :t/dismiss)]])]])

(defn blocked-card-popover
  []
  [blocked-card {:show-dismiss-button? true}])

(defview login-pin
  [{:keys [back-button-handler
           hide-login-actions?
           default-enter-step]
    :or   {default-enter-step :login}}]
  (letsubs [pin                [:keycard/pin]
            enter-step         [:keycard/pin-enter-step]
            status             [:keycard/pin-status]
            error-label        [:keycard/pin-error-label]
            login-multiaccount [:profile/login]
            multiaccount       [:profile/profile]
            small-screen?      [:dimensions/small-screen?]
            retry-counter      [:keycard/retry-counter]]
    (let [{:keys [name] :as account} (or login-multiaccount multiaccount)
          ;; TODO(rasom): this hack fixes state mess when more then two
          ;; pin-view instances are used at the same time. Should be properly
          ;; refactored instead
          enter-step                 (or enter-step default-enter-step)]
      [react/view styles/container
       [topbar/topbar
        (merge
         (when-not hide-login-actions?
           {:right-accessories [{:icon     :main-icons/more
                                 :on-press #(re-frame/dispatch
                                             [:keycard.login.pin.ui/more-icon-pressed])}]})
         {:title      (cond
                        (#{:reset :reset-confirmation} enter-step)
                        (i18n/label :t/keycard-reset-passcode)

                        (and (= :puk enter-step)
                             (not= :blocked-card status))
                        (i18n/label :t/enter-puk-code))
          :navigation {:on-press #(re-frame/dispatch
                                   [(or back-button-handler
                                        :keycard.login.pin.ui/cancel-pressed)])}}
         (when (#{:reset :reset-confirmation} enter-step)
           {:subtitle (i18n/label :t/keycard-enter-new-passcode
                                  {:step (if (= :reset enter-step) 1 2)})}))]
       [react/scroll-view {:style {:flex 1}}
        [react/view
         {:flex            1
          :flex-direction  :column
          :justify-content :space-between
          :align-items     :center}
         [react/view
          {:flex-direction  :column
           :justify-content :center
           :align-items     :center
           :height          140}
          [react/view
           {:margin-horizontal 16
            :flex-direction    :column}
           [react/view
            {:justify-content :center
             :align-items     :center
             :flex-direction  :row}
            [react/view
             {:width           (if small-screen? 50 69)
              :height          (if small-screen? 50 69)
              :justify-content :center
              :align-items     :center}
             [photo account small-screen?]
             [react/view
              {:justify-content  :center
               :align-items      :center
               :width            (if small-screen? 18 24)
               :height           (if small-screen? 18 24)
               :border-radius    (if small-screen? 18 24)
               :position         :absolute
               :right            0
               :bottom           0
               :background-color colors/white
               :border-width     1
               :border-color     colors/black-transparent}
              [react/image
               {:source (resources/get-image :keycard-key)
                :style  {:width  (if small-screen? 6 8)
                         :height (if small-screen? 11 14)}}]]]]
           [react/text
            {:style           {:text-align  :center
                               :margin-top  (if small-screen? 8 12)
                               :color       colors/black
                               :font-weight "500"}
             :number-of-lines 1
             :ellipsize-mode  :middle}
            name]]]
         [react/touchable-highlight
          {:on-press #(re-frame/dispatch [:keycard-settings.ui/recovery-card-pressed
                                          (boolean login-multiaccount)])}
          [react/view
           {:flex-direction  :row
            :align-items     :center
            :justify-content :center}
           [react/text
            {:style {:text-align    :center
                     :margin-bottom (if small-screen? 8 12)
                     :color         colors/blue}}
            (string/lower-case (i18n/label (if login-multiaccount
                                             :t/keycard-recover
                                             :t/keycard-is-frozen-factory-reset)))]]]
         (cond
           (= :after-unblocking status)
           [access-is-reset
            {:hide-login-actions? hide-login-actions?}]

           (= :frozen-card status)
           [frozen-card]

           (= :blocked-card status)
           [blocked-card]

           :else
           [pin.views/pin-view
            {:pin                     pin
             :retry-counter           retry-counter
             :small-screen?           small-screen?
             :status                  status
             :error-label             error-label
             :step                    enter-step
             :save-password-checkbox? (not (contains?
                                            #{:reset :reset-confirmation :puk}
                                            enter-step))}])
         (if hide-login-actions?
           [react/view
            {:flex-direction :row
             :height         32}]
           [toolbar/toolbar
            {:center [quo/button
                      {:on-press #(re-frame/dispatch
                                   [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
                       :type     :secondary}
                      (i18n/label :t/recover-key)]}])]]])))

(rf/defn get-new-key
  {:events [:multiaccounts.create.ui/get-new-key]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            (bottom-sheet/hide-bottom-sheet-old)
            (navigation/navigate-to :get-your-keys nil)))

(defn- more-sheet-content
  []
  [react/view {:flex 1}
   [list.item/list-item
    {:theme    :accent
     :title    (i18n/label :t/create-new-key)
     :icon     :main-icons/profile
     :on-press #(re-frame/dispatch [:multiaccounts.create.ui/get-new-key])}]])

(def more-sheet
  {:content more-sheet-content})
