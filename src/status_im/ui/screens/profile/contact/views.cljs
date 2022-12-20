(ns status-im.ui.screens.profile.contact.views
  (:require [clojure.string :as string]
            [quo.components.list.item :as list-item]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.chat.models :as chat.models]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.profile.components.sheets :as sheets]
            [status-im.ui.screens.profile.contact.styles :as styles]
            [status-im.ui.screens.status.views :as status.views]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :as views]))

(defn actions
  [{:keys [public-key added? blocked? ens-name] :as contact} muted?]
  (concat [{:label               (i18n/label :t/chat)
            :icon                :main-icons/message
            :action              #(re-frame/dispatch [:contact.ui/send-message-pressed
                                                      {:public-key public-key
                                                       :ens-name   ens-name}])
            :accessibility-label :start-conversation-button}]
          (if added?
            [{:label               (i18n/label :t/remove-from-contacts)
              :icon                :main-icons/remove-contact
              :selected            true
              :accessibility-label :in-contacts-button
              :action              #(re-frame/dispatch [:contact.ui/remove-contact-pressed contact])}]
            [{:label               (i18n/label :t/add-to-contacts)
              :icon                :main-icons/add-contact
              :disabled            blocked?
              :accessibility-label :add-to-contacts-button
              :action              (when-not blocked?
                                     #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key
                                                          nil ens-name]))}])
          [{:label               (i18n/label (if (or muted? blocked?) :t/unmute :t/mute))
            :icon                :main-icons/notification
            :accessibility-label :mute-chat
            :selected            muted?
            :disabled            blocked?
            :action              (when-not blocked?
                                   #(re-frame/dispatch [::chat.models/mute-chat-toggled public-key
                                                        (not muted?)]))}]
          [{:label               (i18n/label (if blocked? :t/unblock :t/block))
            :negative            true
            :selected            blocked?
            :icon                :main-icons/cancel
            :action              (if blocked?
                                   #(re-frame/dispatch [:contact.ui/unblock-contact-pressed public-key])
                                   #(re-frame/dispatch [:show-popover
                                                        {:view             sheets/block-contact
                                                         :prevent-closing? true
                                                         :public-key       public-key}]))
            :accessibility-label (if blocked?
                                   :unblock-contact
                                   :block-contact)}]))

(defn render-detail
  [{:keys [public-key names name] :as detail}]
  [quo/list-item
   {:title               (:three-words-name names)
    :subtitle            [quo/text
                          {:monospace true
                           :color     :secondary}
                          (utils/get-shortened-address public-key)]
    :icon                [chat-icon/contact-icon-contacts-tab
                          (multiaccounts/displayed-photo detail)]
    :accessibility-label :profile-public-key
    :on-press            #(re-frame/dispatch [:show-popover
                                              (merge {:view    :share-chat-key
                                                      :address public-key}
                                                     (when (and (:ens-name names) name)
                                                       {:ens-name name}))])
    :accessory           [icons/icon :main-icons/share styles/contact-profile-detail-share-icon]}])

(defn profile-details
  [contact]
  (when contact
    [react/view
     [quo/list-header
      [quo/text
       {:accessibility-label :profile-details
        :color               :inherit}
       (i18n/label :t/profile-details)]]
     [render-detail contact]]))

(defn pin-settings
  [public-key pin-count]
  [quo/list-item
   {:title               (i18n/label :t/pinned-messages)
    :size                :small
    :accessibility-label :pinned-messages-item
    :accessory           :text
    :accessory-text      pin-count
    :disabled            (zero? pin-count)
    :on-press            #(re-frame/dispatch [:contact.ui/pinned-messages-pressed public-key])
    :chevron             true}])

(defn nickname-settings
  [{:keys [names]}]
  [quo/list-item
   {:title               (i18n/label :t/nickname)
    :size                :small
    :accessibility-label :profile-nickname-item
    :accessory           :text
    :accessory-text      (or (:nickname names) (i18n/label :t/none))
    :on-press            #(re-frame/dispatch [:open-modal :nickname])
    :chevron             true}])

(defn save-nickname
  [public-key nickname]
  (re-frame/dispatch [:contacts/update-nickname public-key nickname]))

(defn valid-nickname?
  [nickname]
  (not (string/blank? nickname)))

(defn- nickname-input
  [nickname entered-nickname public-key]
  [quo/text-input
   {:on-change-text      #(reset! entered-nickname %)
    :on-submit-editing   #(when (valid-nickname? @entered-nickname)
                            (save-nickname public-key @entered-nickname))
    :auto-capitalize     :none
    :auto-focus          false
    :max-length          32
    :accessibility-label :nickname-input
    :default-value       nickname
    :placeholder         (i18n/label :t/nickname)
    :return-key-type     :done
    :auto-correct        false}])

(defn nickname-view
  [public-key {:keys [nickname ens-name three-words-name]}]
  (let [entered-nickname (reagent/atom nickname)]
    (fn []
      [kb-presentation/keyboard-avoiding-view
       {:style         {:flex 1}
        :ignore-offset true}
       [topbar/topbar
        {:title    (i18n/label :t/nickname)
         :subtitle (or ens-name three-words-name)
         :modal?   true}]
       [react/view {:flex 1 :padding 16}
        [react/text {:style {:color colors/gray :margin-bottom 16}}
         (i18n/label :t/nickname-description)]
        [nickname-input nickname entered-nickname public-key]
        [react/text
         {:style {:align-self :flex-end
                  :margin-top 16
                  :color      colors/gray}}
         (str (count @entered-nickname) " / 32")]]
       [toolbar/toolbar
        {:show-border? true
         :center
         [quo/button
          {:type     :secondary
           :on-press #(save-nickname public-key @entered-nickname)}
          (i18n/label :t/done)]}]])))

(views/defview nickname
  []
  (views/letsubs [{:keys [public-key names]} [:contacts/current-contact]]
    [nickname-view public-key names]))

(defn button-item
  [{:keys [icon label action selected disabled negative]}]
  [react/touchable-highlight
   {:on-press            action
    :style               {:flex 1}
    :accessibility-label (str label "-item-button")}
   [react/view {:flex 1 :align-items :center}
    [list-item/icon-column
     {:icon          icon
      :size          :small
      :icon-bg-color (if disabled
                       colors/gray-lighter
                       (if negative
                         (if selected colors/red colors/red-light)
                         (if selected colors/blue colors/blue-light)))
      :icon-color    (if disabled
                       colors/gray
                       (if negative
                         (if selected colors/white colors/red)
                         (if selected colors/white colors/blue)))}]
    [react/text
     {:style           {:text-align  :center
                        :color       (if disabled
                                       colors/gray
                                       (if negative colors/red colors/blue))
                        :font-size   12
                        :line-height 16
                        :margin-top  6}
      :number-of-lines 2}
     label]]])

(defn profile
  []
  (let [{:keys [public-key name ens-verified] :as contact} @(re-frame/subscribe
                                                             [:contacts/current-contact])
        current-chat-id                                    @(re-frame/subscribe
                                                             [:chats/current-profile-chat])
        messages                                           @(re-frame/subscribe
                                                             [:chats/profile-messages-stream
                                                              current-chat-id])
        no-messages?                                       @(re-frame/subscribe [:chats/chat-no-messages?
                                                                                 current-chat-id])
        muted?                                             @(re-frame/subscribe [:chats/muted
                                                                                 public-key])
        pinned-messages                                    @(re-frame/subscribe [:chats/pinned
                                                                                 public-key])
        [first-name second-name]                           (multiaccounts/contact-two-names contact true)
        on-share                                           #(re-frame/dispatch
                                                             [:show-popover
                                                              (merge
                                                               {:view    :share-chat-key
                                                                :address public-key}
                                                               (when (and ens-verified name)
                                                                 {:ens-name name}))])]
    (when contact
      [:<>
       [quo/header
        {:right-accessories [{:icon                :main-icons/share
                              :accessibility-label :share-button
                              :on-press            on-share}]
         :left-accessories  [{:icon                :main-icons/close
                              :accessibility-label :back-button
                              :on-press            #(re-frame/dispatch [:navigate-back])}]}]
       [list/flat-list
        {:key-fn                    #(or (:message-id %) (:value %))
         :header                    [:<>
                                     [(profile-header/extended-header
                                       {:on-press         on-share
                                        :bottom-separator false
                                        :title            first-name
                                        :photo            (multiaccounts/displayed-photo contact)
                                        :monospace        (not ens-verified)
                                        :subtitle         second-name
                                        :public-key       public-key})]
                                     [react/view
                                      {:height 1 :background-color colors/gray-lighter :margin-top 8}]
                                     [nickname-settings contact]
                                     [pin-settings public-key (count pinned-messages)]
                                     [react/view {:height 1 :background-color colors/gray-lighter}]
                                     [react/view
                                      {:padding-top    17
                                       :flex-direction :row
                                       :align-items    :stretch
                                       :flex           1}
                                      (for [{:keys [label] :as action} (actions contact muted?)
                                            :when                      label]
                                        ^{:key label}
                                        [button-item action])]
                                     [react/view
                                      {:height 1 :background-color colors/gray-lighter :margin-top 16}]
                                     (when no-messages?
                                       [react/view {:padding-horizontal 32 :margin-top 32}
                                        [react/view (styles/updates-descr-cont)
                                         [react/text {:style {:color colors/gray :line-height 22}}
                                          (i18n/label :t/status-updates-descr)]]])]
         :ref                       #(reset! status.views/messages-list-ref %)
         :on-end-reached            #(re-frame/dispatch [:chat.ui/load-more-messages current-chat-id])
         :on-scroll-to-index-failed #()                        ;;don't remove this
         :render-data               {:chat-id current-chat-id
                                     :profile true}
         :render-fn                 status.views/render-message
         :data                      messages}]])))
