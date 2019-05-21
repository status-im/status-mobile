(ns status-im.ui.screens.ens.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.stateofus :as stateofus])
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
  [react/touchable-opacity {:on-press on-press}
   [react/text {:style {:text-align-vertical :center :color colors/blue}}
    label]])

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
     [toolbar/simple-toolbar
      (i18n/label :t/ens-terms-registration)]
     [react/view {:style {:height 136 :background-color colors/gray-lighter :justify-content :center :align-items :center}}
      [react/text {:style {:text-align-vertical :center :text-align :center
                           :typography :header :letter-spacing -0.275}}
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
      [react/text {:style {:font-size 15 :margin-top 24 :margin-horizontal 16}}
       (i18n/label :t/ens-terms-point-8)]
      [term-point
       (i18n/label :t/ens-terms-point-9 {:address contract})]
      [react/view {:style {:align-items :center :margin-top 16}}
       [link {:on-press #(.openURL react/linking (etherscan-url contract))}
        (i18n/label :t/etherscan-lookup)]]
      [term-point
       (i18n/label :t/ens-terms-point-10)]
      [react/view {:style {:align-items :center :margin-top 16 :margin-bottom 24}}
       [link {:on-press #(.openURL react/linking (etherscan-url "0x314159265dd8dbb310642f98f50c066173c1259b"))}
        (i18n/label :t/etherscan-lookup)]]]]))

;; Registration

(defn- valid-domain? [state]
  (#{:registrable :owned :connected} state))

(defn- final-state? [state]
  (#{:connected :registered :registration-failed} state))

(defn- main-icon [state]
  (cond
    (valid-domain? state)    :main-icons/check
    (= state :unregistrable) :main-icons/cancel
    :else                    :main-icons/username))

(defn- icon-wrapper [color icon]
  [react/view {:style {:margin-right 10 :width 32 :height 32 :border-radius 25 :align-items :center :justify-content :center
                       :background-color color}}
   icon])

(defn- input-icon [state]
  (cond
    (= state :registering)
    nil

    (= state :typing)
    [icon-wrapper colors/blue
     [react/activity-indicator {:color colors/white}]]

    (valid-domain? state)
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:ens/set-state :registering])}
     [icon-wrapper colors/blue
      [vector-icons/icon :main-icons/arrow-right {:color colors/white}]]]

    :else
    [icon-wrapper colors/gray
     [vector-icons/icon :main-icons/arrow-right {:color colors/white}]]))

(defn- default-name [custom-domain?]
  (if custom-domain?
    "username.domain.eth"
    "username"))

(defn- domain-label [custom-domain?]
  (if custom-domain?
    (i18n/label :t/ens-custom-domain)
    (str "." stateofus/domain)))

(defn- domain-switch-label [custom-domain?]
  (if custom-domain?
    (i18n/label :t/ens-want-domain)
    (i18n/label :t/ens-want-custom-domain)))

(defn- help-message [state custom-domain?]
  (if custom-domain?
    (case state
      :owned
      (i18n/label :t/ens-custom-username-owned)
      :unregistrable
      (i18n/label :t/ens-custom-username-unregistrable)
      (i18n/label :t/ens-custom-username-typing))
    (case state
      :invalid
      (i18n/label :t/ens-username-invalid)
      :registrable
      (i18n/label :t/ens-username-registrable)
      :unregistrable
      (i18n/label :t/ens-username-unregistrable)
      (i18n/label :t/ens-username-hints))))

(defn- on-username-change [custom-domain? username]
  (re-frame/dispatch [:ens/set-username custom-domain? username]))

(defn- on-registration [args]
  (re-frame/dispatch [:ens/register args]))

(defn- section [{:keys [title content]}]
  [react/view {:style {:margin-horizontal 16 :align-items :flex-start}}
   [react/text {:style {:color colors/gray :font-size 15}}
    title]
   [react/view {:margin-top 8 :padding-horizontal 16 :padding-vertical 12 :border-width 1 :border-radius 12 :border-color colors/gray-light}
    [react/text {:style {:color colors/black :font-size 15}}
     content]]])

(defn- agreement [{:keys [checked contract]}]
  [react/view {:flex-direction :row :margin-left 6 :margin-top 14 :align-items :center}
   [checkbox/checkbox {:checked?        @checked
                       :style           {:padding 10}
                       :on-value-change #(reset! checked %)}]
   [react/text {:style {:text-align-vertical :center}}
    (i18n/label :t/ens-agree-to)]
   [link {:on-press #(re-frame/dispatch [:navigate-to :ens-terms {:contract contract}])}
    (str (i18n/label :t/ens-terms-registration) " ->")]])

(defn- registration-bottom-bar [{:keys [contract username checked address public-key]}]
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
       (i18n/label :t/ens-10-SNT)]
      [react/text {:style {:color colors/gray :font-size 15}}
       (i18n/label :t/ens-deposit)]]]
    [button {:disabled?    (not @checked)
             :label-style  (when (not @checked) {:color colors/gray})
             :on-press     #(on-registration {:username username :address address :public-key public-key :contract contract})}
     (i18n/label :t/ens-register)]]])

(defn- registration [{:keys [address public-key] :as props}]
  [react/view {:style {:flex 1 :margin-top 24}}
   [section {:title   (i18n/label :t/ens-wallet-address)
             :content address}]
   [react/view {:style {:margin-top 14}}
    [section {:title   (i18n/label :t/contact-code)
              :content public-key}]]
   [agreement props]])

;; states: initial, typing, (invalid, unregistrable, registrable, owned, connected), registering (from registrable), (connected, registered, :registration-failed)

(defn- icon [{:keys [state]}]
  [react/view {:style {:margin-top 68 :margin-bottom 24  :width 60 :height 60 :border-radius 30 :background-color colors/blue :align-items :center :justify-content :center}}
   [vector-icons/icon (main-icon state) {:color colors/white}]])

(defn- username-input [{:keys [username custom-domain? state] :as props}]
  [react/view {:flex-direction :row}
   [react/text-input {:on-change-text    #(on-username-change custom-domain? %)
                      :on-submit-editing #(on-registration props)
                      :auto-capitalize   :none
                      :auto-correct      false
                      :default-value             username
                      :auto-focus        true
                      :placeholder       (default-name custom-domain?)
                      :style             {:flex 1 (if (= state :registering) :padding-horizontal :padding-left) 48 :text-align :center :font-size 22}}]
   [input-icon state]])

(defn- final-state-label [state]
  (case state
    :registered
    (i18n/label :t/ens-registered-title)
    :connected
    (i18n/label :t/ens-connected-title)
    (i18n/label :t/ens-registration-failed-title)))

(defn- final-state-details [{:keys [state username]}]
  (case state
    :registered
    [react/text {:style {:color colors/gray :font-size 14 :text-align :center}}
     (i18n/label :t/ens-registered)]
    :registration-failed
    [react/text {:style {:color colors/gray :font-size 14 :text-align :center}}
     (i18n/label :t/ens-registration-failed)]
    :connected
    [react/view {:flex-direction :row :align-items :center}
     [react/text {:style {:text-align-vertical :center :color colors/black}}
      (stateofus/domain username)]
     [react/text {:style {:text-align-vertical :center}}
      (i18n/label :t/ens-connected)]]
    [react/view {:flex-direction :row :margin-left 6 :margin-top 14 :align-items :center}
     [react/text {:style {:text-align-vertical :center}}
      (str (i18n/label :t/ens-terms-registration) " ->")]]))

(defn- finalized-icon [{:keys [state]}]
  (case state
    :registration-failed
    [react/view {:style {:width 40 :height 40 :border-radius 30 :background-color colors/red-light :align-items :center :justify-content :center}}
     [vector-icons/icon :main-icons/warning {:color colors/red}]]
    [react/view {:style {:width 40 :height 40 :border-radius 30 :background-color colors/gray-lighter :align-items :center :justify-content :center}}
     [vector-icons/icon :main-icons/check {:color colors/blue}]]))

(defn- registration-finalized [{:keys [state] :as props}]
  [react/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [finalized-icon props]
   [react/text {:style {:color colors/black :typography :header :margin-top 32 :margin-horizontal 32 :text-align :center}}
    (final-state-label state)]
   [react/view {:align-items :center :margin-horizontal 32 :margin-top 12 :margin-bottom 20}
    [final-state-details props]]
   (if (= state :registration-failed)
     [react/view
      [button {:on-press #(re-frame/dispatch [:ens/set-state :registering])}
       (i18n/label :t/retry)]
      [button {:background? false
               :on-press    #(re-frame/dispatch [:ens/navigate-back])}
       (i18n/label :t/cancel)]]
     [button {:on-press #(re-frame/dispatch [:ens/navigate-back])}
      (i18n/label :t/ens-got-it)])])

(defn- registration-pending [{:keys [state custom-domain?] :as props}]
  [react/view {:style {:flex 1}}
   [react/scroll-view {:style {:flex 1}}
    [react/view {:style {:flex 1}}
     [react/view {:style {:flex 1 :align-items :center :justify-content :center}}
      [icon props]
      [username-input props]
      [react/view {:style {:height 36 :align-items :center :justify-content :space-between :padding-horizontal 12 :margin-top 24 :margin-horizontal 16 :border-color colors/gray-lighter :border-radius 20 :border-width 1 :flex-direction :row}}
       [react/text {:style {:font-size 12}}
        (domain-label custom-domain?)]
       [react/view {:flex 1 :min-width 24}]
       (when-not (= state :registering)
         ;; Domain type is not shown during registration
         [react/touchable-highlight {:on-press #(re-frame/dispatch [:ens/switch-domain-type])}
          [react/text {:style {:color colors/blue :font-size 12} :number-of-lines 2}
           (domain-switch-label custom-domain?)]])]]
     (if (= state :registering)
       [registration props]
       [react/text {:style {:flex 1 :margin-top 16 :margin-horizontal 16 :font-size 14 :text-align :center}}
        (help-message state custom-domain?)])]]
   (when (= state :registering)
     [registration-bottom-bar props])])

(defn- toolbar []
  [toolbar/toolbar nil
   [toolbar/nav-button (actions/back #(re-frame/dispatch [:ens/navigate-back]))]
   [toolbar/content-title (i18n/label :t/ens-your-username)]])

(views/defview register []
  (views/letsubs [{:keys [registrar]}          [:get-screen-params :ens-register]
                  state                        [:ens/state]
                  username                     [:ens/username]
                  custom-domain?               [:ens/custom-domain?]
                  {:keys [address public-key]} [:account/account]]
    (let [custom-domain? (or custom-domain? false)
          checked       (reagent/atom false)
          props          {:state state :username username :custom-domain? custom-domain? :contract registrar
                          :checked checked :address (ethereum/normalized-address address) :public-key public-key}]
      [react/keyboard-avoiding-view {:flex 1}
       [toolbar]
       (if (final-state? state)
         [registration-finalized props]
         [registration-pending props])])))

;; Welcome

(defn- welcome-item [{:keys [icon-label title]} content]
  [react/view {:style {:flex 1 :margin-top 24 :margin-left 16 :flex-direction :row}}
   [react/view {:style {:height 40 :width 40 :border-radius 25 :border-width 1 :border-color colors/gray-lighter :align-items :center :justify-content :center}}
    [react/text {:style {:typography :header}}
     icon-label]]
   [react/view {:style {:flex 1 :margin-horizontal 16}}
    [react/text {:style {:color colors/black :font-size 15 :typography :main-semibold}}
     title]
    content]])

(views/defview welcome []
  (views/letsubs [props [:get-screen-params :ens-welcome]]
    [react/view {:style {:flex 1}}
     [toolbar/simple-toolbar
      (i18n/label :t/ens-usernames)]
     [react/scroll-view {:style {:flex 1}}
      [react/view {:style {:flex 1 :align-items :center}}
       [react/image {:source (:ens-header resources/ui)
                     :style  {:margin-top 32}}]
       [react/text {:style {:margin-top 32 :margin-bottom 8 :color colors/black :typography :header}}
        (i18n/label :t/ens-get-name)]
       [react/text {:style {:margin-top 8 :margin-bottom 24 :color colors/gray :font-size 15 :margin-horizontal 16 :text-align :center}}
        (i18n/label :t/ens-welcome-hints)]
       [welcome-item {:icon-label "1" :title (i18n/label :t/ens-welcome-point-1-title)}
        [react/view {:flex-direction :row}
         [react/nested-text
          {:style {:color colors/gray}}
          (i18n/label :t/ens-welcome-point-1)
          [{:style {:color colors/black :text-decoration-line :underline}}
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
        (i18n/label :t/ens-powered-by)]]]
     [react/view {:align-items :center :padding-top 8 :padding-bottom 16 :background-color colors/white :position :absolute :left 0 :right 0 :bottom 0
                  :border-top-width 1 :border-top-color colors/gray-lighter}
      [components.common/button {:on-press #(re-frame/dispatch [:navigate-to :ens-register props])
                                 :label    (i18n/label :t/get-started)}]]]))
