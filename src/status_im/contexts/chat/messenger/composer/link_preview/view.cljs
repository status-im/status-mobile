(ns status-im.contexts.chat.messenger.composer.link-preview.view
  (:require
   [clojure.string :as string]
   [quo.core :as quo]
   [react-native.core :as rn]
   [react-native.reanimated :as reanimated]
   [status-im.common.resources :as resources]
   [status-im.constants]
   [status-im.contexts.chat.messenger.composer.constants :as constants]
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

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn link-preview-options
  []
  [rn/view
   [rn/view {:flex-direction  :row
             :justify-content :space-between
             :padding-bottom     12
             :padding-horizontal 20}
    [quo/text
     {:accessibility-label :communities-join-community
      :weight              :semi-bold
      :size                :heading-2}
     (i18n/label :t/preview-link)]
    [quo/icon :info {:size 20}]]
   [quo/action-drawer
    [[{:icon     :i/reveal
       :label    "Show for this message"
       :on-press #(hide-sheet-and-dispatch [:profile.settings/set-unfurl-links-mode
                                            constants/preview-always-ask])}
      {:icon     :i/reveal-whitelist
       :label    "Always show previews"
       :on-press #(hide-sheet-and-dispatch [:profile.settings/set-unfurl-links-mode
                                            constants/preview-always-share])}
      {:icon     :i/hide
       :label    "Never show previews"
       :on-press #(hide-sheet-and-dispatch [:profile.settings/set-unfurl-links-mode
                                            constants/preview-never-ask])}]]]])

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
     :on-press #(rf/dispatch [:show-bottom-sheet
                              {:content (fn []
                                          [link-preview-options])}])}
    (i18n/label :t/options)]])


(defn view
  [theme]
  (let [previews      (rf/sub [:chats/link-previews-unfurled])
        height        (use-animated-height (boolean (seq previews)))
        mode          (rf/sub [:profile/url-unfurling-mode])
        show-previews (atom (or false
                                (= mode constants/preview-always-share)))]
    (cond
      @show-previews
      [reanimated/view
       {:style (reanimated/apply-animations-to-style {:height height} {:z-index 1})}
       [unfurl-links previews]]

      (and (= mode constants/preview-always-ask) (boolean (seq previews)))
      [show-unfurl-link-options theme])))
