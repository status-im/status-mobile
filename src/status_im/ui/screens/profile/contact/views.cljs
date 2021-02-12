(ns status-im.ui.screens.profile.contact.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.chat.models :as chat.models]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.components.sheets :as sheets]
            [status-im.ui.screens.profile.contact.styles :as styles]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.utils.platform :as platform]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [quo.components.list.item :as list-item]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.screens.status.views :as status.views]
            [status-im.ui.screens.chat.views :as chat.views])
  (:require-macros [status-im.utils.views :as views]))

(defn actions
  [{:keys [public-key added? blocked?] :as contact} muted?]
  (concat [{:label               (i18n/label :t/chat)
            :icon                :main-icons/message
            :action              #(re-frame/dispatch [:contact.ui/send-message-pressed {:public-key public-key}])
            :accessibility-label :start-conversation-button}]
          (if added?
            [{:label               (i18n/label :t/remove-from-contacts)
              :icon                :main-icons/remove-contact
              :selected            true
              :accessibility-label :in-contacts-button
              :action              #(re-frame/dispatch [:contact.ui/remove-contact-pressed contact])}]
            [{:label               (i18n/label :t/add-to-contacts)
              :icon                :main-icons/add-contact
              :accessibility-label :add-to-contacts-button
              :action              #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])}])
          (when platform/ios?
            [{:label               (i18n/label (if muted? :t/unmute :t/mute))
              :icon                :main-icons/notification
              :accessibility-label :mute-chat
              :selected            muted?
              :action              #(re-frame/dispatch [::chat.models/mute-chat-toggled public-key (not muted?)])}])
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

(defn render-detail [{:keys [public-key names name] :as detail}]
  [quo/list-item
   {:title               (:three-words-name names)
    :subtitle            [quo/text {:monospace true
                                    :color     :secondary}
                          (utils/get-shortened-address public-key)]
    :icon                [chat-icon/contact-icon-contacts-tab
                          (multiaccounts/displayed-photo detail)]
    :accessibility-label :profile-public-key
    :on-press            #(re-frame/dispatch [:show-popover (merge {:view    :share-chat-key
                                                                    :address public-key}
                                                                   (when (and (:ens-name names) name)
                                                                     {:ens-name name}))])
    :accessory           [icons/icon :main-icons/share styles/contact-profile-detail-share-icon]}])

(defn profile-details [contact]
  (when contact
    [react/view
     [quo/list-header
      [quo/text {:accessibility-label :profile-details
                 :color               :inherit}
       (i18n/label :t/profile-details)]]
     [render-detail contact]]))

(defn nickname-settings [{:keys [names]}]
  [quo/list-item
   {:title               (i18n/label :t/nickname)
    :size                :small
    :accessibility-label :profile-nickname-item
    :accessory           :text
    :accessory-text      (or (:nickname names) (i18n/label :t/none))
    :on-press            #(re-frame/dispatch [:navigate-to :nickname])
    :chevron             true}])

(defn save-nickname [public-key nickname]
  (re-frame/dispatch [:contacts/update-nickname public-key nickname]))

(defn valid-nickname? [nickname]
  (not (string/blank? nickname)))

(defn- nickname-input [nickname entered-nickname public-key]
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

(defn nickname-view [public-key {:keys [nickname ens-name three-words-name]}]
  (let [entered-nickname (reagent/atom nickname)]
    (fn []
      [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
       [topbar/topbar {:title    (i18n/label :t/nickname)
                       :subtitle (or ens-name three-words-name)
                       :modal?   true}]
       [react/view {:flex 1 :padding 16}
        [react/text {:style {:color colors/gray :margin-bottom 16}}
         (i18n/label :t/nickname-description)]
        [nickname-input nickname entered-nickname public-key]
        [react/text {:style {:align-self :flex-end :margin-top 16
                             :color      colors/gray}}
         (str (count @entered-nickname) " / 32")]]
       [toolbar/toolbar {:show-border? true
                         :center
                         [quo/button
                          {:type     :secondary
                           :on-press #(save-nickname public-key @entered-nickname)}
                          (i18n/label :t/done)]}]])))

(views/defview nickname []
  (views/letsubs [{:keys [public-key names]} [:contacts/current-contact]]
    [nickname-view public-key names]))

(defn button-item [{:keys [icon label action selected negative]}]
  [react/touchable-highlight {:on-press action :style {:flex 1}
                              :accessibility-label (str label "-item-button")}
   [react/view {:flex 1 :align-items :center}
    [list-item/icon-column {:icon icon
                            :size :small
                            :icon-bg-color (if negative
                                             (if selected colors/red colors/red-light)
                                             (if selected colors/blue colors/blue-light))
                            :icon-color (if negative
                                          (if selected colors/white colors/red)
                                          (if selected colors/white colors/blue))}]
    [react/text {:style {:text-align :center :color  (if negative colors/red colors/blue)
                         :font-size 12 :line-height 16 :margin-top 6}
                 :number-of-lines 2}
     label]]])

(defn status []
  (let [messages @(re-frame/subscribe [:chats/current-chat-messages-stream])
        no-messages? @(re-frame/subscribe [:chats/current-chat-no-messages?])
        {:keys [profile-public-key]} @(re-frame/subscribe [:chats/current-raw-chat])]
    (when profile-public-key
      [list/flat-list
       {:key-fn                    #(or (:message-id %) (:value %))
        :header                    (when no-messages?
                                     [react/view {:padding-horizontal 32 :margin-top 32}
                                      [react/view (styles/updates-descr-cont)
                                       [react/text {:style {:color colors/gray :line-height 22}}
                                        (i18n/label :t/status-updates-descr)]]])
        :ref                       #(reset! status.views/messages-list-ref %)
        :on-viewable-items-changed chat.views/on-viewable-items-changed
        :on-end-reached            #(re-frame/dispatch [:chat.ui/load-more-messages])
        :on-scroll-to-index-failed #()                      ;;don't remove this
        :render-fn                 status.views/render-message
        :data                      messages}])))

(views/defview profile []
  (views/letsubs [{:keys [public-key name ens-verified]
                   :as   contact}  [:contacts/current-contact]]
    (let [muted? (:muted @(re-frame/subscribe [:chats/chat public-key]))
          [first-name second-name] (multiaccounts/contact-two-names contact true)
          on-share #(re-frame/dispatch [:show-popover (merge
                                                       {:view    :share-chat-key
                                                        :address public-key}
                                                       (when (and ens-verified name)
                                                         {:ens-name name}))])]
      (when contact
        [react/view {:flex 1}
         [quo/animated-header
          {:use-insets        false
           :right-accessories [{:icon     :main-icons/share
                                :accessibility-label :share-button
                                :on-press on-share}]
           :left-accessories  [{:icon                :main-icons/close
                                :accessibility-label :back-button
                                :on-press            #(re-frame/dispatch [:navigate-back])}]
           :extended-header   (profile-header/extended-header
                               {:on-press on-share
                                :bottom-separator false
                                :title    first-name
                                :photo    (multiaccounts/displayed-photo contact)
                                :monospace (not ens-verified)
                                :subtitle second-name})}
          [react/view {:height 1 :background-color colors/gray-lighter :margin-top 8}]
          [nickname-settings contact]
          [react/view {:height 1 :background-color colors/gray-lighter}]
          [react/view {:padding-top 17 :flex-direction :row :align-items :stretch :flex 1}
           (for [{:keys [label] :as action} (actions contact muted?)
                 :when label]
             ^{:key label}
             [button-item action])]
          [react/view {:height 1 :background-color colors/gray-lighter :margin-top 16}]
          [status]]]))))

