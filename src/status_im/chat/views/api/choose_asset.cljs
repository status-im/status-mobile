(ns status-im.chat.views.api.choose-asset
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.utils.money :as money]
            [status-im.chat.views.api.styles :as styles]))

(defn clean-asset [asset]
  (select-keys asset [:name :symbol :decimals :address]))

(defn- render-asset [arg-index bot-db-key]
  (fn [{:keys [name symbol amount decimals] :as asset}]
    [react/touchable-highlight {:on-press #(re-frame/dispatch
                                            [:set-asset-as-command-argument {:arg-index  arg-index
                                                                             :bot-db-key bot-db-key
                                                                             :asset      (clean-asset asset)}])}
     [react/view styles/asset-container
      [react/view styles/asset-main
       [react/image {:source (-> asset :icon :source)
                     :style  styles/asset-icon}]
       [react/text {:style styles/asset-symbol} symbol]
       [react/text {:style styles/asset-name} name]]
      ;;TODO(goranjovic) : temporarily disabled to fix https://github.com/status-im/status-react/issues/4963
      ;;until the resolution of https://github.com/status-im/status-react/issues/4972
      #_[react/text {:style styles/asset-balance}
         (str (money/internal->formatted amount symbol decimals))]]]))

(def assets-separator [react/view styles/asset-separator])

(defview choose-asset-view [{arg-index  :index
                             bot-db-key :bot-db-key}]
  (letsubs [assets [:wallet/visible-assets-with-amount]]
    [react/view
     [list/flat-list {:data                      (filter #(not (:nft? %)) assets)
                      :key-fn                    (comp name :symbol)
                      :render-fn                 (render-asset arg-index bot-db-key)
                      :enableEmptySections       true
                      :separator                 assets-separator
                      :keyboardShouldPersistTaps :always
                      :bounces                   false}]]))