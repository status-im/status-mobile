(ns status-im.contexts.chat.messenger.composer.link-preview.actions.view
  (:require
   [quo.core :as quo]
   [react-native.core :as rn]
   [status-im.contexts.chat.messenger.composer.constants :as constants]
   [status-im.contexts.chat.messenger.composer.link-preview.actions.style :as style]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn link-preview-options
  []
  [rn/view
   [rn/view style/title
    [quo/text
     {:accessibility-label :communities-join-community
      :weight              :semi-bold
      :size                :heading-2}
     (i18n/label :t/preview-link)]
    [quo/icon :info {:size 20}]]
   [quo/action-drawer
    [[{:icon     :i/reveal
       :label    "Show for this message"
       :on-press #(hide-sheet-and-dispatch [:link-preview/show-unfurled-url true])}
      {:icon     :i/reveal-whitelist
       :label    "Always show previews"
       :on-press #(hide-sheet-and-dispatch [:profile.settings/set-unfurl-links-mode
                                            constants/preview-always-share])}
      {:icon     :i/hide
       :label    "Never show previews"
       :on-press (fn []
                   (hide-sheet-and-dispatch [:profile.settings/set-unfurl-links-mode
                                             constants/preview-never-share])
                   (rf/dispatch [:link-preview/clear]))}]]]])
