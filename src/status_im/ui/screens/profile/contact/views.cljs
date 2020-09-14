(ns status-im.ui.screens.profile.contact.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.chat.models :as chat.models]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.vector-icons :as icons]
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
            [clojure.string :as string])
  (:require-macros [status-im.utils.views :as views]))

(defn actions
  [{:keys [public-key added? tribute-to-talk] :as contact}]
  (let [{:keys [tribute-status tribute-label]} tribute-to-talk]
    (concat [(cond-> {:label               (i18n/label :t/send-message)
                      :icon                :main-icons/message
                      :action              #(re-frame/dispatch [:contact.ui/send-message-pressed {:public-key public-key}])
                      :accessibility-label :start-conversation-button}
               (not (#{:none :paid} tribute-status))
               (assoc :subtext tribute-label))]
            ;;TODO hide temporary for v1
            #_{:label               (i18n/label :t/send-transaction)
               :icon                :main-icons/send
               :action              #(re-frame/dispatch [:profile/send-transaction public-key])
               :accessibility-label :send-transaction-button}
            (if added?
              [{:label               (i18n/label :t/remove-from-contacts)
                :icon                :main-icons/remove-contact
                :accessibility-label :in-contacts-button
                :action              #(re-frame/dispatch [:contact.ui/remove-contact-pressed contact])}]
              ;; TODO sheets temporary disabled
                                        ;:action              #(re-frame/dispatch [:bottom-sheet/show-sheet
                                        ;                                          {:content        sheets/remove-contact
                                        ;                                           :content-height 150}
                                        ;                                          contact])
              [{:label               (i18n/label :t/add-to-contacts)
                :icon                :main-icons/add-contact
                :accessibility-label :add-to-contacts-button
                :action              #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])}]))))
                ;; TODO sheets temporary disabled
                ;:action              #(re-frame/dispatch [:bottom-sheet/show-sheet
                ;                                          {:content sheets/add-contact
                ;                                           :content-height 150}
                ;                                          contact])

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

(defn render-chat-settings [{:keys [public-key names]}]
  (let [muted? (:muted @(re-frame/subscribe [:chats/chat public-key]))]
    [react/view
     [quo/list-item
      {:title               (i18n/label :t/nickname)
       :size                :small
       :accessibility-label :profile-nickname-item
       :accessory           :text
       :accessory-text      (or (:nickname names) (i18n/label :t/none))
       :on-press            #(re-frame/dispatch [:navigate-to :nickname])
       :chevron             true}]
     [quo/list-item
      {:title               (i18n/label :t/mute)
       :active               muted?
       :accessibility-label :mute-chat
       :on-press            #(re-frame/dispatch [::chat.models/mute-chat-toggled public-key (not muted?)])
       :accessory           :switch}]]))

(defn chat-settings [contact]
  [react/view
   [quo/list-header
    [quo/text {:accessibility-label :chat-settings
               :color               :inherit}
     (i18n/label :t/chat-settings)]]
   [render-chat-settings contact]])

;; TODO: List item
(defn block-contact-action [{:keys [blocked? public-key]}]
  [react/touchable-highlight {:on-press (if blocked?
                                          #(re-frame/dispatch [:contact.ui/unblock-contact-pressed public-key])
                                          #(re-frame/dispatch [:show-popover
                                                               {:view             sheets/block-contact
                                                                :prevent-closing? true
                                                                :public-key       public-key}]))}
   [react/text {:style               styles/block-action-label
                :accessibility-label (if blocked?
                                       :unblock-contact
                                       :block-contact)}
    (if blocked?
      (i18n/label :t/unblock-contact)
      (i18n/label :t/block-contact))]])

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

(views/defview profile []
  (views/letsubs [{:keys [public-key name ens-verified]
                   :as   contact}  [:contacts/current-contact]]
    (let [[first-name second-name] (multiaccounts/contact-two-names contact true)
          on-share #(re-frame/dispatch [:show-popover (merge
                                                       {:view    :share-chat-key
                                                        :address public-key}
                                                       (when (and ens-verified name)
                                                         {:ens-name name}))])]
      (when contact
        [react/view
         {:style
          (merge {:flex 1})}
         [quo/animated-header
          {:use-insets        true
           :right-accessories [{:icon     :main-icons/share
                                :on-press on-share}]
           :left-accessories  [{:icon                :main-icons/arrow-left
                                :accessibility-label :back-button
                                :on-press            #(re-frame/dispatch [:navigate-back])}]
           :extended-header   (profile-header/extended-header
                               {:on-press on-share
                                :title    first-name
                                :photo    (multiaccounts/displayed-photo contact)
                                :monospace (not ens-verified)
                                :subtitle second-name})}

          [react/view {:padding-top 12}
           (for [{:keys [label subtext accessibility-label icon action disabled?]} (actions contact)]
             ^{:key label}
             (when label
               [quo/list-item {:theme               :accent
                               :title               label
                               :subtitle            subtext
                               :icon                icon
                               :accessibility-label accessibility-label
                               :disabled            disabled?
                               :on-press            action}]))]
          [react/view styles/contact-profile-details-container
           [profile-details contact]
          ;; Mute chat is only supported on ios for now
           (when platform/ios?
             [react/view {}
              [chat-settings contact]])]
          [block-contact-action contact]]]))))
