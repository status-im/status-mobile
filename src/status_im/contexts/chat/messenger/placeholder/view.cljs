(ns status-im.contexts.chat.messenger.placeholder.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.contexts.chat.messenger.placeholder.style :as style]
    [utils.re-frame :as rf]))

(defn- loading-skeleton
  []
  [quo/skeleton-list
   {:content       :messages
    :parent-height (:height (rn/get-window))
    :animated?     false}])

(defn view
  [on-layout-done?]
  (let [theme       (quo.theme/use-theme)
        chat-exist? (rf/sub [:chats/current-chat-exist?])]
    [rn/view {:style (style/container theme @on-layout-done?)}
     (when-not chat-exist?
       [quo/page-nav
        {:icon-name :i/arrow-left
         :on-press  (fn []
                      (rf/dispatch [:navigate-back])
                      (when platform/ios?
                        (rf/dispatch [:chat/close])))}])
     [loading-skeleton]]))
