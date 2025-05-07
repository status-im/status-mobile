(ns legacy.status-im.ui.screens.profile.user.views
  (:require
    [legacy.status-im.ui.components.core :as components]
    [legacy.status-im.ui.components.list.item :as list.item]
    [quo.context]
    [quo.core :as quo]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn content
  []
  [rn/scroll-view {:flex 1}
   [components/list-header "Legacy settings"]
   [list.item/list-item
    {:icon                :main-icons/mobile
     :title               (i18n/label :t/sync-settings)
     :accessibility-label :sync-settings-button
     :chevron             true
     :on-press            #(re-frame/dispatch [:open-modal :screen/legacy-sync-settings])}]])

(defn legacy-settings
  []
  [:<>
   [quo/page-nav
    {:type       :no-title
     :background :blur
     :icon-name  :i/close
     :on-press   #(rf/dispatch [:navigate-back])}]
   [content]])
