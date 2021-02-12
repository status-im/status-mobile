(ns status-im.ui.screens.referrals.home-item
  (:require [status-im.acquisition.chat :as acquisition.chat]
            [status-im.acquisition.core :as acquisition]
            [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [quo.react-native :as rn]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.utils.core :as utils]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.invite.events :as invite]))

(defn icon-style []
  {:color           colors/black
   :width           15
   :height          15
   :container-style {:width        15
                     :height       15
                     :margin-right 2}})

(defn referral-sheet []
  [quo/list-item
   {:theme               :negative
    :title               (i18n/label :t/delete-chat)
    :accessibility-label :delete-chat-button
    :icon                :main-icons/delete
    :on-press            #(re-frame/dispatch [::acquisition.chat/decline])}])

(defn list-item []
  (let [{:keys [id]}    @(re-frame/subscribe [::acquisition/metadata])
        {:keys [chat-id
                chat-name
                group-chat
                color]} {:chat-name  (str "#" id)
                         :chat-id    id
                         :color      "#887af9"
                         :public?    true
                         :group-chat true}]
    (when @(re-frame/subscribe [::invite/has-public-chat-invite])
      [quo/list-item {:icon                      [chat-icon.screen/chat-icon-view-chat-list
                                                  chat-id group-chat chat-name color nil false]
                      :title                     [rn/view {:flex-direction :row
                                                           :flex           1}
                                                  [rn/view {:flex-direction :row
                                                            :flex           1
                                                            :padding-right  16
                                                            :align-items    :center}
                                                   [icons/icon :main-icons/tiny-public (icon-style)]
                                                   [quo/text {:weight              :medium
                                                              :accessibility-label :chat-name-text
                                                              :ellipsize-mode      :tail
                                                              :number-of-lines     1}
                                                    (utils/truncate-str chat-name 30)]]]
                      :title-accessibility-label :chat-name-text
                      :subtitle                  (i18n/label :t/invite-public-chat-home)
                      :on-press                  #(re-frame/dispatch [:navigate-to :tabs {:screen :chat-stack
                                                                                          :params {:screen :referral-enclav}}])
                      :on-long-press             #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                      {:content referral-sheet}])}])))
