(ns status-im.ui.screens.add-new.new-chat.views
  ;; (:require
  ;;  [clojure.string :as string]
  ;;  [quo.core :as quo]
  ;;  [quo.design-system.colors :as colors]
  ;;  [quo.platform :as platform]
  ;;  [quo.react-native :as rn]
  ;;  [re-frame.core :as re-frame]
  ;;  [reagent.core :as reagent]
  ;;  [status-im.ethereum.ens :as ens]
  ;;  [status-im.i18n.i18n :as i18n]
  ;;  [status-im.multiaccounts.core :as multiaccounts]
  ;;  [status-im.qr-scanner.core :as qr-scanner]
  ;;  [status-im.ui.components.animation :as animation]
  ;;  [status-im.ui.components.chat-icon.screen :as chat-icon]
  ;;  [status-im.ui.components.icons.icons :as icons]
  ;;  [status-im.ui.components.invite.views :as invite]
  ;;  [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
  ;;  [status-im.ui.components.list.views :as list]
  ;;  [status-im.ui.components.react :as react]
  ;;  [status-im.ui.components.topbar :as topbar]
  ;;  [status-im.ui.screens.chat.photos :as photos]
  ;;  [status-im.utils.db :as utils.db]
  ;;  [status-im.utils.gfycat.core :as gfycat]
  ;;  [status-im.utils.identicon :as identicon]
  ;;  [status-im.utils.utils :as utils]
  ;;  [utils.debounce :as debounce])
  ;; (:require-macros [status-im.utils.views :as views])
  (:require [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.invite.events :as invite.events]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.db :as utils.db]
            [status-im.utils.utils :as utils]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]
            [quo.core :as quo]
            [quo.react-native :as rn]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]))

;; TODO(@esep): move styles to styles.cljs (https://github.com/status-im/status-mobile/blob/develop/doc/new-guidelines.md#component-styles)

(defn chat-no-contacts []
  [rn/view {:top             128
            :height          302
            :padding         12
            :flex-direction  :column
            :justify-content :space-around
            :align-items     :center}
   [rn/view {:style {:height           120
                     :width            120
                     :background-color colors/neutral-40}}]
   [quo2/text {:size   :paragraph-1
               :weight :semi-bold
               :style  {:color colors/neutral-100}}
    (i18n/label :you-have-no-contacts)]
   [quo2/text {:size  :paragraph-2
               :style {:color colors/neutral-100}}
    (i18n/label :invite-your-friends)]
   [quo2/button {:size 32 :on-press
                 #(rf/dispatch [::invite.events/share-link nil])}
    (i18n/label :invite-friends)]
   [quo2/button {:type :grey :size 32 :on-press #(%)}
    (i18n/label :add-a-contact)]])

(defn contact-item [contact]
  [rn/touchable-opacity
   [:<>
    [rn/view
     ;; [icon-avatar/icon-avatar {:size    :medium
     ;;                           :icon    icon
     ;;                           :color   color
     ;;                           :opacity opacity}]

     [quo2/text (:alias contact)]
     ]]])

(defn contact-list-key-fn [contact]
  (:public-key contact))

(defn render-fn [contact]
  [contact-item contact])

(defn chat-contacts-exist [contacts account]
  ;; show contact list
  [rn/view {:top             128
            :height          302
            :padding         12
            :flex-direction  :column
            :justify-content :space-around
            :align-items     :center}
   ;; user-avatar
   ;; for status: either group-avatar or verified check
   [list/flat-list
    {:key-fn                       contact-list-key-fn
     ;; :getItemLayout                get-item-layout
     ;; :on-end-reached               #(%)
     ;; :keyboard-should-persist-taps :always
     :data                         contacts
     :render-fn                    render-fn}]
   ])

;; (merge (when key-fn            {:keyExtractor (memo-wrap-key-fn key-fn)})
;;        (when render-fn         {:renderItem (memo-wrap-render-fn render-fn render-data)})
;;        (when separator         {:ItemSeparatorComponent (memo-separator-fn separator default-separator?)})
;;        (when empty-component   {:ListEmptyComponent (memo-as-element empty-component)})
;;        (when header            {:ListHeaderComponent (memo-as-element header)})
;;        (when footer            {:ListFooterComponent (memo-as-element footer)}))

(def x (atom {}))
(defn new-chat []
  (let [contacts (rf/sub [:contacts/active])
        account  (rf/sub [:multiaccount])]
    (swap! x assoc :contacts contacts)
    (swap! x assoc :account account)
    [rn/view
     [rn/view {:height         120
               :flex-direction :column}
      [rn/view {:height 20}]
      [rn/view {:flex-direction  :row
                :align-items     :flex-start
                :justify-content :flex-start
                :padding-left    20
                :height          44}
       [rn/view {:justify-content  :center
                 :align-items      :center
                 :background-color colors/neutral-10
                 :width            32
                 :height           32
                 :border-radius    10}
        [rn/touchable-opacity {:on-press #(rf/dispatch [:navigate-back])}
         [icons/icon :main-icons2/close]]]]
      [rn/view {:flex           1
                :flex-direction :row
                :align-items    :center
                :padding-left   20}
       [quo2/text {:size   :heading-1
                   :weight :semi-bold
                   :style  {:color colors/neutral-100}} (i18n/label :new-chat)]]]
     (if (seq? contacts)
       (chat-contacts-exist contacts account)
       (chat-no-contacts))]))

;; (defn new-contact [] (fn [] [rn/view]))

(defn- icon-wrapper
  [color icon]
  [react/view
   {:style {:width            32
            :height           32
            :border-radius    25
            :align-items      :center
            :justify-content  :center
            :background-color color}}
   icon])

(defn- input-icon
  [state new-contact? entered-nickname blocked?]
  (let [icon (if new-contact? :main-icons/add :main-icons/arrow-right)]
    (cond
      (= state :searching)
      [icon-wrapper colors/neutral-40
       [react/activity-indicator {:color colors/neutral-5}]]

      (and (= state :valid) (not blocked?))
      [react/touchable-highlight
       {:on-press #(debounce/dispatch-and-chill [:contact.ui/contact-code-submitted new-contact? entered-nickname] 3000)}
       [icon-wrapper colors/primary-50
        [icons/icon icon {:color colors/neutral-5}]]]

      :else
      [icon-wrapper colors/neutral-40
       [icons/icon icon {:color colors/neutral-5}]])))

(defn get-validation-label
  [value]
  (case value
    :invalid
    (i18n/label :t/profile-not-found)
    :yourself
    (i18n/label :t/can-not-add-yourself)))

(defn- nickname-input [entered-nickname]
  [quo/text-input
   {:on-change-text      #(reset! entered-nickname %)
    :auto-capitalize     :none
    :max-length          32
    :auto-focus          false
    :accessibility-label :nickname-input
    :placeholder         (i18n/label :t/add-nickname)
    :return-key-type     :done
    :auto-correct        false}])

(defn new-contact
  []
  (let [entered-nickname (reagent/atom "")]
    (fn []
      (let [{:keys [state ens-name public-key error]}
            (rf/sub [:contacts/new-identity])
            blocked? (and
                      (utils.db/valid-public-key? (or public-key ""))
                      (rf/sub [:contacts/contact-blocked? public-key]))]
        [rn/view {:style {:flex 1}}
         [topbar/topbar
          {:title             (i18n/label :t/new-contact)
           :modal?            true
           :right-accessories
           [{:icon                :qr
             :accessibility-label :scan-contact-code-button
             :on-press
             #(rf/dispatch [::qr-scanner/scan-code
                            {:title        (i18n/label :t/new-contact)
                             :handler      :contact/qr-code-scanned
                             :new-contact? true
                             :nickname     @entered-nickname}])}]}]
         [rn/view {:flex-direction :row
                   :padding        16}
          [rn/view {:flex          1
                    :padding-right 16}
           [quo/text-input
            {:on-change-text
             #(do
                (rf/dispatch [:set-in [:contacts/new-identity :state] :searching])
                (debounce/debounce-and-dispatch
                 [:new-chat/set-new-identity %] 600))
             :on-submit-editing
             #(when (= state :valid)
                (debounce/dispatch-and-chill
                 [:contact.ui/contact-code-submitted true @entered-nickname] 3000))
             :placeholder         (i18n/label :t/enter-contact-code)
             :show-cancel         false
             :accessibility-label :enter-contact-code-input
             :auto-capitalize     :none
             :return-key-type     :go}]]
          [rn/view {:justify-content :center
                    :align-items     :center}
           [input-icon state true @entered-nickname blocked?]]]
         [rn/view {:min-height 30 :justify-content :flex-end :margin-bottom 16}
          [quo/text {:style {:margin-horizontal 16}
                     :size  :small
                     :align :center
                     :color :secondary}
           (cond (= state :error)
                 (get-validation-label error)
                 (= state :valid)
                 (str (when ens-name (str ens-name " • "))
                      (utils/get-shortened-address public-key))
                 :else "")]]
         [react/text {:style {:margin-horizontal 16 :color colors/neutral-40}}
          (i18n/label :t/nickname-description)]
         [rn/view {:padding 16}
          [nickname-input entered-nickname]
          [react/text {:style {:align-self :flex-end :margin-top 16
                               :color      colors/neutral-40}}
           (str (count @entered-nickname) " / 32")]]]))))

(comment
  ;; open new-chat
  (rf/dispatch [:open-modal :new-chat])
  ;; close it
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:logout])
  )
