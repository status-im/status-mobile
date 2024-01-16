(ns status-im.contexts.communities.actions.channel-view-details.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.common.contact-list-item.view :as contact-list-item]
            [status-im.common.contact-list.view :as contact-list]
            [status-im.common.home.actions.view :as home.actions]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn contact-item-render
  [public-key]
  (let [show-profile-actions          #(rf/dispatch [:show-bottom-sheet
                                                     {:content (fn [] [home.actions/contact-actions
                                                                       {:public-key public-key}])}])
        [primary-name secondary-name] (rf/sub [:contacts/contact-two-names-by-identity public-key])
        {:keys [ens-verified added?]} (rf/sub [:contacts/contact-by-address public-key])]
    [contact-list-item/contact-list-item
     {:on-press       #(rf/dispatch [:chat.ui/show-profile public-key])
      :on-long-press  show-profile-actions
      :accessory      {:type     :options
                       :on-press show-profile-actions}}
     {:primary-name   primary-name
      :secondary-name secondary-name
      :public-key     public-key
      :ens-verified   ens-verified
      :added?         added?}
     {:public-key public-key}]))

(defn footer []
  [rn/view {:style {:margin-top 8}}])

(defn members
  [{:keys [theme chat-id]}]
  (let [{:keys [community-id]} (rf/sub [:get-screen-params])
        items                  (rf/sub [:communities/sorted-community-members-section-list
                                        community-id chat-id])]
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
      :scroll-event-throttle             8}]))

(defn f-view
  [{:keys [theme chat-id]}]
  (let [{:keys [description chat-name emoji muted
                chat-type]} (rf/sub [:chats/chat-by-id
                                     chat-id])
        pins-count          (rf/sub [:chats/pin-messages-count chat-id])]
    [rn/view {:style {:flex 1}}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])
       :right-side [{:icon-name :i/options}]}]
     [quo/text-combinations
      ;; Use style file
      {:container-style                 {:margin-top 12 :margin-horizontal 20}
       :title                           [quo/channel-name {:channel-name chat-name :unlocked? true}]
       :avatar                          emoji
       :title-accessibility-label       :welcome-title
       :description                     description
       :description-accessibility-label :welcome-sub-title}]
     ;; Use style file
     [rn/view {:style {:padding 20}}
      [rn/view
       ;; Use style file
       {:style {:height 102}}
       [quo/channel-actions
        {:actions [{:big?          true
                    :label         (i18n/label :t/pinned-messages-2)
                    ;; Use correct color
                    :color         :blue
                    :icon          :i/pin
                    :counter-value pins-count
                    :on-press      (fn []
                                     (rf/dispatch [:dismiss-keyboard])
                                     (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                                   chat-id]))}
                   {:label    (i18n/label :t/mute-channel)
                    :color    :blue
                    :icon     (if muted :i/muted :i/activity-center)
                    :on-press (fn []
                                (if muted
                                  (home.actions/unmute-chat-action chat-id)
                                  (home.actions/mute-chat-action chat-id chat-type muted)))}]}]]]
     [members
      {:theme      theme
       :chat-id chat-id}]]))

(defn internal-view
  [args]
  (let [screen-params (rf/sub [:get-screen-params :view-channel-members-and-details])]
    [:f> f-view (merge args screen-params)]))

(def view (quo.theme/with-theme internal-view))
