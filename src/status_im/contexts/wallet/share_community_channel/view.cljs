(ns status-im.contexts.wallet.share-community-channel.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.contexts.wallet.share-community-channel.style :as style]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [padding-top (:top (safe-area/get-insets))]
    (fn []
      (let [chat                            (rf/sub [:chats/current-chat-chat-view])
            {:keys [color emoji chat-name]} chat
            mediaserver-port                (rf/sub [:mediaserver/port])
            window-width                    (rf/sub [:dimensions/window-width])
            {:keys [url]}                   (rf/sub [:get-screen-params])
            qr-media-server-uri             (image-server/get-qr-image-uri-for-any-url
                                             {:url         url
                                              :port        mediaserver-port
                                              :qr-size     style/qr-size
                                              :error-level :highest})
            title                           (i18n/label :t/share-channel)]
        [quo/overlay {:type :shell}

         [rn/view
          {:flex        1
           :padding-top padding-top
           :key         :share-community}
          [quo/page-nav
           {:icon-name           :i/close
            :on-press            #(rf/dispatch [:navigate-back])
            :background          :blur
            :accessibility-label :top-bar}]
          [quo/text-combinations
           {:container-style style/header-container
            :title           title}]
          [rn/view {:style style/qr-code-wrapper}
           [quo/gradient-cover
            {:container-style
             {:width         (style/gradient-cover-size window-width)
              :position      :absolute
              :border-radius 12
              :z-index       -1}
             :customization-color (:color chat)}]
           [rn/view
            {:style {:padding-vertical 12}}
            [qr-codes/qr-code
             {:size                (style/qr-code-size window-width)
              :qr-image-uri        qr-media-server-uri
              :avatar              :channel
              :customization-color color
              :emoji               emoji
              :full-name           chat-name}]]]
          [quo/text
           {:size   :paragraph-2
            :weight :regular
            :style  style/scan-notice}
           (i18n/label :t/scan-with-status-app)]]]))))
