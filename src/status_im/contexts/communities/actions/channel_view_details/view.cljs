(ns status-im.contexts.communities.actions.channel-view-details.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.common.home.actions.view :as home.actions]
            [utils.re-frame :as rf]))

(defn f-view
  [{:keys [theme chat-id]}]
  (let [{:keys [description chat-name emoji muted
                chat-type]} (rf/sub [:chats/chat-by-id
                                     chat-id])
        pins-count          (rf/sub [:chats/pin-messages-count chat-id])]
    [rn/view
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
                    ;; Use i18n
                    :label         "Pinned\nMessages"
                    ;; Use correct color
                    :color         :blue
                    :icon          :i/pin
                    :counter-value pins-count
                    :on-press      (fn []
                                     (rf/dispatch [:dismiss-keyboard])
                                     (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                                   chat-id]))}
                   ;; Use i18n
                   {:label    "Mute channel"
                    :color    :blue
                    :icon     (if muted :i/muted :i/activity-center)
                    :on-press (fn []
                                (if muted
                                  (home.actions/unmute-chat-action chat-id)
                                  (home.actions/mute-chat-action chat-id chat-type muted)))}]}]]]]))

(defn internal-view
  [args]
  (let [screen-params (rf/sub [:get-screen-params :view-channel-members-and-details])]
    [:f> f-view (merge args screen-params)]))

(def view (quo.theme/with-theme internal-view))
