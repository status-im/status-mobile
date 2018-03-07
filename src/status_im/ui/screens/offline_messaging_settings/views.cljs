(ns status-im.ui.screens.offline-messaging-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.offline-messaging-settings.styles :as styles]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [reagent.core :as reagent])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn- wnode-icon [connected?]
  [react/view (styles/wnode-icon connected?)
   [vector-icons/icon :icons/wnode {:color (if connected? :white :gray)}]])

(defn- render-row [current-wnode]
  (fn [{:keys [address name id] :as row} _ _]
    (let [connected? (= id current-wnode)]
      [react/list-item
       ^{:key row}
       [react/touchable-highlight
        {:on-press            #(re-frame/dispatch [:connect-wnode id])
         :accessibility-label :mailserver-item}
        [react/view styles/wnode-item
         [wnode-icon connected?]
         [react/view styles/wnode-item-inner
          [react/text {:style styles/wnode-item-name-text}
           name]
          #_
          (when connected?
            [react/text {:style styles/wnode-item-connected-text}
             (i18n/label :t/connected)])]]]])))

(defn- form-title [label wnodes]
  (-> (common/form-title label {:count-value (count wnodes)})
      (update-in [1 2 1] dissoc :margin-top)))

(defn- render-header [wnodes]
  [react/list-item
   [react/view
    [form-title (i18n/label :t/existing-wnodes) wnodes]
    [common/list-header]]])

(defn- render-footer []
  [react/list-item [react/view
                    [common/list-footer]
                    [common/bottom-shadow]]])

(defview offline-messaging-settings []
  (letsubs [current-wnode  [:get :inbox/wnode]
            wnodes         [:get :inbox/wnodes]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar (i18n/label :t/offline-messaging-settings)]
     (when platform/ios?
       [common/separator])
     [react/view {:flex 1}
      ;; TODO(dmitryn) migrate to :header/:footer properties of flat-list
      ;; after merge of https://github.com/status-im/status-react/pull/2297/
      [render-header wnodes]
      [list/flat-list {:data (vals wnodes)
                       :separator? false
                       :render-fn (render-row current-wnode)
                       :ListFooterComponent (reagent/as-element (render-footer))
                       :style styles/wnodes-list}]]]))
