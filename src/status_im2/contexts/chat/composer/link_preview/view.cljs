(ns status-im2.contexts.chat.composer.link-preview.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.chat.composer.link-preview.events]
    [status-im2.contexts.chat.composer.link-preview.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn message-draft-link-previews
  []
  (let [previews (rf/sub [:chat/link-previews-unfurled])]
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
                                 previews)}]))
