(ns status-im.ui.screens.referrals.public-chat
  (:require [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [re-frame.core :as re-frame]
            [status-im.acquisition.chat :as chat.acquisition]
            [status-im.acquisition.core :as acquisition]
            [status-im.ui.components.invite.events :as invite]
            [status-im.i18n.i18n :as i18n]
            [status-im.acquisition.gateway :as gateway]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.screens.chat.styles.main :as chat.styles]
            [status-im.ui.components.invite.chat :as invite.chat]))

(defn non-reward []
  (let [loading (#{(get gateway/network-statuses :initiated)
                   (get gateway/network-statuses :in-flight)}
                 @(re-frame/subscribe [::gateway/network-status]))]

    [rn/view {:border-radius      16
              :padding-horizontal 12
              :padding-vertical   16
              :justify-content    :center
              :align-items        :center
              :border-width       1
              :border-color       (colors/get-color :border-01)}
     [rn/view
      [icons/icon :main-icons/link {:color (colors/get-color :icon-04)}]]
     [rn/view {:style {:padding-vertical 10}}
      [quo/text {:color :secondary
                 :align :center}
       (i18n/label :t/invite-privacy-policy-public) " "
       [quo/text {:color    :link
                  :on-press #(re-frame/dispatch [::invite/terms-and-conditions])}
        (i18n/label :t/invite-privacy-policy2)]]]
     [rn/view {:padding-horizontal 16
               :padding-vertival   8
               :flex-direction     :row}
      [quo/button {:loading  loading
                   :on-press #(re-frame/dispatch [::chat.acquisition/accept-pack])}
       (i18n/label :t/invite-chat-accept-join)]]]))

(defn reward-messages []
  (let [loading  (#{(get gateway/network-statuses :initiated)
                    (get gateway/network-statuses :in-flight)}
                  @(re-frame/subscribe [::gateway/network-status]))
        pending  @(re-frame/subscribe [::invite/pending-reward])
        messages [{:content [{:type  :text
                              :value "👋"}]}
                  {:content [{:type  :text
                              :value (i18n/label :t/invite-public-chat-intro)}
                             {:type :pack}
                             {:type  :button
                              :value [quo/button {:type     :secondary
                                                  :loading  loading
                                                  :disabled pending
                                                  :on-press #(re-frame/dispatch [::chat.acquisition/accept-pack])}
                                      (if pending
                                        (i18n/label :t/invite-chat-pending)
                                        (i18n/label :t/invite-chat-accept-join))]}]}
                  {:content [{:type  :text
                              :value [:<>
                                      (i18n/label :t/invite-privacy-policy-public) " "
                                      [quo/text {:color    :link
                                                 :on-press #(re-frame/dispatch [::invite/terms-and-conditions])}
                                       (i18n/label :t/invite-privacy-policy2)]]}]}]]
    [rn/view {:style (invite.chat/messages-wrapper)}
     (for [message messages]
       [invite.chat/render-message message])]))

(defn view []
  (let [{:keys [rewardable id]} @(re-frame/subscribe [::acquisition/metadata])
        {:keys [chat-id
                chat-name
                group-chat
                color]
         :as   chat-info}        {:chat-name  (str "#" id)
                                  :color      "#887af9"
                                  :public?    true
                                  :group-chat true}]
    [:<>
     [topbar/topbar
      {:content    [toolbar-content/toolbar-content-view chat-info]
       :navigation {:on-press #(re-frame/dispatch [:navigate-to :home])}}]
     [rn/scroll-view {:style                   {:flex 1}
                      :center-content          true
                      :content-container-style {:padding 16}}
      [rn/view {:style {:flex             1
                        :justify-content  :flex-end
                        :align-items      :center
                        :padding-vertical 24}}
       [rn/view {:style {:padding-vertical 16}}
        [chat-icon.screen/chat-intro-icon-view
         chat-name chat-id group-chat
         {:default-chat-icon      (chat.styles/intro-header-icon 120 color)
          :default-chat-icon-text chat.styles/intro-header-icon-text
          :size                   120}]]
       [rn/view {:style {:padding-horizontal 32
                         :padding-bottom     32}}
        [quo/text {:size   :x-large
                   :weight :bold}
         chat-name]]]
      (if rewardable
        [reward-messages]
        [non-reward])]]))

