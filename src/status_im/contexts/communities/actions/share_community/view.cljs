(ns status-im.contexts.communities.actions.share-community.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.contexts.communities.actions.share-community-channel.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (fn []
    (let [{:keys [url community-id]} (rf/sub [:get-screen-params])
          {:keys          [images color]
           community-name :name}     (rf/sub [:communities/community community-id])
          thumbnail                  (:thumbnail images)
          window-width               (rf/sub [:dimensions/window-width])]
      [quo/overlay {:type :shell}
       [rn/view
        {:style {:padding-top (safe-area/get-top)}
         :key   :share-community}
        [quo/page-nav
         {:icon-name           :i/close
          :on-press            #(rf/dispatch [:navigate-back])
          :background          :blur
          :accessibility-label :top-bar}]
        [quo/text-combinations
         {:container-style style/header-container
          :title           (i18n/label :t/share-community)}]
        [rn/view {:style style/qr-code-wrapper}
         [quo/gradient-cover
          {:container-style     (style/gradient-cover-wrapper window-width)
           :customization-color color}]
         [rn/view
          {:style {:padding-vertical 12}}
          [qr-codes/qr-code
           {:size                (style/qr-code-size window-width)
            :url                 url
            :avatar              :community
            :customization-color color
            :picture             thumbnail
            :full-name           community-name}]]]
        [quo/text
         {:size   :paragraph-2
          :weight :regular
          :style  style/scan-notice}
         (i18n/label :t/scan-with-status-app)]]])))
