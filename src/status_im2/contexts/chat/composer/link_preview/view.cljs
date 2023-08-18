(ns status-im2.contexts.chat.composer.link-preview.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.link-preview.events]
    [status-im2.contexts.chat.composer.link-preview.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- use-animated-height
  [previews?]
  (let [height (reanimated/use-shared-value (if previews? constants/links-container-height 0))]
    (rn/use-effect
     (fn []
       (reanimated/animate height (if previews? constants/links-container-height 0)))
     [previews?])
    height))

(defn f-view
  []
  (let [previews (rf/sub [:chats/link-previews-unfurled])
        height   (use-animated-height (boolean (seq previews)))]
    [reanimated/view
     {:style (reanimated/apply-animations-to-style {:height height} {:z-index 1})}
     [quo/url-preview-list
      {:key-fn               :url
       :preview-width        (- (:width (rn/get-window))
                                (* 2 style/padding-horizontal))
       :container-style      (when (seq previews) style/preview-list)
       :container-style-item {:height style/preview-height}
       :horizontal-spacing   style/padding-horizontal
       :loading-message      (i18n/label :t/link-preview-loading-message)
       :on-clear             #(rf/dispatch [:link-preview/clear])
       :data                 (map (fn [{:keys [title thumbnail hostname loading? url]}]
                                    {:title     title
                                     :body      hostname
                                     :loading?  loading?
                                     :thumbnail (:data-uri thumbnail)
                                     :url       url})
                                  previews)}]]))

(defn view
  []
  [:f> f-view])
