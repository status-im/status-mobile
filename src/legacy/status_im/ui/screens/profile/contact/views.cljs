(ns legacy.status-im.ui.screens.profile.contact.views
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.profile-header.view :as profile-header]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.profile.components.sheets :as sheets]
    [quo.components.avatars.user-avatar.style :as user-avatar.style]
    [quo.theme :as theme]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn actions
  [{:keys [public-key added? blocked? ens-name mutual?] :as profile} muted?]
  (concat [{:label               (i18n/label :t/chat)
            :icon                :main-icons/message
            :disabled            (not mutual?)
            :action              #(re-frame/dispatch [:chat.ui/start-chat
                                                      public-key
                                                      ens-name])
            :accessibility-label :start-conversation-button}]
          (if added?
            [{:label               (i18n/label :t/remove-from-contacts)
              :icon                :main-icons/remove-contact
              :selected            true
              :accessibility-label :in-contacts-button
              :action              #(re-frame/dispatch [:contact.ui/remove-contact-pressed profile])}]
            [{:label               (i18n/label :t/add-to-contacts)
              :icon                :main-icons/add-contact
              :disabled            blocked?
              :accessibility-label :add-to-contacts-button
              :action              (when-not blocked?
                                     (fn []
                                       (re-frame/dispatch [:contact.ui/send-contact-request
                                                           public-key])))}])
          [{:label               (i18n/label (if (or muted? blocked?) :t/unmute :t/mute))
            :icon                :main-icons/notification
            :accessibility-label :mute-chat
            :selected            muted?
            :disabled            blocked?
            :action              (when-not blocked?
                                   #(re-frame/dispatch [:chat.ui/mute public-key (not muted?)
                                                        (when-not muted?
                                                          constants/mute-till-unmuted)]))}]
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

(defn pin-settings
  [public-key pin-count]
  [list.item/list-item
   {:title               (i18n/label :t/pinned-messages)
    :size                :small
    :accessibility-label :pinned-messages-item
    :accessory           :text
    :accessory-text      pin-count
    :disabled            (zero? pin-count)
    :on-press            #(rf/dispatch [:pin-message/show-pins-bottom-sheet public-key])
    :chevron             true}])

(defn nickname-settings
  [{:keys [nickname]}]
  [list.item/list-item
   {:title               (i18n/label :t/nickname)
    :size                :small
    :accessibility-label :profile-nickname-item
    :accessory           :text
    :accessory-text      (or nickname (i18n/label :t/none))
    :on-press            #(re-frame/dispatch [:open-modal :nickname])
    :chevron             true}])

(defn save-nickname
  [public-key nickname]
  (re-frame/dispatch [:contacts/update-nickname public-key nickname])
  (re-frame/dispatch [:navigate-back]))

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
    :max-length          constants/profile-name-max-length
    :accessibility-label :nickname-input
    :default-value       nickname
    :placeholder         (i18n/label :t/nickname)
    :return-key-type     :done
    :auto-correct        false}])

(defn nickname-view
  []
  (let [{:keys [public-key nickname] :as profile} (rf/sub [:contacts/current-contact])
        entered-nickname                          (reagent/atom nickname)]
    (fn []
      [kb-presentation/keyboard-avoiding-view
       {:style         {:flex 1}
        :ignore-offset true}
       [topbar/topbar
        {:title    (i18n/label :t/nickname)
         :subtitle (profile.utils/displayed-name profile)
         :modal?   true}]
       [react/view {:flex 1 :padding 16}
        [react/text {:style {:color colors/gray :margin-bottom 16}}
         (i18n/label :t/nickname-description)]
        [nickname-input nickname entered-nickname public-key]
        [react/text
         {:style {:align-self :flex-end
                  :margin-top 16
                  :color      colors/gray}}
         (str (count @entered-nickname) " / " constants/profile-name-max-length)]]
       [toolbar/toolbar
        {:show-border? true
         :center
         [quo/button
          {:type     :secondary
           :on-press #(save-nickname public-key @entered-nickname)}
          (i18n/label :t/done)]}]])))

(defn button-item
  [{:keys [icon label action selected disabled negative]}]
  [react/touchable-highlight
   {:on-press            action
    :disabled            disabled
    :style               {:flex 1}
    :accessibility-label (str label "-item-button")}
   [react/view {:flex 1 :align-items :center}
    [list.item/icon-column
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

(defn profile-view
  []
  (let [{:keys [public-key
                secondary-name
                name
                ens-verified
                customization-color
                compressed-key]
         :as   profile}     @(re-frame/subscribe [:contacts/current-contact])
        muted?              @(re-frame/subscribe [:chats/muted public-key])
        customization-color (or customization-color :primary)
        on-share            #(re-frame/dispatch [:show-popover
                                                 (merge
                                                  {:view    :share-chat-key
                                                   :address (or compressed-key
                                                                public-key)}
                                                  (when (and ens-verified name)
                                                    {:ens-name name}))])]
    (when profile
      [:<>
       [quo/header
        {:right-accessories [{:icon                :main-icons/share
                              :accessibility-label :share-button
                              :on-press            on-share}]
         :left-accessories  [{:icon                :main-icons/close
                              :accessibility-label :back-button
                              :on-press            #(re-frame/dispatch [:navigate-back])}]}]
       [:<>
        [(profile-header/extended-header
          {:on-press         on-share
           :bottom-separator false
           :title            (profile.utils/displayed-name profile)
           :color            (user-avatar.style/customization-color customization-color
                                                                    (theme/get-theme))
           :photo            (profile.utils/photo profile)
           :monospace        (not ens-verified)
           :subtitle         secondary-name
           :compressed-key   compressed-key
           :public-key       public-key})]
        [react/view
         {:height 1 :background-color colors/gray-lighter :margin-top 8}]
        [nickname-settings profile]
        [react/view {:height 1 :background-color colors/gray-lighter}]
        [react/view
         {:padding-top    17
          :flex-direction :row
          :align-items    :stretch
          :flex           1}
         (for [{:keys [label] :as action} (actions profile muted?)
               :when                      label]
           ^{:key label}
           [button-item action])]]])))
