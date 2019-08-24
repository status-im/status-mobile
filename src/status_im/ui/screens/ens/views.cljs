(ns status-im.ui.screens.ens.views
  "

                                                +-------------+
                                                |   Initial   |
                                                +-----+-------+
                                                      |
                                                      |  Typing
                                                      |
                                                      v
                                    +--------------+     +----------------+
                                    |    Valid     |     | Invalid/reason |
                                    +------+-------+     +-------+--------+
                                           |                     |
                                           +----------+----------+
                                                      |
                                                      | Checking
                                                      |
                                                      |
                                                      v
+------------------------------------------+
|   +--------------+  +----------------+   |
|   | Unregistrable|  |  Registrable   |   |        +-----------------------------------+              +-------------+
|   +--------------+  +----------------+   |        |  Connected/details                |              |  Not owned  |
|                                          |        |  (none, address, public+key, all) |              +-------------+
|                                          |        +----------+------------------------+
|           Name available                 |                   |
+-------------------+----------------------+                   |
                    |                                          |
                    |                                          |
                    |                                          |
                    | Registering                              | Connecting
                    | (on-chain, 1 tx)                         | (on-chain, 1tx per info to connect)
                    |                                          |
                    +-----------------------+------------------+
                                            |
                                            |
                                            |  Saving
                                            |
                                            |
                                    +-------+-----+
                                    |    Saved    |
                                    +-------------+

  "
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
            [status-im.ui.screens.profile.components.views :as profile.components])

  (:require-macros [status-im.utils.views :as views]))

;; Components

(defn- button [{:keys [on-press] :as m} label]
  [components.common/button (merge {:button-style {:margin-vertical    8
                                                   :padding-horizontal 32
                                                   :justify-content    :center
                                                   :align-items        :center}
                                    :on-press     on-press
                                    :label        label}
                                   m)])

(defn- link [{:keys [on-press]} label]
  [react/touchable-opacity {:on-press on-press :style {:justify-content :center}}
   [react/text {:style {:color colors/blue}}
    label]])

(defn- section [{:keys [title content]}]
  [react/view {:style {:margin-horizontal 16 :align-items :flex-start}}
   [react/text {:style {:color colors/gray :font-size 15}}
    title]
   [react/view {:margin-top 8 :padding-horizontal 16 :padding-vertical 12 :border-width 1 :border-radius 12
                :border-color colors/gray-lighter}
    [react/text {:style {:font-size 15}}
     content]]])

;; Name details

(views/defview name-details []
  (views/letsubs [{:keys [name address public-key]} [:ens.name/screen]]
    (let [pending? (nil? address)]
      [react/view {:style {:flex 1}}
       [status-bar/status-bar {:type :main}]
       [toolbar/simple-toolbar
        name]
       [react/scroll-view {:style {:flex 1}}
        [react/view {:style {:flex 1 :margin-horizontal 16}}
         [react/view {:flex-direction :row :align-items :center :margin-top 20}
          [react/view {:style {:margin-right 16}}
           [components.common/logo
            {:size      40
             :icon-size 16}]]
          [react/text {:style {:typography :title}}
           (if pending?
             (i18n/label :t/ens-transaction-pending)
             (str (i18n/label :t/ens-10-SNT) ", deposit unlocked"))]]]
        [react/view {:style {:margin-top 22}}
         (when-not pending?
           [section {:title   (i18n/label :t/ens-wallet-address)
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
          [react/view {:style {:margin-top 18}}
           [list/big-list-item {:text          (i18n/label :t/ens-release-username)
                                :text-color    colors/gray
                                :text-style    {:font-weight "500"}
                                :subtext       (i18n/label :t/ens-locked)
                                :icon          :main-icons/delete
                                :icon-color    colors/gray
                                :active?       false
                                :hide-chevron? true}]]]]]])))

;; Terms

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
       [link {:on-press #(.openURL (react/linking) (etherscan-url contract))}
        (i18n/label :t/etherscan-lookup)]]
      [term-point
       (i18n/label :t/ens-terms-point-10)]
      [react/view {:style {:align-items :center :margin-top 16 :margin-bottom 8}}
       [link {:on-press #(.openURL (react/linking) (etherscan-url (:mainnet ethereum.ens/ens-registries)))}
        (i18n/label :t/etherscan-lookup)]]]]))

;; Registration

(defn- valid-domain? [state]
  (#{:registrable :owned :connected} state))

(defn- main-icon [state]
  (cond
    (valid-domain? state)    :main-icons/check
    (= state :unregistrable) :main-icons/cancel
    :else                    :main-icons/username))

(defn- icon-wrapper [color icon]
  [react/view {:style {:margin-right 10 :width 32 :height 32 :border-radius 25
                       :align-items :center :justify-content :center :background-color color}}
   icon])

(defn- input-action [{:keys [state custom-domain? username]}]
  (if (= state :connected)
    ;; Already registered, just need to save the contact
    [:ens/save-username custom-domain? username]
    [:ens/set-state username :registering]))

(defn- disabled-input-action []
  [icon-wrapper colors/gray
   [vector-icons/icon :main-icons/arrow-right {:color colors/white}]])

(defn- input-icon [{:keys [state custom-domain? username] :as props} usernames]
  (cond
    (= state :registering)
    nil

    (= state :valid)
    [icon-wrapper colors/blue
     [react/activity-indicator {:color colors/white}]]

    (valid-domain? state)
    (let [name (ens/fullname custom-domain? username)]
      (if (contains? (set usernames) name)
        [disabled-input-action]
        [react/touchable-highlight {:on-press #(re-frame/dispatch (input-action props))}
         [icon-wrapper colors/blue
          [vector-icons/icon :main-icons/arrow-right {:color colors/white}]]]))

    :else
    [disabled-input-action]))

(defn- default-name [custom-domain?]
  (if custom-domain?
    "vitalik94.domain.eth"
    "vitalik94"))

(defn- domain-label [custom-domain?]
  (if custom-domain?
    (i18n/label :t/ens-custom-domain)
    (str "." stateofus/domain)))

(defn- domain-switch-label [custom-domain?]
  (if custom-domain?
    (i18n/label :t/ens-want-domain)
    (i18n/label :t/ens-want-custom-domain)))

(defn- help-message [state custom-domain?]
  (case state
    (:initial :too-short)
    (if custom-domain?
      (i18n/label :t/ens-custom-username-hints)
      (i18n/label :t/ens-username-hints))
    :invalid
    (if custom-domain?
      (i18n/label :t/ens-custom-username-hints)
      (i18n/label :t/ens-username-invalid))
    :unregistrable
    (if custom-domain?
      (i18n/label :t/ens-custom-username-unregistrable)
      (i18n/label :t/ens-username-unregistrable))
    :registrable
    (i18n/label :t/ens-username-registrable)
    :owned
    (i18n/label :t/ens-username-owned)
    :connected
    (i18n/label :t/ens-username-connected)
    ""))

(defn- on-username-change [custom-domain? username]
  (re-frame/dispatch [:ens/set-username-candidate custom-domain? username]))

(defn- on-registration [props]
  (re-frame/dispatch [:ens/register props]))

(defn- agreement [{:keys [checked contract]}]
  [react/view {:flex-direction :row :margin-horizontal 20 :margin-top 14 :align-items :flex-start :justify-content :center}
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

(defn- registration-bottom-bar [{:keys [checked amount-label] :as props}]
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
    [button {:disabled?    (not @checked)
             :label-style  (when (not @checked) {:color colors/gray})
             :on-press     #(on-registration props)}
     (i18n/label :t/ens-register)]]])

(defn- registration [{:keys [address public-key] :as props}]
  [react/view {:style {:flex 1 :margin-top 24}}
   [section {:title   (i18n/label :t/ens-wallet-address)
             :content address}]
   [react/view {:style {:margin-top 14}}
    [section {:title   (i18n/label :t/key)
              :content public-key}]]
   [agreement props]])

(defn- icon [{:keys [state]}]
  [react/view {:style {:margin-top 68 :margin-bottom 24  :width 60 :height 60 :border-radius 30
                       :background-color colors/blue :align-items :center :justify-content :center}}
   [vector-icons/icon (main-icon state) {:color colors/white}]])

(defn- username-input [{:keys [custom-domain? username state] :as props} usernames]
  [react/view {:flex-direction :row :justify-content :center}
   [react/text-input {:on-change-text    #(on-username-change custom-domain? %)
                      :on-submit-editing #(on-registration props)
                      :auto-capitalize   :none
                      :auto-correct      false
                      :default-value     username
                      :auto-focus        true
                      :text-align        :center
                      :placeholder       (default-name custom-domain?)
                      :style             {:flex 1 :font-size 22
                                          (if (= state :registering) :padding-horizontal :padding-left) 48}}]
   [input-icon props usernames]])

(defn- final-state-label [state]
  (case state
    :registered
    (i18n/label :t/ens-registered-title)
    :saved
    (i18n/label :t/ens-saved-title)
    :registration-failed
    (i18n/label :t/ens-registration-failed-title)
    ""))

(defn- final-state-details [{:keys [state username]}]
  (case state
    :registered
    [react/text {:style {:color colors/gray :font-size 14}}
     (i18n/label :t/ens-registered)]
    :registration-failed
    [react/text {:style {:color colors/gray :font-size 14}}
     (i18n/label :t/ens-registration-failed)]
    :saved
    [react/view {:style {:flex-direction :row :align-items :center}}
     [react/nested-text
      {:style {}}
      (stateofus/subdomain username)
      [{:style {:color colors/gray}}
       (i18n/label :t/ens-saved)]]]
    [react/view {:flex-direction :row :margin-left 6 :margin-top 14 :align-items :center}
     [react/text
      (str (i18n/label :t/ens-terms-registration) " ->")]]))

(defn- finalized-icon [{:keys [state]}]
  (case state
    :registration-failed
    [react/view {:style {:width 40 :height 40 :border-radius 30 :background-color colors/red-light
                         :align-items :center :justify-content :center}}
     [vector-icons/icon :main-icons/warning {:color colors/red}]]
    [react/view {:style {:width 40 :height 40 :border-radius 30 :background-color colors/gray-lighter
                         :align-items :center :justify-content :center}}
     [vector-icons/icon :main-icons/check {:color colors/blue}]]))

(defn- registration-finalized [{:keys [state username] :as props}]
  [react/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [finalized-icon props]
   [react/text {:style {:typography :header :margin-top 32 :margin-horizontal 32 :text-align :center}}
    (final-state-label state)]
   [react/view {:align-items :center :margin-horizontal 32 :margin-top 12 :margin-bottom 20 :justify-content :center}
    [final-state-details props]]
   (if (= state :registration-failed)
     [react/view
      [button {:on-press #(re-frame/dispatch [:ens/set-state username :registering])}
       (i18n/label :t/retry)]
      [button {:background? false
               :on-press    #(re-frame/dispatch [:ens/clear-cache-and-navigate-back])}
       (i18n/label :t/cancel)]]
     [button {:on-press #(re-frame/dispatch [:ens/clear-cache-and-navigate-back])}
      (i18n/label :t/ens-got-it)])])

(views/defview registration-pending [{:keys [state custom-domain?] :as props} usernames]
  (views/letsubs [usernames [:multiaccount/usernames]]
    [react/view {:style {:flex 1}}
     [react/scroll-view {:style {:flex 1}}
      [react/view {:style {:flex 1}}
       [react/view {:style {:flex 1 :align-items :center :justify-content :center}}
        [icon props]
        [username-input props usernames]
        [react/view {:style {:height 36 :align-items :center :justify-content :space-between :padding-horizontal 12
                             :margin-top 24 :margin-horizontal 16 :border-color colors/gray-lighter :border-radius 20
                             :border-width 1 :flex-direction :row}}
         [react/text {:style {:font-size 12 :typography :main-medium}}
          (domain-label custom-domain?)]
         [react/view {:flex 1 :min-width 24}]
         (when-not (= state :registering)
           ;; Domain type is not shown during registration
           [react/touchable-highlight {:on-press #(re-frame/dispatch [:ens/switch-domain-type])}
            [react/text {:style {:color colors/blue :font-size 12 :typography :main-medium} :number-of-lines 2}
             (domain-switch-label custom-domain?)]])]]
       (if (= state :registering)
         [registration props]
         [react/text {:style {:flex 1 :margin-top 16 :margin-horizontal 16 :font-size 14 :text-align :center}}
          (help-message state custom-domain?)])]]
     (when (= state :registering)
       [registration-bottom-bar props])]))

(defn- toolbar []
  [toolbar/toolbar nil
   [toolbar/nav-button (actions/back #(re-frame/dispatch [:ens/clear-cache-and-navigate-back]))]
   [toolbar/content-title (i18n/label :t/ens-your-username)]])

(views/defview register []
  (views/letsubs [{:keys [address state registering?] :as props} [:ens.registration/screen]]
    (let [checked (reagent/atom false)
          props   (merge props {:checked checked :address (ethereum/normalized-address address)})]
      [react/keyboard-avoiding-view {:flex 1}
       [status-bar/status-bar {:type :main}]
       [toolbar]
       ;; NOTE: this view is used both for finalized and pending registration
       ;; and when the registration data is cleared for a brief moment state
       ;; is nil and registration-pending show which triggers the keyboard
       ;; and it's ugly
       ;; TODO: something less crazy with proper separated views and routes
       (if registering?
         [registration-pending props]
         [registration-finalized props])])))

;; Welcome

(defn- welcome-item [{:keys [icon-label title]} content]
  [react/view {:style {:flex 1 :margin-top 24 :margin-left 16 :flex-direction :row}}
   [react/view {:style {:height 40 :width 40 :border-radius 25 :border-width 1 :border-color colors/gray-lighter
                        :align-items :center :justify-content :center}}
    [react/text {:style {:typography :title}}
     icon-label]]
   [react/view {:style {:flex 1 :margin-horizontal 16}}
    [react/text {:style {:font-size 15 :typography :main-semibold}}
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
        (let [action #(do (re-frame/dispatch [:ens/save-preferred-name name])
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
          [name-item {:name name :action #(re-frame/dispatch [:ens/navigate-to-name name])}])]
       [react/text {:style {:color colors/gray :font-size 15}}
        (i18n/label :t/ens-no-usernames)])]
    [react/view {:style {:padding-top 22 :border-color colors/gray-lighter :border-top-width 1}}
     [react/text {:style {:color colors/gray :margin-horizontal 16}}
      (i18n/label :t/ens-chat-settings)]
     (when (> (count names) 1)
       [profile.components/settings-item {:label-kw  :ens-primary-username
                                          :value     preferred-name
                                          :action-fn #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                          {:content         (fn [] (name-list names preferred-name))
                                                                           :content-height  (+ 72 (* (min 4 (count names)) 64))}])}])
     [profile.components/settings-switch-item {:label-kw  :ens-show-username
                                               :action-fn #(re-frame/dispatch [:ens/switch-show-username])
                                               :value     show?}]]
    (let [message (merge {:from public-key :last-in-group? true :display-username? true :display-photo? true :username name
                          :content {:text (i18n/label :t/ens-test-message)} :content-type "text/plain" :timestamp-str "9:41 AM"}
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
