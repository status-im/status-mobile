(ns status-im.contexts.communities.actions.channel-view-details.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.contact-list-item.view :as contact-list-item]
            [status-im.common.contact-list.view :as contact-list]
            [status-im.common.home.actions.view :as home.actions]
            [status-im.constants :as constants]
            [status-im.contexts.communities.actions.channel-view-details.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- contact-item
  [public-key _ _ {:keys [theme]}]
  (let [show-profile-actions     (rn/use-callback
                                  (fn []
                                    (rf/dispatch
                                     [:show-bottom-sheet
                                      {:content (fn [] [home.actions/contact-actions
                                                        {:public-key public-key}])}]))
                                  [public-key])
        on-press                 (rn/use-callback
                                  (fn []
                                    (rf/dispatch [:chat.ui/show-profile public-key]))
                                  [public-key])
        [primary-name
         secondary-name]         (rf/sub [:contacts/contact-two-names-by-identity public-key])
        {:keys [ens-verified
                added?
                compressed-key]} (rf/sub [:contacts/contact-by-address public-key])]
    [contact-list-item/contact-list-item
     {:on-press      on-press
      :on-long-press show-profile-actions
      :accessory     {:type     :options
                      :on-press show-profile-actions}}
     {:primary-name    primary-name
      :secondary-name  secondary-name
      :compressed-key  compressed-key
      :public-key      public-key
      :ens-verified    ens-verified
      :added?          added?
      ;; We hardcode the height of the container to match exactly the height
      ;; used in the `get-item-layout` function.
      :container-style {:height constants/contact-item-height}}
     theme]))

(defn- footer
  []
  [rn/view {:style style/footer}])

(defn- get-item-layout
  [_ index]
  #js {:length constants/contact-item-height
       :offset (* constants/contact-item-height index)
       :index  index})

(defn- members
  [community-id chat-id theme]
  (let [online-members  (rf/sub [:communities/chat-members-sorted community-id chat-id :online])
        offline-members (rf/sub [:communities/chat-members-sorted community-id chat-id :offline])]
    [rn/section-list
     {:key-fn                            :public-key
      :content-container-style           {:padding-bottom 20}
      :get-item-layout                   get-item-layout
      :content-inset-adjustment-behavior :never
      :sections                          [{:title (i18n/label :t/online)
                                           :data  online-members}
                                          {:title (i18n/label :t/offline)
                                           :data  offline-members}]
      :sticky-section-headers-enabled    false
      :render-section-header-fn          contact-list/contacts-section-header
      :render-section-footer-fn          footer
      :render-data                       {:theme theme}
      :render-fn                         contact-item
      :scroll-event-throttle             32}]))

(defn view
  []
  (let [{:keys [chat-id community-id]} (rf/sub [:get-screen-params
                                                :screen/chat.view-channel-members-and-details])
        {:keys [description chat-name emoji muted chat-type color]
         :as   chat}                   (rf/sub [:chats/chat-by-id chat-id])
        pins-count                     (rf/sub [:chats/pin-messages-count chat-id])
        theme                          (quo.theme/use-theme)]
    (rn/use-mount (fn []
                    (rf/dispatch [:pin-message/load-pin-messages chat-id])))
    [:<>
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
     [members community-id chat-id theme]]))
