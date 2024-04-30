(ns legacy.status-im.ui.screens.profile.user.views
  (:require
    [legacy.status-im.ui.components.common.common :as components.common]
    [legacy.status-im.ui.components.core :as components]
    [legacy.status-im.ui.components.list.item :as list.item]
    [quo.core :as quo]
    [quo.theme]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn content
  []
  (let [{:keys [mnemonic]} (rf/sub [:profile/profile])]
    [rn/scroll-view {:flex 1}
     [components/list-header "Legacy settings"]
     [list.item/list-item
      {:icon                :main-icons/security
       :title               (i18n/label :t/privacy-and-security)
       :accessibility-label :privacy-and-security-settings-button
       :chevron             true
       :accessory           (when mnemonic
                              [components.common/counter {:size 22} 1])
       :on-press            #(re-frame/dispatch [:open-modal :legacy-privacy-and-security])}]
     [list.item/list-item
      {:icon                :main-icons/mobile
       :title               (i18n/label :t/sync-settings)
       :accessibility-label :sync-settings-button
       :chevron             true
       :on-press            #(re-frame/dispatch [:open-modal :legacy-sync-settings])}]]))

(defn legacy-settings
  []
  [:<>
   [quo/page-nav
    {:type       :no-title
     :background :blur
     :icon-name  :i/close
     :on-press   #(rf/dispatch [:navigate-back])}]
   [content]])
