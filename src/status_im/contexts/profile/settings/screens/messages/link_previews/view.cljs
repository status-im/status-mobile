(ns status-im.contexts.profile.settings.screens.messages.link-previews.view
  (:require
   [quo.core :as quo]
   [react-native.core :as rn]
   [status-im.common.events-helper :as events-helper]
   [status-im.contexts.chat.messenger.composer.constants :as constants]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn- update-link-previews
  [mode]
  (rf/dispatch [:profile.settings/set-unfurl-links-mode mode]))

(defn view
  []
  (let [mode       (rf/sub [:profile/url-unfurling-mode])]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top {:title (i18n/label :t/preview-link)}]
     [quo/category
      {:label     (i18n/label :t/for-urls)
       :data      [{:title        (i18n/label :t/preview-always-share)
                    :blur?        true
                    :action       :selector
                    :action-props {:type      :radio
                                   :on-change (update-link-previews constants/preview-always-share)
                                   :checked?  (= mode  constants/preview-always-share)}}
                   {:title        (i18n/label :t/preview-never-share)
                    :blur?        true
                    :action       :selector
                    :action-props {:type      :radio
                                   :on-change (update-link-previews constants/preview-never-ask)
                                   :checked?  (= mode  constants/preview-never-ask)}}
                   {:title        (i18n/label :t/preview-always-ask)
                    :blur?        true
                    :action       :selector
                    :action-props {:type      :radio
                                   :on-change (update-link-previews constants/preview-alway-ask)
                                   :checked?  (= mode  constants/preview-alway-ask)}}]
       :blur?     true
       :list-type :settings}]]))
