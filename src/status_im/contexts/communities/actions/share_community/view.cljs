(ns status-im.contexts.communities.actions.share-community.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.common.resources :as resources]
    [status-im.contexts.communities.actions.share-community.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [url community-id]} (rf/sub [:get-screen-params])
        window-width               (rf/sub [:dimensions/window-width])]
    (fn []
      (let [{{:keys [thumbnail]} :images
             color               :color
             community-name      :name} (rf/sub [:communities/community community-id])
            navigate-back               (rn/use-callback #(rf/dispatch [:navigate-back]))
            on-press-share              (rn/use-callback
                                         #(rf/dispatch
                                           [:open-share
                                            {:options (if platform/ios?
                                                        {:activityItemSources
                                                         [{:placeholderItem {:type    :text
                                                                             :content url}
                                                           :item            {:default {:type    :text
                                                                                       :content url}}
                                                           :linkMetadata    {:title
                                                                             (i18n/label
                                                                              :t/share-community)}}]}
                                                        {:title     (i18n/label :t/share-community)
                                                         :subject   (i18n/label :t/share-community)
                                                         :message   url
                                                         :isNewTask true})}]))]
        [quo/overlay {:type :shell}
         [rn/view
          {:style {:padding-top (safe-area/get-top)}
           :key   :share-community}
          [quo/page-nav
           {:icon-name           :i/close
            :on-press            navigate-back
            :background          :blur
            :accessibility-label :top-bar}]
          [quo/text-combinations
           {:container-style style/header-container
            :title           (i18n/label :t/share-community)}]
          [rn/view {:style style/qr-code-wrapper}
           [quo/gradient-cover
            {:container-style       (style/gradient-cover-wrapper window-width)
             :customization-color   color
             :bottom-color-override colors/white-opa-5}]
           [rn/view
            {:style style/qr-top-wrapper}
            [rn/view {:flex-direction :row}
             (when thumbnail
               [fast-image/fast-image
                {:source thumbnail
                 :style  style/community-avatar}])
             [quo/text
              {:size   :heading-2
               :weight :semi-bold
               :style  (style/community-name thumbnail)}
              community-name]]
            [rn/view {:style style/share-button-container}
             [quo/button
              {:icon-only?          true
               :type                :grey
               :background          :blur
               :size                32
               :accessibility-label :link-to-community
               :on-press            on-press-share}
              :i/share]]]
           [rn/view
            {:style {:padding-vertical 12
                     :align-items      :center}}
            [qr-codes/qr-code
             {:size                (style/qr-code-size window-width)
              :url                 url
              :avatar              :community
              :customization-color color
              :picture             (or thumbnail (resources/get-mock-image :status-logo))
              :full-name           community-name}]]]
          [quo/text
           {:size   :paragraph-2
            :weight :regular
            :style  style/scan-notice}
           (i18n/label :t/scan-with-status-app)]]]))))
