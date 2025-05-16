(ns status-im.contexts.chat.messenger.composer.link-preview.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.common.resources :as resources]
    [status-im.constants]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.link-preview.actions.view :as preview-actions]
    [status-im.contexts.chat.messenger.composer.link-preview.events]
    [status-im.contexts.chat.messenger.composer.link-preview.style :as style]
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

(defn unfurl-links
  [previews]
  [quo/url-preview-list
   {:key-fn               :url
    :preview-width        (- (:width (rn/get-window))
                             (* 2 style/padding-horizontal))
    :container-style      (when (seq previews) style/preview-list)
    :container-style-item {:height style/preview-height}
    :horizontal-spacing   style/padding-horizontal
    :loading-message      (i18n/label :t/link-preview-loading-message)
    :on-clear             #(rf/dispatch [:link-preview/clear])
    :data                 (map
                           (fn [{:keys [title display-name thumbnail hostname loading? url favicon]}]
                             {:title     (or display-name title)
                              :body      (or (when-not display-name hostname)
                                             status-im.constants/status-hostname)
                              :logo      (if (string/starts-with? url "https://status.app")
                                           (resources/get-mock-image :status-logo)
                                           favicon)
                              :loading?  loading?
                              :thumbnail (:data-uri thumbnail)
                              :url       url})
                           previews)}])

(defn show-unfurl-link-options
  [theme]
  [rn/view
   {:style (style/unfurl-link-options theme)}
   [quo/text
    {:size :paragraph-2}
    (i18n/label :t/show-link-previews)]
   [quo/button
    {:type     :outline
     :size     24
     :on-press (fn []
                 (rf/dispatch [:show-bottom-sheet
                               {:content (fn []
                                           [preview-actions/link-preview-options])}]))}
    (i18n/label :t/options)]])


(defn view
  [theme]
  (let [previews      (rf/sub [:chats/link-previews-unfurled])
        mode          (rf/sub [:profile/url-unfurling-mode])
        height        (use-animated-height (and (boolean (seq previews))
                                                (not= mode constants/preview-never-share)))
        show-current? (rf/sub [:chat/show-current-preview])
        show-options  (and (= mode constants/preview-always-ask)
                           (boolean (seq previews))
                           (not show-current?))]
    [reanimated/view
     {:style (reanimated/apply-animations-to-style {:height height} {:z-index 1})}
     (when (or (= mode constants/preview-always-share) show-current?)
       [unfurl-links previews])
     (when show-options
       [show-unfurl-link-options theme])]))
