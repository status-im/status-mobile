(ns status-im.ui.screens.signing.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.accounts.core :as accounts]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as anim]
            [reagent.core :as reagent]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.button.view :as button]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.i18n :as i18n]
            [status-im.utils.security :as security]
            [status-im.ui.screens.signing.sheets :as sheets]
            [status-im.ethereum.tokens :as tokens]
            [clojure.string :as string]
            [status-im.ui.screens.signing.styles :as styles]))

(defn hide-panel-anim
  [bottom-anim-value alpha-value window-height]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         (- window-height)
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0
                               :duration        500
                               :useNativeDriver true})])))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         40
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0.4
                               :duration        500
                               :useNativeDriver true})])))

(defn separator []
  [react/view {:height 1 :background-color colors/gray-lighter}])

(defn acc-text [txt1 txt2]
  [react/nested-text nil
   txt1 " "
   [{:style {:color colors/gray}} txt2]])

(defn displayed-name [contact]
  (if (or (:preferred-name contact) (:name contact))
    (accounts/displayed-name contact)
    (:address contact)))

(defn contact-item [contact]
  [list-item/list-item {:type        :small
                        :title       (i18n/label :t/to)
                        :accessories [[react/text {:ellipsize-mode :middle :number-of-lines 1 :style {:flex-wrap :wrap}}
                                       (displayed-name contact)]]}])

(defn token-item [{:keys [icon color] :as token} display-symbol]
  (when token
    [react/view
     [list-item/list-item
      {:type        :small :title (i18n/label :t/wallet-asset)
       :accessories [[acc-text (:name token) display-symbol]
                     (if icon
                       [list/item-image (assoc icon
                                               :style {:background-color colors/gray-lighter
                                                       :border-radius    16}
                                               :image-style {:width 32 :height 32})]
                       [chat-icon/custom-icon-view-list (:name token) color 32])]}]
     [separator]]))

(defn header [{:keys [in-progress?] :as sign} {:keys [contact amount token approve?] :as tx} display-symbol fee fee-display-symbol]
  [react/view styles/header
   (when sign
     [react/touchable-highlight (when-not in-progress? {:on-press #(re-frame/dispatch [:set :signing/sign nil])})
      [react/view {:padding-right 16}
       [icons/icon :main-icons/back]]])
   [react/view {:flex 1}
    (if amount
      [react/text {:style {:typography :title-bold}} (str (if approve? (i18n/label :t/authorize) (i18n/label :t/sending))
                                                          " " amount " " display-symbol)]
      [react/text {:style {:typography :title-bold}} (i18n/label :t/contract-interaction)])
    (if sign
      [react/nested-text {:style           {:color colors/gray}
                          :ellipsize-mode  :middle
                          :number-of-lines 1} (i18n/label :t/to) " "
       [{:style {:color colors/black}} (displayed-name contact)]]
      [react/text {:style {:margin-top 6 :color colors/gray}}
       (str fee " " fee-display-symbol " " (string/lower-case (i18n/label :t/network-fee)))])]
   [react/touchable-highlight (when-not in-progress? {:on-press #(re-frame/dispatch [:signing.ui/cancel-is-pressed])})
    [react/view {:padding 6}
     [react/text {:style {:color colors/blue}} (i18n/label :t/cancel)]]]])

(views/defview password-view [{:keys [type error in-progress? enabled?]}]
  (views/letsubs [phrase [:signing/phrase]]
    (case type
      :password
      [react/view {:padding-top 16 :padding-bottom 16}
       [react/view {:align-items :center}
        [react/text {:style {:color colors/gray :padding-bottom 8}} (i18n/label :t/signing-phrase)]
        [react/text phrase]]
       [text-input/text-input-with-label
        {:secure-text-entry   true
         :placeholder         (i18n/label :t/enter-password)
         :on-change-text      #(re-frame/dispatch [:signing.ui/password-is-changed (security/mask-data %)])
         :accessibility-label :enter-password-input
         :auto-capitalize     :none
         :editable            (not in-progress?)
         :error               error
         :container           {:margin-top 24 :margin-bottom 32 :margin-horizontal 16}}]
       [react/view {:align-items :center :height 44}
        (if in-progress?
          [react/activity-indicator {:animating true
                                     :size      :large}]
          [button/primary-button {:on-press  #(re-frame/dispatch [:signing.ui/sign-is-pressed])
                                  :disabled? (not enabled?)}
           (i18n/label :t/transactions-sign)])]]
      [react/view])))

(views/defview message-sheet []
  (views/letsubs [{:keys [formatted-data] :as sign} [:signing/sign]]
    [react/view styles/message
     [react/view styles/message-header
      [react/text {:style {:typography :title-bold}} (i18n/label :t/signing-a-message)]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:signing.ui/cancel-is-pressed])}
       [react/view {:padding 6}
        [react/text {:style {:color colors/blue}} (i18n/label :t/cancel)]]]]
     [separator]
     [react/view {:padding-top 16}
      [react/view styles/message-border
       [react/scroll-view
        [react/text (or formatted-data "")]]]
      [password-view sign]]]))

(views/defview sheet [{:keys [contact amount token approve?] :as tx}]
  (views/letsubs [fee   [:signing/fee]
                  sign  [:signing/sign]
                  chain [:ethereum/chain-keyword]
                  {:keys [amount-error gas-error]} [:signing/amount-errors]]
    (let [display-symbol     (wallet.utils/display-symbol token)
          fee-display-symbol (wallet.utils/display-symbol (tokens/native-currency chain))]
      [react/view styles/sheet
       [header sign tx display-symbol fee fee-display-symbol]
       [separator]
       (if sign
         [react/view {:padding-top 20}
          [password-view sign]]
         [react/view
          [contact-item contact]
          [separator]
          [token-item token display-symbol]
          (when-not approve?
            [react/view
             [list-item/list-item {:type        :small :title (i18n/label :t/send-request-amount)
                                   :error       amount-error
                                   :accessories [[acc-text (if amount (str amount) "0")
                                                  (or display-symbol fee-display-symbol)]]}]
             [separator]])
          [list-item/list-item
           {:type        :small :title (i18n/label :t/network-fee) :error gas-error
            :accessories [[acc-text fee fee-display-symbol] :chevron]
            :on-press    #(re-frame/dispatch [:signing.ui/open-fee-sheet
                                              {:content        (fn [] [sheets/fee-bottom-sheet fee-display-symbol])
                                               :content-height 270}])}]
          [react/view {:align-items :center :margin-top 16 :margin-bottom 40}
           [button/primary-button {:on-press  #(re-frame/dispatch [:set :signing/sign {:type :password}])
                                   :disabled? (or amount-error gas-error)}
            (i18n/label :t/sign-with-password)]]])])))

(defn signing-view [tx window-height]
  (let [bottom-anim-value (anim/create-value window-height)
        alpha-value       (anim/create-value 0)
        clear-timeout     (atom nil)
        current-tx        (reagent/atom nil)
        update?           (reagent/atom nil)]
    (reagent/create-class
     {:component-will-update (fn [_ [_ tx _]]
                               (when @clear-timeout (js/clearTimeout @clear-timeout))
                               (cond
                                 @update?
                                 (do (reset! update? false)
                                     (show-panel-anim bottom-anim-value alpha-value))

                                 (and @current-tx tx)
                                 (do (reset! update? true)
                                     (js/setTimeout #(reset! current-tx tx) 600)
                                     (hide-panel-anim bottom-anim-value alpha-value (- window-height)))

                                 tx
                                 (do (reset! current-tx tx)
                                     (show-panel-anim bottom-anim-value alpha-value))

                                 :else
                                 (do (reset! clear-timeout (js/setTimeout #(reset! current-tx nil) 500))
                                     (hide-panel-anim bottom-anim-value alpha-value (- window-height)))))
      :reagent-render        (fn []
                               (when @current-tx
                                 [react/view {:position :absolute :top 0 :bottom 0 :left 0 :right 0}
                                  [react/animated-view {:flex 1 :background-color :black :opacity alpha-value}]
                                  [react/animated-view {:style
                                                        {:position  :absolute
                                                         :height    window-height
                                                         :left      0
                                                         :right     0
                                                         ;;:bottom bottom-anim-value
                                                         :transform [{:translateY bottom-anim-value}]}}
                                   [react/keyboard-avoiding-view {:style {:flex 1}}
                                    [react/view {:flex 1}]
                                    (if (:message @current-tx)
                                      [message-sheet]
                                      [sheet @current-tx])]]]))})))

(views/defview signing []
  (views/letsubs [tx [:signing/tx]
                  {window-height :height} [:dimensions/window]]
    ;;we use select-keys here because we don't want to update view if other keys in map is changed
    [signing-view (when tx (select-keys tx [:contact :amount :token :approve? :message])) window-height]))
