(ns status-im.ui.screens.chat.message.pinned-message
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [quo.design-system.colors :as colors]
            [quo.core :as quo]
            [quo2.foundations.colors :as quo2.colors]
            [quo2.foundations.typography :as typography]
            [reagent.core :as reagent]
            [status-im.chat.models.pin-message :as models.pin-message]
            [status-im.ui.components.list.views :as list]
            [status-im.utils.handlers :refer [<sub]]
<<<<<<< HEAD
<<<<<<< HEAD
            [status-im.ui.screens.chat.message.message :as message]))
            [status-im.ui.screens.chat.message.message-old :as message-old] ))
=======
            [status-im.ui.screens.chat.message.message-old :as message-old]
            [quo.react-native :as rn]
            [status-im.switcher.constants :as constants]
            [quo2.components.icon :as icons]))
>>>>>>> 409149f87... feat: unpin messages
=======
            [status-im.ui.screens.chat.message.styles :as style]
            [status-im.ui.screens.chat.message.message-old :as message-old]
            [quo.react-native :as rn]
            [status-im.switcher.constants :as constants]
            [quo2.components.icon :as icons]
            [quo2.reanimated :as reanimated]
            [quo.react :as react]))
>>>>>>> 3524c2971... feat: unpin messages new ui

(def selected-unpin (reagent/atom nil))

(defn render-pin-fn [{:keys [message-id outgoing] :as message}
                     _
                     _
                     {:keys [group-chat public? current-public-key space-keeper]}]
  [rn/touchable-without-feedback {:style {:width "100%"}
                                  :on-press #(reset! selected-unpin message-id)}
   [rn/view {:style {:flex-direction  :row
                     :align-items     :center
                     :justify-content :space-between
                     :flex            1
                     :padding-right   20}}
    [message-old/chat-message
     (assoc message
            :group-chat group-chat
            :public? public?
            :current-public-key current-public-key
            :show-input? false
            :pinned false
            :display-username? (not outgoing)
            :display-photo? false
            :last-in-group? false
            :in-popover? true)
     space-keeper]
    [rn/view {:style {:position    :absolute
                      :right       18
                      :padding-top 4}}
     [quo/radio {:value (= @selected-unpin message-id)}]]]])

(def list-key-fn #(or (:message-id %) (:value %)))

(defn pinned-messages-limit-list [chat-id]
  (let [pinned-messages @(re-frame/subscribe [:chats/pinned-sorted-list chat-id])]
    [list/flat-list
     {:key-fn                       list-key-fn
      :data                         (reverse pinned-messages)
      :render-data                  {:chat-id chat-id}
      :render-fn                    render-pin-fn
      :on-scroll-to-index-failed    identity
      :style                        {:flex-grow 0
                                     :border-top-width 1
                                     :border-bottom-width 1
                                     :border-top-color colors/gray-lighter
                                     :border-bottom-color colors/gray-lighter}
      :content-container-style      {:padding-bottom 10
                                     :padding-top 10}}]))

<<<<<<< HEAD

(defn pin-limit-popover []
<<<<<<< HEAD
  (let [{:keys [message]} (<sub [:popover/popover])]
    [react/view {:style {:flex-shrink 1}}
     [react/view {:style {:height 60
                          :justify-content :center}}
      [react/text {:style {:padding-horizontal 40
                           :text-align :center}}
       (i18n/label :t/pin-limit-reached)]]
     [pinned-messages-limit-list (message :chat-id)]
     [react/view {:flex-direction :row :padding-horizontal 16 :height 60 :justify-content :space-between :align-items :center}
      [quo/button
       {:on-press #(do
                     (reset! selected-unpin nil)
                     (re-frame/dispatch [:hide-popover]))
        :type :secondary}
       (i18n/label :t/cancel)]
      [quo/button
       {:on-press #(do
                     (re-frame/dispatch [::models.pin-message/send-pin-message {:chat-id    (message :chat-id)
                                                                                :message-id @selected-unpin
                                                                                :pinned    false}])
                     (re-frame/dispatch [::models.pin-message/send-pin-message (assoc message :pinned true)])
                     (re-frame/dispatch [:hide-popover])
                     (reset! selected-unpin nil))
        :type :secondary
        :disabled (nil? @selected-unpin)
        :theme (if (nil? @selected-unpin) :disabled :negative)}
       (i18n/label :t/unpin)]]]))

(defn message-render-fn []
  [react/text-class "asdf"]
  ;[{:keys [outgoing type] :as message}
  ;                       idx
  ;                       _
  ;                       {:keys [group-chat public? community? current-public-key
  ;                               chat-id show-input? message-pin-enabled edit-enabled in-pinned-view?]}]
  ;[react/view {:style (when (and platform/android? (not in-pinned-view?)) {:scaleY -1})}
  ; (if (= type :datemark)
  ;   [message-datemark/chat-datemark (:value message)]
  ;   [message/chat-message
  ;    (assoc message
  ;      :incoming-group (and group-chat (not outgoing))
  ;      :group-chat group-chat
  ;      :public? public?
  ;      :community? community?
  ;      :current-public-key current-public-key
  ;      :show-input? show-input?
  ;      :message-pin-enabled message-pin-enabled
  ;      :edit-enabled edit-enabled)])]
  )

(defn pinned-messages-list [chat-id]
  (let [
        ;pinned-messages (vec (vals @(re-frame/subscribe [:chats/pinned chat-id])))
        ]
  [react/view {:style {:padding-horizontal 20}}
   [react/text-class {:style (merge typography/heading-1 typography/font-semi-bold)} (i18n/label :t/pinned-messages)]
   [list/flat-list
    :data [1 2 3]
    :render-fn message-render-fn
    :key-fn first
    ]]
  ))

(defn pinned-banner [chat-id]
  (let [pinned-messages (<sub [:chats/pinned chat-id])
        latest-pin-text (get-in (first (vals pinned-messages)) [:content :text])
        pins-count (count (seq pinned-messages))]
    (when (> pins-count 0)
  [react/touchable-opacity-class
                      {:style {:height 50
                       :background-color quo2.colors/primary-50-opa-20
                       :flex-direction :row
                       :align-items :center
                       :padding-horizontal 20
                       :padding-vertical 10}
                       :active-opacity 1
                       :on-press (fn [actions]
                                    (re-frame/dispatch [:bottom-sheet/show-sheet
                                                        {:content #(pinned-messages-list chat-id)}]))}
   [message/pin-icon "#000000"]
   [react/text-class {:number-of-lines 1
                :style (merge typography/paragraph-2 {:margin-left 10 :margin-right 50})} latest-pin-text]
   [react/view {:style {:position :absolute
                        :right 22
                        :height 20
                        :width 20
                        :border-radius 8
                        :justify-content :center
                        :align-items :center
                        :background-color quo2.colors/neutral-80-opa-5}}
    [react/text-class {:style (merge typography/label typography/font-medium)} pins-count]]])))
=======
  (let [{:keys [width]} (constants/dimensions)]
  [rn/view {:style {:width (* width 0.95)
                    :background-color quo2.colors/neutral-80-opa-70
                    :flex-direction :row
                    :border-radius 16
                    :padding 12}}
   [rn/view {:style {:background-color quo2.colors/neutral-80-opa-20
                     :width 36
                     :height 36
                     :border-radius 18
                     :justify-content :center
                     :align-items :center}}
    [rn/view {:style {:width 18
                      :height 18
                      :border-radius 9
                      :border-color quo2.colors/danger-50-opa-40
                      :border-width 1
                      :justify-content :center
                      :align-items :center}}
     [rn/text {:style {:color quo2.colors/danger-50}} "!"]]
    ]
   [rn/view {:style {:margin-left 8}}
   [rn/text {:style (merge typography/paragraph-1 typography/font-semi-bold {:color "#ffffff"})} (i18n/label :t/cannot-pin-title)]
   [rn/text {:style (merge typography/paragraph-2 typography/font-regular {:color "#ffffff"})} (i18n/label :t/cannot-pin-desc)]
    [rn/view {:style {:background-color quo2.colors/primary-60
                      :border-radius 8
                      :justify-content :center
                      :align-items :center
                      :padding-horizontal 8
                      :padding-vertical 4
                      :align-self :flex-start
                      :margin-top 10}}
     [rn/text {:style (merge typography/paragraph-2 typography/font-medium  {:color "#ffffff"})} (i18n/label :t/view-pinned-messages)]]]
   [rn/touchable-opacity {:active-opacity 1
                          :on-press #(re-frame/dispatch [:hide-popover])
                           :style {:position :absolute
                                   :top 16
                                   :right 16}}
   [icons/icon :main-icons/close {:color "#ffffff"
                                  :height 8
                                  :width 8}]]]
  ))

;(defn pin-limit-popover []
;  (let [{:keys [message]} (<sub [:popover/popover])]
;    [rn/view {:style {:flex-shrink 1}}
;     [rn/view {:style {:height 60
;                          :justify-content :center}}
;      [rn/text {:style {:padding-horizontal 40
;                           :text-align :center}}
;       (i18n/label :t/pin-limit-reached)]]
;     [pinned-messages-limit-list (message :chat-id)]
;     [rn/view {:flex-direction :row :padding-horizontal 16 :height 60 :justify-content :space-between :align-items :center}
;      [quo/button
;       {:on-press #(do
;                     (reset! selected-unpin nil)
;                     (re-frame/dispatch [:hide-popover]))
;        :type :secondary}
;       (i18n/label :t/cancel)]
;      [quo/button
;       {:on-press #(do
;                     (re-frame/dispatch [::models.pin-message/send-pin-message {:chat-id    (message :chat-id)
;                                                                                :message-id @selected-unpin
;                                                                                :pinned    false}])
;                     (re-frame/dispatch [::models.pin-message/send-pin-message (assoc message :pinned true)])
;                     (re-frame/dispatch [:hide-popover])
;                     (reset! selected-unpin nil))
;        :type :secondary
;        :disabled (nil? @selected-unpin)
;        :theme (if (nil? @selected-unpin) :disabled :negative)}
;       (i18n/label :t/unpin)]]]))
>>>>>>> 409149f87... feat: unpin messages
=======
(defn pin-limit-popover [chat-id pinned-messages-list]
  [:f>
   (fn []
     (let [{:keys [width]} (constants/dimensions)
           show-pin-limit-modal? (<sub [:chats/pin-modal chat-id])
           opacity-animation (reanimated/use-shared-value 0)
           z-index-animation (reanimated/use-shared-value -1)]
       (react/effect! #(do
                         (reanimated/set-shared-value opacity-animation (reanimated/with-timing (if show-pin-limit-modal? 1 0)))
                         (reanimated/set-shared-value z-index-animation (reanimated/with-timing (if show-pin-limit-modal? 10 -1)))))
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:opacity opacity-animation
                                  :z-index z-index-animation}
                                 (style/pin-popover width))}
        [rn/view {:style (style/pin-alert-container)}
         [rn/view {:style (style/pin-alert-circle)}
          [rn/text {:style {:color quo2.colors/danger-50}} "!"]]]
        [rn/view {:style {:margin-left 8}}
         [rn/text {:style (merge typography/paragraph-1 typography/font-semi-bold {:color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-100)})} (i18n/label :t/cannot-pin-title)]
         [rn/text {:style (merge typography/paragraph-2 typography/font-regular {:color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-100)})} (i18n/label :t/cannot-pin-desc)]
         [rn/touchable-opacity
          {:active-opacity 1
           :on-press (fn []
                       (re-frame/dispatch [::models.pin-message/hide-pin-limit-modal chat-id])
                       (re-frame/dispatch [:bottom-sheet/show-sheet
                                           {:content #(pinned-messages-list chat-id)}]))
           :style (style/view-pinned-messages)}
          [rn/text {:style (merge typography/paragraph-2 typography/font-medium  {:color quo2.colors/white})} (i18n/label :t/view-pinned-messages)]]]
        [rn/touchable-opacity {:active-opacity 1
                               :on-press #(re-frame/dispatch [::models.pin-message/hide-pin-limit-modal chat-id])
                               :style {:position :absolute
                                       :top 16
                                       :right 16}}
         [icons/icon :main-icons/close {:color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-100)
                                        :height 8
                                        :width 8}]]]))])
>>>>>>> 3524c2971... feat: unpin messages new ui
