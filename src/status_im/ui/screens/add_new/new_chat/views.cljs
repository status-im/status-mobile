(ns status-im.ui.screens.add-new.new-chat.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [quo.react-native :as rn]
            [status-im.utils.core :as utils]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.invite.events :as invite.events]
            [quo.theme :as theme]
            [quo2.components.buttons.button :as button]
            [quo2.components.avatars.icon-avatar :as icon-avatar]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

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
   [text/text {:size   :paragraph-1
               :weight :semi-bold
               :style  {:color colors/neutral-100}}
    (i18n/label :you-have-no-contacts)]
   [text/text {:size  :paragraph-2
               :style {:color colors/neutral-100}}
    (i18n/label :invite-your-friends)]
   [button/button {:size 32 :on-press
                   #(re-frame/dispatch [::invite.events/share-link nil])}
    (i18n/label :invite-friends)]
   [button/button {:type :grey :size 32 :on-press #(%)}
    (i18n/label :add-a-contact)]])

(defn contact-item [contact]
  [rn/touchable-opacity
   [:<>
    [rn/view
     ;; [icon-avatar/icon-avatar {:size    :medium
     ;;                           :icon    icon
     ;;                           :color   color
     ;;                           :opacity opacity}]

     [text/text (:nickname contact)]
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
  (let [contacts @(re-frame/subscribe [:contacts/active])
        account  @(re-frame/subscribe [:multiaccount])]
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
        [rn/touchable-opacity {:on-press #(re-frame/dispatch [:navigate-back])}
         [icons/icon :main-icons2/close]]]]
      [rn/view {:flex           1
                :flex-direction :row
                :align-items    :center
                :padding-left   20}
       [text/text {:size   :heading-1
                   :weight :semi-bold
                   :style  {:color colors/neutral-100}} (i18n/label :new-chat)]]]
     (if (seq? contacts)
       (chat-contacts-exist contacts account)
       (chat-no-contacts))]))

(defn new-contact [] (fn [] [rn/view]))

(comment
  ;; open new-chat
  (re-frame/dispatch [:open-modal :new-chat])
  ;; close it
  (re-frame/dispatch [:navigate-back])
  )
