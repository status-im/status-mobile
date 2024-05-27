(ns status-im.contexts.communities.actions.channel-view-details.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.contact-list-item.view :as contact-list-item]
            [status-im.common.contact-list.view :as contact-list]
            [status-im.common.home.actions.view :as home.actions]
            [status-im.contexts.communities.actions.channel-view-details.style :as style]
            [status-im.feature-flags :as ff]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- contact-item
  [public-key]
  (let [show-profile-actions                         #(rf/dispatch
                                                       [:show-bottom-sheet
                                                        {:content (fn [] [home.actions/contact-actions
                                                                          {:public-key public-key}])}])
        [primary-name secondary-name]                (rf/sub [:contacts/contact-two-names-by-identity
                                                              public-key])
        {:keys [ens-verified added? compressed-key]} (rf/sub [:contacts/contact-by-address public-key])
        theme                                        (quo.theme/use-theme)]
    [contact-list-item/contact-list-item
     {:on-press      #(rf/dispatch [:chat.ui/show-profile public-key])
      :on-long-press show-profile-actions
      :accessory     {:type     :options
                      :on-press show-profile-actions}}
     {:primary-name   primary-name
      :secondary-name secondary-name
      :compressed-key compressed-key
      :public-key     public-key
      :ens-verified   ens-verified
      :added?         added?}
     theme]))

(defn- footer
  []
  [rn/view {:style style/footer}])

(defn- get-item-layout
  [_ index]
  #js {:length 200 :offset (* 200 index) :index index})

(defn- members
  [items]
  [rn/section-list
   {:key-fn                            :public-key
    :content-container-style           {:padding-bottom 20}
    :get-item-layout                   get-item-layout
    :content-inset-adjustment-behavior :never
    :sections                          items
    :sticky-section-headers-enabled    false
    :render-section-header-fn          contact-list/contacts-section-header
    :render-section-footer-fn          footer
    :render-fn                         contact-item
    :scroll-event-throttle             8}])

(defn view
  [_args]
  (fn []
    (let [{:keys [chat-id community-id]} (rf/sub [:get-screen-params
                                                  :screen/chat.view-channel-members-and-details])
          {:keys [description chat-name emoji muted chat-type color]
           :as   chat}                   (rf/sub [:chats/chat-by-id chat-id])
          pins-count                     (rf/sub [:chats/pin-messages-count chat-id])
          items                          (rf/sub [:communities/sorted-community-members-section-list
                                                  community-id])
          theme                          (quo.theme/use-theme)]
      (rn/use-mount (fn []
                      (rf/dispatch [:pin-message/load-pin-messages chat-id])))
      [:<>
       (when (ff/enabled? ::ff/shell.jump-to)
         [quo/floating-shell-button
          {:jump-to {:on-press            #(rf/dispatch [:shell/navigate-to-jump-to])
                     :customization-color color
                     :label               (i18n/label :t/jump-to)}}
          style/floating-shell-button])
       [quo/gradient-cover {:customization-color color :opacity 0.4}]
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
         :theme                           theme
         :emoji                           (when (not (string/blank? emoji)) (string/trim emoji))
         :customization-color             color
         :title-accessibility-label       :welcome-title
         :description                     description
         :description-accessibility-label :welcome-sub-title}]
       [rn/view {:style style/wrapper}
        [rn/view
         {:style style/channel-actions-wrapper}
         [quo/channel-actions
          {:actions
           [{:big?                true
             :label               (i18n/label :t/pinned-messages-2)
             :customization-color color
             :icon                :i/pin
             :counter-value       pins-count
             :on-press            (fn []
                                    (rf/dispatch [:dismiss-keyboard])
                                    (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                                  chat-id]))}
            {:label               (if muted (i18n/label :t/unmute-channel) (i18n/label :t/mute-channel))
             :customization-color color
             :icon                (if muted :i/muted :i/activity-center)
             :on-press            (fn []
                                    (if muted
                                      (home.actions/unmute-chat-action chat-id)
                                      (home.actions/mute-chat-action chat-id chat-type muted)))}]}]]]
       [members items color]])))
