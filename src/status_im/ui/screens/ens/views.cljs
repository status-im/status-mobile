(ns status-im.ui.screens.ens.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ens.core :as ens]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ethereum.ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.radio :as radio]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.utils.navigation :as navigation])
  (:require-macros [status-im.utils.views :as views]))

(defn- button
  [{:keys [on-press] :as m} label]
  [components.common/button (merge {:button-style {:margin-vertical    8
                                                   :padding-horizontal 32
                                                   :justify-content    :center
                                                   :align-items        :center}
                                    :on-press     on-press
                                    :label        label}
                                   m)])

(defn- link
  [{:keys [on-press]} label]
  [react/touchable-opacity {:on-press on-press
                            :style {:justify-content :center}}
   [react/text {:style {:color colors/blue}}
    label]])

(defn- section
  [{:keys [title content]}]
  [react/view {:style {:margin-horizontal 16
                       :align-items :flex-start}}
   [react/text {:style {:color colors/gray :font-size 15}}
    title]
   [react/view {:margin-top 8
                :padding-horizontal 16
                :padding-vertical 12
                :border-width 1
                :border-radius 12
                :border-color colors/gray-lighter}
    [react/text {:style {:font-size 15}}
     content]]])

(defn- domain-label
  [custom-domain?]
  (if custom-domain?
    (i18n/label :t/ens-custom-domain)
    (str "." stateofus/domain)))

(defn- domain-switch-label
  [custom-domain?]
  (if custom-domain?
    (i18n/label :t/ens-want-domain)
    (i18n/label :t/ens-want-custom-domain)))

(defn- big-blue-icon
  [state]
  [react/view {:style {:margin-top 68
                       :margin-bottom 24
                       :width 60
                       :height 60
                       :border-radius 30
                       :background-color colors/blue
                       :align-items :center
                       :justify-content :center}}
   [vector-icons/icon
    (case state
      (:available :connected :connected-with-different-key :owned)
      :main-icons/check
      (:taken :error)
      :main-icons/cancel
      :main-icons/username)
    {:color colors/white}]])

(defn- toolbar []
  [toolbar/toolbar nil
   [toolbar/nav-button (actions/back #(re-frame/dispatch [:navigate-back]))]
   [toolbar/content-title (i18n/label :t/ens-your-username)]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; SEARCH SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- icon-wrapper [color icon]
  [react/view {:style {:margin-right 16
                       :margin-top 11
                       :width 32
                       :height 32
                       :border-radius 25
                       :align-items :center
                       :justify-content :center
                       :background-color color}}
   icon])

(defn- input-icon
  [state]
  (case state
    :searching
    [icon-wrapper colors/gray
     [react/activity-indicator {:color colors/white}]]

    (:available :connected :connected-with-different-key :owned)
    [react/touchable-highlight
     {:on-press #(re-frame/dispatch [::ens/input-icon-pressed])}
     [icon-wrapper colors/blue
      [vector-icons/icon :main-icons/arrow-right {:color colors/white}]]]

    [icon-wrapper colors/gray
     [vector-icons/icon :main-icons/arrow-right {:color colors/white}]]))

(defn help-message-text-element
  ([label]
   [react/text {:style {:flex 1
                        :margin-top 16
                        :margin-horizontal 16
                        :font-size 14
                        :text-align :center}}
    (i18n/label label)])
  ([label second-label]
   [react/nested-text {:style {:flex 1
                               :margin-top 16
                               :margin-horizontal 16
                               :font-size 14
                               :text-align :center}}
    (i18n/label label) " "
    [{:style {:font-weight "700"}}
     (i18n/label second-label)]]))

(defn help-message
  [state custom-domain?]
  (case state
    :already-added
    [help-message-text-element :t/ens-username-already-added]
    :available
    [help-message-text-element :t/ens-username-available]
    :owned
    [help-message-text-element
     :t/ens-username-owned
     :t/ens-username-continue]
    :connected
    [help-message-text-element
     :t/ens-username-connected
     :t/ens-username-connected-continue]
    :connected-with-different-key
    [help-message-text-element
     :t/ens-username-owned
     :t/ens-username-connected-with-different-key]
    (if custom-domain?
      (case state
        :too-short
        [help-message-text-element :t/ens-custom-username-hints]
        :invalid
        [help-message-text-element :t/ens-custom-username-hints]
        :taken
        [help-message-text-element :t/ens-custom-username-taken]
        [react/text ""])
      (case state
        :too-short
        [help-message-text-element :t/ens-username-hints]
        :invalid
        [help-message-text-element :t/ens-username-invalid]
        :taken
        [help-message-text-element :t/ens-username-taken]
        [react/text ""]))))

(defn- username-input
  [username state placeholder]
  (let [input-ref (atom nil)]
    (fn [username state placeholder]
      [react/view {:flex-direction :row :justify-content :center}
       ;;NOTE required so that the keyboards shows up when navigating
       ;;back from checkout screen
       [:> navigation/navigation-events
        {:on-did-focus
         (fn []
           (.focus @input-ref))}]
       ;;NOTE setting the key as placeholder forces the component to remount
       ;;when the placeholder changes, this prevents the placeholder from
       ;;disappearing when switching between stateofus and custom domain
       ^{:key placeholder}
       [react/text-input
        {:ref               #(reset! input-ref %)
         :on-change-text    #(re-frame/dispatch [::ens/set-username-candidate %])
         :on-submit-editing #(re-frame/dispatch [::ens/input-submitted])
         :auto-capitalize   :none
         :auto-complete-type "off"
         :auto-focus        true
         :auto-correct      false
         :keyboard-type     :visible-password
         :default-value     ""
         :text-align        :center
         :placeholder       placeholder
         :placeholder-text-color colors/text-gray
         :style             {:flex 1
                             :font-size 22
                             :padding-left 48}}]
       [input-icon state]])))

(views/defview search []
  (views/letsubs [{:keys [state custom-domain? username]}
                  [:ens/search-screen]]
    [react/keyboard-avoiding-view {:flex 1}
     [status-bar/status-bar {:type :main}]
     [toolbar]
     [react/scroll-view {:style {:flex 1}
                         ;;NOTE required so that switching custom-domain
                         ;;works on first tap and persists keyboard
                         ;;instead of dismissing keyboard and requiring two taps
                         :keyboardShouldPersistTaps :always}
      [react/view {:style {:flex 1}}
       [react/view {:style {:flex 1
                            :align-items :center
                            :justify-content :center}}
        [big-blue-icon state]
        [username-input username state (if custom-domain?
                                         "vitalik94.domain.eth"
                                         "vitalik94")]
        [react/view {:style {:height 36
                             :align-items :center
                             :justify-content :space-between
                             :padding-horizontal 12
                             :margin-top 24
                             :margin-horizontal 16
                             :border-color colors/gray-lighter
                             :border-radius 20
                             :border-width 1
                             :flex-direction :row}}
         [react/text {:style {:font-size 13
                              :typography :main-medium}}
          (domain-label custom-domain?)]
         [react/view {:flex 1 :min-width 24}]
         [react/touchable-highlight {:on-press #(re-frame/dispatch [::ens/switch-domain-type])}
          [react/text {:style {:color colors/blue
                               :font-size 12
                               :typography :main-medium}
                       :number-of-lines 2}
           (domain-switch-label custom-domain?)]]]]
       [help-message state custom-domain?]]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CHECKOUT SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- agreement [checked contract]
  [react/view {:flex-direction :row
               :margin-left 26 ;; 10 for checkbox + 16
               :margin-right 16
               :margin-top 14
               :align-items :flex-start
               :justify-content :center}
   [checkbox/checkbox {:checked?        @checked
                       :style           {:padding 0}
                       :on-value-change #(reset! checked %)}]
   [react/view {:style {:padding-left 10}}
    [react/view {:style {:flex-direction :row}}
     [react/text
      (i18n/label :t/ens-agree-to)]
     [link {:on-press #(re-frame/dispatch [:navigate-to :ens-terms {:contract contract}])}
      (i18n/label :t/ens-terms-registration)]]
    [react/text
     (i18n/label :t/ens-understand)]]])

(defn- registration-bottom-bar
  [checked? amount-label]
  [react/view {:style {:height           60
                       :background-color colors/white
                       :border-top-width 1
                       :border-top-color colors/gray-lighter}}
   [react/view {:style {:margin-horizontal 16
                        :flex-direction    :row
                        :justify-content   :space-between}}
    [react/view {:flex-direction :row}
     [react/view {:style {:margin-top 12 :margin-right 8}}
      [components.common/logo
       {:size      36
        :icon-size 16}]]
     [react/view {:flex-direction :column :margin-vertical 8}
      [react/text {:style {:font-size 15}}
       amount-label]
      [react/text {:style {:color colors/gray :font-size 15}}
       (i18n/label :t/ens-deposit)]]]
    [button {:disabled?    (not @checked?)
             :label-style  (when (not @checked?) {:color colors/gray})
             :on-press     #(re-frame/dispatch [::ens/register-name-pressed])}
     (i18n/label :t/ens-register)]]])

(defn- registration
  [checked contract address public-key]
  [react/view {:style {:flex 1 :margin-top 24}}
   [section {:title   (i18n/label :t/wallet-address)
             :content address}]
   [react/view {:style {:margin-top 14}}
    [section {:title   (i18n/label :t/key)
              :content public-key}]]
   [agreement checked contract]])

(views/defview checkout []
  (views/letsubs [{:keys [username address custom-domain? public-key
                          contract amount-label]}
                  [:ens/checkout-screen]]
    (let [checked? (reagent/atom false)]
      [react/keyboard-avoiding-view {:flex 1}
       [status-bar/status-bar {:type :main}]
       [toolbar]
       [react/scroll-view {:style {:flex 1}}
        [react/view {:style {:flex 1
                             :align-items :center
                             :justify-content :center}}
         [big-blue-icon nil]
         [react/text {:text-align :center
                      :style      {:flex 1
                                   :font-size 22
                                   :padding-horizontal 48}}
          username]
         [react/view {:style {:height 36
                              :align-items :center
                              :justify-content :space-between
                              :padding-horizontal 12
                              :margin-top 24
                              :margin-horizontal 16
                              :border-color colors/gray-lighter :border-radius 20
                              :border-width 1
                              :flex-direction :row}}
          [react/text {:style {:font-size 13
                               :typography :main-medium}}
           (domain-label custom-domain?)]
          [react/view {:flex 1 :min-width 24}]]]
        [registration checked? contract address public-key]]
       [registration-bottom-bar checked? amount-label]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CONFIRMATION SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- finalized-icon
  [state]
  (case state
    :registration-failed
    [react/view {:style {:width 40 :height 40 :border-radius 30 :background-color colors/red-light
                         :align-items :center :justify-content :center}}
     [vector-icons/icon :main-icons/warning {:color colors/red}]]
    [react/view {:style {:width 40 :height 40 :border-radius 30 :background-color colors/gray-lighter
                         :align-items :center :justify-content :center}}
     [vector-icons/icon :main-icons/check {:color colors/blue}]]))

(defn- final-state-label
  [state username]
  (case state
    :available
    (i18n/label :t/ens-username-registration-confirmation
                {:username (stateofus/subdomain username)})
    :connected-with-different-key
    (i18n/label :t/ens-username-connection-confirmation
                {:username (stateofus/subdomain username)})
    :connected
    (i18n/label :t/ens-saved-title)
    ;;NOTE: this state can't be reached atm
    :registration-failed
    (i18n/label :t/ens-registration-failed-title)))

(defn- final-state-details
  [state username]
  (case state
    :available
    [react/text {:style {:color colors/gray :font-size 15 :text-align :center}}
     (i18n/label :t/ens-username-you-can-follow-progress)]
    :connected-with-different-key
    [react/text {:style {:color colors/gray :font-size 15 :text-align :center}}
     (i18n/label :t/ens-username-you-can-follow-progress)]
    :connected
    [react/nested-text
     {:style {:font-size 15 :text-align :center}}
     (stateofus/subdomain username)
     [{:style {:color colors/gray}}
      (i18n/label :t/ens-saved)]]
    ;;NOTE: this state can't be reached atm
    :registration-failed
    [react/text {:style {:color colors/gray :font-size 14}}
     (i18n/label :t/ens-registration-failed)]))

(views/defview confirmation []
  (views/letsubs [{:keys [state username]} [:ens/confirmation-screen]]
    [react/keyboard-avoiding-view {:flex 1}
     [status-bar/status-bar {:type :main}]
     [toolbar]
     [react/view {:style {:flex 1
                          :align-items :center
                          :justify-content :center}}
      [finalized-icon state]
      [react/text {:style {:typography :header
                           :margin-top 32
                           :margin-horizontal 32
                           :text-align :center}}
       (final-state-label state username)]
      [react/view {:align-items :center
                   :margin-horizontal 32
                   :margin-top 12
                   :margin-bottom 20
                   :justify-content :center}
       [final-state-details state username]]
      (if (= state :registration-failed)
        [react/view
         [button {:on-press #(re-frame/dispatch [::ens/retry-pressed])}
          (i18n/label :t/retry)]
         [button {:background? false
                  :on-press    #(re-frame/dispatch [::ens/cancel-pressed])}
          (i18n/label :t/cancel)]]
        [button {:on-press #(re-frame/dispatch [::ens/got-it-pressed])}
         (i18n/label :t/ens-got-it)])]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; TERMS SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- term-point [content]
  [react/view {:style {:flex 1 :margin-top 24 :margin-horizontal 16 :flex-direction :row}}
   [react/view {:style {:width 16 :margin-top 8}}
    [react/view {:style {:background-color colors/gray :width 4 :height 4 :border-radius 25}}]]
   [react/text {:style {:flex 1 :font-size 15}}
    content]])

(defn- etherscan-url [address]
  (str "https://etherscan.io/address/" address))

(views/defview terms []
  (views/letsubs [{:keys [contract]} [:get-screen-params :ens-terms]]
    [react/scroll-view {:style {:flex 1}}
     [status-bar/status-bar {:type :main}]
     [toolbar/simple-toolbar
      (i18n/label :t/ens-terms-registration)]
     [react/view {:style {:height 136 :background-color colors/gray-lighter :justify-content :center :align-items :center}}
      [react/text {:style {:text-align :center :typography :header :letter-spacing -0.275}}
       (i18n/label :t/ens-terms-header)]]
     [react/view
      [term-point
       (i18n/label :t/ens-terms-point-1)]
      [term-point
       (i18n/label :t/ens-terms-point-2)]
      [term-point
       (i18n/label :t/ens-terms-point-3)]
      [term-point
       (i18n/label :t/ens-terms-point-4)]
      [term-point
       (i18n/label :t/ens-terms-point-5)]
      [term-point
       (i18n/label :t/ens-terms-point-6)]
      [term-point
       (i18n/label :t/ens-terms-point-7)]]
     [react/view
      [react/text {:style {:font-size 15 :margin-top 24 :margin-horizontal 16 :font-weight "700"}}
       (i18n/label :t/ens-terms-point-8)]
      [term-point
       (i18n/label :t/ens-terms-point-9 {:address contract})]
      [react/view {:style {:align-items :center :margin-top 16 :margin-bottom 8}}
       [link {:on-press #(.openURL react/linking (etherscan-url contract))}
        (i18n/label :t/etherscan-lookup)]]
      [term-point
       (i18n/label :t/ens-terms-point-10)]
      [react/view {:style {:align-items :center :margin-top 16 :margin-bottom 8}}
       [link {:on-press #(.openURL react/linking (etherscan-url (:mainnet ethereum.ens/ens-registries)))}
        (i18n/label :t/etherscan-lookup)]]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; NAME DETAILS SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(views/defview name-details []
  (views/letsubs [{:keys [name address custom-domain? public-key pending?]}
                  [:ens.name/screen]]
    [react/view {:style {:flex 1}}
     [status-bar/status-bar {:type :main}]
     [toolbar/simple-toolbar
      name]
     [react/scroll-view {:style {:flex 1}}
      (when-not custom-domain?
        [react/view {:style {:flex 1 :margin-horizontal 16}}
         [react/view {:flex-direction :row :align-items :center :margin-top 20}
          [react/view {:style {:margin-right 16}}
           [components.common/logo
            {:size      40
             :icon-size 16}]]
          [react/text {:style {:typography :title}}
           (if pending?
             (i18n/label :t/ens-transaction-pending)
             (str (i18n/label :t/ens-10-SNT) ", deposit unlocked"))]]])
      [react/view {:style {:margin-top 22}}
       (when-not pending?
         [section {:title   (i18n/label :t/wallet-address)
                   :content (ethereum/normalized-address address)}])
       (when-not pending?
         [react/view {:style {:margin-top 14}}
          [section {:title   (i18n/label :t/key)
                    :content public-key}]])
       [react/view {:style {:margin-top 16 :margin-bottom 32}}
        [list/big-list-item {:text          (i18n/label :t/ens-remove-username)
                             :subtext       (i18n/label :t/ens-remove-hints)
                             :text-color    colors/gray
                             :text-style    {:font-weight "500"}
                             :icon          :main-icons/close
                             :icon-color    colors/gray
                             :hide-chevron? true}]
        (when-not custom-domain?
          [react/view {:style {:margin-top 18}}
           [list/big-list-item {:text          (i18n/label :t/ens-release-username)
                                :text-color    colors/gray
                                :text-style    {:font-weight "500"}
                                :subtext       (i18n/label :t/ens-locked)
                                :icon          :main-icons/delete
                                :icon-color    colors/gray
                                :active?       false
                                :hide-chevron? true}]])]]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; WELCOME SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- welcome-item [{:keys [icon-label title]} content]
  [react/view {:style {:flex 1
                       :margin-top 24
                       :margin-left 16
                       :flex-direction :row}}
   [react/view {:style {:height 40
                        :width 40
                        :border-radius 25
                        :border-width 1
                        :border-color colors/gray-lighter
                        :align-items :center
                        :justify-content :center}}
    [react/text {:style {:typography :title}}
     icon-label]]
   [react/view {:style {:flex 1
                        :margin-horizontal 16}}
    [react/text {:style {:font-size 15
                         :typography :main-semibold}}
     title]
    content]])

(defn- welcome []
  [react/view {:style {:flex 1}}
   [react/scroll-view {:content-container-style {:align-items :center}}
    [react/image {:source (:ens-header resources/ui)
                  :style  {:margin-top 32}}]
    [react/text {:style {:margin-top 32 :margin-bottom 8 :typography :header}}
     (i18n/label :t/ens-get-name)]
    [react/text {:style {:margin-top 8 :margin-bottom 24 :color colors/gray :font-size 15 :margin-horizontal 16
                         :text-align :center}}
     (i18n/label :t/ens-welcome-hints)]
    [welcome-item {:icon-label "1" :title (i18n/label :t/ens-welcome-point-1-title)}
     [react/view {:flex-direction :row}
      [react/nested-text
       {:style {:color colors/gray}}
       (i18n/label :t/ens-welcome-point-1)
       [{:style {:text-decoration-line :underline :color colors/black}}
        (stateofus/subdomain "myname")]]]]
    [welcome-item {:icon-label "2" :title (i18n/label :t/ens-welcome-point-2-title)}
     [react/text {:style {:color colors/gray}}
      (i18n/label :t/ens-welcome-point-2)]]
    [welcome-item {:icon-label "3" :title (i18n/label :t/ens-welcome-point-3-title)}
     [react/text {:style {:color colors/gray}}
      (i18n/label :t/ens-welcome-point-3)]]
    [welcome-item {:icon-label "@" :title (i18n/label :t/ens-welcome-point-4-title)}
     [react/text {:style {:color colors/gray}}
      (i18n/label :t/ens-welcome-point-4)]]
    [react/text {:style {:margin-top 16 :text-align :center :color colors/gray :typography :caption :padding-bottom 96}}
     (i18n/label :t/ens-powered-by)]]
   [react/view {:align-items :center :background-color colors/white
                :position :absolute :left 0 :right 0 :bottom 0
                :border-top-width 1 :border-top-color colors/gray-lighter}
    [button {:on-press #(re-frame/dispatch [::ens/get-started-pressed])
             :label    (i18n/label :t/get-started)}]]])

(defn- name-item [{:keys [name action hide-chevron?]}]
  (let [stateofus-username (stateofus/username name)
        s                  (or stateofus-username name)]
    [list/big-list-item {:text          s
                         :subtext       (when stateofus-username stateofus/domain)
                         :action-fn     action
                         :icon          :main-icons/username
                         :hide-chevron? hide-chevron?}]))

(defn- name-list [names preferred-name]
  [react/view {:style {:flex 1 :margin-top 16}}
   [react/view {:style {:margin-horizontal 16 :align-items :center :justify-content :center}}
    [react/nested-text
     {:style {:color colors/gray}}
     (i18n/label :t/ens-displayed-with)
     [{:style {:color colors/black :text-align :center}}
      (str "\n@" preferred-name)]]]
   [react/view {:style {:flex 1 :margin-top 8}}
    [react/scroll-view {:style {:flex 1}}
     [react/view {:style {:flex 1}}
      (for [name names]
        (let [action #(do (re-frame/dispatch [::ens/save-preferred-name name])
                          (re-frame/dispatch [:bottom-sheet/hide-sheet]))]
          ^{:key name}
          [react/touchable-highlight {:on-press action}
           [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :center :margin-right 16}}
            [react/view {:style {:flex 1}}
             [name-item {:name name :hide-chevron? true :action action}]]
            [radio/radio (= name preferred-name)]]]))]]]])

(defn- registered [names {:keys [preferred-name address public-key name]} show?]
  [react/view {:style {:flex 1}}
   [react/scroll-view
    [react/view {:style {:margin-top 8}}
     [list/big-list-item {:text      (i18n/label :t/ens-add-username)
                          :action-fn #(re-frame/dispatch [::ens/add-username-pressed])
                          :icon      :main-icons/add}]]
    [react/view {:style {:margin-top 22 :margin-bottom 8}}
     [react/text {:style {:color colors/gray :margin-horizontal 16}}
      (i18n/label :t/ens-your-usernames)]
     (if (seq names)
       [react/view {:style {:margin-top 8}}
        (for [name names]
          ^{:key name}
          [name-item {:name name :action #(re-frame/dispatch [::ens/navigate-to-name name])}])]
       [react/text {:style {:color colors/gray :font-size 15}}
        (i18n/label :t/ens-no-usernames)])]
    [react/view {:style {:padding-top 22 :border-color colors/gray-lighter :border-top-width 1}}
     [react/text {:style {:color colors/gray :margin-horizontal 16}}
      (i18n/label :t/ens-chat-settings)]
     (when (> (count names) 1)
       [profile.components/settings-item
        {:label-kw  :ens-primary-username
         :value     preferred-name
         :action-fn #(re-frame/dispatch [:bottom-sheet/show-sheet
                                         {:content
                                          (fn [] (name-list names preferred-name))
                                          :content-height
                                          (+ 72 (* (min 4 (count names)) 64))}])}])
     [profile.components/settings-switch-item
      {:label-kw  :ens-show-username
       :action-fn #(re-frame/dispatch [::ens/switch-show-username])
       :value     show?}]]
    (let [message (merge {:from public-key
                          :last-in-group? true
                          :display-username? true
                          :display-photo? true
                          :alias name
                          :content {:text (i18n/label :t/ens-test-message)
                                    :name (when show? preferred-name)}
                          :content-type "text/plain"
                          :timestamp-str "9:41 AM"}
                         (when show?
                           {:name preferred-name}))]
      [message/message-body message
       [message/text-message message]])]])

(views/defview main []
  (views/letsubs [{:keys [names multiaccount preferred-name show?]} [:ens.main/screen]]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [status-bar/status-bar {:type :main}]
     [toolbar/simple-toolbar
      (i18n/label :t/ens-usernames)]
     (if (seq names)
       [registered names multiaccount show?]
       [welcome])]))
