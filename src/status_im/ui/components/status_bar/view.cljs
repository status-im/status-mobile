(ns status-im.ui.components.status-bar.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.styles :as styles]))

(defn status-bar [{type :type}]
  (let [[status-bar-style view-style]
        (case type
          :main         [styles/status-bar-main styles/view-main]
          :transparent  [styles/status-bar-transparent styles/view-transparent]
          :modal        [styles/status-bar-modal styles/view-modal]
          :modal-white  [styles/status-bar-modal-white styles/view-modal-white]
          :modal-wallet [styles/status-bar-modal-wallet styles/view-model-wallet]
          :transaction  [styles/status-bar-transaction styles/view-transaction]
          :wallet       [styles/status-bar-wallet styles/view-wallet]
          [styles/status-bar-default styles/view-default])]
    [react/view
     [react/status-bar status-bar-style]
     [react/view {:style view-style}]]))
