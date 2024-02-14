(ns status-im.contexts.wallet.share-community-channel.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.wallet.share-community-channel.style :as style]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def qr-size 500)

(defn view
  []
  (let [padding-top (:top (safe-area/get-insets))]
    (fn []
      (let [chat                (rf/sub [:chats/current-chat-chat-view])
            mediaserver-port    (rf/sub [:mediaserver/port])
            {:keys [url]}       (rf/sub [:get-screen-params])
            qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         url
                                  :port        mediaserver-port
                                  :qr-size     qr-size
                                  :error-level :highest})
            title               (i18n/label :t/share-channel)]
        [quo/overlay {:type :shell}
         [rn/view
          {:flex        1
           :padding-top padding-top
           :key         :share-community}
          [quo/page-nav
           {:icon-name           :i/close
            :on-press            #(rf/dispatch [:navigate-back])
            :background          :blur
            :right-side          [{:icon-name :i/scan
                                   :on-press  #(js/alert "To be implemented")}]
            :accessibility-label :top-bar}]
          [quo/text-combinations
           {:container-style style/header-container
            :title           title}]
          [rn/view {:style {:padding-horizontal 20}}
           [quo/share-community-channel-qr-code
            {:qr-image-uri        qr-media-server-uri
             :customization-color (:color chat)
             :emoji               (:emoji chat)
             :full-name           (:chat-name chat)}]]
          [quo/text
           {:size   :paragraph-2
            :weight :regular
            :style  style/scan-notice}
           (i18n/label :t/scan-with-status-app)]]]))))
