(ns status-im.contexts.communities.actions.channel-view-details.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.common.contact-list-item.view :as contact-list-item]
            [status-im.common.contact-list.view :as contact-list]
            [status-im.common.home.actions.view :as home.actions]
            [status-im.contexts.communities.actions.channel-view-details.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- contact-item-render
  [public-key]
  (let [show-profile-actions          #(rf/dispatch [:show-bottom-sheet
                                                     {:content (fn [] [home.actions/contact-actions
                                                                       {:public-key public-key}])}])
        [primary-name secondary-name] (rf/sub [:contacts/contact-two-names-by-identity public-key])
        {:keys [ens-verified added?]} (rf/sub [:contacts/contact-by-address public-key])]
    [contact-list-item/contact-list-item
     {:on-press      #(rf/dispatch [:chat.ui/show-profile public-key])
      :on-long-press show-profile-actions
      :accessory     {:type     :options
                      :on-press show-profile-actions}}
     {:primary-name   primary-name
      :secondary-name secondary-name
      :public-key     public-key
      :ens-verified   ens-verified
      :added?         added?}
     {:public-key public-key}]))

(defn- footer
  []
  [rn/view {:style style/footer}])

(defn- members
  [items]
  [rn/section-list
   {:key-fn                            :public-key
    :content-container-style           {:padding-bottom 20}
    :get-item-layout                   (fn [_ index]
                                         #js
                                          {:length 200
                                           :offset (* 200 index)
                                           :index  index})
    :content-inset-adjustment-behavior :never
    :sections                          items
    :sticky-section-headers-enabled    false
    :render-section-header-fn          contact-list/contacts-section-header
    :render-section-footer-fn          footer
    :render-fn                         contact-item-render
    :scroll-event-throttle             8}])

(defn- view-internal
  [_args]
  (fn [{:keys [theme]}]
    (let [{:keys [chat-id community-id]} (rf/sub [:get-screen-params :view-channel-members-and-details])
          {:keys [description chat-name emoji muted chat-type]
           :as   chat}                   (rf/sub [:chats/chat-by-id chat-id])
          pins-count                     (rf/sub [:chats/pin-messages-count chat-id])
          items                          (rf/sub [:communities/sorted-community-members-section-list
                                                  community-id])
          profile-color                  (rf/sub [:profile/customization-color])]
      [rn/view {:style {:flex 1}}
       [quo/page-nav
        {:background :blur
         :icon-name  :i/arrow-left
         :on-press   #(rf/dispatch [:navigate-back])
         :right-side [{:icon-name :i/options
                       :on-press  #(rf/dispatch [:show-bottom-sheet
                                                 {:content (fn [] [home.actions/chat-actions
                                                                   chat
                                                                   false
                                                                   true])}])}]}]
       [quo/text-combinations
        {:container-style                 style/text-combinations
         :title                           [quo/channel-name
                                           {:channel-name chat-name
                                            :unlocked?    true}]
         :emoji                           (when (not (string/blank? emoji)) emoji)
         :emoji-background-color          (colors/resolve-color profile-color theme 10)
         :title-accessibility-label       :welcome-title
         :description                     description
         :description-accessibility-label :welcome-sub-title}]
       [rn/view {:style style/wrapper}
        [rn/view
         {:style style/channel-actions-wrapper}
         [quo/channel-actions
          {:actions [{:big?          true
                      :label         (i18n/label :t/pinned-messages-2)
                      :color         profile-color
                      :icon          :i/pin
                      :counter-value pins-count
                      :on-press      (fn []
                                       (rf/dispatch [:dismiss-keyboard])
                                       (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                                     chat-id]))}
                     {:label    (if muted (i18n/label :t/unmute-channel) (i18n/label :t/mute-channel))
                      :color    profile-color
                      :icon     (if muted :i/muted :i/activity-center)
                      :on-press (fn []
                                  (if muted
                                    (home.actions/unmute-chat-action chat-id)
                                    (home.actions/mute-chat-action chat-id chat-type muted)))}]}]]]
       [members items profile-color]])))

(defn view [] (quo.theme/with-theme view-internal))
