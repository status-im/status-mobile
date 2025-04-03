(ns status-im.contexts.communities.actions.share-community-channel.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.contexts.communities.actions.share-community-channel.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn navigate-back [] (rf/dispatch [:navigate-back]))

(defn view
  []
  (fn []
    (let [params                          (quo.context/use-screen-params)
          ;; NOTE(seanstrom): We need to store these screen params for when the modal closes
          ;; because the screen params will be cleared.
          {:keys [url chat-id]}           @(rn/use-ref-atom params)
          {:keys [color emoji chat-name]} (rf/sub [:chats/community-channel-ui-details-by-id chat-id])
          on-share-community-channel      (rn/use-callback
                                           #(rf/dispatch
                                             [:communities/share-community-channel-url-with-data
                                              chat-id])
                                           [chat-id])]
      [quo/overlay {:type :shell}
       [rn/view
        {:style (style/container (safe-area/get-top))
         :key   :share-community}
        [quo/page-nav
         {:icon-name           :i/close
          :on-press            navigate-back
          :background          :blur
          :accessibility-label :top-bar}]
        [quo/text-combinations
         {:container-style style/header-container
          :title           (i18n/label :t/share-channel)}]
        [rn/view {:style style/qr-code-wrapper}
         [qr-codes/share-qr-code
          {:type                :channel
           :qr-data             url
           :customization-color color
           :emoji               emoji
           :full-name           chat-name
           :on-share-press      on-share-community-channel}]]
        [quo/text
         {:size   :paragraph-2
          :weight :regular
          :style  style/scan-notice}
         (i18n/label :t/scan-with-status-app)]]])))
