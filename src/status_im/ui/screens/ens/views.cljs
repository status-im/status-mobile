(ns status-im.ui.screens.ens.views
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.design-system.colors :as colors]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.ens.core :as ens]
    [status-im.ethereum.core :as ethereum]
    [status-im.ethereum.ens :as ethereum.ens]
    [status-im.ethereum.stateofus :as stateofus]
    [status-im.ethereum.tokens :as tokens]
    [utils.i18n :as i18n]
    [status-im.react-native.resources :as resources]
    [status-im.ui.components.chat-icon.screen :as chat-icon]
    [status-im.ui.components.checkbox.view :as checkbox]
    [status-im.ui.components.common.common :as components.common]
    [status-im.ui.components.icons.icons :as icons]
    [status-im.ui.components.react :as react]
    [status-im.ui.components.toolbar :as toolbar]
    [status-im.ui.components.topbar :as topbar]
    [status-im.ui.screens.chat.utils :as chat.utils]
    [status-im.ui.screens.profile.components.views :as profile.components]
    [status-im.ui.screens.wallet.send.sheets :as sheets]
    [status-im.utils.utils :as utils]
    [utils.debounce :as debounce])
  (:require-macros [status-im.utils.views :as views]))

(defn- link
  [{:keys [on-press]} label]
  [react/touchable-opacity
   {:on-press on-press
    :style    {:justify-content :center}}
   [react/text {:style {:color colors/blue}}
    label]])

(defn- section
  [{:keys [title content]}]
  [react/view
   {:style {:margin-horizontal 16
            :align-items       :flex-start}}
   [react/text {:style {:color colors/gray :font-size 15}}
    title]
   [react/view
    {:margin-top         8
     :padding-horizontal 16
     :padding-vertical   12
     :border-width       1
     :border-radius      12
     :border-color       colors/gray-lighter}
    [quo/text {:monospace true}
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
  [react/view
   {:style {:margin-top       68
            :margin-bottom    24
            :width            60
            :height           60
            :border-radius    30
            :background-color colors/blue
            :align-items      :center
            :justify-content  :center}}
   [icons/icon
    (case state
      (:available :connected :connected-with-different-key :owned)
      :main-icons/check
      (:taken :error)
      :main-icons/cancel
      :main-icons/username)
    {:color colors/white-persist}]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; SEARCH SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- icon-wrapper
  [color icon]
  [react/view
   {:style {:margin-right     16
            :margin-top       11
            :width            32
            :height           32
            :border-radius    25
            :align-items      :center
            :justify-content  :center
            :background-color color}}
   icon])

(defn- input-icon
  [state]
  (case state
    :searching
    [icon-wrapper colors/gray
     [react/activity-indicator {:color colors/white-persist}]]

    (:available :connected :connected-with-different-key :owned)
    [react/touchable-highlight
     {:on-press #(debounce/dispatch-and-chill [::ens/input-submitted] 3000)}
     [icon-wrapper colors/blue
      [icons/icon :main-icons/arrow-right {:color colors/white-persist}]]]

    [icon-wrapper colors/gray
     [icons/icon :main-icons/arrow-right {:color colors/white-persist}]]))

(defn help-message-text-element
  ([label]
   [react/text
    {:style {:flex              1
             :margin-top        16
             :margin-horizontal 16
             :font-size         14
             :text-align        :center}}
    (i18n/label label)])
  ([label second-label]
   [react/nested-text
    {:style {:flex              1
             :margin-top        16
             :margin-horizontal 16
             :font-size         14
             :text-align        :center}}
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
    :invalid-ens
    [help-message-text-element
     :t/ens-username-owned
     :t/ens-username-registration-invalid]
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
  [_ _ _]
  (let [input-ref (atom nil)]
    (fn [_ state placeholder]
      [react/view {:flex-direction :row :justify-content :center}
       ;;NOTE required so that the keyboards shows up when navigating back from checkout screen
       ;; TODO: navigation-events were deprecated
       ;; [:> navigation/navigation-events
       ;;  {:on-did-focus
       ;;   (fn []
       ;;     (.focus ^js @input-ref))}]
       ;;NOTE setting the key as placeholder forces the component to remount
       ;;when the placeholder changes, this prevents the placeholder from
       ;;disappearing when switching between stateofus and custom domain
       ^{:key placeholder}
       [react/text-input
        {:ref                    #(reset! input-ref %)
         :on-change-text         #(do
                                    (re-frame/dispatch [:set-in [:ens/registration :state] :searching])
                                    (debounce/debounce-and-dispatch [::ens/set-username-candidate %]
                                                                    600))
         :on-submit-editing      #(re-frame/dispatch [::ens/input-submitted])
         :auto-capitalize        :none
         :auto-complete-type     "off"
         :auto-focus             true
         :auto-correct           false
         :default-value          ""
         :text-align             :center
         :placeholder            placeholder
         :placeholder-text-color colors/text-gray
         :style                  {:flex         1
                                  :font-size    22
                                  :padding-left 48}}]
       [input-icon state]])))

(views/defview search
  []
  (views/letsubs [{:keys [state custom-domain? username]}
                  [:ens/search-screen]]
    [react/keyboard-avoiding-view {:flex 1}
     [react/scroll-view
      {:style                        {:flex 1}
       ;;NOTE required so that switching custom-domain works on first tap and persists keyboard
       ;;instead of dismissing keyboard and requiring two taps
       :keyboard-should-persist-taps :always}
      [react/view {:style {:flex 1}}
       [react/view
        {:style {:flex            1
                 :align-items     :center
                 :justify-content :center}}
        [big-blue-icon state]
        [username-input username state
         (if custom-domain?
           "vitalik94.domain.eth"
           "vitalik94")]
        [react/view
         {:style {:height             36
                  :align-items        :center
                  :justify-content    :space-between
                  :padding-horizontal 12
                  :margin-top         24
                  :margin-horizontal  16
                  :border-color       colors/gray-lighter
                  :border-radius      20
                  :border-width       1
                  :flex-direction     :row}}
         [react/text
          {:style {:font-size  13
                   :typography :main-medium}}
          (domain-label custom-domain?)]
         [react/view {:flex 1 :min-width 24}]
         [react/touchable-highlight {:on-press #(re-frame/dispatch [::ens/switch-domain-type])}
          [react/text
           {:style           {:color      colors/blue
                              :font-size  12
                              :typography :main-medium}
            :number-of-lines 2}
           (domain-switch-label custom-domain?)]]]]
       [help-message state custom-domain?]]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CHECKOUT SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- agreement
  [checked contract]
  [react/view
   {:flex-direction  :row
    :margin-left     26 ;; 10 for checkbox + 16
    :margin-right    16
    :margin-top      14
    :align-items     :flex-start
    :justify-content :center}
   [checkbox/checkbox
    {:checked?        @checked
     :style           {:padding 0}
     :on-value-change #(reset! checked %)}]
   [react/nested-text {:style {:margin-left 10}}
    (i18n/label :t/ens-agree-to)
    [{:style    {:color colors/blue}
      :on-press #(re-frame/dispatch [:navigate-to :ens-terms {:contract contract}])}
     (i18n/label :t/ens-terms-registration)]
    "\n"
    (i18n/label :t/ens-understand)]])

(defn render-account
  [address]
  (let [account @(re-frame/subscribe [:account-by-address address])]
    [quo/list-item
     {:icon     [chat-icon/custom-icon-view-list (:name account) (:color account)]
      :title    (:name account)
      :subtitle (utils/get-shortened-checksum-address (:address account))
      :chevron  true
      :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                     {:content (fn [] [sheets/accounts-list :from
                                                       ::ens/change-address])}])}]))

(defn- registration
  [checked contract address public-key]
  [react/view {:style {:flex 1 :margin-top 24}}
   [react/text {:style {:color colors/gray :font-size 15 :margin-horizontal 16}}
    (i18n/label :t/wallet)]
   [render-account address]
   [react/view {:style {:margin-top 14}}
    [section
     {:title   (i18n/label :t/key)
      :content public-key}]]
   [agreement checked contract]])

(defn checkout
  []
  (let [checked? (reagent/atom false)]
    (fn []
      (let [{:keys [username address custom-domain? public-key
                    chain amount-label sufficient-funds?]}
            @(re-frame/subscribe [:ens/checkout-screen])]
        [react/keyboard-avoiding-view {:flex 1}
         [react/scroll-view {:style {:flex 1}}
          [react/view
           {:style {:flex            1
                    :align-items     :center
                    :justify-content :center}}
           [big-blue-icon nil]
           [react/text
            {:text-align :center
             :style      {:flex               1
                          :font-size          22
                          :padding-horizontal 48}}
            username]
           [react/view
            {:style {:height             36
                     :align-items        :center
                     :justify-content    :space-between
                     :padding-horizontal 12
                     :margin-top         24
                     :margin-horizontal  16
                     :border-color       colors/gray-lighter
                     :border-radius      20
                     :border-width       1
                     :flex-direction     :row}}
            [react/text
             {:style {:font-size  13
                      :typography :main-medium}}
             (domain-label custom-domain?)]
            [react/view {:flex 1 :min-width 24}]]]
          [registration checked? (stateofus/get-cached-registrar chain) address public-key]]
         [toolbar/toolbar
          {:show-border? true
           :size         :large
           :left         [react/view {:flex-direction :row :align-items :center}
                          [react/image
                           {:source tokens/snt-icon-source
                            :style  {:width 36 :height 36}}]
                          [react/view {:flex-direction :column :margin 8}
                           [react/text {:style {:font-size 15}}
                            amount-label]
                           [react/text {:style {:color colors/gray :font-size 15}}
                            (i18n/label :t/ens-deposit)]]]
           :right        [react/view {:padding-horizontal 8}
                          [quo/button
                           {:disabled (or (not @checked?) (not sufficient-funds?))
                            :on-press #(debounce/dispatch-and-chill [::ens/register-name-pressed address]
                                                                    2000)}
                           (if sufficient-funds?
                             (i18n/label :t/ens-register)
                             (i18n/label :t/not-enough-snt))]]}]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CONFIRMATION SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- finalized-icon
  [state]
  (case state
    :registration-failed
    [react/view
     {:style {:width            40
              :height           40
              :border-radius    30
              :background-color colors/red-light
              :align-items      :center
              :justify-content  :center}}
     [icons/icon :main-icons/warning {:color colors/red}]]
    [react/view
     {:style {:width            40
              :height           40
              :border-radius    30
              :background-color colors/gray-lighter
              :align-items      :center
              :justify-content  :center}}
     [icons/icon :main-icons/check {:color colors/blue}]]))

(defn- final-state-label
  [state username]
  (case state
    :available
    (i18n/label :t/ens-username-registration-confirmation
                {:username (stateofus/username-with-domain username)})
    :connected-with-different-key
    (i18n/label :t/ens-username-connection-confirmation
                {:username (stateofus/username-with-domain username)})
    :connected
    (i18n/label :t/ens-saved-title)
    ;;NOTE: this state can't be reached atm
    :registration-failed
    (i18n/label :t/ens-registration-failed-title)
    nil))

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
     (stateofus/username-with-domain username)
     [{:style {:color colors/gray}}
      (i18n/label :t/ens-saved)]]
    ;;NOTE: this state can't be reached atm
    :registration-failed
    [react/text {:style {:color colors/gray :font-size 14}}
     (i18n/label :t/ens-registration-failed)]
    nil))

(views/defview confirmation
  []
  (views/letsubs [{:keys [state username]} [:ens/confirmation-screen]]
    [react/keyboard-avoiding-view {:flex 1}
     [react/view
      {:style {:flex            1
               :align-items     :center
               :justify-content :center}}
      [finalized-icon state]
      [react/text
       {:style {:typography        :header
                :margin-top        32
                :margin-horizontal 32
                :text-align        :center}}
       (final-state-label state username)]
      [react/view
       {:align-items       :center
        :margin-horizontal 32
        :margin-top        12
        :margin-bottom     20
        :justify-content   :center}
       [final-state-details state username]]
      (if (= state :registration-failed)
        [react/view
         [quo/button {:on-press #(re-frame/dispatch [::ens/retry-pressed])}
          (i18n/label :t/retry)]
         [quo/button {:on-press #(re-frame/dispatch [::ens/cancel-pressed])}
          (i18n/label :t/cancel)]]
        [quo/button {:on-press #(re-frame/dispatch [::ens/got-it-pressed])}
         (i18n/label :t/ens-got-it)])]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; TERMS SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- term-point
  [content]
  [react/view {:style {:flex 1 :margin-top 24 :margin-horizontal 16 :flex-direction :row}}
   [react/view {:style {:width 16 :margin-top 8}}
    [react/view {:style {:background-color colors/gray :width 4 :height 4 :border-radius 25}}]]
   [react/text {:style {:flex 1 :font-size 15}}
    content]])

(defn- etherscan-url
  [address]
  (str "https://etherscan.io/address/" address))

(views/defview terms
  []
  (views/letsubs [{:keys [contract]} [:get-screen-params :ens-terms]]
    [react/scroll-view {:style {:flex 1}}
     [react/view
      {:style {:height           136
               :background-color colors/gray-lighter
               :justify-content  :center
               :align-items      :center}}
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
       [link {:on-press #(.openURL ^js react/linking (etherscan-url contract))}
        (i18n/label :t/etherscan-lookup)]]
      [term-point
       (i18n/label :t/ens-terms-point-10)]
      [react/view {:style {:align-items :center :margin-top 16 :margin-bottom 8}}
       [link
        {:on-press #(.openURL ^js react/linking (etherscan-url (:mainnet ethereum.ens/ens-registries)))}
        (i18n/label :t/etherscan-lookup)]]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; NAME DETAILS SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def release-instructions-link "https://our.status.im/managing-your-ens-name-in-v1/")

(defn open-release-instructions-link!
  []
  (.openURL ^js react/linking release-instructions-link))

(views/defview name-details
  []
  (views/letsubs [{:keys [name address custom-domain? public-key
                          expiration-date releasable? pending?]}
                  [:ens.name/screen]]
    [react/view {:style {:flex 1}}
     [topbar/topbar {:title name}]
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
         [section
          {:title   (i18n/label :t/wallet-address)
           :content (ethereum/normalized-hex address)}])
       (when-not pending?
         [react/view {:style {:margin-top 14}}
          [section
           {:title   (i18n/label :t/key)
            :content public-key}]])
       [react/view {:style {:margin-top 16 :margin-bottom 32}}
        ;;TODO this is temporary fix for accounts with failed txs we still need this for regular ens
        ;;names (not pending) but we need to detach public key in the contract
        (when pending?
          [quo/list-item
           {:title    (i18n/label :t/ens-remove-username)
            ;:subtext       (i18n/label :t/ens-remove-hints)
            :icon     :main-icons/close
            :theme    :negative
            :on-press #(re-frame/dispatch [::ens/remove-username name])}])
        (when (and (not custom-domain?) (not pending?))
          [react/view {:style {:margin-top 18}}
           [quo/list-item
            {:title    (i18n/label :t/ens-release-username)
             :theme    :accent
             :disabled (not releasable?)
             :subtitle (when (and expiration-date
                                  (not releasable?))
                         (i18n/label :t/ens-locked
                                     {:date expiration-date}))
             :icon     :main-icons/delete
             :on-press #(open-release-instructions-link!)}]])]]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; WELCOME SCREEN
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- welcome-item
  [{:keys [icon-label title]} content]
  [react/view
   {:style {:flex           1
            :margin-top     24
            :margin-left    16
            :flex-direction :row}}
   [react/view
    {:style {:height          40
             :width           40
             :border-radius   25
             :border-width    1
             :border-color    colors/gray-lighter
             :align-items     :center
             :justify-content :center}}
    [react/text {:style {:typography :title}}
     icon-label]]
   [react/view
    {:style {:flex              1
             :margin-horizontal 16}}
    [react/text
     {:style {:font-size  15
              :typography :main-semibold}}
     title]
    content]])

(defn- welcome
  []
  (let [name (:name @(re-frame/subscribe [:profile/profile]))]
    [react/view {:style {:flex 1}}
     [react/scroll-view {:content-container-style {:align-items :center}}
      [react/image
       {:source (resources/get-theme-image :ens-header)
        :style  {:margin-top 32}}]
      [react/text {:style {:margin-top 32 :margin-bottom 8 :typography :header}}
       (i18n/label :t/ens-get-name)]
      [react/text
       {:style {:margin-top        8
                :margin-bottom     24
                :color             colors/gray
                :font-size         15
                :margin-horizontal 16
                :text-align        :center}}
       (i18n/label :t/ens-welcome-hints)]
      [welcome-item {:icon-label "1" :title (i18n/label :t/ens-welcome-point-customize-title)}
       [react/view {:flex-direction :row}
        [react/text
         {:style {:color colors/gray}}
         (i18n/label :t/ens-welcome-point-customize {:name name})]]]
      [welcome-item {:icon-label "2" :title (i18n/label :t/ens-welcome-point-simplify-title)}
       [react/text {:style {:color colors/gray}}
        (i18n/label :t/ens-welcome-point-simplify)]]
      [welcome-item {:icon-label "3" :title (i18n/label :t/ens-welcome-point-receive-title)}
       [react/text {:style {:color colors/gray}}
        (i18n/label :t/ens-welcome-point-receive)]]
      [welcome-item {:icon-label "4" :title (i18n/label :t/ens-welcome-point-register-title)}
       [react/text {:style {:color colors/gray}}
        (i18n/label :t/ens-welcome-point-register)]]
      [welcome-item {:icon-label "@" :title (i18n/label :t/ens-welcome-point-verify-title)}
       [react/text {:style {:color colors/gray}}
        (i18n/label :t/ens-welcome-point-verify)]]
      [react/text
       {:style
        {:margin-top 16 :text-align :center :color colors/gray :typography :caption :padding-bottom 96}}
       (i18n/label :t/ens-powered-by)]]
     [toolbar/toolbar
      {:show-border? true
       :right        [quo/button
                      {:on-press #(re-frame/dispatch [::ens/get-started-pressed])
                       :type     :secondary
                       :after    :main-icons/next}
                      (i18n/label :t/get-started)]}]]))

(defn- name-item
  [{:keys [name action subtitle]}]
  (let [stateofus-username (stateofus/username name)
        s                  (or stateofus-username name)]
    [quo/list-item
     (merge {:title    s
             :subtitle (if subtitle
                         subtitle
                         (when stateofus-username stateofus/domain))
             :icon     :main-icons/username}
            (when action
              {:on-press action}))]))

(defn- name-list
  [names preferred-name]
  [react/view {:style {:flex 1 :margin-top 16}}
   [react/view {:style {:margin-horizontal 16 :align-items :center :justify-content :center}}
    [react/nested-text
     {:style {:color colors/gray}}
     (i18n/label :t/ens-displayed-with)
     [{:style {:color colors/black :text-align :center}}
      (str "\n@" preferred-name)]]]
   [react/view {:style {:flex 1 :margin-top 8}}
    (for [name names]
      (let [action             #(do (re-frame/dispatch [::ens/save-preferred-name name])
                                    (re-frame/dispatch [:bottom-sheet/hide-old]))
            stateofus-username (stateofus/username name)
            s                  (or stateofus-username name)]
        ^{:key name}
        [quo/list-item
         {:accessibility-label (if (= name preferred-name)
                                 :primary-username
                                 :not-primary-username)
          :title               s
          :subtitle            (when stateofus-username stateofus/domain)
          :icon                :main-icons/username
          :on-press            action
          :accessory           :radio
          :active              (= name preferred-name)}]))]])

(views/defview in-progress-registrations
  [registrations]
  [react/view {:style {:margin-top 8}}
   (for [[tx-hash {:keys [state username]}] registrations
         :when                              (or (= state :submitted) (= state :failure))]
     ^{:key tx-hash}
     [name-item
      {:name     username
       :action   (when-not (= state :submitted)
                   #(re-frame/dispatch [:ens/clear-registration tx-hash]))
       :subtitle (case state
                   :submitted (i18n/label :t/ens-registration-in-progress)
                   :failure   (i18n/label :t/ens-registration-failure)
                   nil)}])])

(views/defview my-name
  []
  (views/letsubs [contact-name [:multiaccount/preferred-name]]
    (when-not (string/blank? contact-name)
      (chat.utils/format-author-old {:primary-name
                                     (str "@"
                                          (or (stateofus/username contact-name)
                                              contact-name))}))))

(views/defview registered
  [names {:keys [preferred-name]} _ registrations]
  [react/view {:style {:flex 1}}
   [react/scroll-view
    [react/view {:style {:margin-top 8}}
     [quo/list-item
      {:title    (i18n/label :t/ens-add-username)
       :theme    :accent
       :on-press #(re-frame/dispatch [::ens/add-username-pressed])
       :icon     :main-icons/add}]]
    [react/view {:style {:margin-top 22 :margin-bottom 8}}
     [react/text {:style {:color colors/gray :margin-horizontal 16}}
      (i18n/label :t/ens-your-usernames)]
     (when registrations
       [in-progress-registrations registrations])
     (if (seq names)
       [react/view {:style {:margin-top 8}}
        (for [name names]
          ^{:key name}
          [name-item
           {:name   name
            :action #(re-frame/dispatch [::ens/navigate-to-name name])}])]
       [react/text
        {:style {:color             colors/gray
                 :font-size         15
                 :margin-horizontal 16}}
        (i18n/label :t/ens-no-usernames)])]
    [react/view {:style {:padding-vertical 22 :border-color colors/gray-lighter :border-top-width 1}}
     (when (> (count names) 1)
       [react/view
        [react/text {:style {:color colors/gray :margin-horizontal 16}}
         (i18n/label :t/ens-chat-settings)]
        [profile.components/settings-item
         {:label-kw  :ens-primary-username
          :value     preferred-name
          :action-fn #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                          {:content
                                           (fn [] (name-list names preferred-name))}])}]])]]])

(views/defview main
  []
  (views/letsubs [{:keys [names profile/profile show? registrations]} [:ens.main/screen]]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     (if (or (seq names) registrations)
       [registered names profile show? registrations]
       [welcome])]))
