(ns status-im.ui.screens.profile.contact.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.tabbar.styles :as tabs.styles]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.large-toolbar.view :as large-toolbar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.contact.styles :as styles]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.screens.profile.components.sheets :as sheets]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.multiaccounts.core :as multiaccounts])
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

(defn render-detail [{:keys [name public-key] :as detail}]
  [list-item/list-item
   {:title    name
    :subtitle (utils/get-shortened-address public-key)
    :icon     [chat-icon/contact-icon-contacts-tab detail]
    :accessibility-label :profile-public-key
    :on-press #(re-frame/dispatch [:show-popover {:view :share-chat-key :address public-key}])
    :accessories [[icons/icon :main-icons/share styles/contact-profile-detail-share-icon]]}])

(defn profile-details-list-view [contact]
  [list/flat-list {:data                    [contact]
                   :default-separator?      true
                   :key-fn                  :public-key
                   :render-fn               render-detail}])

(defn profile-details [contact]
  [react/view
   [list-item/list-item {:type                      :section-header
                         :title                     :t/profile-details
                         :title-accessibility-label :profile-details}]
   [profile-details-list-view contact]])

(defn block-contact-action [{:keys [blocked? public-key] :as contact}]
  [react/touchable-highlight {:on-press (if blocked?
                                          #(re-frame/dispatch [:contact.ui/unblock-contact-pressed public-key])
                                          #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                               {:content        sheets/block-contact
                                                                :content-height 160}
                                                               contact]))}
   [react/text {:style styles/block-action-label
                :accessibility-label (if blocked?
                                       :unblock-contact
                                       :block-contact)}
    (if blocked?
      (i18n/label :t/unblock-contact)
      (i18n/label :t/block-contact))]])

(defn- header-in-toolbar [{:keys [photo-path] :as account}]
  (let [displayed-name (multiaccounts/displayed-name account)]
    [react/view {:flex           1
                 :flex-direction :row
                 :align-items    :center
                 :align-self     :stretch}
     [photos/photo photo-path {:size 40}]
     [react/text {:style {:typography   :title-bold
                          :line-height  21
                          :margin-right 40
                          :margin-left  16
                          :text-align   :left}
                  :accessibility-label :account-name}
      displayed-name]]))

(defn- header [account]
  [profile.components/profile-header
   {:contact                account
    :allow-icon-change?     false
    :include-remove-action? false}])

(views/defview profile []
  (views/letsubs [list-ref (reagent/atom nil)
                  contact  [:contacts/current-contact]]
    (let [header-in-toolbar    (header-in-toolbar contact)
          header               (header contact)
          content
          [[list/action-list (actions contact)
            {:container-style        styles/action-container
             :action-style           styles/action
             :action-label-style     styles/action-label
             :action-subtext-style   styles/action-subtext
             :action-separator-style styles/action-separator
             :icon-opts              styles/action-icon-opts}]
           [react/view styles/contact-profile-details-container
            [profile-details contact]]
           [block-contact-action contact]]
          generated-view (large-toolbar/generate-view
                          header-in-toolbar
                          toolbar/default-nav-back
                          nil
                          header
                          content
                          list-ref)]
      [react/safe-area-view
       {:style
        (merge {:flex 1}
               (when platform/ios?
                 {:margin-bottom tabs.styles/tabs-diff}))}
       [status-bar/status-bar {:type :main}]
       (:minimized-toolbar generated-view)
       (:content-with-header generated-view)])))